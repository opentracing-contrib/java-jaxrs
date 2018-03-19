package io.opentracing.contrib.jaxrs2.itest.common;


import io.opentracing.contrib.jaxrs2.client.ClientTracingFeature.Builder;
import io.opentracing.contrib.jaxrs2.server.OperationNameProvider.HTTPMethodOperationName;
import io.opentracing.contrib.jaxrs2.server.ServerSpanDecorator;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import io.opentracing.contrib.jaxrs2.server.SpanFinishingFilter;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.noop.NoopTracerFactory;
import io.opentracing.tag.AbstractTag;
import io.opentracing.util.GlobalTracer;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.servlet.DispatcherType;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;

/**
 * @author Pavol Loffay
 */
public abstract class AbstractJettyTest {

    public static final String SERVER_TRACING_FEATURE = "serveTracingFeature";
    public static final String CLIENT_ATTRIBUTE = "clientBuilder";
    public static final String TRACER_ATTRIBUTE = "tracer";

    // static to close it at the end
    static Server jettyServer;
    protected final MockTracer mockTracer = new MockTracer();
    protected final String contextPath = "/context";
    protected final Client client;

    public AbstractJettyTest() {
        this.client = getClient();

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath(contextPath);

        initServletContext(context);
        initTracing(context);

        this.jettyServer = new Server(0);
        this.jettyServer.setHandler(context);
        try {
            this.jettyServer.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected Client getClient() {
        return ClientBuilder.newClient();
    }

    protected abstract void initServletContext(ServletContextHandler context);

    protected void initTracing(ServletContextHandler context) {
        client.register(new Builder(mockTracer).build());

        ServerTracingDynamicFeature serverTracingFeature =
            new ServerTracingDynamicFeature.Builder(mockTracer)
                .withOperationNameProvider(HTTPMethodOperationName.newBuilder())
                .withDecorators(Collections.singletonList(ServerSpanDecorator.STANDARD_TAGS))
                .withSkipPattern("/health")
            .build();
        // TODO clarify dispatcher types
        context.addFilter(new FilterHolder(new SpanFinishingFilter(mockTracer)), "/*",
            EnumSet.of(
                DispatcherType.REQUEST,
                DispatcherType.FORWARD,
                // TODO CXF does not call AsyncListener#onComplete() without this (it calls only onStartAsync)
                DispatcherType.ASYNC,
                DispatcherType.ERROR,
                DispatcherType.INCLUDE));

        context.setAttribute(CLIENT_ATTRIBUTE, client);
        context.setAttribute(TRACER_ATTRIBUTE, mockTracer);
        context.setAttribute(SERVER_TRACING_FEATURE, serverTracingFeature);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        if (jettyServer != null) {
            jettyServer.stop();
        }
    }

    @After
    public void after() {
        assertOnErrors(mockTracer.finishedSpans());
        mockTracer.reset();
    }

    @AfterClass
    public static void resetGlobalTracer() {
        try {
            Field globalTracerField = GlobalTracer.class.getDeclaredField("tracer");
            globalTracerField.setAccessible(true);
            globalTracerField.set(null, NoopTracerFactory.create());
            globalTracerField.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException("Error resetting " + GlobalTracer.class, e);
        }
    }

    public String url(String path) {
        return String.format("http://localhost:%d%s%s", getPort(), contextPath == "/" ? "" : contextPath, path);
    }

    public int getPort() {
        return ((ServerConnector)jettyServer.getConnectors()[0]).getLocalPort();
    }

    public static void assertOnErrors(List<MockSpan> spans) {
        for (MockSpan mockSpan: spans) {
            Assert.assertEquals(mockSpan.generatedErrors().toString(), 0, mockSpan.generatedErrors().size());
        }
    }

    protected Callable<Boolean> finishedSpansSizeEquals(final int size) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return mockTracer.finishedSpans().size() == size;
            }
        };
    }

    public MockSpan getSpanWithTag(List<MockSpan> spans, ImmutableTag... expected) {
        Set<ImmutableTag> expectedSet = new HashSet<>();
        if (expected != null) {
            expectedSet.addAll(Arrays.asList(expected));
        }

        MockSpan found = null;
        for (MockSpan mockSpan: spans) {
            Set<ImmutableTag> actual = new HashSet<>();
            for (Map.Entry<String, Object> tagEntry: mockSpan.tags().entrySet()) {
                actual.add(new ImmutableTag(tagEntry.getKey(), tagEntry.getValue()));
            }

            if (actual.containsAll(expectedSet)) {
                if (found != null) {
                    throw new IllegalStateException(
                        String.format("There are >=  twos pans containing the same tags %s", expectedSet.toString()));
                }
                found = mockSpan;
            }
        }
        return found;
    }

    public static class ImmutableTag {
        private String key;
        private Object value;

        public ImmutableTag(AbstractTag<?> tag, Object value) {
            this(tag.getKey(), value);
        }

        public ImmutableTag(String key, Object value) {
            this.key = key;
            this.value =value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ImmutableTag that = (ImmutableTag) o;
            return Objects.equals(key, that.key) &&
                Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }
    }
}
