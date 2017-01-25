package io.opentracing.contrib.jaxrs2.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import io.opentracing.contrib.jaxrs2.internal.CastUtils;
import io.opentracing.contrib.jaxrs2.internal.SpanWrapper;

public class SpanServerResponseFilter implements ContainerResponseFilter {

    private static final Logger log = Logger.getLogger(SpanServerResponseFilter.class.getName());

    private List<ServerSpanDecorator> spanDecorators;

    public SpanServerResponseFilter(List<ServerSpanDecorator> spanDecorators) {
        this.spanDecorators = new ArrayList<>(spanDecorators);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

        SpanWrapper spanWrapper = CastUtils
                .cast(requestContext.getProperty(SpanServerRequestFilter.SPAN_PROP_ID), SpanWrapper.class);

        if (spanWrapper != null && !spanWrapper.isFinished()) {
            log.finest("Finishing server span");

            if (spanDecorators != null) {
                for (ServerSpanDecorator decorator: spanDecorators) {
                    decorator.decorateResponse(responseContext, spanWrapper.span());
                }
            }

            spanWrapper.finish();
        }
    }
}
