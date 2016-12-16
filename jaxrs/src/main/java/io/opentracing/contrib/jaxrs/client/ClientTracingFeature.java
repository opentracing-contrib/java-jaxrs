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

        builder.client.register(new SpanClientRequestFilter(builder.tracer, builder.spanDecorators), 0)
                .register(new SpanClientResponseFilter(builder.spanDecorators), 0);
    }

    public static class Builder {
        private Tracer tracer;
        private Client client;
        private List<SpanDecorator> spanDecorators = new ArrayList<>();

        public static Builder traceAll(Tracer tracer, Client client) {
            Builder builder = new Builder();
            builder.tracer = tracer;
            builder.client = client;
            builder.withStandardTags();
            return builder;
        }

        public Builder withEmptyDecorators() {
            this.spanDecorators.clear();
            return this;
        }

        public Builder withStandardTags() {
            this.spanDecorators.add(SpanDecorator.STANDARD_TAGS);
            return this;
        }

        public Client build() {
            new ClientTracingFeature(this);
            return client;
        }
    }
}
