package io.opentracing.contrib.jaxrs.server;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import io.opentracing.Span;
import io.opentracing.contrib.jaxrs.internal.SpanWrapper;
import io.opentracing.contrib.jaxrs.internal.CastUtils;


/**
 * This class can be injected into resource method with '@BeanParam CurrentSpan currentSpan'.
 *
 * @author Pavol Loffay
 */
public class CurrentSpan {

    @Context
    private HttpServletRequest request;

    public Span get() {
        SpanWrapper spanWrapper = CastUtils.cast(request.getAttribute(SpanServerRequestFilter.SPAN_PROP_ID), SpanWrapper.class);
        return spanWrapper != null ? spanWrapper.span() : null;
    }
}
