package io.opentracing.contrib.jaxrs2.client;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

/**
 * @author Pavol Loffay
 */
@Provider
public class ClientTracingFeature implements Feature {
    private static final Logger log = Logger.getLogger(ClientTracingFeature.class.getName());

    private Builder builder;

    /**
     * When using this constructor application has to call {@link GlobalTracer#register} to register
     * tracer instance.
     */
    public ClientTracingFeature() {
        this(new Builder(GlobalTracer.get()));
    }

    public ClientTracingFeature(Builder builder) {
        this.builder = builder;
    }

    @Override
    public boolean configure(FeatureContext context) {
        log.info("Registering client OpenTracing, with configuration:" + builder.toString());
        context.register(new SpanClientRequestFilter(builder.tracer, builder.spanDecorators),
            builder.priority);
        context.register(new SpanClientResponseFilter(builder.spanDecorators),
            builder.priority);
        return true;
    }

    /**
     * Builder for configuring {@link Client} to trace outgoing requests.
     *
     * By default get's operation name is HTTP method and get is decorated with
     * {@link ClientSpanDecorator#STANDARD_TAGS} which adds set of standard tags.
     */
    public static class Builder {
        private Tracer tracer;
        private List<ClientSpanDecorator> spanDecorators;
        private int priority;

        public Builder(Tracer tracer) {
            this.tracer = tracer;
            this.spanDecorators = Collections.singletonList(ClientSpanDecorator.STANDARD_TAGS);
            // by default do not use Priorities.AUTHENTICATION due to security concerns
            this.priority = Priorities.HEADER_DECORATOR;
        }

        /**
         * Set span decorators.
         * @return builder
         */
        public Builder withDecorators(List<ClientSpanDecorator> spanDecorators) {
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
         * @return client tracing feature. This feature should be manually registered to {@link Client}
         */
        public ClientTracingFeature build() {
            return new ClientTracingFeature(this);
        }
    }
}
