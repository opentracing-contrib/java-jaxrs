package sk.loffay.opentracing.jax.rs.client;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import sk.loffay.opentracing.jax.rs.server.SpanServerRequestFilter;

/**
 * @author Pavol Loffay
 */
public class SpanClientRequestFilter implements ClientRequestFilter {

    public static ThreadLocal<Span> threadLocalSpan = new ThreadLocal<>();

    private Tracer tracer;

    public SpanClientRequestFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        Tracer.SpanBuilder spanBuilder = tracer.buildSpan(requestContext.getUri().getPath())
                .withTag(Tags.HTTP_URL.getKey(), requestContext.getUri().toURL().toString())
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);

        Span parentSpan = SpanServerRequestFilter.threadLocalSpan.get();
        if (parentSpan != null) {
            spanBuilder.asChildOf(parentSpan);
        }

        threadLocalSpan.set(spanBuilder.start());
    }
}
