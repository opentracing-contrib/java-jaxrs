package io.opentracing.contrib.jaxrs2.internal;

import io.opentracing.Scope;
import io.opentracing.Span;
import java.sql.Wrapper;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wrapper class used for exchanging span between filters.
 *
 * @author Pavol Loffay
 */
public class SpanWrapper {

    public static final String PROPERTY_NAME = SpanWrapper.class.getName() + ".activeSpanWrapper";

    private Scope scope;
    private AtomicBoolean finished = new AtomicBoolean();

    public SpanWrapper(Scope scope) {
        this.scope = scope;
    }

    public Span get() {
        return scope.span();
    }

    public Scope getScope() {
        return scope;
    }

    public synchronized void finish() {
        if (!finished.get()) {
            finished.set(true);
            scope.span().finish();
        }
    }

    public boolean isFinished() {
        return finished.get();
    }
}
