package com.shaili.myredis;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class IntegrationTest {

    private static Thread serverThread;
    private static final int TEST_PORT = 6399; // alag port, taaki normal server se conflict na ho

    @BeforeAll
    static void startServer() throws InterruptedException {
        serverThread = new Thread(() -> {
            TestableRedisServer.start(TEST_PORT);
        });
        serverThread.setDaemon(true);
        serverThread.start();
        Thread.sleep(500); // server ko boot hone ka thoda time do
    }

    @Test
    void setAndGetWorkOverRealSocket() throws IOException {
        try (Socket socket = new Socket("localhost", TEST_PORT)) {
            OutputStream out = socket.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            sendCommand(out, "SET", "testkey", "testvalue");
            String setResponse = in.readLine();
            assertEquals("+OK", setResponse);

            sendCommand(out, "GET", "testkey");
            String lengthLine = in.readLine();
            String valueLine = in.readLine();
            assertEquals("$9", lengthLine);
            assertEquals("testvalue", valueLine);
        }
    }

    private void sendCommand(OutputStream out, String... parts) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(parts.length).append("\r\n");
        for (String part : parts) {
            sb.append("$").append(part.length()).append("\r\n").append(part).append("\r\n");
        }
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        out.flush();
    }
}