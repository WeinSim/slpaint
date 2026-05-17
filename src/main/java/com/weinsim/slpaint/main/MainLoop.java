package com.weinsim.slpaint.main;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.util.nfd.NativeFileDialog.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.newdawn.slick.util.Log;

import com.weinsim.slpaint.main.apps.App;
import com.weinsim.slpaint.main.apps.MainApp;
import com.weinsim.slpaint.renderengine.Window;
import com.weinsim.sutil.SUtil;

public class MainLoop {

    private static ArrayList<App> apps;

    private static LinkedList<Runnable> eventQueue;

    public static void main(String[] args) {
        long programStartTime = System.nanoTime();
        boolean firstLoop = true;

        if (MainApp.DEV_BUILD) {
            SUtil.printNumLines();
            testImports();
        }

        // String font1 = TextFont.DEFAULT_FONT_NAME;
        // TextFont.createFontAtlas(font1, 50);
        // TextFont.createFontAtlas(font1, 36);
        // TextFont.createFontAtlas(font2, 36);
        // TextFont.createFontAtlas(font2, 18);

        // Disables the message
        // "INFO:Use Java PNG Loader = true"
        // that would otherwise show up when loading the first image
        Log.setVerbose(false);

        initGLFW();
        NFD_Init();

        apps = new ArrayList<>();
        MainApp mainApp = new MainApp(args.length == 0
                ? (MainApp.DEV_BUILD ? "src/main/resources/com/weinsim/slpaint/images/test.png" : null)
                : args[0]);
        eventQueue = new LinkedList<>();

        double deltaT = 1.0 / 60.0;
        boolean stop = false;
        while (!stop) {
            long startTime = System.nanoTime();

            while (!eventQueue.isEmpty())
                eventQueue.removeFirst().run();

            for (int i = apps.size() - 1; i >= 0; i--) {
                App app = apps.get(i);

                glfwPollEvents();

                app.makeContextCurrent();
                app.update(deltaT);
                app.render();

                if (app.isCloseRequested()) {
                    if (app.finish()) {
                        app.closeDisplay();
                        apps.remove(i);

                        if (app == mainApp)
                            stop = true;
                    } else {
                        app.unrequestClose();
                    }
                }
            }

            if (MainApp.DEV_BUILD && firstLoop) {
                long duration = System.nanoTime() - programStartTime;
                System.out.format("Startup time: %.1fms\n", duration * 1e-6);
                firstLoop = false;
            }

            long duration = System.nanoTime() - startTime;
            deltaT = 1e-9 * duration;
        }

        for (App app : apps) {
            app.closeDisplay();
        }

        NFD_Quit();
        terminateGLFW();
    }

    public static void addApp(App app) {
        apps.add(app);
    }

    public static void queueEvent(Runnable event) {
        eventQueue.add(event);
    }

    private static void initGLFW() {
        GLFWErrorCallback err = GLFWErrorCallback.createPrint(System.err);
        new GLFWErrorCallback() {

            @Override
            public void invoke(int error, long description) {
                if (error == GLFW_CURSOR_UNAVAILABLE)
                    return;

                err.invoke(error, description);
            }
        }.set();

        // Initialize Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        Window.createCursors();
    }

    private static void terminateGLFW() {
        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    /**
     * Checks that all sutil imports are self contained.
     * Would probably make more sense to explicitly forbid com.weinsim.slpaint
     * instead of manually allowing certain imports.
     */
    private static void testImports() {
        testImports(
                new File("src/main/java/com/weinsim/sutil"),
                new String[] { "com.weinsim.sutil", "java", "org" });
    }

    private static void testImports(File file, String[] allowedImports) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                testImports(child, allowedImports);
            }
        } else {
            if (!file.getName().endsWith(".java")) {
                return;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                for (int lineNumber = 1; line != null; line = reader.readLine(), lineNumber++) {
                    String[] parts = line.split(" ");
                    if (parts.length == 0)
                        continue;
                    if (!parts[0].equals("import"))
                        continue;

                    String importName = parts[parts.length - 1];
                    // int endIndex = importName.indexOf('.');
                    // importName = importName.substring(0, endIndex);
                    boolean allowed = false;
                    for (String allowedImport : allowedImports) {
                        if (importName.startsWith(allowedImport)) {
                            allowed = true;
                            break;
                        }
                    }
                    if (allowed)
                        continue;
                    System.out.format("Illegal import in %s:%d:  %s\n", file.getPath(), lineNumber, line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // private static void textFilesizes() {
    // Random random = new Random();
    // for (int i = 0; i < 1000; i++) {
    // long l = Math.abs(random.nextInt());
    // System.out.format("%010d %s\n", l, MainApp.formatFilesize(l));
    // }
    // }

    // private static void testImages() {
    // // try {
    // // BufferedImage img = ImageIO.read(new File("smiley.png"));
    // // System.out.println(img.getRGB(0, 100));
    // // String[] fileExtensions = ImageIO.getWriterFileSuffixes();
    // // for (String extension : fileExtensions) {
    // // boolean ret = ImageIO.write(img, extension, new File("output." +
    // extension));
    // // System.out.format(".%s\t%b\n", extension, ret);
    // // }
    // // } catch (IOException e) {
    // // e.printStackTrace();
    // // }
    // for (String suffix : ImageIO.getReaderFileSuffixes()) {
    // check(suffix);
    // }
    // try {
    // System.out.println(Class.forName("com.sun.imageio.plugins.jpeg.JPEGImageReader"));
    // } catch (ClassNotFoundException e) {
    // e.printStackTrace();
    // }

    // }

    // private static void check(String suffix) {
    // Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix(suffix);
    // int i = 0;
    // while (readers.hasNext()) {
    // ImageReader reader = readers.next();
    // System.out.printf("%s: %d: %s%n", suffix, i++, reader);
    // }
    // }
}