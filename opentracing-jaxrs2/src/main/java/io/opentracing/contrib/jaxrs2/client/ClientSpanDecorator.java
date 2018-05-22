package io.opentracing.contrib.jaxrs2.client;

import io.opentracing.Span;
import io.opentracing.contrib.jaxrs2.internal.URIUtils;
import io.opentracing.tag.Tags;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;

/**
 * @author Pavol Loffay
 */
public interface ClientSpanDecorator {

    /**
     * Decorate get by incoming object.
     *
     * @param requestContext
     * @param span
     */
    void decorateRequest(ClientRequestContext requestContext, Span span);

    /**
     * Decorate spans by outgoing object.
     *
     * @param responseContext
     * @param span
     */
    void decorateResponse(ClientResponseContext responseContext, Span span);

    /**
     * Adds standard tags: {@link io.opentracing.tag.Tags#SPAN_KIND},
     * {@link io.opentracing.tag.Tags#PEER_HOSTNAME}, {@link io.opentracing.tag.Tags#PEER_PORT},
     * {@link io.opentracing.tag.Tags#HTTP_METHOD}, {@link io.opentracing.tag.Tags#HTTP_URL} and
     * {@link io.opentracing.tag.Tags#HTTP_STATUS}
     */
    ClientSpanDecorator STANDARD_TAGS = new ClientSpanDecorator() {
        @Override
        public void decorateRequest(ClientRequestContext requestContext, Span span) {
            Tags.COMPONENT.set(span, "jaxrs");
            Tags.PEER_HOSTNAME.set(span, requestContext.getUri().getHost());
            Tags.PEER_PORT.set(span, requestContext.getUri().getPort());

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
     * As operation name provides HTTP path. If there are path parameters used in URL then
     * spans for the same requests would have different operation names, therefore use carefully.
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
