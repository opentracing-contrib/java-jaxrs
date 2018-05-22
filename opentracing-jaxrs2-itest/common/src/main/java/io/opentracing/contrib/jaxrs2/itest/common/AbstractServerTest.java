package io.opentracing.contrib.jaxrs2.itest.common;

import static org.awaitility.Awaitility.await;

import io.opentracing.mock.MockSpan;
import io.opentracing.tag.Tags;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public abstract class AbstractServerTest extends AbstractJettyTest {

    @Test
    public void testServerStandardTags() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/hello/1?q=a"))
            .request()
            .get();
        response.close();
        await().until(finishedSpansSizeEquals(1));

        Assert.assertEquals(1, mockTracer.finishedSpans().size());
        assertOnErrors(mockTracer.finishedSpans());

        MockSpan mockSpan = mockTracer.finishedSpans().get(0);
        Assert.assertEquals("GET", mockSpan.operationName());
        Assert.assertEquals(5, mockSpan.tags().size());
        Assert.assertEquals(Tags.SPAN_KIND_SERVER, mockSpan.tags().get(Tags.SPAN_KIND.getKey()));
        Assert.assertEquals("jaxrs", mockSpan.tags().get(Tags.COMPONENT.getKey()));
        Assert.assertEquals(url("/hello/1?q=a"), mockSpan.tags().get(Tags.HTTP_URL.getKey()));
        Assert.assertEquals("GET", mockSpan.tags().get(Tags.HTTP_METHOD.getKey()));
        Assert.assertEquals(200, mockSpan.tags().get(Tags.HTTP_STATUS.getKey()));
    }

    @Test
    public void testServerAndClientTracingChaining() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/clientTracingChaining"))
                .request()
                .get();
        response.close();
        await().until(finishedSpansSizeEquals(3));

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(mockSpans.size(),3);
        assertOnErrors(mockSpans);
        Assert.assertEquals("GET", mockSpans.get(0).operationName());
        Assert.assertEquals("GET", mockSpans.get(1).operationName());
        Assert.assertEquals("GET", mockSpans.get(2).operationName());
        Assert.assertEquals(1, new HashSet<>(Arrays.asList(
                mockSpans.get(0).context().traceId(),
                mockSpans.get(1).context().traceId(),
                mockSpans.get(2).context().traceId())).size());

        MockSpan clientSpan = getSpanWithTag(mockSpans, new ImmutableTag(Tags.SPAN_KIND, Tags.SPAN_KIND_CLIENT),
            new ImmutableTag(Tags.HTTP_URL, url("/hello/1")));
        assertOnErrors(mockSpans);
        Assert.assertEquals(7, clientSpan.tags().size());
        Assert.assertEquals(Tags.SPAN_KIND_CLIENT, clientSpan.tags().get(Tags.SPAN_KIND.getKey()));
        Assert.assertEquals("jaxrs", clientSpan.tags().get(Tags.COMPONENT.getKey()));
        Assert.assertEquals("localhost", clientSpan.tags().get(Tags.PEER_HOSTNAME.getKey()));
        Assert.assertEquals(getPort(), clientSpan.tags().get(Tags.PEER_PORT.getKey()));
        Assert.assertEquals("GET", clientSpan.tags().get(Tags.HTTP_METHOD.getKey()));
        Assert.assertEquals(url("/hello/1"), clientSpan.tags().get(Tags.HTTP_URL.getKey()));
        Assert.assertEquals(200, clientSpan.tags().get(Tags.HTTP_STATUS.getKey()));
    }

    @Test
    public void testAsyncError() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/asyncError"))
            .request()
            .get();
        response.close();
        await().until(finishedSpansSizeEquals(1));

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        assertOnErrors(mockSpans);
//        TODO resteasy and CXF do not propagate exception to the filter, https://issues.jboss.org/browse/RESTEASY-1758
//        MockSpan mockSpan = mockSpans.get(0);
//        Assert.assertEquals(5, mockSpan.tags().size());
//        Assert.assertEquals(true, mockSpan.tags().get(Tags.ERROR.getKey()));
//        Assert.assertEquals(1, mockSpan.logEntries().size());
//        Assert.assertEquals(2, mockSpan.logEntries().get(0).fields().size());
//        Assert.assertEquals(Tags.ERROR.getKey(), mockSpan.logEntries().get(0).fields().get("event"));
//        Assert.assertTrue(mockSpan.logEntries().get(0).fields().get("error.object") instanceof Throwable);
    }

    @Test
    public void testAsyncSever() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/async"))
                .request()
                .get();
        response.close();
        await().until(finishedSpansSizeEquals(2));

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(2, mockSpans.size());
        assertOnErrors(mockSpans);
        Assert.assertEquals("expensiveOperation", mockSpans.get(0).operationName());
        Assert.assertEquals("GET", mockSpans.get(1).operationName());
        Assert.assertEquals(1, new HashSet<>(Arrays.asList(
                mockSpans.get(0).context().traceId(),
                mockSpans.get(1).context().traceId())).size());
    }

    @Test
    public void testTracedCustomOperationName() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/operation"))
                .request()
                .get();
        response.close();
        await().until(finishedSpansSizeEquals(1));

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        assertOnErrors(mockTracer.finishedSpans());
        Assert.assertEquals("renamedOperation", mockSpans.get(0).operationName());
    }

    @Test
    public void testTracedFalseMethod() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/tracedFalseIn"))
            .request()
            .get();
        response.close();
        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(0, mockSpans.size());
    }

    @Test
    public void testTracedFalseClass() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/tracedFalse/foo"))
            .request()
            .get();
        response.close();
        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(0, mockSpans.size());
    }

    @Test
    public void testTracedFalseClassOverride() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/tracedFalse/enabled"))
            .request()
            .get();
        response.close();
        await().until(finishedSpansSizeEquals(1));

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        assertOnErrors(mockTracer.finishedSpans());
        Assert.assertEquals("GET", mockSpans.get(0).operationName());
    }

    @Test
    public void testNotExistingURL() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/doesNotExist"))
                .request()
                .get();
        response.close();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        // TODO jax-rs interceptors do not trace this https://github.com/opentracing-contrib/java-jaxrs/issues/51
        Assert.assertEquals(0, mockSpans.size());
    }

    @Test
    public void testExceptionInHandler() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/exception"))
            .request()
            .get();
        response.close();
        Assert.assertEquals(500, response.getStatus());
        await().until(finishedSpansSizeEquals(1));

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        MockSpan mockSpan = mockSpans.get(0);
        Assert.assertEquals(6, mockSpan.tags().size());
        Assert.assertEquals(true, mockSpan.tags().get(Tags.ERROR.getKey()));
        // TODO resteasy and CXF returns 200
//        Assert.assertEquals(500, mockSpan.tags().get(Tags.HTTP_STATUS.getKey()));
    }

    @Test
    public void testMappedExceptionInHandler() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/mappedException"))
            .request()
            .get();
        response.close();
        Assert.assertEquals(405, response.getStatus());
        await().until(finishedSpansSizeEquals(1));

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        MockSpan mockSpan = mockSpans.get(0);
        Assert.assertEquals(5, mockSpan.tags().size());
        Assert.assertNull(mockSpan.tags().get(Tags.ERROR.getKey()));
    }

    @Test
    public void testPathParam() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/path/foo"))
                .request()
                .get();
        response.close();
        await().until(finishedSpansSizeEquals(1));

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        assertOnErrors(mockTracer.finishedSpans());
        Assert.assertEquals("GET", mockSpans.get(0).operationName());
    }

    @Test
    public void testRequestBlockedByFilter() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/filtered"))
                .request()
                .get();
        response.close();
        Assert.assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(0, mockSpans.size());
    }

    @Test
    public void testSkipPattern() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/health"))
            .request()
            .get();
        response.close();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(0, mockSpans.size());
    }

    @Test
    public void testSerializationResponseAndRequestWithBody() {
        Response response = ClientBuilder.newClient()
            .target(url("/postWithBody"))
            .request()
            .post(Entity.entity("entity", MediaType.TEXT_PLAIN_TYPE));
        Assert.assertEquals("entity", response.readEntity(String.class));
        response.close();
        await().until(finishedSpansSizeEquals(3));

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(3, mockSpans.size());

        final MockSpan serializationRequestSpan = mockSpans.get(0);
        final MockSpan serializationResponseSpan = mockSpans.get(1);
        final MockSpan parentSpan = mockSpans.get(2);
        assertRequestSerialization(parentSpan, serializationRequestSpan);
        assertResponseSerialization(parentSpan, serializationResponseSpan);
    }

    @Test
    public void testMultipleServerRequests() throws ExecutionException, InterruptedException {
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        List<Future<?>> futures = new ArrayList<>(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            futures.add(executorService.submit(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < 10; j++) {
                        ClientBuilder.newClient()
                            .target(url("/hello/" + j))
                            .request()
                            .get()
                            .close();
                    }
                }
            }));
        }

        for (Future<?> future: futures) {
            future.get();
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        assertOnErrors(mockSpans);
        Assert.assertEquals(numberOfThreads*10, mockSpans.size());
        for (MockSpan mockSpan: mockSpans) {
            Assert.assertEquals(0, mockSpan.parentId());
        }
    }

    private void assertRequestSerialization(MockSpan parentSpan, MockSpan serializationSpan) {
        Assert.assertEquals("deserialize", serializationSpan.operationName());
        assertSerializationSpan(parentSpan, serializationSpan);
    }

    private void assertResponseSerialization(MockSpan parentSpan, MockSpan serializationSpan) {
        Assert.assertEquals("serialize", serializationSpan.operationName());
        assertSerializationSpan(parentSpan, serializationSpan);
    }

    private void assertSerializationSpan(MockSpan parentSpan, MockSpan serializationSpan) {
        Assert.assertEquals(2, serializationSpan.tags().size());
        Assert.assertTrue(Objects.toString(serializationSpan.tags().get("media.type")).contains(MediaType.TEXT_PLAIN));
        Assert.assertEquals(String.class.getName(), serializationSpan.tags().get("entity.type"));

        Assert.assertEquals(parentSpan.context().spanId(), serializationSpan.parentId());
        Assert.assertEquals(parentSpan.context().traceId(), serializationSpan.context().traceId());
    }
}
