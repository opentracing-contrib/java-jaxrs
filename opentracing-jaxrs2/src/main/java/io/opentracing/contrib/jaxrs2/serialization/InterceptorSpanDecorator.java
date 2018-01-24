package io.opentracing.contrib.jaxrs2.serialization;

import io.opentracing.Span;
import javax.ws.rs.ext.InterceptorContext;

public interface InterceptorSpanDecorator {

    /**
     * Decorate spans by outgoing object.
     *
     * @param context
     * @param span
     */
    void decorateRead(InterceptorContext context, Span span);

    /**
     * Decorate spans by outgoing object.
     *
     * @param context
     * @param span
     */
    void decorateWrite(InterceptorContext context, Span span);

    /**
     * Adds tags: \"media.type\", \"entity.type\"
     */
    InterceptorSpanDecorator STANDARD_TAGS = new InterceptorSpanDecorator() {
        @Override
        public void decorateRead(InterceptorContext context, Span span) {
            span.setTag("media.type", context.getMediaType().toString());
            span.setTag("entity.type", context.getType().getName());
        }

        @Override
        public void decorateWrite(InterceptorContext context, Span span) {
            decorateRead(context, span);
        }
    };
}
