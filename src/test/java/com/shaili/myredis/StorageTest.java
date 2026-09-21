package com.shaili.myredis;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StorageTest {

    @Test
    void evictsOldestKeyWhenCapacityExceeded() {
        Storage storage = new Storage(2); // sirf 2 keys ki jagah

        storage.set("a", "1");
        storage.set("b", "2");
        storage.set("c", "3"); // "a" evict ho jaayega

        assertNull(storage.get("a"));
        assertEquals("2", storage.get("b"));
        assertEquals("3", storage.get("c"));
    }

    @Test
    void basicSetAndGetStillWork() {
        Storage storage = new Storage(100);

        storage.set("name", "Shaili");
        assertEquals("Shaili", storage.get("name"));

        storage.delete("name");
        assertNull(storage.get("name"));
    }
}