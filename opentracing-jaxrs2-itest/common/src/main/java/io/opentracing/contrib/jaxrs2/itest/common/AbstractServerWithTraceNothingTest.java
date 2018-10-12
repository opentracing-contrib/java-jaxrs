package io.opentracing.contrib.jaxrs2.itest.common;

import io.opentracing.contrib.jaxrs2.client.ClientTracingFeature;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import io.opentracing.contrib.jaxrs2.server.SpanFinishingFilter;
import io.opentracing.mock.MockSpan;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.DispatcherType;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.util.EnumSet;
import java.util.List;

import static org.awaitility.Awaitility.await;

/**
 * This is a test whether {@code ServerTracingDynamicFeature.Buidler.withTraceNothing()} behaves as documented:
 * <pre><code>
 *     "Only resources annotated with {@literal @}Traced will be traced."
 * </code></pre>
 *
 * Tests that <a href="https://github.com/opentracing-contrib/java-jaxrs/issues/107">Issue 107</a> is fixed.
 *
 * @author Sjoerd Talsma
 */
public abstract class AbstractServerWithTraceNothingTest extends AbstractJettyTest {

    @Override
    protected void initTracing(ServletContextHandler context) {
        client.register(new ClientTracingFeature.Builder(mockTracer).build());

        ServerTracingDynamicFeature serverTracingBuilder =
                new ServerTracingDynamicFeature.Builder(mockTracer)
                        .withTraceNothing() // This should only trace @Traced annotations, per documentation!
                        .build();
        context.addFilter(new FilterHolder(new SpanFinishingFilter()),
                "/*", EnumSet.of(DispatcherType.REQUEST));

        context.setAttribute(TRACER_ATTRIBUTE, mockTracer);
        context.setAttribute(CLIENT_ATTRIBUTE, client);
        context.setAttribute(SERVER_TRACING_FEATURE, serverTracingBuilder);
    }

    @Test
    public void testUnAnnotatedMethod() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/hello/1?q=a"))
                .request()
                .get();
        response.close();
        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(0, mockSpans.size());
    }

    @Test
    public void testTracedFalseMethod() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/hello/1?q=a"))
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
    public void testTraceEnabledOnMethodWithinTracedFalseClass() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(url("/tracedFalse/enabled"))
                .request()
                .get();
        response.close();
        await().until(finishedSpansSizeEquals(1));

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(1, mockSpans.size());
        assertOnErrors(mockTracer.finishedSpans());
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

}
