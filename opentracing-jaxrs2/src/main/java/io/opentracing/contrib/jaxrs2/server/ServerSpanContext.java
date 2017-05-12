package io.opentracing.contrib.jaxrs2.server;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import io.opentracing.SpanContext;
import io.opentracing.contrib.jaxrs2.internal.CastUtils;
import io.opentracing.contrib.jaxrs2.internal.SpanWrapper;


/**
 * This class can be injected into resource method with '@BeanParam {@link ServerSpanContext} serverSpanContext'.
 *
 * @author Pavol Loffay
 */
public class ServerSpanContext {

    @Context
    private HttpServletRequest request;

    /**
     * Get {@link SpanContext} of span tracing server request
     *
     * @return span context of span tracing server request
     */
    public SpanContext get() {
        SpanWrapper spanWrapper = CastUtils.cast(request.getAttribute(ServerTracingFilter.SPAN_PROP_ID), SpanWrapper.class);
        return spanWrapper != null ? spanWrapper.get().context() : null;
    }
}
