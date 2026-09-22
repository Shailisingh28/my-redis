package com.shaili.myredis;

import java.io.*;
import java.util.Map;

public class Storage {

    private final LRUCache data;
    private static final String SNAPSHOT_FILE = "myredis_dump.txt";

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

    public void saveToDisk() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SNAPSHOT_FILE))) {
            for (Map.Entry<String, String> entry : data.getAllEntries().entrySet()) {
                writer.write(entry.getKey() + "\t" + entry.getValue());
                writer.newLine();
            }
            System.out.println("Snapshot saved to " + SNAPSHOT_FILE);
        } catch (IOException e) {
            System.err.println("Error saving snapshot: " + e.getMessage());
        }
    }

    public void loadFromDisk() {
        File file = new File(SNAPSHOT_FILE);
        if (!file.exists()) {
            System.out.println("No snapshot file found, starting fresh.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int loadedCount = 0;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t", 2);
                if (parts.length == 2) {
                    data.put(parts[0], parts[1]);
                    loadedCount++;
                }
            }
            System.out.println("Loaded " + loadedCount + " keys from snapshot.");
        } catch (IOException e) {
            System.err.println("Error loading snapshot: " + e.getMessage());
        }
    }
}