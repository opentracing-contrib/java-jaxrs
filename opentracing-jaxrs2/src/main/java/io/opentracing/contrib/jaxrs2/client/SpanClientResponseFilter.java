package io.opentracing.contrib.jaxrs2.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import io.opentracing.contrib.jaxrs2.internal.CastUtils;
import io.opentracing.contrib.jaxrs2.internal.SpanWrapper;

/**
 * @author Pavol Loffay
 */
public class SpanClientResponseFilter implements ClientResponseFilter {

    private static final Logger log = Logger.getLogger(SpanClientResponseFilter.class.getName());

    private List<ClientSpanDecorator> spanDecorators;

    public SpanClientResponseFilter(List<ClientSpanDecorator> spanDecorators) {
        this.spanDecorators = new ArrayList<>(spanDecorators);
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        SpanWrapper spanWrapper = CastUtils
                .cast(requestContext.getProperty(SpanClientRequestFilter.SPAN_PROP_ID), SpanWrapper.class);
        if (spanWrapper != null && !spanWrapper.isFinished()) {
            log.finest("Finishing client span");

            if (spanDecorators != null) {
                for (ClientSpanDecorator decorator: spanDecorators) {
                    decorator.decorateResponse(responseContext, spanWrapper.span());
                }
            }

            spanWrapper.finish();
        }
    }
}
