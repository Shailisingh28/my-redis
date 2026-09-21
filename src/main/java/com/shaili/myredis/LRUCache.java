package com.shaili.myredis;

import java.util.HashMap;
import java.util.Map;

public class LRUCache {

    private static class DNode {
        String key;
        String value;
        DNode prev;
        DNode next;

        DNode(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    private final Map<String, DNode> map;
    private final int capacity;
    private final DNode head; // dummy head, sabse recent yaha ke paas hoga
    private final DNode tail; // dummy tail, sabse purana yaha ke paas hoga

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.map = new HashMap<>();
        this.head = new DNode(null, null);
        this.tail = new DNode(null, null);
        head.next = tail;
        tail.prev = head;
    }

    public synchronized String get(String key) {
        DNode node = map.get(key);
        if (node == null) {
            return null;
        }
        moveToFront(node);
        return node.value;
    }

    public synchronized void put(String key, String value) {
        DNode existing = map.get(key);

        if (existing != null) {
            existing.value = value;
            moveToFront(existing);
            return;
        }

        if (map.size() >= capacity) {
            evictLeastRecentlyUsed();
        }

        DNode newNode = new DNode(key, value);
        map.put(key, newNode);
        addToFront(newNode);
    }

    private void moveToFront(DNode node) {
        removeNode(node);
        addToFront(node);
    }

    private void addToFront(DNode node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(DNode node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void evictLeastRecentlyUsed() {
        DNode leastRecent = tail.prev;
        System.out.println("Evicting key (LRU): " + leastRecent.key);
        removeNode(leastRecent);
        map.remove(leastRecent.key);
    }

    public int size() {
        return map.size();
    }
}