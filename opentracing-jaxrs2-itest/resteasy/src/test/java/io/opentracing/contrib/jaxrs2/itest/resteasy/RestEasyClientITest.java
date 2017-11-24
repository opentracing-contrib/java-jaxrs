package io.opentracing.contrib.jaxrs2.itest.resteasy;

import io.opentracing.contrib.concurrent.TracedExecutorService;
import io.opentracing.contrib.jaxrs2.itest.common.AbstractClientTest;
import java.io.IOException;
import java.util.concurrent.Executors;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.Before;

/**
 * @author Pavol Loffay
 */
public class RestEasyClientITest extends AbstractClientTest {

    @Override
    protected Client getClient() {
        ResteasyProviderFactory instance = ResteasyProviderFactory.getInstance();
        instance.getClientDynamicFeatures().add(new DummyFeature());
        instance.register(DummyFeature.class);
        instance.register(DummyClientFilter.class);
        instance.getClientRequestFilters().registerSingleton(new DummyClientFilter());
        instance.getClientRequestFilters().registerSingleton(new ClientRequestFilter() {
            @Override
            public void filter(ClientRequestContext requestContext) throws IOException {
                System.out.println("lambda client filter");
            }
        });

        ResteasyClientBuilder clientBuilder = (ResteasyClientBuilder) ClientBuilder.newBuilder();
        // TODO when I do this filters are registered
        clientBuilder.providerFactory(instance);

        /**
         * To avoid  RESTEASY004655 "connection is still allocated" in {@link #testAsyncMultipleRequests()}
         */
        clientBuilder.connectionPoolSize(150);
        clientBuilder.asyncExecutor(new TracedExecutorService(Executors.newFixedThreadPool(8), mockTracer));
        return clientBuilder.build();
    }

    @Override
    protected void initServletContext(ServletContextHandler context) {
        RestEasyHelper.initServletContext(context);
    }

    @Before
    public void before() throws Exception {
        super.before();
        ResteasyProviderFactory.getInstance().getClientDynamicFeatures().add(new DummyFeature());
        ResteasyProviderFactory.getInstance().getClientRequestFilters().registerClass(DummyClientFilter.class);
        RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
    }

    @Provider
    public static class DummyFeature implements DynamicFeature {
        @Override
        public void configure(ResourceInfo resourceInfo, FeatureContext context) {
            System.out.println("configure client dynamic feature");
            context.register(new DummyClientFilter("FromDynamicFeature"));
        }
    }

    @Priority(Priorities.HEADER_DECORATOR)
    public static class DummyClientFilter implements ClientRequestFilter {

        private String name;

        public DummyClientFilter() {
          this("DefaultConstructor");
        }

        public DummyClientFilter(String name) {
            this.name = name;
        }
        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            System.out.println(name);
        }
    }
}
