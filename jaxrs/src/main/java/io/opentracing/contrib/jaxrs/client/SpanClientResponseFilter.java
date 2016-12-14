package io.opentracing.contrib.jaxrs.client;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentracing.contrib.jaxrs.SpanWrapper;

/**
 * @author Pavol Loffay
 */
public class SpanClientResponseFilter implements ClientResponseFilter {

    private static final Logger log = LoggerFactory.getLogger(SpanClientResponseFilter.class);

    private Optional<List<SpanDecorator>> spanDecorators;

    public SpanClientResponseFilter(List<SpanDecorator> spanDecorators) {
        this.spanDecorators = Optional.ofNullable(spanDecorators);
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        SpanWrapper spanWrapper = (SpanWrapper)requestContext.getProperty(SpanClientRequestFilter.SPAN_PROP_ID);
        if (spanWrapper != null && !spanWrapper.isFinished()) {
            log.trace("Finishing client span: {}", spanWrapper);
            spanDecorators.ifPresent(decorators ->
                    decorators.forEach(decorator -> decorator.decorateResponse(responseContext, spanWrapper.span())));
            spanWrapper.finish();
        }
    }
}
