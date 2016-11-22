package sk.loffay.opentracing.jax.rs;

import io.opentracing.Span;


/**
 * @author Pavol Loffay
 */
public class CurrentSpan {

    private Span span;

    public CurrentSpan() {
        span = SpanExtractRequestFilter.threadLocalSpan.get();
    }

    public Span injectedSpan() {
        return span;
    }
}
