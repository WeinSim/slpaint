package com.weinsim.sutil.json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import com.weinsim.sutil.json.tokens.JSONParseException;
import com.weinsim.sutil.json.values.JSONValue;

public class JSONTester {

    private static final String SOURCE_DIRECTORY = "res/test/json/";
    private static final String PASS_DIRECTORY = "pass";
    private static final String FAIL_DIRECTORY = "fail";

    public static void test() {
        System.out.println("Running JSON Test");
        testPass();
        testFail();
    }

    private static void testPass() {
        String dirName = SOURCE_DIRECTORY + PASS_DIRECTORY;
        File dir = new File(dirName);
        File[] files = dir.listFiles();
        Arrays.sort(files, (f1, f2) -> f1.getName().compareTo(f2.getName()));
        int total = files.length;
        int numPass = 0;

        for (File file : files) {
            String source = "";
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    source += line;
                    source += '\n';
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            JSONValue value1;
            try {
                value1 = JSONParser.parseValue(source);
            } catch (JSONParseException e) {
                System.out.format("%s: failed to parse JSON String\n", file.getName());
                continue;
            }

            String serial = JSONSerializer.serialize(value1);
            JSONValue value2 = null;
            try {
                value2 = JSONParser.parseValue(serial);
            } catch (JSONParseException e) {
                System.out.format("%s: failed to parse serialized JSON String\n", file.getName());
            }

            if (!value1.equals(value2)) {
                System.out.format("%s: serialized and reconstructed JSON Object does not match original\n",
                        file.getName());
                continue;
            }

            numPass++;
        }

        double percent = ((double) numPass) / total * 100.0;
        System.out.format("Valid files: correctly identified %d / %d (%03.1f%%)\n", numPass, total, percent);
    }

    private static void testFail() {
        String dirName = SOURCE_DIRECTORY + FAIL_DIRECTORY;
        File dir = new File(dirName);
        File[] files = dir.listFiles();
        Arrays.sort(files, (f1, f2) -> f1.getName().compareTo(f2.getName()));
        int total = files.length;
        int numPass = 0;

        for (File file : files) {
            String source = "";
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    source += line;
                    source += '\n';
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            JSONValue value1 = null;
            System.out.format("%s: ", file.getName());
            try {
                value1 = JSONParser.parseValue(source);
            } catch (JSONParseException e) {
                System.out.println(e.getMessage());
            }

            if (value1 != null) {
                System.out.println("FAILED. incorrectly parsed invalid JSON file");
                continue;
            }

            numPass++;
        }

        double percent = ((double) numPass) / total * 100.0;
        System.out.format("Invalid files: correctly identified %d / %d (%03.1f%%)\n", numPass, total, percent);
    }

}
