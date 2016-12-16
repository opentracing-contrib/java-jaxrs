package io.opentracing.contrib.jaxrs.client;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import io.opentracing.contrib.jaxrs.SpanWrapper;

/**
 * @author Pavol Loffay
 */
public class SpanClientResponseFilter implements ClientResponseFilter {

    private static final Logger log = Logger.getLogger(SpanClientResponseFilter.class.getName());

    private List<SpanDecorator> spanDecorators;

    public SpanClientResponseFilter(List<SpanDecorator> spanDecorators) {
        this.spanDecorators = spanDecorators;
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        SpanWrapper spanWrapper = (SpanWrapper)requestContext.getProperty(SpanClientRequestFilter.SPAN_PROP_ID);
        if (spanWrapper != null && !spanWrapper.isFinished()) {
            log.finest("Finishing client span");

            if (spanDecorators != null) {
                for (SpanDecorator decorator: spanDecorators) {
                    decorator.decorateResponse(responseContext, spanWrapper.span());
                }
            }

            spanWrapper.finish();
        }
    }
}
