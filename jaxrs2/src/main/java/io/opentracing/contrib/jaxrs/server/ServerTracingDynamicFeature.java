package io.opentracing.contrib.jaxrs.server;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import io.opentracing.Tracer;

/**
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

            context.register(new SpanServerRequestFilter(builder.tracer,
                    new OperationNameAdapter(operationName(resourceInfo), builder.operationNameProvider),
                    builder.spanDecorators));
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

    public static class Builder {
        private final Tracer tracer;
        private boolean allTraced;
        private OperationNameProvider operationNameProvider;
        private List<SpanDecorator> spanDecorators = new ArrayList<>();

        private Builder(Tracer tracer) {
            this.tracer = tracer;
            this.operationNameProvider = OperationNameProvider.HTTP_METHOD_NAME_PROVIDER;
            this.withStandardTags();
        }

        public static Builder traceAll(Tracer tracer) {
            Builder builder = new Builder(tracer);
            builder.allTraced = true;
            return builder;
        }

        public static Builder traceNothing(Tracer tracer) {
            return new Builder(tracer);
        }

        public Builder withOperationNameProvider(OperationNameProvider operationNameProvider) {
            this.operationNameProvider = operationNameProvider;
            return this;
        }

        public Builder withStandardTags() {
            this.spanDecorators.add(SpanDecorator.STANDARD_TAGS);
            return this;
        }

        public Builder withEmptyDecorators() {
            this.spanDecorators.clear();
            return this;
        }

        public Builder withDecorator(SpanDecorator spanDecorator) {
            this.spanDecorators.add(spanDecorator);
            return this;
        }

        public Tracer tracer() {
            return tracer;
        }

        public ServerTracingDynamicFeature build() {
            return new ServerTracingDynamicFeature(this);
        }
    }
}
