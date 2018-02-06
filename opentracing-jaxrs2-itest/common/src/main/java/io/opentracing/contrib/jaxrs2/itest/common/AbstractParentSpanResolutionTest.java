package io.opentracing.contrib.jaxrs2.itest.common;

import static org.awaitility.Awaitility.await;

import io.opentracing.Scope;
import io.opentracing.mock.MockSpan;
import io.opentracing.tag.Tags;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import javax.servlet.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

public abstract class AbstractParentSpanResolutionTest extends AbstractJettyTest {

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

        Assert.assertEquals(preceding.context().spanId(), original.parentId());
    }

    class FilterThatInitsSpan implements Filter {


        @Override
        public void init(FilterConfig filterConfig) throws ServletException {

        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            Scope scope = mockTracer.buildSpan("initializing-span")
                    .withTag(Tags.COMPONENT.getKey(), "preceding-opentracing-filter")
                    .startActive(true);
            chain.doFilter(request, response);
            scope.close();
        }

        @Override
        public void destroy() {

        }
    }
}
