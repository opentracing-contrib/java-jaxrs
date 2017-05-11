package io.opentracing.contrib.jaxrs2.itest.common;


import io.opentracing.contrib.jaxrs2.client.ClientTracingFeature.Builder;
import io.opentracing.contrib.jaxrs2.server.ServerSpanDecorator;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * @author Pavol Loffay
 */
public abstract class AbstractJettyTest {

    public static final String SERVER_TRACING_FEATURE = "serveTracingFeature";
    public static final String CLIENT_ATTRIBUTE = "clientBuilder";
    public static final String TRACER_ATTRIBUTE = "tracer";

    protected Server jettyServer;
    protected MockTracer mockTracer;
    protected Client client;

    protected abstract void initServletContext(ServletContextHandler context);

    protected void initTracing(ServletContextHandler context) {
        client.register(new Builder(mockTracer).build());

        ServerTracingDynamicFeature serverTracingFeature =
            new ServerTracingDynamicFeature.Builder(mockTracer)
                .withDecorators(Collections.singletonList(ServerSpanDecorator.STANDARD_TAGS))
            .build();

        context.setAttribute(CLIENT_ATTRIBUTE, client);
        context.setAttribute(TRACER_ATTRIBUTE, mockTracer);
        context.setAttribute(SERVER_TRACING_FEATURE, serverTracingFeature);
    }

    @Before
    public void before() throws Exception {
        mockTracer = new MockTracer(MockTracer.Propagator.TEXT_MAP);
        client = ClientBuilder.newBuilder().build();

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        initServletContext(context);
        initTracing(context);

        jettyServer = new Server(0);
        jettyServer.setHandler(context);
        jettyServer.start();
    }

    @After
    public void after() throws Exception {
        jettyServer.stop();
    }

    public String url(String path) {
        return "http://localhost:" + getPort() + path;
    }

    public int getPort() {
        return ((ServerConnector)jettyServer.getConnectors()[0]).getLocalPort();
    }

    public static void assertOnErrors(List<MockSpan> spans) {
        for (MockSpan mockSpan: spans) {
            Assert.assertEquals(mockSpan.generatedErrors().toString(), 0, mockSpan.generatedErrors().size());
        }
    }
}
