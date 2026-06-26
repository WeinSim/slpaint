package com.weinsim.slpaint.main.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class ImageFile {

    public static final ImageFormat DEFAULT_FORMAT = ImageFormat.PNG;

    private final File file;
    private final ImageFormat format;

    private final BufferedImage loadedImage;
    private BufferedImage lastSavedImage = null;

    /**
     * Creates a new {@code ImageFile} and loads the image from the specified path
     * into {@code loadedImage}.
     */
    public ImageFile(String path) throws IOException {
        this.file = new File(path);

        // load image from file
        FileInputStream fis = new FileInputStream(file);
        try (ImageInputStream iis = ImageIO.createImageInputStream(fis)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            String formatName = null;
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                formatName = reader.getFormatName();
                format = ImageFormat.fromString(formatName);
                if (format == null) {
                    throw new IOException(String.format(
                            "Unknown image format: \"%s\". This format is not known by the interface main.ImageFormat.",
                            formatName));
                }
                reader.setInput(iis);
                loadedImage = reader.read(0);
            } else {
                throw new IOException(String.format(
                        "No fitting ImageReader was found for the given file (%s)",
                        file.getAbsolutePath()));
            }
        }
    }

    public ImageFile(String path, BufferedImage image, ImageFormat format) throws IOException {
        this.file = new File(path);
        this.format = format;

        loadedImage = null;

        saveImage(image);
    }

    public void saveImage(BufferedImage image) throws IOException {
        // String formatName = format.extensions[0];
        // Iterator<ImageWriter> writers =
        // ImageIO.getImageWritersByFormatName(formatName);
        // while (writers.hasNext())
        // System.out.println(writers.next());

        // Remove alpha channel if it is known that the format doesn't support alpha.
        BufferedImage saveImage = image;
        if (!format.supportsAlpha) {
            saveImage = new BufferedImage(
                    image.getWidth(),
                    image.getHeight(),
                    BufferedImage.TYPE_INT_RGB);

            Graphics2D g = saveImage.createGraphics();
            g.drawImage(image, 0, 0, Color.WHITE, null);
            g.dispose();
        }

        if (ImageIO.write(saveImage, format.extensions[0], file)) {
            lastSavedImage = image;
            return;
        }

        throw new IOException(String.format("No appropriate ImageWriter found (format=%s)", format.toString()));
    }

    public BufferedImage getLoadedImage() {
        return loadedImage;
    }

    public BufferedImage getLastSavedImage() {
        return lastSavedImage;
    }

    public ImageFormat getFormat() {
        return format;
    }

    public String getPath() {
        return file.getParent();
    }

    public String getName() {
        return file.getName();
    }

    public long getSize() {
        return file.length();
    }

}
