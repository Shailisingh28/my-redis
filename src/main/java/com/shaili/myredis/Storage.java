package com.shaili.myredis;

public class Storage {

    private final LRUCache data;

    public Storage(int capacity) {
        this.data = new LRUCache(capacity);
    }

    public void set(String key, String value) {
        data.put(key, value);
    }

    public String get(String key) {
        return data.get(key);
    }

    public boolean delete(String key) {
        return data.remove(key);
    }

    public int size() {
        return data.size();
    }
}