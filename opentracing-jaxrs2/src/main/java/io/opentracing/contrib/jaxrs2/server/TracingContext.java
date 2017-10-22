package io.opentracing.contrib.jaxrs2.server;

import io.opentracing.SpanContext;
import io.opentracing.contrib.jaxrs2.internal.CastUtils;
import io.opentracing.contrib.jaxrs2.internal.SpanWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import static io.opentracing.contrib.jaxrs2.internal.SpanWrapper.PROPERTY_NAME;

/**
 * @author Pavol Loffay
 */
public class TracingContext {

    @Context
    private HttpServletRequest request;

    public SpanContext spanContext() {
        SpanWrapper spanWrapper = CastUtils.cast(request.getAttribute(PROPERTY_NAME), SpanWrapper.class);
        return spanWrapper != null ? spanWrapper.get().context() : null;
  }
}
