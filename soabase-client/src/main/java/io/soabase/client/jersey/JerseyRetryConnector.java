package io.soabase.client.jersey;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.SettableFuture;
import io.soabase.client.Common;
import io.soabase.client.SoaRequestId;
import io.soabase.client.retry.RetryComponents;
import io.soabase.client.retry.RetryContext;
import io.soabase.core.features.discovery.SoaDiscoveryInstance;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;
import java.net.URI;
import java.util.concurrent.Future;

public class JerseyRetryConnector implements Connector
{
    private final Connector connector;
    private final RetryComponents retryComponents;

    public JerseyRetryConnector(Client client, RetryComponents retryComponents, Configuration runtimeConfig)
    {
        this.retryComponents = retryComponents;
        connector = new ApacheConnectorProvider().getConnector(client, runtimeConfig);
    }

    @Override
    public ClientResponse apply(ClientRequest request)
    {
        RetryContext retryContext = new RetryContext(retryComponents, request.getUri(), request.getMethod());
        return internalApply(request, retryContext, 0);
    }

    @Override
    public Future<?> apply(ClientRequest request, AsyncConnectorCallback callback)
    {
        Preconditions.checkNotNull(callback, "callback is assumed to be non null");
        RetryContext retryContext = new RetryContext(retryComponents, request.getUri(), request.getMethod());
        return internalApply(request, retryContext, callback, 0);
    }

    @Override
    public String getName()
    {
        return connector.getName();
    }

    @Override
    public void close()
    {
        connector.close();
    }

    private void filterRequest(ClientRequest request, RetryContext retryContext)
    {
        String id = SoaRequestId.get();
        if ( id != null )
        {
            request.getHeaders().putSingle(SoaRequestId.REQUEST_ID_HEADER_NAME, id);
        }

        SoaDiscoveryInstance instance = Common.hostToInstance(retryContext.getComponents().getDiscovery(), retryContext.getOriginalHost());
        retryContext.setInstance(instance);
        URI filteredUri = Common.filterUri(request.getUri(), instance);
        if ( filteredUri != null )
        {
            request.setUri(filteredUri);
        }
    }

    @VisibleForTesting
    protected ClientResponse internalApply(ClientRequest request, RetryContext retryContext, int tryCount)
    {
        ClientResponse clientResponse;
        try
        {
            filterRequest(request, retryContext);
            clientResponse = connector.apply(request);
        }
        catch ( ProcessingException e )
        {
            if ( retryContext.shouldBeRetried(tryCount, 0, e) )
            {
                return internalApply(request, retryContext, tryCount + 1);
            }
            throw e;
        }

        if ( retryContext.shouldBeRetried(tryCount, clientResponse.getStatus(), null) )
        {
            return internalApply(request, retryContext, tryCount + 1);
        }

        return clientResponse;
    }


    @VisibleForTesting
    protected Future<?> internalApply(final ClientRequest request, final RetryContext retryContext, final AsyncConnectorCallback callback, final int tryCount)
    {
        AsyncConnectorCallback localCallback = new AsyncConnectorCallback()
        {
            @Override
            public void response(ClientResponse response)
            {
                if ( !isRetry(request, response, retryContext, null, callback, tryCount) )
                {
                    callback.response(response);
                }
            }

            @Override
            public void failure(Throwable failure)
            {
                if ( !isRetry(request, null, retryContext, failure, callback, tryCount) )
                {
                    callback.failure(failure);
                }
            }
        };
        filterRequest(request, retryContext);
        connector.apply(request, localCallback);
        return SettableFuture.create(); // just a dummy
    }

    private boolean isRetry(final ClientRequest request, ClientResponse response, final RetryContext retryContext, Throwable failure, final AsyncConnectorCallback callback, final int tryCount)
    {
        int status = (response != null) ? response.getStatus() : 0;
        if ( retryContext.shouldBeRetried(tryCount, status, failure) )
        {
            Runnable runnable = new Runnable()
            {
                @Override
                public void run()
                {
                    internalApply(request, retryContext, callback, tryCount + 1);
                }
            };
            retryComponents.getExecutorService().submit(runnable);
            return true;
        }
        return false;
    }
}
