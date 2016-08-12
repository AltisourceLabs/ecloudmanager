/*
 * MIT License
 *
 * Copyright (c) 2016  Altisource
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.ecloudmanager.node.verizon;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.ecloudmanager.tmrk.cloudapi.exceptions.CloudapiException;
import org.ecloudmanager.tmrk.cloudapi.model.*;
import org.ecloudmanager.tmrk.cloudapi.service.environment.ComputePoolService;
import org.ecloudmanager.tmrk.cloudapi.service.environment.EnvironmentService;
import org.ecloudmanager.tmrk.cloudapi.service.environment.LayoutService;
import org.ecloudmanager.tmrk.cloudapi.service.network.NetworkService;
import org.ecloudmanager.tmrk.cloudapi.service.organization.CatalogService;
import org.ecloudmanager.tmrk.cloudapi.service.organization.OrganizationService;
import org.ecloudmanager.tmrk.cloudapi.util.TmrkUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Cache is used to due to expensive retreived objects from Terremark cloud.
 * <p/>
 * <p>We expect the following to happen with Terremark objects stored in cache:
 * <ul>
 * <li>Willing to spend some memory to improve speed.</li>
 * <li>Keys will sometimes get queried more than once.</li>
 * <li>Cache will not need to store more data than what would fit in RAM.</li>
 * </ul>
 * <p>Obtaining a Cache is done using the CacheBuilder builder pattern.
 *
 * @author irosu
 */

public class CloudCachedEntityService {

    private CloudGenericEntityService delegate;

    private CloudServicesRegistry registry;
    private Map<Class<? extends ResourceType>, TypedCache<? extends ResourceType>> cacheMap;

    private Map<Class<? extends ResourceType>, Map<String, String>> nameToHrefMap;

    private Map<String, QueryCache<? extends ResourceType>> queryCacheMap;

    public CloudCachedEntityService(CloudServicesRegistry registry) {
        this.registry = registry;
        delegate = new CloudGenericEntityService(registry);
        init();
    }

    private void init() {
        cacheMap = new ConcurrentHashMap<>(10);
        queryCacheMap = new ConcurrentHashMap<>(10);
        nameToHrefMap = new ConcurrentHashMap<>(100);
        // add to cache VerizonInfrastructure objects
        addCache(EnvironmentType.class);
        addCache(NetworkType.class);
        addCache(PublicIpType.class);
        addCache(FirewallAclType.class);
        addCache(LayoutRowType.class);
        addCache(LayoutGroupType.class);
        addCache(DeviceTagListType.class);

        addQueryCache("getEnvironments", EnvironmentService.class, EnvironmentsType.class);

        populate();
    }

    private void release() {
        // cleanup
        for (TypedCache<? extends ResourceType> cache : cacheMap.values()) {
            cache.getCache();
        }
        cacheMap.clear();

    }

    private void populate() {
        OrganizationService organizationService = registry.getOrganizationService();
        OrganizationType org = organizationService.getOrganizations().getOrganization().get(0);

        EnvironmentsType envs = getEnvironments(TmrkUtils.getIdFromHref(org.getHref()));
        Map<String, String> map = nameToHrefMap.get(EnvironmentType.class);
        if (map == null) {
            map = new HashMap<>();
            nameToHrefMap.put(EnvironmentType.class, map);
        }
        for (EnvironmentType env : envs.getEnvironment()) {
            map.put(env.getName(), env.getHref());
        }

        List<CatalogEntryType> catalogEntriesByOrg = new ArrayList<>();
        CatalogType catalogType = getCatalog(TmrkUtils.getIdFromHref(org.getHref()));//catalogService
        // .getCatalogByOrganizationId(organizationId);
        CatalogLocationsType catalogLocations = catalogType.getLocations().getValue();
        List<CatalogLocationType> catalogs = catalogLocations.getLocation();
        for (CatalogLocationType clt : catalogs) {
            catalogEntriesByOrg.addAll(clt.getCatalog().getValue().getCatalogEntry());
        }

        Map<String, String> mapCatalog = nameToHrefMap.get(CatalogEntryType.class);
        if (mapCatalog == null) {
            mapCatalog = new HashMap<>();
            nameToHrefMap.put(CatalogEntryType.class, mapCatalog);
        }
        for (CatalogEntryType cat : catalogEntriesByOrg) {
            mapCatalog.put(cat.getName(), cat.getHref());
        }

        /*
         * CATALOGS CONFIGS
         */
        Map<String, String> mapCatalogConfig = nameToHrefMap.get(CatalogEntryConfigurationType.class);
        if (mapCatalogConfig == null) {
            mapCatalogConfig = new HashMap<>();
            nameToHrefMap.put(CatalogEntryConfigurationType.class, mapCatalogConfig);
        }
        for (CatalogEntryType cet : catalogEntriesByOrg) {
            CatalogEntryConfigurationType config = null;
            try {
                config = getCatalogConfiguration(TmrkUtils.getIdFromHref(cet.getHref()));
                mapCatalogConfig.put(config.getName(), config.getHref());
            } catch (UncheckedExecutionException | CloudapiException e) {
                // there are catalogs without configuration
                // TODO - log
            }
        }
        /*
         * END OF CATALOGS CONFIGS
         */

        Map<String, String> mapNetwork = nameToHrefMap.get(NetworksType.class);
        if (mapNetwork == null) {
            mapNetwork = new HashMap<>();
            nameToHrefMap.put(NetworksType.class, mapNetwork);
        }

        for (EnvironmentType env : envs.getEnvironment()) {
            String id = TmrkUtils.getIdFromHref(env.getHref());
            NetworksType networks = null;
            try {
                networks = getNetworks(id);
            } catch (UncheckedExecutionException | CloudapiException e) {
                // permissions could be missing
                // TODO - log
            }
            if (networks != null) {
                for (NetworkReferenceType net : networks.getNetwork()) {
                    // remove the check after user permissions received
                    mapNetwork.put(net.getName(), net.getHref());
                }
            }
        }
    }

    public CatalogType getCatalog(String organizationId) {
        try {
            QueryCache<CatalogType> cache = getQueryCache("getCatalogByOrganizationId", CatalogService.class,
                    CatalogType.class);
            return cache.getCache().get(organizationId);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CloudapiException) {
                throw (CloudapiException) cause;
            }
        }
        return null;
    }

    public CatalogEntryConfigurationType getCatalogConfiguration(String catalogId) {
        try {
            QueryCache<CatalogEntryConfigurationType> cache = getQueryCache
                    ("getCatalogEntryConfigurationByCatalogId", CatalogService.class, CatalogEntryConfigurationType.class);
            return cache.getCache().get(catalogId);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CloudapiException) {
                throw (CloudapiException) cause;
            }
        }
        return null;
    }

    public EnvironmentsType getEnvironments(final String organizationId) {
        try {
            QueryCache<EnvironmentsType> cache = getQueryCache("getEnvironments", EnvironmentService.class,
                    EnvironmentsType.class);
            return cache.getCache().get(organizationId);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CloudapiException) {
                throw (CloudapiException) cause;
            }
        }
        return null;

    }

    public ComputePoolReferencesResourceType getComputePools(final String environmentId) {
        try {
            QueryCache<ComputePoolReferencesResourceType> cache = getQueryCache("getComputePoolsByEnvironment",
                    ComputePoolService.class, ComputePoolReferencesResourceType.class);
            return cache.getCache().get(environmentId);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CloudapiException) {
                throw (CloudapiException) cause;
            }
        }
        return null;

    }

    public DeviceLayoutType getRows(final String environmentId) {
        try {
            QueryCache<DeviceLayoutType> cache = getQueryCache("getDeviceLayoutByEnvironmentId", LayoutService.class,
                    DeviceLayoutType.class);
            return cache.getCache().get(environmentId);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CloudapiException) {
                throw (CloudapiException) cause;
            }
        }
        return null;

    }

    public NetworksType getNetworks(String environmentId) {
        try {
            QueryCache<NetworksType> cache = getQueryCache("getNetworks", NetworkService.class, NetworksType.class);
            return cache.getCache().get(environmentId);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CloudapiException) {
                throw (CloudapiException) cause;
            }
        }
        return null;
    }

    private <T extends ResourceType> TypedCache<T> addCache(Class<T> type) {
        TypedCache<T> cache = new TypedCache<T>(type);
        cacheMap.put(type, cache);
        //        nameToHrefMap.put(type, new HashMap<String, String>());
        return cache;
    }

    private <T extends ResourceType> QueryCache<T> addQueryCache(String methodName, Class<?> serviceType, Class<T>
            resourceType) {
        QueryCache<T> cache = new QueryCache<T>(methodName, serviceType, resourceType);
        queryCacheMap.put(methodName, cache);
        return cache;
    }

    private <T extends ResourceType> TypedCache<T> getCache(Class<T> type) {
        @SuppressWarnings("unchecked")
        TypedCache<T> cache = (TypedCache<T>) cacheMap.get(type);
        if (cache == null) {
            cache = addCache(type);
        }
        return cache;
    }

    private <T extends ResourceType> QueryCache<T> getQueryCache(String methodName, Class<?> serviceType, Class<T>
            resourceType) {
        @SuppressWarnings("unchecked")
        QueryCache<T> cache = (QueryCache<T>) queryCacheMap.get(methodName);
        if (cache == null) {
            cache = addQueryCache(methodName, serviceType, resourceType);
        }
        return cache;
    }

    /**
     * @param type
     * @param hrefOrName
     * @return the ResourceType
     */
    public <T extends ResourceType> T getByHrefOrName(Class<T> type, String hrefOrName) {
        try {
            String href = hrefOrName;
            TypedCache<T> cache = getCache(type);
            if (!href.startsWith("/cloudapi")) { //TODO: refactor in a better way
                // assume is name
                href = nameToHrefMap.get(type).get(hrefOrName);
            }
            return cache.getCache().get(href);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CloudapiException) {
                throw (CloudapiException) cause;
            }
        }
        return null;
    }


    /**
     * Wrapper TypedCached over Guava LoadingCache
     *
     * @param <T>
     * @author irosu
     */
    private class TypedCache<T extends ResourceType> {

        final LoadingCache<String, T> cache;

        TypedCache(final Class<T> type) {
            this.cache = createCache(type);
        }

        LoadingCache<String, T> createCache(final Class<T> type) {
            return CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .build(new CacheLoader<String, T>() {

                        @Override
                        public T load(String key) throws Exception {
                            return delegate.getByHref(type, key);
                        }

                    });
        }

        LoadingCache<String, T> getCache() {
            return cache;
        }
    }

    private class QueryCache<T extends ResourceType> {

        final Object delegate;

        final LoadingCache<String, T> cache;

        final Method method;

        QueryCache(final String methodName, final Class<?> serviceType, final Class<T> resourceType) {
            this.cache = createCache(resourceType);
            this.delegate = getDelegate(serviceType);
            this.method = getMethod(methodName, delegate.getClass());
        }

        Method getMethod(String methodName, Class<?> serviceType) {
            try {
                return serviceType.getMethod(methodName, String.class);
            } catch (NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
            return null;
        }

        <U> U getDelegate(Class<U> serviceType) {
            return registry.getDelegate(serviceType);
        }

        LoadingCache<String, T> createCache(final Class<T> type) {
            return CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .build(new CacheLoader<String, T>() {

                        @SuppressWarnings("unchecked")
                        @Override
                        public T load(String key) throws Exception {
                            try {
                                return (T) method.invoke(delegate, key);
                            } catch (InvocationTargetException e) {
                                Throwable cause = e.getCause();
                                if (cause instanceof CloudapiException) {
                                    throw (CloudapiException) cause;
                                } else {
                                    throw e;
                                }

                            }
                        }

                    });
        }

        LoadingCache<String, T> getCache() {
            return cache;
        }
    }
}
