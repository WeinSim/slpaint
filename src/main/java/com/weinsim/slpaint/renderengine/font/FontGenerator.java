package com.weinsim.slpaint.renderengine.font;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import com.weinsim.sutil.color.Color;
import com.weinsim.sutil.color.SRGBInt;

public class FontGenerator {

    private static final char[] CHAR_RANGES = {
            0x0020, 0x007E,
            0x00A0, 0x00FF
    };

    // private static final char BULLET_CHAR = 0x2022; // • (BULLET)
    private static final char[] EXTRA_CHARS = {
            TextFont.UNKNOWN_CHAR,
            // BULLET_CHAR,
    };

    private FontGenerator() {
    }

    public static void createFontAtlas(String name, int textSize) throws IOException {
        System.out.format("Generating font atlas for font \"%s\" at size %d:\n", name, textSize);
        String directory = String.format(
                "src/main/resources/com/weinsim/slpaint/%s%s/",
                TextFont.FONT_DIRECTORY,
                name);
        // delete old files
        System.out.println("  Deleting old files...");
        Files.list(Path.of(directory))
                .filter(p -> p.getFileName().toString().contains(String.format("output",
                        textSize)))
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        // create base bitmap
        System.out.println("  Generating font bitmaps...");
        runCommand(directory, getFontGenerationCommand(
                name,
                SDFGenerator.SDF_MAX_DIST,
                1,
                512,
                512,
                textSize,
                CHAR_RANGES,
                EXTRA_CHARS,
                Color.sGrey(0)));

        // convert generated images into SDFs
        System.out.println("  Generating SDFs...");
        String fntFileName = String.format("output.fnt", textSize);
        BufferedReader reader = new BufferedReader(new FileReader(new File(directory, fntFileName)));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(" ");
            if (!parts[0].equals("page"))
                continue;
            int eqIndex = parts[2].indexOf('=');
            String filename = parts[2].substring(eqIndex + 2, parts[2].length() - 1);
            SDFGenerator.turnIntoSDF(new File(directory, filename));
        }
        reader.close();
        System.out.println("Done");
    }

    private static ArrayList<String> getFontGenerationCommand(String fontName, int padding, int spacing,
            int textureWidth, int textureHeight, int fontSize, char[] charRanges, char[] extraChars, Color bgColor) {

        ArrayList<String> commands = new ArrayList<>();
        commands.add("fontbm");
        addArgument(commands, "font-file", "%s.ttf".formatted(fontName));
        addArgument(commands, "output", "output");
        addArgument(commands, "padding-up", padding);
        addArgument(commands, "padding-down", padding);
        addArgument(commands, "padding-left", padding);
        addArgument(commands, "padding-right", padding);
        addArgument(commands, "spacing-vert", spacing);
        addArgument(commands, "spacing-horiz", spacing);
        addArgument(commands, "texture-size", "%dx%d".formatted(textureWidth, textureHeight));
        addArgument(commands, "font-size", fontSize);
        addArgument(commands, "monochrome", null);
        StringBuilder charsBuilder = new StringBuilder();
        for (int i = 0; i < charRanges.length / 2; i++)
            charsBuilder.append("%d-%d,".formatted((int) charRanges[2 * i], (int) charRanges[2 * i + 1]));
        for (int extraChar : extraChars)
            charsBuilder.append("%d,".formatted((int) extraChar));
        int len = charsBuilder.length();
        if (len > 0)
            charsBuilder.deleteCharAt(len - 1);
        addArgument(commands, "chars", charsBuilder.toString());
        SRGBInt sRGB = bgColor.sRGBInt();
        addArgument(commands, "background-color", "%d,%d,%d".formatted(sRGB.red(), sRGB.green(), sRGB.blue()));
        // System.out.print("Generated command: ");
        // for (String str : commands) {
        // System.out.print(str + " ");
        // }
        // System.out.println();
        return commands;
    }

    private static void addArgument(ArrayList<String> commands, String argument, int value) {
        addArgument(commands, argument, Integer.toString(value));
    }

    private static void addArgument(ArrayList<String> commands, String argument, String value) {
        commands.add("--" + argument);
        if (value != null)
            commands.add(value);
    }

    public static int runCommand(String directory, ArrayList<String> commands) throws IOException {
        int exitVal = 1;
        try {
            // ProcessBuilder pb = new ProcessBuilder("sh", "-c", "ls");
            ProcessBuilder pb = new ProcessBuilder(commands);
            // pb.directory(new File(System.getProperty("user.home")));
            pb.directory(new File(directory));
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream())),
                    errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = outReader.readLine()) != null)
                output.append(line + "\n");
            while ((line = errReader.readLine()) != null)
                output.append(line + "\n");
            if (!output.isEmpty())
                System.out.print(output);

            exitVal = process.waitFor();
            // if (exitVal == 0) {
            // System.out.println(output);
            // }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return exitVal;
    }

}
