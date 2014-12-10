package io.soabase.example;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import io.dropwizard.Application;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.soabase.client.SoaClientBundle;
import io.soabase.client.SoaClientConfiguration;
import io.soabase.core.ConfigurationAccessor;
import io.soabase.core.SoaBundle;
import io.soabase.core.SoaCli;
import io.soabase.core.SoaConfiguration;
import io.soabase.zookeeper.discovery.CuratorBundle;
import io.soabase.zookeeper.discovery.CuratorConfiguration;
import java.io.Closeable;
import java.net.URL;
import java.util.List;

public abstract class ExampleAppBase extends Application<ExampleConfiguration> implements Managed
{
    private final List<Closeable> closeables = Lists.newArrayList();
    private final String configFqpn;

    public ExampleAppBase(String configFqpn)
    {
        this.configFqpn = configFqpn;
    }

    public void initialize(Bootstrap<ExampleConfiguration> bootstrap)
    {
        ConfigurationAccessor<ExampleConfiguration, SoaConfiguration> soaAccessor = new ConfigurationAccessor<ExampleConfiguration, SoaConfiguration>()
        {
            @Override
            public SoaConfiguration accessConfiguration(ExampleConfiguration configuration)
            {
                return configuration.getSoaConfiguration();
            }
        };
        ConfigurationAccessor<ExampleConfiguration, CuratorConfiguration> curatorAccessor = new ConfigurationAccessor<ExampleConfiguration, CuratorConfiguration>()
        {
            @Override
            public CuratorConfiguration accessConfiguration(ExampleConfiguration configuration)
            {
                return configuration.getCuratorConfiguration();
            }
        };
        ConfigurationAccessor<ExampleConfiguration, SoaClientConfiguration> clientAccessor = new ConfigurationAccessor<ExampleConfiguration, SoaClientConfiguration>()
        {
            @Override
            public SoaClientConfiguration accessConfiguration(ExampleConfiguration configuration)
            {
                return configuration.getClientConfiguration();
            }
        };
        bootstrap.addBundle(new CuratorBundle<>(soaAccessor, curatorAccessor));
        bootstrap.addBundle(new SoaBundle<>(soaAccessor));
        bootstrap.addBundle(new SoaClientBundle<>(soaAccessor, clientAccessor));
    }

    @Override
    public void run(String... arguments) throws Exception
    {
        if ( arguments.length == 0 )
        {
            URL config = Resources.getResource(configFqpn);

            arguments = new String[]
            {
                "-f",
                config.getPath(),
                "-o",
                "curator.connectionString=localhost:2181",
                "-o",
                "soa.discovery.type=zookeeper",
                "-o",
                "soa.discovery.name=" + getClass().getSimpleName(),
                "-o",
                "soa.discovery.bindAddress=localhost"
            };
        }

        super.run(SoaCli.parseArgs(arguments));
    }

    @Override
    public void run(ExampleConfiguration configuration, Environment environment) throws Exception
    {
        environment.lifecycle().manage(this);

        internalRun(configuration, environment);
    }

    protected abstract void internalRun(ExampleConfiguration configuration, Environment environment);

    @Override
    public void start() throws Exception
    {
        // NOP
    }

    @Override
    public void stop() throws Exception
    {
        for ( Closeable closeable : closeables )
        {
            closeable.close();
        }
    }
}