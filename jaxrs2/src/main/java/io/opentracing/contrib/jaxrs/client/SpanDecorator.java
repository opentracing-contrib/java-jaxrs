package io.opentracing.contrib.jaxrs.client;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;

import io.opentracing.Span;
import io.opentracing.contrib.jaxrs.URLUtils;
import io.opentracing.tag.Tags;

/**
 * @author Pavol Loffay
 */
public interface SpanDecorator {

    void decorateRequest(ClientRequestContext containerRequestContext, Span span);

    void decorateResponse(ClientResponseContext containerResponseContext, Span span);

    SpanDecorator STANDARD_TAGS = new SpanDecorator() {
        @Override
        public void decorateRequest(ClientRequestContext requestContext, Span span) {
            span.setTag("http.method", requestContext.getMethod())
                .setTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);

            String url = URLUtils.url(requestContext.getUri());
            if (url != null) {
                span.setTag(Tags.HTTP_URL.getKey(), url);
            }
        }

        @Override
        public void decorateResponse(ClientResponseContext responseContext, Span span) {
            span.setTag(Tags.HTTP_STATUS.getKey(), responseContext.getStatus());
        }
    };
}
