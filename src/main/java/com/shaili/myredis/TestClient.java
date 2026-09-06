package com.shaili.myredis;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TestClient {

    public static void main(String[] args) throws IOException {
        for (int i = 1; i <= 13; i++) {
            String key = "key" + i;
            String value = "value" + i;

            try (Socket socket = new Socket("localhost", 6379)) {
                OutputStream out = socket.getOutputStream();

                String command = "*3\r\n"
                        + "$3\r\nSET\r\n"
                        + "$" + key.length() + "\r\n" + key + "\r\n"
                        + "$" + value.length() + "\r\n" + value + "\r\n";

                out.write(command.getBytes(StandardCharsets.UTF_8));
                out.flush();

                System.out.println("Sent: SET " + key + " " + value);

            } catch (IOException e) {
                System.err.println("Error sending key" + i + ": " + e.getMessage());
            }
        }
    }
}