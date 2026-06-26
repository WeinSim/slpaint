package com.weinsim.misc;

import java.io.*;
import java.util.Arrays;

/**
 * Add a newline character at the end of each file and insert newline character
 * after last method.
 */
public class AddNewLineChars {

    public static void main(String[] args) {
        String input = "src";
        System.out.format("Running AddNewLineChars on input file \"%s\"\n", input);
        run(new File(input));
    }

    private static void run(File file) {
        final String[] allwedExtensions = {
                "java",
                "txt",
                "glsl",
                "json"
        };
        final String filename = file.getName();
        boolean processFile = file.isDirectory()
                || Arrays.stream(allwedExtensions).anyMatch(s -> filename.endsWith("." + s));
        if (!processFile)
            return;
        System.out.println(file.getAbsolutePath());
        if (file.isDirectory()) {
            for (File child : file.listFiles())
                run(child);
            return;
        }
        String content = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            content = reader.readAllAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (content == null)
            return;
        if (!content.endsWith("\n"))
            content += "\n";
        if (content.endsWith("    }\n}\n"))
            content = content.substring(0, content.length() - 2) + "\n}\n";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
