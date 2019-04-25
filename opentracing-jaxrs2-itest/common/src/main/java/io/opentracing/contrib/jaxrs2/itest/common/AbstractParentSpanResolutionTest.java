package io.opentracing.contrib.jaxrs2.itest.common;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.contrib.jaxrs2.client.ClientTracingFeature;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import io.opentracing.contrib.jaxrs2.server.SpanFinishingFilter;
import io.opentracing.mock.MockSpan;
import io.opentracing.tag.Tags;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import static org.awaitility.Awaitility.await;

public abstract class AbstractParentSpanResolutionTest extends AbstractJettyTest {

    protected abstract boolean shouldUseParentSpan();

    @Override
    protected void initTracing(ServletContextHandler context) {
        client.register(new ClientTracingFeature.Builder(mockTracer).build());

        ServerTracingDynamicFeature.Builder builder = new ServerTracingDynamicFeature.Builder(mockTracer);
        if (shouldUseParentSpan()) {
            builder = builder.withJoinExistingActiveSpan(true);
        }
        ServerTracingDynamicFeature serverTracingFeature = builder.build();

        context.addFilter(new FilterHolder(new SpanFinishingFilter()),
                "/*", EnumSet.of(DispatcherType.REQUEST));

        context.setAttribute(TRACER_ATTRIBUTE, mockTracer);
        context.setAttribute(CLIENT_ATTRIBUTE, client);
        context.setAttribute(SERVER_TRACING_FEATURE, serverTracingFeature);
    }


    @Override
    protected void initServletContext(ServletContextHandler context) {
        context.addFilter(new FilterHolder(new FilterThatInitsSpan()), "/*",
                EnumSet.of(
                        DispatcherType.REQUEST,
                        DispatcherType.FORWARD,
                        // TODO CXF does not call AsyncListener#onComplete() without this (it calls only onStartAsync)
                        DispatcherType.ASYNC,
                        DispatcherType.ERROR,
                        DispatcherType.INCLUDE
                )
        );
    }

    @Test
    public void testUseActiveSpanIfSet() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/hello/1"))
                .request()
                .get();
        response.close();
        await().until(finishedSpansSizeEquals(2));

        List<MockSpan> spans = mockTracer.finishedSpans();

        Assert.assertEquals(2, spans.size());

        MockSpan preceding = getSpanWithTag(spans, new ImmutableTag(Tags.COMPONENT.getKey(), "preceding-opentracing-filter"));
        MockSpan original = getSpanWithTag(spans, new ImmutableTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER));

        if (shouldUseParentSpan()) {
            Assert.assertEquals(preceding.context().spanId(), original.parentId());
        } else {
            Assert.assertEquals(0, original.parentId());
        }
    }

    class FilterThatInitsSpan implements Filter {


        @Override
        public void init(FilterConfig filterConfig) throws ServletException {

        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            Span span = mockTracer.buildSpan("initializing-span")
                    .withTag(Tags.COMPONENT.getKey(), "preceding-opentracing-filter")
                    .start();
            try (Scope scope = mockTracer.activateSpan(span)) {
                chain.doFilter(request, response);
            } finally {
                span.finish();
            }
        }

        @Override
        public void destroy() {

        }
    }
}
