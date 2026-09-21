package com.shaili.myredis;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LRUCacheTest {

    @Test
    void evictsLeastRecentlyUsedWhenCapacityExceeded() {
        LRUCache cache = new LRUCache(2); // sirf 2 keys ki jagah hai

        cache.put("a", "1");
        cache.put("b", "2");
        cache.put("c", "3"); // yeh "a" ko evict kar dega, kyunki woh least recently used tha

        assertNull(cache.get("a")); // evicted ho chuka
        assertEquals("2", cache.get("b"));
        assertEquals("3", cache.get("c"));
    }

    @Test
    void accessingKeyMakesItMostRecentlyUsed() {
        LRUCache cache = new LRUCache(2);

        cache.put("a", "1");
        cache.put("b", "2");
        cache.get("a"); // "a" ko access kiya, ab woh most recent ban gaya
        cache.put("c", "3"); // ab "b" evict hona chahiye, "a" nahi (kyunki humne abhi use kiya tha)

        assertEquals("1", cache.get("a"));
        assertNull(cache.get("b")); // "b" evicted hua
        assertEquals("3", cache.get("c"));
    }
}