package io.opentracing.contrib.jaxrs.client;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;

import io.opentracing.Tracer;

/**
 * @author Pavol Loffay
 */
public class ClientTracingFeature {

    private static final Logger log = Logger.getLogger(ClientTracingFeature.class.getName());

    private ClientTracingFeature(Builder builder) {
        if (log.isLoggable(Level.INFO)) {
            log.info("Registering client tracing for: " + builder.client);
        }

        builder.client.register(new SpanClientRequestFilter(builder.tracer, builder.operationNameProvider,
                    builder.spanDecorators), 0)
                .register(new SpanClientResponseFilter(builder.spanDecorators), 0);
    }

    /**
     * Builder for configuring {@link Client} to trace outgoing requests.
     *
     * By default span operation name is set by {@link ClientOperationNameProvider#HTTP_METHOD_NAME_PROVIDER} and
     * span is decorated with {@link ClientSpanDecorator#STANDARD_TAGS}.
     */
    public static class Builder {
        private Tracer tracer;
        private Client client;
        private ClientOperationNameProvider operationNameProvider;
        private List<ClientSpanDecorator> spanDecorators = new ArrayList<>();

        private Builder(Tracer tracer, Client client) {
            this.tracer = tracer;
            this.client = client;
            this.withStandardTags();
            this.operationNameProvider = ClientOperationNameProvider.HTTP_METHOD_NAME_PROVIDER;
        }

        /**
         * @param tracer tracer instance
         * @param client client instance
         * @return builder
         */
        public static Builder traceAll(Tracer tracer, Client client) {
            Builder builder = new Builder(tracer, client);
            return builder;
        }

        /**
         * Overrides default span operation name provider {@link ClientOperationNameProvider#HTTP_METHOD_NAME_PROVIDER}
         * @param operationNameProvider span operation name provider
         * @return builder
         */
        public Builder withOperationNameProvider(ClientOperationNameProvider operationNameProvider) {
            this.operationNameProvider = operationNameProvider;
            return this;
        }

        /**
         * Clears all previously passed decorators.
         * @return builder
         */
        public Builder withEmptyDecorators() {
            this.spanDecorators.clear();
            return this;
        }

        /**
         * Clears all previously passed decorators.
         * @return builder
         */
        public Builder withDecorator(ClientSpanDecorator spanDecorator) {
            this.spanDecorators.add(spanDecorator);
            return this;
        }

        /**
         * Adds {@link ClientSpanDecorator#STANDARD_TAGS} decorator  to decorators.
         * @return builder
         */
        public Builder withStandardTags() {
            this.spanDecorators.add(ClientSpanDecorator.STANDARD_TAGS);
            return this;
        }

        /**
         * @return passed tracer instance
         */
        public Tracer tracer() {
            return tracer;
        }

        /**
         * @return passed client instance
         */
        public Client client() {
            return client;
        }

        /**
         * Registers tracing filters.
         * @return client
         */
        public Client build() {
            new ClientTracingFeature(this);
            return client;
        }
    }
}
