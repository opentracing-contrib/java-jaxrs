package io.opentracing.contrib.jaxrs.server;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import io.opentracing.Tracer;

/**
 * This class has to be registered as JAX-RS provider to enable tracing of server requests.
 *
 * @author Pavol Loffay
 */
@Provider
public class ServerTracingDynamicFeature implements DynamicFeature {

    private static final Logger log = Logger.getLogger(ServerTracingDynamicFeature.class.getName());

    private Builder builder;

    private ServerTracingDynamicFeature(Builder builder) {
        this.builder = builder;
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        if (builder.allTraced || shouldBeTraced(resourceInfo)) {
            // TODO why it is called twice for the same endpoint
            if (log.isLoggable(Level.INFO)) {
                String msg = String.format("%s registering tracing: %s#%s", this,
                        resourceInfo.getResourceClass().getCanonicalName(),
                        resourceInfo.getResourceMethod().getName());
                log.info(msg);
            }

            context.register(new SpanServerRequestFilter(builder.tracer, operationName(resourceInfo), builder.spanDecorators));
            context.register(new SpanServerResponseFilter(builder.spanDecorators));
        }
    }

    protected Traced closestTracedAnnotation(ResourceInfo resourceInfo) {
        Traced tracedAnnotation = resourceInfo.getResourceClass().getAnnotation(Traced.class);
        if (tracedAnnotation == null) {
            tracedAnnotation = resourceInfo.getResourceMethod().getAnnotation(Traced.class);
        }

        return tracedAnnotation;
    }

    protected boolean shouldBeTraced(ResourceInfo resourceInfo) {
        return closestTracedAnnotation(resourceInfo) != null;
    }

    protected String operationName(ResourceInfo resourceInfo) {
        Traced traced = closestTracedAnnotation(resourceInfo);
        return traced != null ? traced.operationName() : null;
    }

    /**
     * Builder for creating JAX-RS dynamic feature for tracing server requests.
     *
     * By default span's operation name is HTTP method and span is decorated with
     * {@link ServerSpanDecorator#STANDARD_TAGS} which adds standard tags.
     * If you want to set different span name provide custom span decorator {@link ServerSpanDecorator}.
     */
    public static class Builder {
        private final Tracer tracer;
        private boolean allTraced;
        private List<ServerSpanDecorator> spanDecorators;

        private Builder(Tracer tracer) {
            this.tracer = tracer;
            this.spanDecorators = Arrays.asList(ServerSpanDecorator.STANDARD_TAGS);
        }

        /**
         * This enables tracing of all requests.
         * @param tracer tracer implementation
         * @return builder
         */
        public static Builder traceAll(Tracer tracer) {
            Builder builder = new Builder(tracer);
            builder.allTraced = true;
            return builder;
        }

        /**
         * When constructed with this only resources annotation with {@link Traced} will be traced.
         * @param tracer tracer implementation
         * @return builder
         */
        public static Builder traceNothing(Tracer tracer) {
            return new Builder(tracer);
        }

        /**
         * Set span decorators.
         * @param spanDecorators span decorator
         * @return builder
         */
        public Builder withDecorators(List<ServerSpanDecorator> spanDecorators) {
            this.spanDecorators = spanDecorators;
            return this;
        }

        public Tracer tracer() {
            return tracer;
        }

        /**
         * @return server tracing dynamic feature. This feature should be registered in {@link javax.ws.rs.core.Application}
         */
        public ServerTracingDynamicFeature build() {
            return new ServerTracingDynamicFeature(this);
        }
    }
}
