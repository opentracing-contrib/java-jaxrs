package io.opentracing.contrib.jaxrs;

import io.opentracing.Span;

/**
 * This interface provides ability to change span (e.g. add tags, logs, change operation name).
 *
 * @author Pavol Loffay
 */
public interface SpanDecorator<IN, OUT>{

    /**
     * Decorate span by incoming object.
     *
     * @param request
     * @param span
     */
    void decorateRequest(IN request, Span span);

    /**
     * Decorate spans by outgoing object.
     *
     * @param response
     * @param span
     */
    void decorateResponse(OUT response, Span span);
}
