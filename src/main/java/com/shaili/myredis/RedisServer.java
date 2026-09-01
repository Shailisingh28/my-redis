package com.shaili.myredis;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RedisServer {

    private static final int PORT = 6379;

    public static void main(String[] args) {
        System.out.println("Starting MyRedis server on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}