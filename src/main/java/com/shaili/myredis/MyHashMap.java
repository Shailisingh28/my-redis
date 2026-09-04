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

    public MyHashMap() {
        buckets = new Node[INITIAL_CAPACITY];
        size = 0;
    }

    private int getBucketIndex(String key) {
        int hash = key.hashCode();
        int index = hash % buckets.length;
        return Math.abs(index);
    }

    public void put(String key, String value) {
        int index = getBucketIndex(key);
        Node head = buckets[index];

        Node current = head;
        while (current != null) {
            if (current.key.equals(key)) {
                current.value = value; // key already exist karti hai, value update karo
                return;
            }
            current = current.next;
        }

        Node newNode = new Node(key, value);
        newNode.next = head;
        buckets[index] = newNode;
        size++;
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