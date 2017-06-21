package io.opentracing.contrib.jaxrs2.itest.jersey;

import io.opentracing.contrib.concurrent.TracedExecutorService;
import io.opentracing.contrib.jaxrs2.itest.common.AbstractClientTest;
import java.util.concurrent.Executors;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * @author Pavol Loffay
 */
public class JerseyClientITest extends AbstractClientTest {

    @Override
    protected Client getClient() {
        return ClientBuilder.newBuilder()
            .register(new DelegateExecutorServiceProvider(
                new TracedExecutorService(Executors.newFixedThreadPool(8), mockTracer)))
            .build();
    }

    @Override
    protected void initServletContext(ServletContextHandler context) {
        JerseyHelper.initServletContext(context);
    }
}
