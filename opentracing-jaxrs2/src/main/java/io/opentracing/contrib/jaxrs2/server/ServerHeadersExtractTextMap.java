package io.opentracing.contrib.jaxrs2.server;

import io.opentracing.contrib.web.servlet.filter.HttpServletRequestExtractAdapter;
import io.opentracing.propagation.TextMap;
import java.util.Iterator;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Helper class used to iterate over HTTP headers.
 *
 * @author Pavol Loffay
 */
public class ServerHeadersExtractTextMap implements TextMap {

    private final MultivaluedMap<String, String> headers;

    public ServerHeadersExtractTextMap(MultivaluedMap<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return new HttpServletRequestExtractAdapter.MultivaluedMapFlatIterator<>(headers.entrySet());
    }

    @Override
    public void put(String key, String value) {
        throw new UnsupportedOperationException(
                ServerHeadersExtractTextMap.class.getName() +" should only be used with Tracer.extract()");
    }
}
