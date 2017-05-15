package io.opentracing.contrib.jaxrs2.server;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Priorities;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

/**
 * This class has to be registered as JAX-RS provider to enable tracing of server requests.
 *
 * @author Pavol Loffay
 */
@Provider
public class ServerTracingDynamicFeature implements DynamicFeature {
    private static final Logger log = Logger.getLogger(ServerTracingDynamicFeature.class.getName());

    private Builder builder;

    /**
     * When using this constructor application has to call {@link GlobalTracer#register} to register
     * tracer instance. Ideally it should be called in {@link javax.servlet.ServletContextListener}.
     *
     * For a custom configuration use {@link Builder#build()}.
     */
    public ServerTracingDynamicFeature() {
        this(new Builder(GlobalTracer.get()));
    }

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

            context.register(new ServerTracingFilter(builder.tracer, operationName(resourceInfo), builder
                    .spanDecorators), builder.priority);
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
        private int priority;

        public Builder(Tracer tracer) {
            this.tracer = tracer;
            this.spanDecorators = Arrays.asList(
                ServerSpanDecorator.HTTP_WILDCARD_PATH_OPERATION_NAME,
                ServerSpanDecorator.STANDARD_TAGS);
            // by default do not use Priorities.AUTHENTICATION due to security concerns
            this.priority = Priorities.HEADER_DECORATOR;
            this.allTraced = true;
        }

        /**
         * Only resources annotated with {@link Traced} will be traced.
         * @return builder
         */
        public Builder withTraceNothing() {
            allTraced = false;
            return this;
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

        /**
         * @param priority the overriding priority for the registered component.
         *                 Default is {@link Priorities#HEADER_DECORATOR}
         * @return builder
         *
         * @see Priorities
         */
        public Builder withPriority(int priority) {
            this.priority = priority;
            return this;
        }

        /**
         * @return server tracing dynamic feature. This feature should be manually registered to {@link javax.ws.rs.core.Application}
         */
        public ServerTracingDynamicFeature build() {
            return new ServerTracingDynamicFeature(this);
        }
    }
}
