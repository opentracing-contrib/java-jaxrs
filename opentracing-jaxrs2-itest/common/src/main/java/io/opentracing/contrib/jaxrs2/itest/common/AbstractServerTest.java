package io.opentracing.contrib.jaxrs2.itest.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;

import io.opentracing.mock.MockSpan;
import io.opentracing.tag.Tags;

/**
 * @author Pavol Loffay
 */
public abstract class AbstractServerTest extends AbstractJettyTest {

    @Test
    public void testServerStandardTags() throws Exception {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/hello/1"))
            .request()
            .get();
        response.close();

        Assert.assertEquals(1, mockTracer.finishedSpans().size());
        assertOnErrors(mockTracer.finishedSpans());

        MockSpan mockSpan = mockTracer.finishedSpans().get(0);
        Assert.assertEquals("GET", mockSpan.operationName());
        Assert.assertEquals(4, mockSpan.tags().size());
        Assert.assertEquals(Tags.SPAN_KIND_SERVER, mockSpan.tags().get(Tags.SPAN_KIND.getKey()));
        Assert.assertEquals(url("/hello/1"), mockSpan.tags().get(Tags.HTTP_URL.getKey()));
        Assert.assertEquals("GET", mockSpan.tags().get(Tags.HTTP_METHOD.getKey()));
        Assert.assertEquals(200, mockSpan.tags().get(Tags.HTTP_STATUS.getKey()));
    }

    @Test
    public void testServerAndClientTracingChaining() throws Exception {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/clientTracingChaining"))
                .request()
                .get();
        response.close();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(3, mockSpans.size());
        assertOnErrors(mockSpans);
        Assert.assertEquals("GET", mockSpans.get(0).operationName());
        Assert.assertEquals("GET", mockSpans.get(1).operationName());
        Assert.assertEquals("GET", mockSpans.get(2).operationName());
        Assert.assertEquals(1, new HashSet<>(Arrays.asList(
                mockSpans.get(0).context().traceId(),
                mockSpans.get(1).context().traceId(),
                mockSpans.get(2).context().traceId())).size());

        MockSpan clientSpan = mockSpans.get(1);
        assertOnErrors(mockSpans);
        Assert.assertEquals(6, clientSpan.tags().size());
        Assert.assertEquals(Tags.SPAN_KIND_CLIENT, clientSpan.tags().get(Tags.SPAN_KIND.getKey()));
        Assert.assertEquals("localhost", clientSpan.tags().get(Tags.PEER_HOSTNAME.getKey()));
        Assert.assertEquals(getPort(), clientSpan.tags().get(Tags.PEER_PORT.getKey()));
        Assert.assertEquals("GET", clientSpan.tags().get(Tags.HTTP_METHOD.getKey()));
        Assert.assertEquals(url("/hello/1"), clientSpan.tags().get(Tags.HTTP_URL.getKey()));
        Assert.assertEquals(200, clientSpan.tags().get(Tags.HTTP_STATUS.getKey()));
    }

    @Test
    public void testAsyncSever() throws Exception {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/async"))
                .request()
                .get();
        response.close();

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
    public void testTracedCustomOperationName() throws Exception {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/operation"))
                .request()
                .get();
        response.close();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        assertOnErrors(mockTracer.finishedSpans());
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
        // TODO jax-rs interceptors do not trace this
        Assert.assertEquals(0, mockSpans.size());
    }

    @Test
    public void testPathParam() throws Exception {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/path/foo"))
                .request()
                .get();
        response.close();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        assertOnErrors(mockTracer.finishedSpans());
        Assert.assertEquals("GET", mockSpans.get(0).operationName());
    }
}
