package io.opentracing.contrib.jaxrs.server;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import io.opentracing.contrib.jaxrs.SpanWrapper;

public class SpanServerResponseFilter implements ContainerResponseFilter {

    private static final Logger log = Logger.getLogger(SpanServerResponseFilter.class.getName());

    private List<SpanDecorator> spanDecorators;

    public SpanServerResponseFilter(List<SpanDecorator> spanDecorators) {
        this.spanDecorators = spanDecorators;
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

        SpanWrapper spanWrapper = (SpanWrapper)requestContext.getProperty(SpanServerRequestFilter.SPAN_PROP_ID);

        if (spanWrapper != null && !spanWrapper.isFinished()) {
            log.finest("Finishing server span");

            if (spanDecorators != null) {
                for (SpanDecorator decorator: spanDecorators) {
                    decorator.decorateResponse(responseContext, spanWrapper.span());
                }
            }

            spanWrapper.finish();
        }
    }
}
