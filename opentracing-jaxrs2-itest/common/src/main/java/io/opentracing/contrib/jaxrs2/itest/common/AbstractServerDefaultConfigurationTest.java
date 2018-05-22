package io.opentracing.contrib.jaxrs2.itest.common;

import static org.awaitility.Awaitility.await;

import io.opentracing.contrib.jaxrs2.client.ClientTracingFeature.Builder;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import io.opentracing.contrib.jaxrs2.server.SpanFinishingFilter;
import io.opentracing.mock.MockSpan;
import io.opentracing.tag.Tags;
import java.util.EnumSet;
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
public abstract class AbstractServerDefaultConfigurationTest extends AbstractJettyTest {

    @Override
    protected void initTracing(ServletContextHandler context) {
        client.register(new Builder(mockTracer).build());

        ServerTracingDynamicFeature serverTracingBuilder =
                new ServerTracingDynamicFeature.Builder(mockTracer)
                        .build();
        context.addFilter(new FilterHolder(new SpanFinishingFilter(mockTracer)),
            "/*", EnumSet.of(DispatcherType.REQUEST));

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
        await().until(finishedSpansSizeEquals(1));

        Assert.assertEquals(1, mockTracer.finishedSpans().size());
        assertOnErrors(mockTracer.finishedSpans());

        MockSpan mockSpan = mockTracer.finishedSpans().get(0);
        Assert.assertEquals("/hello/{id}", mockSpan.operationName());
        Assert.assertEquals(5, mockSpan.tags().size());
        Assert.assertEquals(Tags.SPAN_KIND_SERVER, mockSpan.tags().get(Tags.SPAN_KIND.getKey()));
        Assert.assertEquals("jaxrs", mockSpan.tags().get(Tags.COMPONENT.getKey()));
        Assert.assertEquals(url("/hello/1"), mockSpan.tags().get(Tags.HTTP_URL.getKey()));
        Assert.assertEquals("GET", mockSpan.tags().get(Tags.HTTP_METHOD.getKey()));
        Assert.assertEquals(200, mockSpan.tags().get(Tags.HTTP_STATUS.getKey()));
    }
}
