package io.opentracing.contrib.jaxrs.server;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public class IteratorTest {

    @Test
    public void testEmpty() {
        Map<String, List<String>> map = new HashMap<>();

        ServerHeadersExtractTextMap.MultivaluedMapFlatIterator<String, String> iterator =
                new ServerHeadersExtractTextMap.MultivaluedMapFlatIterator<>(map.entrySet());

        Assert.assertFalse(iterator.hasNext());
    }

    /**
     * Corner case.
     * This should not really happen. In this case value should be null or an empty string.
     */
    @Test(expected = NoSuchElementException.class)
    public void testEmptyValue() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("key", Collections.<String>emptyList());

        ServerHeadersExtractTextMap.MultivaluedMapFlatIterator<String, String> iterator =
                new ServerHeadersExtractTextMap.MultivaluedMapFlatIterator<>(map.entrySet());

        Assert.assertTrue(iterator.hasNext());
        iterator.next();
    }

    @Test
    public void testOneKeyOneValue() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("key", new ArrayList<>(Arrays.asList("value")));

        ServerHeadersExtractTextMap.MultivaluedMapFlatIterator<String, String> iterator =
                new ServerHeadersExtractTextMap.MultivaluedMapFlatIterator<>(map.entrySet());

        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(new AbstractMap.SimpleImmutableEntry<>("key", "value"), iterator.next());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void testOneKeyMultipleValues() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("key", new ArrayList<>(Arrays.asList("value1", "value2")));

        ServerHeadersExtractTextMap.MultivaluedMapFlatIterator<String, String> iterator =
                new ServerHeadersExtractTextMap.MultivaluedMapFlatIterator<>(map.entrySet());

        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(new AbstractMap.SimpleImmutableEntry<>("key", "value1"), iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(new AbstractMap.SimpleImmutableEntry<>("key", "value2"), iterator.next());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void testMultipleKeysMultipleValues() {
        Map<String, List<String>> map = new TreeMap<>();
        map.put("key", new ArrayList<>(Arrays.asList("value1", "value2")));
        map.put("key2", new ArrayList<>(Arrays.asList("value1", "value2")));

        ServerHeadersExtractTextMap.MultivaluedMapFlatIterator<String, String> iterator =
                new ServerHeadersExtractTextMap.MultivaluedMapFlatIterator<>(map.entrySet());

        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(new AbstractMap.SimpleImmutableEntry<>("key", "value1"), iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(new AbstractMap.SimpleImmutableEntry<>("key", "value2"), iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(new AbstractMap.SimpleImmutableEntry<>("key2", "value1"), iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(new AbstractMap.SimpleImmutableEntry<>("key2", "value2"), iterator.next());
        Assert.assertFalse(iterator.hasNext());
    }
}
