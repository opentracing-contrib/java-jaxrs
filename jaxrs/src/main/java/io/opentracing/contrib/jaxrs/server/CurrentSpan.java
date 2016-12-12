package io.opentracing.contrib.jaxrs.server;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import io.opentracing.Span;


/**
 * @author Pavol Loffay
 */
public class CurrentSpan {

    @Context
    private HttpServletRequest request;

    public Span injectedSpan() {
        return (Span) request.getAttribute(SpanServerRequestFilter.SPAN_PROP_ID);
    }
}
