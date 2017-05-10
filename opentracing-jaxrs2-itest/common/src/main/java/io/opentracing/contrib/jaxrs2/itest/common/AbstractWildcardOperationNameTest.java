package io.opentracing.contrib.jaxrs2.itest.common;

import io.opentracing.contrib.jaxrs2.client.ClientTracingFeature;
import io.opentracing.contrib.jaxrs2.client.ClientTracingFeature.Builder;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Assert;
import org.junit.Test;

import io.opentracing.contrib.jaxrs2.server.ServerSpanDecorator;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import io.opentracing.mock.MockSpan;

/**
 * @author Pavol Loffay
 */
public abstract class AbstractWildcardOperationNameTest extends AbstractJettyTest {

    @Override
    protected void initTracing(ServletContextHandler context) {
        client.register(new ClientTracingFeature(new Builder(mockTracer)));

        ServerTracingDynamicFeature serverTracingBuilder =
            new ServerTracingDynamicFeature.Builder(mockTracer)
                .withDecorators(Arrays.asList(ServerSpanDecorator.HTTP_WILDCARD_PATH_OPERATION_NAME))
            .build();

        context.setAttribute(TRACER_ATTRIBUTE, mockTracer);
        context.setAttribute(CLIENT_ATTRIBUTE, client);
        context.setAttribute(SERVER_TRACING_FEATURE, serverTracingBuilder);
    }

    @Test
    public void testOnePathParam() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/path/foo"))
                .request()
                .get();
        response.close();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        assertOnErrors(mockTracer.finishedSpans());
        Assert.assertEquals("path/{pathParam}", mockSpans.get(0).operationName());
    }

    @Test
    public void testTwoPathParams() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/path/foo/path2/bar"))
                .request()
                .get();
        response.close();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        assertOnErrors(mockTracer.finishedSpans());
        Assert.assertEquals("path/{pathParam}/path2/{pathParam2}", mockSpans.get(0).operationName());
    }

    @Test
    public void testRegexParam() throws Exception {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/path/param/path/word1"))
                .request()
                .get();
        response.close();

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        assertOnErrors(mockTracer.finishedSpans());
        Assert.assertEquals("path/{pathParam}/path/{regexParam}", mockSpans.get(0).operationName());
    }
}
