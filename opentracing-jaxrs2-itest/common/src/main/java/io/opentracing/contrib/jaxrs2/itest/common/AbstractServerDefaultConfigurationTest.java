package io.opentracing.contrib.jaxrs2.itest.common;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Assert;
import org.junit.Test;

import io.opentracing.contrib.jaxrs2.client.ClientTracingFeature.Builder;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import io.opentracing.mock.MockSpan;
import io.opentracing.tag.Tags;

/**
 * @author Pavol Loffay
 */
public abstract class AbstractServerDefaultConfigurationTest extends AbstractJettyTest {

    @Override
    protected void initTracing(ServletContextHandler context) {
        client.register(new Builder(mockTracer).build());

        ServerTracingDynamicFeature serverTracingBuilder =
                new ServerTracingDynamicFeature.Builder(mockTracer)
                        .build();

        context.setAttribute(TRACER_ATTRIBUTE, mockTracer);
        context.setAttribute(CLIENT_ATTRIBUTE, client);
        context.setAttribute(SERVER_TRACING_FEATURE, serverTracingBuilder);
    }

    @Test
    public void testDefaultOperationNameAndTags() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/hello/1"))
                .request()
                .get();
        response.close();

        Assert.assertEquals(1, mockTracer.finishedSpans().size());
        assertOnErrors(mockTracer.finishedSpans());

        MockSpan mockSpan = mockTracer.finishedSpans().get(0);
        Assert.assertEquals("hello/{id}", mockSpan.operationName());
        Assert.assertEquals(4, mockSpan.tags().size());
        Assert.assertEquals(Tags.SPAN_KIND_SERVER, mockSpan.tags().get(Tags.SPAN_KIND.getKey()));
        Assert.assertEquals(url("/hello/1"), mockSpan.tags().get(Tags.HTTP_URL.getKey()));
        Assert.assertEquals("GET", mockSpan.tags().get(Tags.HTTP_METHOD.getKey()));
        Assert.assertEquals(200, mockSpan.tags().get(Tags.HTTP_STATUS.getKey()));
    }
}
