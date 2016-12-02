package sk.loffay.opentracing.jax.rs.client;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;

import io.opentracing.Tracer;

/**
 * @author Pavol Loffay
 */
public class ClientTracingFeature {

    private ClientTracingFeature(Builder builder) {
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
