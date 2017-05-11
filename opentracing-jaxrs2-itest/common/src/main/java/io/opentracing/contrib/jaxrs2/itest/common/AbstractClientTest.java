package io.opentracing.contrib.jaxrs2.itest.common;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Assert;
import org.junit.Test;

import io.opentracing.NoopTracerFactory;
import io.opentracing.contrib.jaxrs2.client.ClientTracingFeature;
import io.opentracing.contrib.jaxrs2.client.ClientTracingFeature.Builder;
import io.opentracing.contrib.jaxrs2.client.TracingProperties;
import io.opentracing.contrib.jaxrs2.server.ServerSpanDecorator;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;

/**
 * @author Pavol Loffay
 */
public abstract class AbstractClientTest extends AbstractJettyTest {

    @Override
    protected void initTracing(ServletContextHandler context) {
        client.register(new Builder(mockTracer).build());

        ServerTracingDynamicFeature serverTracingBuilder =
                new ServerTracingDynamicFeature.Builder(NoopTracerFactory.create())
                        .withDecorators(Arrays.asList(ServerSpanDecorator.HTTP_WILDCARD_PATH_OPERATION_NAME))
                        .build();

        context.setAttribute(TRACER_ATTRIBUTE, mockTracer);
        context.setAttribute(CLIENT_ATTRIBUTE, client);
        context.setAttribute(SERVER_TRACING_FEATURE, serverTracingBuilder);
    }

    @Test
    public void testDefaultConfiguration() {
        MockTracer mockTracer = new MockTracer(Propagator.TEXT_MAP);
        GlobalTracer.register(mockTracer);
        Client client = ClientBuilder.newClient()
                .register(ClientTracingFeature.class);

        Response response = client.target(url("/hello"))
                .request()
                .get();
        response.close();

        Assert.assertEquals(1, mockTracer.finishedSpans().size());
    }

    @Test
    public void testStandardTags() {
        Response response = client.target(url("/hello"))
                .request()
                .get();
        response.close();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        assertOnErrors(mockTracer.finishedSpans());

        MockSpan mockSpan = mockSpans.get(0);
        Assert.assertEquals("GET", mockSpan.operationName());
        Assert.assertEquals(6, mockSpan.tags().size());
        Assert.assertEquals(Tags.SPAN_KIND_CLIENT, mockSpan.tags().get(Tags.SPAN_KIND.getKey()));
        Assert.assertEquals(url("/hello"), mockSpan.tags().get(Tags.HTTP_URL.getKey()));
        Assert.assertEquals("GET", mockSpan.tags().get(Tags.HTTP_METHOD.getKey()));
        Assert.assertEquals(200, mockSpan.tags().get(Tags.HTTP_STATUS.getKey()));
        Assert.assertEquals(getPort(), mockSpan.tags().get(Tags.PEER_PORT.getKey()));
        Assert.assertEquals("localhost", mockSpan.tags().get(Tags.PEER_HOSTNAME.getKey()));
    }

    /**
     * TODO Some jax-rs clients does not support redirects (RestEasy, CXF). If they will support we
     * make sure that span context is copied to redirected requests.
     */
    @Test
    public void testRedirect() {
        Response response = client.target(url("/redirect"))
                .request()
                // disable for jersey
                .property("jersey.config.client.followRedirects", false)
                .get();
        response.close();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        assertOnErrors(mockTracer.finishedSpans());

        MockSpan mockSpan = mockSpans.get(0);
        Assert.assertEquals("GET", mockSpan.operationName());
        Assert.assertEquals(6, mockSpan.tags().size());
        Assert.assertEquals(Tags.SPAN_KIND_CLIENT, mockSpan.tags().get(Tags.SPAN_KIND.getKey()));
        Assert.assertEquals(url("/redirect"), mockSpan.tags().get(Tags.HTTP_URL.getKey()));
        Assert.assertEquals("GET", mockSpan.tags().get(Tags.HTTP_METHOD.getKey()));
        Assert.assertEquals(303, mockSpan.tags().get(Tags.HTTP_STATUS.getKey()));
        Assert.assertEquals(getPort(), mockSpan.tags().get(Tags.PEER_PORT.getKey()));
        Assert.assertEquals("localhost", mockSpan.tags().get(Tags.PEER_HOSTNAME.getKey()));
    }

    @Test
    public void testParentSpan() {
        MockSpan parentSpan = mockTracer.buildSpan("foo").start();

        Response response = client.target(url("/hello"))
                .request()
                .property(TracingProperties.CHILD_OF, parentSpan.context())
                .get();
        response.close();

        parentSpan.finish();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(2, mockSpans.size());
        assertOnErrors(mockTracer.finishedSpans());

        MockSpan clientSpan = mockSpans.get(0);
        Assert.assertEquals(parentSpan.context().traceId(), clientSpan.context().traceId());
        Assert.assertEquals(parentSpan.context().spanId(), clientSpan.parentId());
    }

    @Test
    public void testClientTracingDisabled() throws Exception {
        Response response = client.target(url("/hello"))
                .request()
                .property(TracingProperties.TRACING_DISABLED, true)
                .get();
        response.close();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(0, mockSpans.size());
    }

    @Test
    public void testStandardTagsAsync() throws InterruptedException, ExecutionException {
        Future<Response> responseFuture = client.target(url("/hello"))
                .request()
                .async()
                .get(new InvocationCallback<Response>() {
                    @Override
                    public void completed(Response response) {
                        // when completed span is already finished
                        Assert.assertEquals(1, mockTracer.finishedSpans().size());
                    }

                    @Override
                    public void failed(Throwable throwable) {
                        Assert.fail();
                    }
                });

        responseFuture.get();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        assertOnErrors(mockSpans);
        Assert.assertEquals(1, mockSpans.size());

        MockSpan mockSpan = mockSpans.get(0);
        Assert.assertEquals("GET", mockSpan.operationName());
        Assert.assertEquals(6, mockSpan.tags().size());
        Assert.assertEquals(Tags.SPAN_KIND_CLIENT, mockSpan.tags().get(Tags.SPAN_KIND.getKey()));
        Assert.assertEquals(url("/hello"), mockSpan.tags().get(Tags.HTTP_URL.getKey()));
        Assert.assertEquals("GET", mockSpan.tags().get(Tags.HTTP_METHOD.getKey()));
        Assert.assertEquals(200, mockSpan.tags().get(Tags.HTTP_STATUS.getKey()));
        Assert.assertEquals(getPort(), mockSpan.tags().get(Tags.PEER_PORT.getKey()));
        Assert.assertEquals("localhost", mockSpan.tags().get(Tags.PEER_HOSTNAME.getKey()));
    }

    @Test
    public void testUnknownHostException() {
        try {
            Response response = client.target("http://nonexisting.example.com")
                    .request()
                    .get();
            response.close();
        } catch (ProcessingException ex) {
        }

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        // TODO currently it is not possible to catch exceptions thrown by jax-rs
        Assert.assertEquals(0, mockSpans.size());
    }
}
