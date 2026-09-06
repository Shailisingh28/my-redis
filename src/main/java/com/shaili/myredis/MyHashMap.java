package com.shaili.myredis;

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
    private int size;
    private static final int INITIAL_CAPACITY = 16;
    private static final double LOAD_FACTOR_THRESHOLD = 0.75;

    public MyHashMap() {
        buckets = new Node[INITIAL_CAPACITY];
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

        // Insert ke baad check karo, kya resize zaroori hai
        double currentLoadFactor = (double) size / buckets.length;
        if (currentLoadFactor > LOAD_FACTOR_THRESHOLD) {
            resize();
        }
    }

    private void resize() {
        Node[] oldBuckets = buckets;
        int newCapacity = oldBuckets.length * 2;
        buckets = new Node[newCapacity];

        System.out.println("Resizing hash table from " + oldBuckets.length + " to " + newCapacity + " buckets...");

        // Har purani key ko dobara hash karke naye array mein daalo
        for (Node head : oldBuckets) {
            Node current = head;
            while (current != null) {
                Node next = current.next; // yaad rakho next, kyunki hum current.next badalne wale hain

                int newIndex = getBucketIndex(current.key, buckets.length);
                current.next = buckets[newIndex];
                buckets[newIndex] = current;

                current = next;
            }
        }
    }

    public String get(String key) {
        int index = getBucketIndex(key);
        Node current = buckets[index];

        while (current != null) {
            if (current.key.equals(key)) {
                return current.value;
            }
            current = current.next;
        }

        return null;
    }

    public boolean remove(String key) {
        int index = getBucketIndex(key);
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
    }

    public int size() {
        return size;
    }
}