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

package org.ecloudmanager.tmrk.cloudapi;


import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.ecloudmanager.tmrk.cloudapi.exceptions.CloudapiException;
import org.ecloudmanager.tmrk.cloudapi.model.ErrorType;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import java.io.Closeable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Properties;


/**
 * @author irosu
 */
public class CloudapiEndpointFactory implements Closeable, CloudapiConstants {

    private Properties configuration;

    private ResteasyWebTarget baseTarget;


    public CloudapiEndpointFactory() {
        this(System.getProperties());
    }

    public CloudapiEndpointFactory(String accessKey, String privateKey) {
        this(createConfiguration(accessKey, privateKey));
    }

    public CloudapiEndpointFactory(final Map<String, String> properties) {
        this(createConfiguration(properties));
    }

    public CloudapiEndpointFactory(Properties configuration) {
        this.configuration = configuration;
    }

    private static Properties createConfiguration(String accessKey, String privateKey) {
        Properties configuration = new Properties();
        configuration.put(TMRK_API_ACCESS_KEY_PROP, accessKey);
        configuration.put(TMRK_API_PRIVATE_KEY_PROP, privateKey);
        return configuration;
    }

    private static Properties createConfiguration(final Map<String, String> properties) {
        Properties configuration = new Properties();
        configuration.putAll(properties);
        return configuration;
    }

    public void updateCredentials(String accessKey, String privateKey) {
        if (accessKey == null || privateKey == null) {
            configuration = System.getProperties();
        } else {
            configuration = createConfiguration(accessKey, privateKey);
        }

        ResteasyClient client = baseTarget.getResteasyClient();
        ApacheHttpClient4Engine httpEngine = (ApacheHttpClient4Engine) client.httpEngine();
        HttpClient httpClient = httpEngine.getHttpClient();
        DefaultHttpClient defaultHttpClient = (DefaultHttpClient) httpClient;

        defaultHttpClient.removeRequestInterceptorByClass(CloudapiRequestAuhtorization.class);
        defaultHttpClient.addRequestInterceptor(new CloudapiRequestAuhtorization(configuration));
    }

    public void open() {
        final ResteasyClient client = new ResteasyClientBuilder().connectionPoolSize(100).build();
        ApacheHttpClient4Engine httpEngine = (ApacheHttpClient4Engine) client.httpEngine();
        HttpClient httpClient = httpEngine.getHttpClient();
        DefaultHttpClient.class.cast(httpClient).addRequestInterceptor(new CloudapiRequestAuhtorization(configuration));

        client.register(new CloudapiHttpHeadersRequestFilter(configuration));
        client.register(new CloudapiJAXBMessageBodyProvider(configuration));
        baseTarget = client.target(TMRK_API_URL);
    }

    public boolean isOpen() {
        final ResteasyClient client = baseTarget.getResteasyClient();
        return (client != null && !client.isClosed());
    }

    /**
     * Close the factory, releasing any resources that it holds.
     */
    public void close() {
        final ResteasyClient client = baseTarget.getResteasyClient();
        if (client != null && !client.isClosed()) {
            client.close();
        }
    }


    /**
     * Create a new jax-rs client proxy. This method returns a new proxy instance
     * each time it is invoked.
     *
     * @return endpoint service proxy instance
     * @throws CloudapiException if argument is not an interface
     */
    public <T> T createEndpoint(Class<T> clazz) {
        if (clazz.isInterface()) {
            if (isOpen()) {
                return new EndpointProxyFactory<>(clazz).getInstance();
            } else {
                throw new CloudapiException("Endpoint factory not open or closed.");
            }
        } else {
            throw new CloudapiException(clazz.getName() + " in not an interface, so cannot be proxied by JDK.");
        }
    }


    private class EndpointProxyFactory<T> {

        private final Class<T> proxyInterface;

        private EndpointProxyFactory(Class<T> proxyInterface) {
            this.proxyInterface = proxyInterface;
        }


        @SuppressWarnings("unchecked")
        private T getInstance() {
            return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{proxyInterface}, new
                InvocationHandler() {

                final T delegate = baseTarget.proxy(proxyInterface);

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                    Object result = null;
                    try {
                        result = method.invoke(delegate, args);
                    } catch (InvocationTargetException e) {
                        Throwable targetException = e.getTargetException(); // TODO: maybe replace with getCause()
                        targetException.printStackTrace();
                        if (targetException instanceof WebApplicationException) {
                            WebApplicationException ex = (WebApplicationException) targetException;
                            Response response = ex.getResponse();
                            Family family = response.getStatusInfo().getFamily();
                            // handle HTTP error status
                            switch (family) {
                                case CLIENT_ERROR:
                                case SERVER_ERROR:
                                    try {
                                        ErrorType error = response.readEntity(ErrorType.class);
                                        if (error != null) {
                                            throw new CloudapiException(error);
                                        }
//                                        System.err.println(response.readEntity(String.class));
                                    } catch (ProcessingException pe) {
                                        pe.printStackTrace();
                                    }
                                    throw new CloudapiException(targetException);
                                default:
                                    break;

                            }
                        } else {
                            throw new CloudapiException(targetException);
                        }
                    }
                    return result;
                }

            });

        }

    }
}
