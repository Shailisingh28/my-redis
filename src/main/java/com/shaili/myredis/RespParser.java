package com.shaili.myredis;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RespParser {
    public static List<String> parseCommand(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null || !line.startsWith("*")) {
            return null;
        }
        int numArgs = Integer.parseInt(line.substring(1));
        List<String> command = new ArrayList<>();
        for (int i = 0; i < numArgs; i++) {
            String lengthLine = reader.readLine();
            if (lengthLine == null || !lengthLine.startsWith("$")) {
                return null;
            }
            String value = reader.readLine();
            command.add(value);
        }
        return command;
    }
}
