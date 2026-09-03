package com.shaili.myredis;

import java.util.HashMap;
import java.util.Map;

public class Storage {
    private final Map<String, String> data = new HashMap<>();

    public void set(String key, String value) {
        data.put(key, value);
    }

    public String get(String key) {
        return data.get(key);
    }

    public boolean delete(String key) {
        return data.remove(key) != null;
    }

}
