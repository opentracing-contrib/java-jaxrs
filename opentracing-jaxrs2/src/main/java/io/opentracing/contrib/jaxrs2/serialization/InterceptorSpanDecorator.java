package io.opentracing.contrib.jaxrs2.serialization;

import io.opentracing.Span;
import io.opentracing.tag.Tags;

import javax.ws.rs.ext.InterceptorContext;
import javax.ws.rs.ext.ReaderInterceptorContext;
import javax.ws.rs.ext.WriterInterceptorContext;

public interface InterceptorSpanDecorator {

    /**
     * Decorate spans by incoming object.
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
     * Decorate spans by error throw when processing incoming object.
     *
     * @param e
     * @param context
     * @param span
     */
    void decorateReadException(Exception e, ReaderInterceptorContext context, Span span);

    /**
     * Decorate spans by error thrown when processing outgoing object.
     *
     * @param e
     * @param context
     * @param span
     */
    void decorateWriteException(Exception e, WriterInterceptorContext context, Span span);

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

        @Override
        public void decorateReadException(Exception e, ReaderInterceptorContext context, Span span) {
            Tags.ERROR.set(span, true);
        }

        @Override
        public void decorateWriteException(Exception e, WriterInterceptorContext context, Span span) {
            Tags.ERROR.set(span, true);
        }
    };
}
