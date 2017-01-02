package io.opentracing.contrib.jaxrs.client;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;

import io.opentracing.Span;
import io.opentracing.contrib.jaxrs.SpanDecorator;
import io.opentracing.contrib.jaxrs.internal.URIUtils;
import io.opentracing.tag.Tags;

/**
 * @author Pavol Loffay
 */
public interface ClientSpanDecorator extends SpanDecorator<ClientRequestContext, ClientResponseContext> {

    /**
     * Adds standard tags: {@link io.opentracing.tag.Tags#SPAN_KIND}, {@link io.opentracing.tag.Tags#HTTP_METHOD} and
     * {@link io.opentracing.tag.Tags#HTTP_URL}
     */
    ClientSpanDecorator STANDARD_TAGS = new ClientSpanDecorator() {
        @Override
        public void decorateRequest(ClientRequestContext requestContext, Span span) {
            Tags.SPAN_KIND.set(span, Tags.SPAN_KIND_CLIENT);
            Tags.HTTP_METHOD.set(span, requestContext.getMethod());

            String url = URIUtils.url(requestContext.getUri());
            if (url != null) {
                Tags.HTTP_URL.set(span, url);
            }
        }

        @Override
        public void decorateResponse(ClientResponseContext responseContext, Span span) {
            Tags.HTTP_STATUS.set(span, responseContext.getStatus());
        }
    };

    /**
     * As operation name provides HTTP method e.g. GET, POST..
     */
    ClientSpanDecorator HTTP_METHOD_OPERATION_NAME = new ClientSpanDecorator() {
        @Override
        public void decorateRequest(ClientRequestContext clientRequestContext, Span span) {
            span.setOperationName(clientRequestContext.getMethod());
        }

        @Override
        public void decorateResponse(ClientResponseContext response, Span span) {
        }
    };

    /**
     * As operation name provides HTTP path.
     */
    ClientSpanDecorator HTTP_PATH_OPERATION_NAME = new ClientSpanDecorator() {
        @Override
        public void decorateRequest(ClientRequestContext clientRequestContext, Span span) {
            span.setOperationName(URIUtils.path(clientRequestContext.getUri()));
        }

        @Override
        public void decorateResponse(ClientResponseContext response, Span span) {
        }
    };
}
