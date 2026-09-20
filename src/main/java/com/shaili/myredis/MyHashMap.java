package com.shaili.myredis;

import java.util.concurrent.locks.ReentrantLock;

public class MyHashMap {

    private static class Node {
        String key;
        String value;
        Node next;

        Node(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    private Node[] buckets;
    private ReentrantLock[] locks;
    private int size;
    private static final int INITIAL_CAPACITY = 16;
    private static final double LOAD_FACTOR_THRESHOLD = 0.75;

    public MyHashMap() {
        buckets = new Node[INITIAL_CAPACITY];
        locks = new ReentrantLock[INITIAL_CAPACITY];
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new ReentrantLock();
        }
        size = 0;
    }

    private int getBucketIndex(String key, int arrayLength) {
        int hash = key.hashCode();
        int index = hash % arrayLength;
        return Math.abs(index);
    }

    private int getBucketIndex(String key) {
        return getBucketIndex(key, buckets.length);
    }

    public void put(String key, String value) {
        int index = getBucketIndex(key);
        locks[index].lock();
        try {
            Node head = buckets[index];
            Node current = head;
            while (current != null) {
                if (current.key.equals(key)) {
                    current.value = value;
                    return;
                }
                current = current.next;
            }

            Node newNode = new Node(key, value);
            newNode.next = head;
            buckets[index] = newNode;
            size++;

        } finally {
            locks[index].unlock();
        }

        double currentLoadFactor = (double) size / buckets.length;
        if (currentLoadFactor > LOAD_FACTOR_THRESHOLD) {
            resize();
        }
    }

    public String get(String key) {
        int index = getBucketIndex(key);
        locks[index].lock();
        try {
            Node current = buckets[index];
            while (current != null) {
                if (current.key.equals(key)) {
                    return current.value;
                }
                current = current.next;
            }
            return null;
        } finally {
            locks[index].unlock();
        }
    }

    public boolean remove(String key) {
        int index = getBucketIndex(key);
        locks[index].lock();
        try {
            Node current = buckets[index];
            Node previous = null;

            while (current != null) {
                if (current.key.equals(key)) {
                    if (previous == null) {
                        buckets[index] = current.next;
                    } else {
                        previous.next = current.next;
                    }
                    size--;
                    return true;
                }
                previous = current;
                current = current.next;
            }
            return false;
        } finally {
            locks[index].unlock();
        }
    }

    private synchronized void resize() {
        // Double-check: shayad kisi doosre thread ne already resize kar diya ho
        // jab tak yeh thread resize() method mein aane ka wait kar raha tha
        double currentLoadFactor = (double) size / buckets.length;
        if (currentLoadFactor <= LOAD_FACTOR_THRESHOLD) {
            return; // koi aur thread already resize kar chuka, ab zaroorat nahi
        }

        Node[] oldBuckets = buckets;
        int newCapacity = oldBuckets.length * 2;
        buckets = new Node[newCapacity];

        ReentrantLock[] newLocks = new ReentrantLock[newCapacity];
        for (int i = 0; i < newLocks.length; i++) {
            newLocks[i] = new ReentrantLock();
        }
        locks = newLocks;

        System.out.println("Resizing hash table from " + oldBuckets.length + " to " + newCapacity + " buckets...");

        for (Node head : oldBuckets) {
            Node current = head;
            while (current != null) {
                Node next = current.next;
                int newIndex = getBucketIndex(current.key, buckets.length);
                current.next = buckets[newIndex];
                buckets[newIndex] = current;
                current = next;
            }
        }
    }

    public int size() {
        return size;
    }
}