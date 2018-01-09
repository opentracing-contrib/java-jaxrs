package io.opentracing.contrib.jaxrs2.itest.resteasy;

import io.opentracing.contrib.concurrent.TracedExecutorService;
import io.opentracing.contrib.jaxrs2.itest.common.AbstractClientTest;
import java.util.concurrent.Executors;
import javax.ws.rs.client.Client;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

/**
 * @author Pavol Loffay
 */
public class RestEasyClientITest extends AbstractClientTest {

    @Override
    protected Client getClient() {
        ResteasyClientBuilder clientBuilder = (ResteasyClientBuilder) ResteasyClientBuilder.newBuilder();
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
}
