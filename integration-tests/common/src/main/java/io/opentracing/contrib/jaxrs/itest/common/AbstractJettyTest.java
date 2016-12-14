package io.opentracing.contrib.jaxrs.itest.common;


import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.TextMapPropagator;
import io.opentracing.tag.Tags;

/**
 * @author Pavol Loffay
 */
public abstract class AbstractJettyTest {

    public static final String TRACER_ATTRIBUTE = "tracer";
    public static final String CLIENT_ATTRIBUTE = "client";

    public static int SERVER_PORT = 3000;

    protected Server jettyServer;
    protected MockTracer mockTracer;
    protected Client client;

    protected abstract void initServletContext(ServletContextHandler context);

    @Before
    public void before() throws Exception {
        mockTracer = new MockTracer(new TextMapPropagator());
        client = ClientBuilder.newBuilder().build();

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setAttribute(CLIENT_ATTRIBUTE, client);
        context.setAttribute(TRACER_ATTRIBUTE, mockTracer);

        initServletContext(context);

        jettyServer = new Server(SERVER_PORT);
        jettyServer.setHandler(context);
        jettyServer.start();
    }

    @After
    public void after() throws Exception {
        jettyServer.stop();
    }

    @Test
    public void testHello() throws Exception {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/hello"))
                .request()
                .get();
        response.close();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        Assert.assertEquals("hello", mockSpans.get(0).operationName());
    }

    @Test
    public void testClientTracingDisabled() throws Exception {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/clientTracingDisabled"))
                .request()
                .get();
        response.close();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(2, mockSpans.size());
        Assert.assertEquals("hello", mockSpans.get(0).operationName());
        Assert.assertEquals("clientTracingDisabled", mockSpans.get(1).operationName());
        Assert.assertNotEquals(mockSpans.get(0).context().traceId(), mockSpans.get(1).context().traceId());
    }

    @Test
    public void testClientTracingEnabled() throws Exception {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/clientTracingEnabled"))
                .request()
                .get();
        response.close();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(3, mockSpans.size());
        Assert.assertEquals("hello", mockSpans.get(0).operationName());
        Assert.assertEquals("hello", mockSpans.get(1).operationName());
        Assert.assertEquals("clientTracingEnabled", mockSpans.get(2).operationName());
        Assert.assertEquals(1, new HashSet<>(Arrays.asList(
                mockSpans.get(0).context().traceId(),
                mockSpans.get(1).context().traceId(),
                mockSpans.get(2).context().traceId())).size());
    }

    @Test
    public void testAsync() throws Exception {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/async"))
                .request()
                .get();
        response.close();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(2, mockSpans.size());
        Assert.assertEquals("expensiveOperation", mockSpans.get(0).operationName());
        Assert.assertEquals("async", mockSpans.get(1).operationName());
        Assert.assertEquals(1, new HashSet<>(Arrays.asList(
                mockSpans.get(0).context().traceId(),
                mockSpans.get(1).context().traceId())).size());
    }

    @Test
    public void testStandardTags() throws Exception {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/hello"))
                .request()
                .get();
        response.close();

        Assert.assertEquals(1, mockTracer.finishedSpans().size());

        MockSpan mockSpan = mockTracer.finishedSpans().get(0);
        Assert.assertEquals(4, mockSpan.tags().size());
        Assert.assertEquals(Tags.SPAN_KIND_SERVER, mockSpan.tags().get(Tags.SPAN_KIND.getKey()));
        Assert.assertEquals(url("/hello"), mockSpan.tags().get(Tags.HTTP_URL.getKey()));
        Assert.assertEquals("GET", mockSpan.tags().get("http.method"));
        Assert.assertEquals(200, mockSpan.tags().get(Tags.HTTP_STATUS.getKey()));
    }

    @Test
    public void testTracedCustomOperationName() throws Exception {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/operation"))
                .request()
                .get();
        response.close();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        Assert.assertEquals("renamedOperation", mockSpans.get(0).operationName());
    }

    @Test
    public void testNotExistingURL() throws Exception {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/doesNotExist"))
                .request()
                .get();
        response.close();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(0, mockSpans.size());
    }

    public String url(String path) {
        return "http://localhost:" + SERVER_PORT + path;
    }
}
