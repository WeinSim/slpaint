package com.weinsim.slpaint.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Loader {

    private static final ClassLoader CLASS_LOADER = Loader.class.getClassLoader();

    private Loader() {
    }

    public static InputStream getInputStream(String path) throws IOException {
        path = "com/weinsim/slpaint/" + path;
        InputStream in = CLASS_LOADER.getResourceAsStream(path);
        if (in != null)
            return in;

        // try loading from filesystem
        File file = new File("src/main/resources/" + path);
        if (file.exists())
            return new FileInputStream(file);

        final String message = String.format("Unable to find resource %s", path);
        throw new FileNotFoundException(message);
    }

    public static String getString(String path) throws IOException {
        try (InputStream in = getInputStream(path)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    // kind of wasteful but good enough for now
    public static boolean exists(String path) {
        try {
            getInputStream(path);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
