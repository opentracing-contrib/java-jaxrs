package sk.loffay.opentracing.jax.rs.server;

import io.opentracing.Span;


/**
 * @author Pavol Loffay
 */
public class CurrentSpan {

    private Span span;

    public CurrentSpan() {
        span = SpanServerRequestFilter.threadLocalSpan.get();
    }

    public Span injectedSpan() {
        return span;
    }
}
