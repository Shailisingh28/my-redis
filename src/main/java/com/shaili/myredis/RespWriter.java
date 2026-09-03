package com.shaili.myredis;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class RespWriter {

    public static void writeSimpleString(OutputStream out, String message) throws IOException {
        String response = "+" + message + "\r\n";
        out.write(response.getBytes(StandardCharsets.UTF_8));
        out.flush();// steam se kho data turant bhej do
    }

    public static void writeBulkString(OutputStream out, String value) throws IOException {
        if (value == null) {
            out.write("$-1\r\n".getBytes(StandardCharsets.UTF_8));
        } else {
            String response = "$" + value.length() + "\r\n" + value + "\r\n";
            out.write(response.getBytes(StandardCharsets.UTF_8));
        }
        out.flush();
    }

    public static void writeError(OutputStream out, String message) throws IOException {
        String response = "-ERR " + message + "\r\n";
        out.write(response.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }
}