package io.opentracing.contrib.jaxrs2.client;

import io.opentracing.contrib.jaxrs2.serialization.InterceptorSpanDecorator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Priorities;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

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
     *
     * For a custom configuration use {@link Builder#build()}.
     */
    public ClientTracingFeature() {
        this(new Builder(GlobalTracer.get()));
    }

    private ClientTracingFeature(Builder builder) {
        this.builder = builder;
    }

    @Override
    public boolean configure(FeatureContext context) {
        log.info("Registering client OpenTracing, with configuration:" + builder.toString());
        context.register(new ClientTracingFilter(builder.tracer, builder.spanDecorators),
            builder.priority);
        context.register(new ClientTracingInterceptor(builder.tracer, builder.serializationSpanDecorators),
            builder.serializationPriority);
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
        private List<InterceptorSpanDecorator> serializationSpanDecorators;
        private int priority;
        private int serializationPriority;

        public Builder(Tracer tracer) {
            this.tracer = tracer;
            this.spanDecorators = Collections.singletonList(ClientSpanDecorator.STANDARD_TAGS);
            this.serializationSpanDecorators = Arrays.asList(InterceptorSpanDecorator.STANDARD_TAGS);
            // by default do not use Priorities.AUTHENTICATION due to security concerns
            this.priority = Priorities.HEADER_DECORATOR;
            this.serializationPriority = Priorities.ENTITY_CODER;
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
         * Set serialization span decorators.
         * @return builder
         */
        public Builder withSerializationDecorators(List<InterceptorSpanDecorator> spanDecorators) {
            this.serializationSpanDecorators = spanDecorators;
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
         * @param priority the overriding priority for the registered component.
         *                 Default is {@link Priorities#ENTITY_CODER}
         * @return builder
         *
         * @see Priorities
         */
        public Builder withSerializationPriority(int priority) {
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
