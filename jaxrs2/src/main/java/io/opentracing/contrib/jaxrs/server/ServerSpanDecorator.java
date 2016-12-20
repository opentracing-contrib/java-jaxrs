package io.opentracing.contrib.jaxrs.server;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;

import io.opentracing.Span;
import io.opentracing.contrib.jaxrs.SpanDecorator;
import io.opentracing.contrib.jaxrs.internal.URIUtils;
import io.opentracing.tag.Tags;

/**
 * @author Pavol Loffay
 */
public interface ServerSpanDecorator extends SpanDecorator<ContainerRequestContext, ContainerResponseContext> {

    /**
     * Adds standard tags: {@link io.opentracing.tag.Tags#SPAN_KIND}, {@link io.opentracing.tag.Tags#HTTP_METHOD} and
     * {@link io.opentracing.tag.Tags#HTTP_URL}
     */
    ServerSpanDecorator STANDARD_TAGS = new ServerSpanDecorator() {
        @Override
        public void decorateRequest(ContainerRequestContext requestContext, Span span) {
            Tags.SPAN_KIND.set(span, Tags.SPAN_KIND_SERVER);
            Tags.HTTP_METHOD.set(span, requestContext.getMethod());

            String url = URIUtils.url(requestContext.getUriInfo().getAbsolutePath());
            if (url != null) {
                Tags.HTTP_URL.set(span, url);
            }
        }

        @Override
        public void decorateResponse(ContainerResponseContext responseContext, Span span) {
            Tags.HTTP_STATUS.set(span, responseContext.getStatus());
        }
    };
}
