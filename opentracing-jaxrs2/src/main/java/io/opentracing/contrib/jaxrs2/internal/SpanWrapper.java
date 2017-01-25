package io.opentracing.contrib.jaxrs2.internal;

import java.util.concurrent.atomic.AtomicBoolean;

import io.opentracing.Span;

/**
 * Wrapper class used for exchanging span between filters.
 *
 * @author Pavol Loffay
 */
public class SpanWrapper {

    private Span span;
    private AtomicBoolean finished = new AtomicBoolean();

    public SpanWrapper(Span span) {
        this.span = span;
    }

    public Span span() {
        return span;
    }

    public synchronized void finish() {
        if (!finished.get()) {
            finished.set(true);
            span.finish();
        }
    }

    public boolean isFinished() {
        return finished.get();
    }
}
