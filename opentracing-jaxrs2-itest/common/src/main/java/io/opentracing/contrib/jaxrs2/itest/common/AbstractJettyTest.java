package io.opentracing.contrib.jaxrs2.itest.common;


import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import io.opentracing.contrib.jaxrs2.client.ClientTracingFeature;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;

/**
 * @author Pavol Loffay
 */
public abstract class AbstractJettyTest {

    public static final String TRACER_BUILDER_ATTRIBUTE = "tracerBuilder";
    public static final String CLIENT_BUILDER_ATTRIBUTE = "clientBuilder";

    protected Server jettyServer;
    protected MockTracer mockTracer;
    protected Client client;

    protected abstract void initServletContext(ServletContextHandler context);

    protected void initTracing(ServletContextHandler context) {
        ClientTracingFeature.Builder clientTracingBuilder = ClientTracingFeature.Builder
                .traceAll(mockTracer, client);

        ServerTracingDynamicFeature.Builder serverTracingBuilder = ServerTracingDynamicFeature.Builder
                .traceAll(mockTracer);

        context.setAttribute(CLIENT_BUILDER_ATTRIBUTE, clientTracingBuilder);
        context.setAttribute(TRACER_BUILDER_ATTRIBUTE, serverTracingBuilder);
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
