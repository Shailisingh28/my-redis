package com.shaili.myredis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class RedisServer {

    private static final int PORT = 6379;
    private static final Storage storage = new Storage(1000);

    public static void main(String[] args) {
        System.out.println("Starting MyRedis server on port " + PORT + "...");
        storage.loadFromDisk();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                Thread clientThread = new Thread(() -> handleClient(clientSocket));// naya thread banaya, usme
                                                                                   // handleClient chala di, main thread
                                                                                   // FREE hai
                clientThread.start();
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream();

            while (true) {
                List<String> command = RespParser.parseCommand(reader);

                if (command == null) {
                    System.out.println("Client disconnected.");
                    break;
                }

                System.out.println("Received command: " + command);
                processCommand(command, out);
            }

        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }

    private static void processCommand(List<String> command, OutputStream out) throws IOException {
        String cmdName = command.get(0).toUpperCase();

        switch (cmdName) {
            case "SET" -> {
                String key = command.get(1);
                String value = command.get(2);
                storage.set(key, value);
                storage.saveToDisk();
                RespWriter.writeSimpleString(out, "OK");
            }
            case "GET" -> {
                String key = command.get(1);
                String value = storage.get(key);
                RespWriter.writeBulkString(out, value);
            }
            case "DEL" -> {
                String key = command.get(1);
                boolean deleted = storage.delete(key);
                RespWriter.writeSimpleString(out, deleted ? "1" : "0");
            }
            case "PING" -> RespWriter.writeSimpleString(out, "PONG");
            case "COMMAND" -> RespWriter.writeSimpleString(out, "OK"); // redis-cli setup query, dummy response
            case "HELLO" -> RespWriter.writeSimpleString(out, "OK"); // protocol handshake, abhi ke liye simplified

            default -> RespWriter.writeError(out, "unknown command '" + cmdName + "'");
        }
    }

}