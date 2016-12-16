package io.opentracing.contrib.jaxrs.server;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;

import io.opentracing.Span;
import io.opentracing.contrib.jaxrs.URLUtils;
import io.opentracing.tag.Tags;

/**
 * @author Pavol Loffay
 */
public interface SpanDecorator {

    void decorateRequest(ContainerRequestContext containerRequestContext, Span span);

    void decorateResponse(ContainerResponseContext containerRequestContext, Span span);


    SpanDecorator STANDARD_TAGS = new SpanDecorator() {
        @Override
        public void decorateRequest(ContainerRequestContext requestContext, Span span) {
            span.setTag("http.method", requestContext.getMethod())
                .setTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);

            String url = URLUtils.url(requestContext.getUriInfo().getAbsolutePath());
            if (url != null) {
                span.setTag(Tags.HTTP_URL.getKey(), url);
            }
        }

        @Override
        public void decorateResponse(ContainerResponseContext responseContext, Span span) {
            span.setTag(Tags.HTTP_STATUS.getKey(), responseContext.getStatus());
        }
    };
}
