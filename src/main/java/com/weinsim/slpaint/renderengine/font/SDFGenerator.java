package com.weinsim.slpaint.renderengine.font;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.weinsim.slpaint.main.image.Image;
import com.weinsim.sutil.SUtil;
import com.weinsim.sutil.color.Color;
import com.weinsim.sutil.color.SRGBInt;

/**
 * (Bad) java port of http://www.codersnotes.com/notes/signed-distance-fields/
 */
public class SDFGenerator {

    static final int SDF_MAX_DIST = 4;

    private Point[][] gridInside, gridOutside;
    private int width, height;

    private SDFGenerator(File file) throws IOException {
        BufferedImage image = Image.toARGB(ImageIO.read(file));
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        width = image.getWidth();
        height = image.getHeight();
        gridInside = new Point[width][height];
        gridOutside = new Point[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Points inside get marked with a dx/dy of zero.
                // Points outside get marked with an infinitely large distance.
                Color c = Color.sRGB(pixels[y * width + x]);
                SRGBInt sRGB = c.sRGBInt();
                if (sRGB.red() == 0) {
                    put(gridInside, x, y, Point.maxDist());
                    put(gridOutside, x, y, Point.minDist());
                } else {
                    put(gridInside, x, y, Point.minDist());
                    put(gridOutside, x, y, Point.maxDist());
                }
            }
        }
        generateSDF(gridInside);
        generateSDF(gridOutside);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Calculate the actual distance from the dx/dy
                double dist1 = Math.sqrt(get(gridInside, x, y).distSq()),
                        dist2 = Math.sqrt(get(gridOutside, x, y).distSq());
                double dist = dist1 - dist2;
                Color outColor = Color.sGrey(SUtil.map(dist, -SDF_MAX_DIST, SDF_MAX_DIST, 0, 1));
                pixels[y * width + x] = outColor.sRGBPacked().argb();
            }
        }
        ImageIO.write(image, "PNG", file);
    }

    public static void turnIntoSDF(File file) throws IOException {
        new SDFGenerator(file);
    }

    private void generateSDF(Point[][] g) {
        // Pass 0
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Point p = get(g, x, y);
                // x x x
                // x O .
                // . . .
                p = compare(g, p, x, y, -1, 0);
                p = compare(g, p, x, y, 0, -1);
                p = compare(g, p, x, y, -1, -1);
                p = compare(g, p, x, y, 1, -1);
                put(g, x, y, p);
            }
            for (int x = width - 1; x >= 0; x--) {
                Point p = get(g, x, y);
                // . . .
                // . O x
                // . . .
                p = compare(g, p, x, y, 1, 0);
                put(g, x, y, p);
            }
        }
        // Pass 1
        for (int y = height - 1; y >= 0; y--) {
            for (int x = width - 1; x >= 0; x--) {
                Point p = get(g, x, y);
                // . . .
                // . O x
                // x x x
                p = compare(g, p, x, y, 1, 0);
                p = compare(g, p, x, y, 0, 1);
                p = compare(g, p, x, y, -1, 1);
                p = compare(g, p, x, y, 1, 1);
                put(g, x, y, p);
            }
            for (int x = 0; x < width; x++) {
                Point p = get(g, x, y);
                // . . .
                // x O .
                // . . .
                p = compare(g, p, x, y, -1, 0);
                put(g, x, y, p);
            }
        }
    }

    private Point get(Point[][] grid, int x, int y) {
        if (x >= 0 && y >= 0 && x < width && y < height)
            return grid[y][x];
        else
            return Point.maxDist();
    }

    private void put(Point[][] g, int x, int y, Point p) {
        g[y][x] = p;
    }

    private Point compare(Point[][] g, Point p, int x, int y, int offsetx, int offsety) {
        Point other = get(g, x + offsetx, y + offsety).add(offsetx, offsety);
        return other.distSq() < p.distSq() ? other : p;
    }

    record Point(double dx, double dy) {

        double distSq() {
            return dx * dx + dy * dy;
        }

        Point add(double dx, double dy) {
            return new Point(this.dx + dx, this.dy + dy);
        }

        static Point minDist() {
            return new Point(0, 0);
        }

        static Point maxDist() {
            return new Point(1e6, 1e6);
        }

    }

}
