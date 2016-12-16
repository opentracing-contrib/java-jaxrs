package io.opentracing.contrib.jaxrs.server;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import io.opentracing.Span;
import io.opentracing.contrib.jaxrs.SpanWrapper;


/**
 * @author Pavol Loffay
 */
public class CurrentSpan {

    @Context
    private HttpServletRequest request;

    public Span injectedSpan() {
        SpanWrapper spanWrapper = (SpanWrapper) request.getAttribute(SpanServerRequestFilter.SPAN_PROP_ID);
        return spanWrapper != null ? spanWrapper.span() : null;
    }
}
