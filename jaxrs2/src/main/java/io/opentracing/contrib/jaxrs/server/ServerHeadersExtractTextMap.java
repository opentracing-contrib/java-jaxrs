package io.opentracing.contrib.jaxrs.server;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import io.opentracing.propagation.TextMap;

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
        return new MultivaluedMapFlatIterator<>(headers.entrySet());
    }

    @Override
    public void put(String key, String value) {
        throw new UnsupportedOperationException(
                ServerHeadersExtractTextMap.class.getName() +" should only be used with Tracer.extract()");
    }

    public static final class MultivaluedMapFlatIterator<K, V> implements Iterator<Map.Entry<K, V>> {

        private final Iterator<Map.Entry<K, List<V>>> mapIterator;
        private Map.Entry<K, List<V>> mapEntry;
        private Iterator<V> listIterator;

        public MultivaluedMapFlatIterator(Set<Map.Entry<K, List<V>>> multiValuesEntrySet) {
            this.mapIterator = multiValuesEntrySet.iterator();
        }

        @Override
        public boolean hasNext() {
            if (listIterator != null && listIterator.hasNext()) {
                return true;
            }

            return mapIterator.hasNext();
        }

        @Override
        public Map.Entry<K, V> next() {
            if (mapEntry == null || (!listIterator.hasNext() && mapIterator.hasNext())) {
                mapEntry = mapIterator.next();
                listIterator = mapEntry.getValue().iterator();
            }

            return new AbstractMap.SimpleImmutableEntry<>(mapEntry.getKey(), listIterator.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
