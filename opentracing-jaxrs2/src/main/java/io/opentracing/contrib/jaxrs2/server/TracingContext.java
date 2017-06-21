package io.opentracing.contrib.jaxrs2.server;

import io.opentracing.SpanContext;
import io.opentracing.contrib.jaxrs2.internal.CastUtils;
import io.opentracing.contrib.jaxrs2.internal.SpanWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

/**
 * @author Pavol Loffay
 */
public class TracingContext {

    @Context
    private HttpServletRequest request;

    public SpanContext spanContext() {
        SpanWrapper spanWrapper = CastUtils.cast(request.getAttribute(ServerTracingFilter.SPAN_PROP_ID), SpanWrapper.class);
        return spanWrapper != null ? spanWrapper.get().context() : null;
  }
}
