package io.opentracing.contrib.jaxrs2.internal;

import io.opentracing.Scope;
import io.opentracing.Span;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wrapper class used for exchanging span between filters.
 *
 * @author Pavol Loffay
 */
public class SpanWrapper {

    public static final String PROPERTY_NAME = SpanWrapper.class.getName() + ".activeSpanWrapper";

    private Scope scope;
    private Span span;
    private AtomicBoolean finished = new AtomicBoolean();

    public SpanWrapper(Span span, Scope scope) {
        this.span = span;
        this.scope = scope;

    }

    public Span get() {
        return span;
    }

    public Scope getScope() {
        return scope;
    }

    public void finish() {
        if (finished.compareAndSet(false, true)) {
            span.finish();
        }
    }

    public boolean isFinished() {
        return finished.get();
    }
}
