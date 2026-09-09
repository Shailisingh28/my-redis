package com.shaili.myredis;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyHashMapTest {

    @Test
    void putAndGetBasicKey() {
        MyHashMap map = new MyHashMap();
        map.put("name", "Shaili");

        assertEquals("Shaili", map.get("name"));
    }

    @Test
    void getReturnsNullForMissingKey() {
        MyHashMap map = new MyHashMap();

        assertNull(map.get("doesNotExist"));
    }

    @Test
    void putUpdatesExistingKey() {
        MyHashMap map = new MyHashMap();
        map.put("age", "23");
        map.put("age", "24"); // same key, naya value

        assertEquals("24", map.get("age"));
        assertEquals(1, map.size()); // size badhna nahi chahiye, sirf update hona chahiye
    }

    @Test
    void removeDeletesKey() {
        MyHashMap map = new MyHashMap();
        map.put("city", "Noida");

        boolean removed = map.remove("city");

        assertTrue(removed);
        assertNull(map.get("city"));
    }

    @Test
    void removeReturnsFalseForMissingKey() {
        MyHashMap map = new MyHashMap();

        assertFalse(map.remove("neverExisted"));
    }

    @Test
    void resizeHappensAndDataSurvives() {
        MyHashMap map = new MyHashMap();

        // 13 keys daalo, taaki resize (16 * 0.75 = 12 threshold) trigger ho
        for (int i = 1; i <= 13; i++) {
            map.put("key" + i, "value" + i);
        }

        // Confirm karo saari keys, resize ke baad bhi, sahi values deti hain
        for (int i = 1; i <= 13; i++) {
            assertEquals("value" + i, map.get("key" + i));
        }
    }

    @Test
    void collisionHandlingWorksCorrectly() {
        MyHashMap map = new MyHashMap();

        // Alag keys, jo collision karti hon ya na karein, dono ka data safe rehna
        // chahiye
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");

        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
        assertEquals("3", map.get("c"));
    }
}