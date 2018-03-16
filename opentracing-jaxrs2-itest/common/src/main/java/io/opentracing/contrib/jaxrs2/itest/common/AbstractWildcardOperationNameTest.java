package io.opentracing.contrib.jaxrs2.itest.common;

import static org.awaitility.Awaitility.await;

import io.opentracing.contrib.jaxrs2.client.ClientTracingFeature.Builder;
import io.opentracing.contrib.jaxrs2.server.OperationNameProvider.WildcardOperationName;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import io.opentracing.contrib.jaxrs2.server.SpanFinishingFilter;
import io.opentracing.mock.MockSpan;
import java.util.EnumSet;
import java.util.List;
import javax.servlet.DispatcherType;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public abstract class AbstractWildcardOperationNameTest extends AbstractJettyTest {

    @Override
    protected void initTracing(ServletContextHandler context) {
        client.register(new Builder(mockTracer).build());

        ServerTracingDynamicFeature serverTracingBuilder =
            new ServerTracingDynamicFeature.Builder(mockTracer)
                .withOperationNameProvider(WildcardOperationName.newBuilder())
            .build();
        context.addFilter(new FilterHolder(new SpanFinishingFilter(mockTracer)),
            "/*", EnumSet.of(DispatcherType.REQUEST));

        context.setAttribute(TRACER_ATTRIBUTE, mockTracer);
        context.setAttribute(CLIENT_ATTRIBUTE, client);
        context.setAttribute(SERVER_TRACING_FEATURE, serverTracingBuilder);
    }

    @Test
    public void testRoot() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/"))
            .request()
            .get();
        response.close();
        await().until(finishedSpansSizeEquals(1));

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        assertOnErrors(mockTracer.finishedSpans());
        Assert.assertEquals("/", mockSpans.get(0).operationName());
    }

    @Test
    public void testOnePathParam() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/path/foo"))
                .request()
                .get();
        response.close();
        await().until(finishedSpansSizeEquals(1));

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        assertOnErrors(mockTracer.finishedSpans());
        Assert.assertEquals("/path/{pathParam}", mockSpans.get(0).operationName());
    }

    @Test
    public void testTwoPathParams() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/path/foo/path2/bar"))
                .request()
                .get();
        response.close();
        await().until(finishedSpansSizeEquals(1));

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        assertOnErrors(mockTracer.finishedSpans());
        Assert.assertEquals("/path/{pathParam}/path2/{pathParam2}", mockSpans.get(0).operationName());
    }

    @Test
    public void testRegexParam() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/path/param/path/word1"))
                .request()
                .get();
        response.close();
        await().until(finishedSpansSizeEquals(1));

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        assertOnErrors(mockTracer.finishedSpans());
        Assert.assertEquals("/path/{pathParam}/path/{regexParam}", mockSpans.get(0).operationName());
    }
}
