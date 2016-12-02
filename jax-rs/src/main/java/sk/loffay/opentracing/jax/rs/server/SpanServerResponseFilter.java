package sk.loffay.opentracing.jax.rs.server;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentracing.Span;

public class SpanServerResponseFilter implements ContainerResponseFilter {

    private static final Logger log = LoggerFactory.getLogger(SpanServerResponseFilter.class);

    private Optional<List<SpanDecorator>> spanDecorators;

    public SpanServerResponseFilter(List<SpanDecorator> spanDecorators) {
        this.spanDecorators = Optional.ofNullable(spanDecorators);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

        Span span = (Span)requestContext.getProperty(SpanServerRequestFilter.SPAN_PROP_ID);

        if (span != null) {
            log.trace("Finishing server span: {}", span);

            spanDecorators.ifPresent(decorators ->
                    decorators.forEach(decorator -> decorator.decorateResponse(responseContext, span)));
            span.finish();
        }
    }
}
