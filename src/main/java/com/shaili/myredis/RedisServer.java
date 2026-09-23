package com.shaili.myredis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class RedisServer {

    private static final Logger logger = LoggerFactory.getLogger(RedisServer.class);
    private static final int PORT = 6379;
    private static final Storage storage = new Storage(1000);

    public static void main(String[] args) {
        logger.info("Starting MyRedis server on port {}...", PORT);

        storage.loadFromDisk();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Server is listening on port {}", PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("New client connected: {}", clientSocket.getInetAddress());

                Thread clientThread = new Thread(() -> handleClient(clientSocket));
                clientThread.start();
            }

        } catch (IOException e) {
            logger.error("Server error: {}", e.getMessage(), e);
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
                    logger.info("Client disconnected: {}", clientSocket.getInetAddress());
                    break;
                }

                logger.info("Received command from {}: {}", clientSocket.getInetAddress(), command);
                processCommand(command, out);
            }

        } catch (IOException e) {
            logger.error("Error handling client: {}", e.getMessage());
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
            case "COMMAND" -> RespWriter.writeSimpleString(out, "OK");
            case "HELLO" -> RespWriter.writeSimpleString(out, "OK");
            default -> {
                logger.warn("Unknown command received: {}", cmdName);
                RespWriter.writeError(out, "unknown command '" + cmdName + "'");
            }
        }
    }
}