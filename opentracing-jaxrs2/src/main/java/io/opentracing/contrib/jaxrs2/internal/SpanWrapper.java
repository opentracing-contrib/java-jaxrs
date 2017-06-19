package io.opentracing.contrib.jaxrs2.internal;

import io.opentracing.Span;
import java.util.concurrent.atomic.AtomicBoolean;

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

    public Span get() {
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
