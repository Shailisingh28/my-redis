package com.shaili.myredis;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class TestableRedisServer {

    public static void start(int port) {
        Storage storage = new Storage(100);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handle(clientSocket, storage)).start();
            }
        } catch (IOException e) {
            // test server band ho gaya, yeh expected hai jab test khatam ho
        }
    }

    private static void handle(Socket clientSocket, Storage storage) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream();

            while (true) {
                List<String> command = RespParser.parseCommand(reader);
                if (command == null)
                    break;

                String cmdName = command.get(0).toUpperCase();
                switch (cmdName) {
                    case "SET" -> {
                        storage.set(command.get(1), command.get(2));
                        RespWriter.writeSimpleString(out, "OK");
                    }
                    case "GET" -> RespWriter.writeBulkString(out, storage.get(command.get(1)));
                    default -> RespWriter.writeError(out, "unknown command");
                }
            }
        } catch (IOException e) {
            // client disconnect, expected
        }
    }
}