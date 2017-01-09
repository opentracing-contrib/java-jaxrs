package io.opentracing.contrib.jaxrs.client;

import java.util.Arrays;
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

        builder.client.register(new SpanClientRequestFilter(builder.tracer, builder.spanDecorators), 0)
                .register(new SpanClientResponseFilter(builder.spanDecorators), 0);
    }

    /**
     * Builder for configuring {@link Client} to trace outgoing requests.
     *
     * By default span's operation name is HTTP method and span is decorated with
     * {@link ClientSpanDecorator#STANDARD_TAGS} which adds set of standard tags.
     */
    public static class Builder {
        private Tracer tracer;
        private Client client;
        private List<ClientSpanDecorator> spanDecorators;

        private Builder(Tracer tracer, Client client) {
            this.tracer = tracer;
            this.client = client;
            this.spanDecorators = Arrays.asList(ClientSpanDecorator.STANDARD_TAGS);
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
         * Set span decorators.
         * @return builder
         */
        public Builder withDecorators(List<ClientSpanDecorator> spanDecorators) {
            this.spanDecorators= spanDecorators;
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
