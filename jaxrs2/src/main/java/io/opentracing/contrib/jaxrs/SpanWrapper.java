package io.opentracing.contrib.jaxrs;

import io.opentracing.Span;

/**
 * @author Pavol Loffay
 */
public class SpanWrapper {

    private Span span;
    private boolean finished;

    public SpanWrapper(Span span) {
        this.span = span;
    }

    public Span span() {
        return span;
    }

    synchronized public void finish() {
        if (!finished) {
            finished = true;
            span.finish();
        }
    }

    public boolean isFinished() {
        return finished;
    }
}
