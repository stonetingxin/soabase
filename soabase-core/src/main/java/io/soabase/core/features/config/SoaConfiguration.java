/**
 * Copyright 2014 Jordan Zimmerman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.soabase.core.features.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.soabase.core.features.attributes.NullDynamicAttributesFactory;
import io.soabase.core.features.attributes.SoaDynamicAttributesFactory;
import io.soabase.core.features.discovery.DefaultDiscoveryHealthFactory;
import io.soabase.core.features.discovery.NullDiscoveryFactory;
import io.soabase.core.features.discovery.SoaDiscoveryFactory;
import io.soabase.core.features.discovery.SoaDiscoveryHealthFactory;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class SoaConfiguration
{
    @Valid
    @NotNull
    private SoaDiscoveryFactory discoveryFactory = new NullDiscoveryFactory();

    @Valid
    @NotNull
    private SoaDiscoveryHealthFactory discoveryHealthFactory = new DefaultDiscoveryHealthFactory();

    @Valid
    @NotNull
    private SoaDynamicAttributesFactory attributesFactory = new NullDynamicAttributesFactory();

    @Valid
    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Service Names can only contain letters and numbers")
    private String serviceName;

    @Valid
    private int shutdownWaitMaxMs = (int)TimeUnit.MINUTES.toMillis(1);

    @Valid
    private String instanceName;

    @Valid
    private List<String> scopes = ImmutableList.of();

    @Valid
    private boolean addCorsFilter = true;

    @Valid
    private boolean registerInDiscovery = true;

    @Valid
    private int discoveryHealthCheckPeriodMs = (int)TimeUnit.SECONDS.toMillis(10);

    @Valid
    private String adminJerseyPath = "/api";

    @JsonProperty("checkPeriodMs")
    public int getDiscoveryHealthCheckPeriodMs()
    {
        return discoveryHealthCheckPeriodMs;
    }

    @JsonProperty("checkPeriodMs")
    public void setDiscoveryHealthCheckPeriodMs(int discoveryHealthCheckPeriodMs)
    {
        this.discoveryHealthCheckPeriodMs = discoveryHealthCheckPeriodMs;
    }

    @JsonProperty("discovery")
    public SoaDiscoveryFactory getDiscoveryFactory()
    {
        return discoveryFactory;
    }

    @JsonProperty("discovery")
    public void setDiscoveryFactory(SoaDiscoveryFactory discoveryFactory)
    {
        this.discoveryFactory = discoveryFactory;
    }

    @JsonProperty("attributes")
    public SoaDynamicAttributesFactory getAttributesFactory()
    {
        return attributesFactory;
    }

    @JsonProperty("attributes")
    public void setAttributesFactory(SoaDynamicAttributesFactory attributesFactory)
    {
        this.attributesFactory = attributesFactory;
    }

    @JsonProperty("shutdownWaitMaxMs")
    public int getShutdownWaitMaxMs()
    {
        return shutdownWaitMaxMs;
    }

    @JsonProperty("shutdownWaitMaxMs")
    public void setShutdownWaitMaxMs(int shutdownWaitMaxMs)
    {
        this.shutdownWaitMaxMs = shutdownWaitMaxMs;
    }

    @JsonProperty("instanceName")
    public String getInstanceName()
    {
        return instanceName;
    }

    @JsonProperty("instanceName")
    public void setInstanceName(String instanceName)
    {
        this.instanceName = instanceName;
    }

    @JsonProperty("additionalScopes")
    public List<String> getScopes()
    {
        return scopes;
    }

    @JsonProperty("additionalScopes")
    public void setScopes(List<String> scopes)
    {
        this.scopes = ImmutableList.copyOf(scopes);
    }

    @JsonProperty("addCorsFilter")
    public boolean isAddCorsFilter()
    {
        return addCorsFilter;
    }

    @JsonProperty("addCorsFilter")
    public void setAddCorsFilter(boolean addCorsFilter)
    {
        this.addCorsFilter = addCorsFilter;
    }

    @JsonProperty("discoveryHealth")
    public SoaDiscoveryHealthFactory getDiscoveryHealthFactory()
    {
        return discoveryHealthFactory;
    }

    @JsonProperty("discoveryHealth")
    public void setDiscoveryHealthFactory(SoaDiscoveryHealthFactory discoveryHealthFactory)
    {
        this.discoveryHealthFactory = discoveryHealthFactory;
    }

    @JsonProperty("adminJerseyPath")
    public String getAdminJerseyPath()
    {
        return adminJerseyPath;
    }

    @JsonProperty("adminJerseyPath")
    public void setAdminJerseyPath(String adminJerseyPath)
    {
        this.adminJerseyPath = adminJerseyPath;
    }

    @JsonProperty("serviceName")
    public String getServiceName()
    {
        return serviceName;
    }

    @JsonProperty("serviceName")
    public void setServiceName(String serviceName)
    {
        this.serviceName = serviceName;
    }

    @JsonProperty("registerInDiscovery")
    public boolean isRegisterInDiscovery()
    {
        return registerInDiscovery;
    }

    @JsonProperty("registerInDiscovery")
    public void setRegisterInDiscovery(boolean registerInDiscovery)
    {
        this.registerInDiscovery = registerInDiscovery;
    }
}
