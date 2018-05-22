package io.opentracing.contrib.jaxrs2.itest.common;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;

import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.client.ClientTracingFeature;
import io.opentracing.contrib.jaxrs2.client.ClientTracingFeature.Builder;
import io.opentracing.contrib.jaxrs2.client.TracingProperties;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.noop.NoopTracerFactory;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import io.opentracing.util.ThreadLocalScopeManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public abstract class AbstractClientTest extends AbstractJettyTest {

    @Override
    protected void initTracing(ServletContextHandler context) {
        client.register(new Builder(mockTracer).build());

        Tracer serverTracer = NoopTracerFactory.create();
        ServerTracingDynamicFeature serverTracingBuilder =
                new ServerTracingDynamicFeature.Builder(serverTracer)
                        .build();

        context.setAttribute(TRACER_ATTRIBUTE, serverTracer);
        context.setAttribute(CLIENT_ATTRIBUTE, ClientBuilder.newClient());
        context.setAttribute(SERVER_TRACING_FEATURE, serverTracingBuilder);
    }

    @Test
    public void testDefaultConfiguration() {
        MockTracer mockTracer = new MockTracer(new ThreadLocalScopeManager(), Propagator.TEXT_MAP);
        GlobalTracer.register(mockTracer);

        Client client = ClientBuilder.newClient()
                .register(ClientTracingFeature.class);

        Response response = client.target(url("/hello"))
                .request()
                .get();
        response.close();
        assertNoActiveSpan();
        Assert.assertEquals(1, mockTracer.finishedSpans().size());
    }

    @Test
    public void testStandardTags() {
        Response response = client.target(url("/hello/1"))
                .request()
                .get();
        response.close();
        assertNoActiveSpan();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        assertOnErrors(mockTracer.finishedSpans());

        MockSpan mockSpan = mockSpans.get(0);
        Assert.assertEquals("GET", mockSpan.operationName());
        Assert.assertEquals(7, mockSpan.tags().size());
        Assert.assertEquals(Tags.SPAN_KIND_CLIENT, mockSpan.tags().get(Tags.SPAN_KIND.getKey()));
        Assert.assertEquals("jaxrs", mockSpan.tags().get(Tags.COMPONENT.getKey()));
        Assert.assertEquals(url("/hello/1"), mockSpan.tags().get(Tags.HTTP_URL.getKey()));
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
        assertNoActiveSpan();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        assertOnErrors(mockTracer.finishedSpans());

        MockSpan mockSpan = mockSpans.get(0);
        Assert.assertEquals("GET", mockSpan.operationName());
        Assert.assertEquals(7, mockSpan.tags().size());
        Assert.assertEquals(Tags.SPAN_KIND_CLIENT, mockSpan.tags().get(Tags.SPAN_KIND.getKey()));
        Assert.assertEquals("jaxrs", mockSpan.tags().get(Tags.COMPONENT.getKey()));
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
        assertNoActiveSpan();

        parentSpan.finish();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(2, mockSpans.size());
        assertOnErrors(mockTracer.finishedSpans());

        MockSpan clientSpan = mockSpans.get(0);
        Assert.assertEquals(parentSpan.context().traceId(), clientSpan.context().traceId());
        Assert.assertEquals(parentSpan.context().spanId(), clientSpan.parentId());
    }

    @Test
    public void testClientTracingDisabled() {
        Response response = client.target(url("/hello"))
                .request()
                .property(TracingProperties.TRACING_DISABLED, true)
                .get();
        response.close();
        assertNoActiveSpan();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(0, mockSpans.size());
    }

    @Test
    public void testStandardTagsAsync() throws InterruptedException, ExecutionException {
        Future<Response> responseFuture = client.target(url("/hello/1"))
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
                        Assert.fail(throwable.toString());
                    }
                });

        responseFuture.get();
        assertNoActiveSpan();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        assertOnErrors(mockSpans);
        Assert.assertEquals(1, mockSpans.size());

        MockSpan mockSpan = mockSpans.get(0);
        Assert.assertEquals("GET", mockSpan.operationName());
        Assert.assertEquals(7, mockSpan.tags().size());
        Assert.assertEquals(Tags.SPAN_KIND_CLIENT, mockSpan.tags().get(Tags.SPAN_KIND.getKey()));
        Assert.assertEquals("jaxrs", mockSpan.tags().get(Tags.COMPONENT.getKey()));
        Assert.assertEquals(url("/hello/1"), mockSpan.tags().get(Tags.HTTP_URL.getKey()));
        Assert.assertEquals("GET", mockSpan.tags().get(Tags.HTTP_METHOD.getKey()));
        Assert.assertEquals(200, mockSpan.tags().get(Tags.HTTP_STATUS.getKey()));
        Assert.assertEquals(getPort(), mockSpan.tags().get(Tags.PEER_PORT.getKey()));
        Assert.assertEquals("localhost", mockSpan.tags().get(Tags.PEER_HOSTNAME.getKey()));
    }

    @Test
    public void testSyncMultipleRequests() throws ExecutionException, InterruptedException {
        int numberOfCalls = 100;

        Map<Long, MockSpan> parentSpans = new LinkedHashMap<>(numberOfCalls);

        ExecutorService executorService = Executors.newFixedThreadPool(8);
        List<Future<?>> futures = new ArrayList<>(numberOfCalls);
        for (int i = 0; i < numberOfCalls; i++) {
            final String requestUrl = url("/hello/" + i);

            final MockSpan parentSpan = mockTracer.buildSpan(requestUrl)
                .ignoreActiveSpan().start();
            parentSpan.setTag("request-url", requestUrl);
            parentSpans.put(parentSpan.context().spanId(), parentSpan);

            futures.add(executorService.submit(new Runnable() {
                @Override
                public void run() {
                    Scope parentScope = mockTracer.scopeManager().activate(parentSpan, true);
                    Response response = client.target(requestUrl)
                        .request()
                        .get();
                    response.close();
                    assertEquals(parentScope, mockTracer.scopeManager().active());
                }
            }));
        }

        // wait to finish all calls
        for (Future<?> future: futures) {
            future.get();
        }

        executorService.awaitTermination(1, TimeUnit.SECONDS);
        executorService.shutdown();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(numberOfCalls, mockSpans.size());
        assertOnErrors(mockSpans);

        for (MockSpan clientSpan: mockSpans) {
            MockSpan parentSpan = parentSpans.get(clientSpan.parentId());
            Assert.assertNotNull(parentSpan);
            Assert.assertEquals(parentSpan.tags().get("request-url"), clientSpan.tags().get(Tags.HTTP_URL.getKey()));

            Assert.assertEquals(parentSpan.context().traceId(), clientSpan.context().traceId());
            Assert.assertEquals(parentSpan.context().spanId(), clientSpan.parentId());
            Assert.assertEquals(0, clientSpan.generatedErrors().size());
        }
    }

    @Test
    public void testAsyncMultipleRequests() throws ExecutionException, InterruptedException {
        int numberOfCalls = 100;

        Map<Long, MockSpan> parentSpans = new LinkedHashMap<>(numberOfCalls);

        ExecutorService executorService = Executors.newFixedThreadPool(8);
        List<Future<?>> futures = new ArrayList<>(numberOfCalls);
        for (int i = 0; i < numberOfCalls; i++) {
            final String requestUrl = url("/hello/" + i);

            final MockSpan parentSpan = mockTracer.buildSpan(requestUrl)
                .ignoreActiveSpan().start();
            parentSpan.setTag("request-url", requestUrl);
            parentSpans.put(parentSpan.context().spanId(), parentSpan);

            futures.add(executorService.submit(new Runnable() {
                @Override
                public void run() {
                    Scope parentScope = mockTracer.scopeManager().activate(parentSpan, true);
                    try {
                        Future<Response> responseFuture = client.target(requestUrl)
                            .request()
                            .async()
                            .get(new InvocationCallback<Response>() {
                                @Override
                                public void completed(Response response) {
                                    // when completed span is already finished
                                }
                                @Override
                                public void failed(Throwable throwable) {
                                    Assert.fail();
                                }
                            });

                        Response response = responseFuture.get();
                        response.close();
                        assertEquals(parentScope, mockTracer.scopeManager().active());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }));
        }

        // wait to finish all calls
        for (Future<?> future: futures) {
            future.get();
        }

        executorService.awaitTermination(1, TimeUnit.SECONDS);
        executorService.shutdown();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(numberOfCalls, mockSpans.size());
        assertOnErrors(mockSpans);

        for (MockSpan clientSpan: mockSpans) {
            MockSpan parentSpan = parentSpans.get(clientSpan.parentId());
            Assert.assertNotNull(parentSpan);
            Assert.assertEquals(parentSpan.tags().get("request-url"), clientSpan.tags().get(Tags.HTTP_URL.getKey()));

            Assert.assertEquals(parentSpan.context().traceId(), clientSpan.context().traceId());
            Assert.assertEquals(parentSpan.context().spanId(), clientSpan.parentId());
            Assert.assertEquals(0, clientSpan.generatedErrors().size());
        }
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

        assertNoActiveSpan();
        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        // TODO currently it is not possible to catch exceptions thrown by jax-rs https://github.com/opentracing-contrib/java-jaxrs/issues/51
        Assert.assertEquals(0, mockSpans.size());
    }

    @Test
    public void testSerializationResponseAndRequestWithBody() {
        String response = client.target(url("/postWithBody"))
            .request()
            .post(Entity.entity("entity", MediaType.TEXT_PLAIN_TYPE), String.class);
        assertNoActiveSpan();

        Assert.assertEquals("entity", response);

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(3, mockSpans.size());

        final MockSpan serializationRequestSpan = mockSpans.get(0);
        final MockSpan parentSpan = mockSpans.get(1);
        final MockSpan serializationResponseSpan = mockSpans.get(2);
        assertRequestSerialization(parentSpan, serializationRequestSpan);
        assertResponseSerialization(parentSpan, serializationResponseSpan);
    }

    private void assertRequestSerialization(MockSpan parentSpan, MockSpan serializationSpan) {
        Assert.assertEquals("serialize", serializationSpan.operationName());
        assertSerializationSpan(parentSpan, serializationSpan);
    }

    private void assertResponseSerialization(MockSpan parentSpan, MockSpan serializationSpan) {
        Assert.assertEquals("deserialize", serializationSpan.operationName());
        assertSerializationSpan(parentSpan, serializationSpan);
    }

    private void assertSerializationSpan(MockSpan parentSpan, MockSpan serializationSpan) {
        /* Resteasy client adds the charset. Remove it for assertion. */
        final MediaType mediaType =
            MediaType.valueOf(Objects.toString(serializationSpan.tags().get("media.type")));
        Assert.assertEquals(MediaType.TEXT_PLAIN_TYPE, new MediaType(mediaType.getType(), mediaType.getSubtype()));
        Assert.assertEquals(String.class.getName(), serializationSpan.tags().get("entity.type"));

        Assert.assertEquals(parentSpan.context().spanId(), serializationSpan.parentId());
        Assert.assertEquals(parentSpan.context().traceId(), serializationSpan.context().traceId());
    }

    private void assertNoActiveSpan() {
        assertNull(mockTracer.activeSpan());
    }
}
