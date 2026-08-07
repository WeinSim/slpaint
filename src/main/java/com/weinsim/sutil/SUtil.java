package com.weinsim.sutil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import com.weinsim.sutil.math.SVector;

public class SUtil {

    /*
     * Command for displaying number of java files in current directory or
     * subdirectories:
     * ls -lR . | grep \\.java | wc -l
     */
    public static void printNumLines() {
        int numLinesTotal = getNumLines(true);
        int numLinesNoSUtil = getNumLines(false);
        int numLinesSUtil = numLinesTotal - numLinesNoSUtil;

        System.out.format("Number of lines: %d (%d + %d S)\n", numLinesTotal, numLinesNoSUtil, numLinesSUtil);
    }

    private static int getNumLines(boolean includeSU) {
        File start = new File("src");
        return getNumLines(start, includeSU);
    }

    private static int getNumLines(File file, boolean includeSU) {
        if (file.isDirectory()) {
            String[] subdirectories = file.list();
            int sum = 0;
            for (String sub : subdirectories) {
                if (sub.equals("sutil") && !includeSU) {
                    continue;
                }
                sum += getNumLines(new File(file, sub), includeSU);
            }
            return sum;
        } else {
            if (!file.getName().endsWith(".java")) {
                return 0;
            } else {
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] bytes = fis.readAllBytes();
                    // number of lines = 1 + number of newline characters
                    int sum = 1;
                    for (byte b : bytes) {
                        if (b == '\n')
                            sum++;
                    }
                    return sum;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        }
    }

    public static void compareFiles(String file1, String file2) {
        System.out.println("Comparing the following 2 files:");
        System.out.format("File 1: \"%s\"\n", file1);
        System.out.format("File 2: \"%s\"\n", file2);
        File f1 = new File(file1), f2 = new File(file2);
        byte[] bytes1 = null, bytes2 = null;
        try (FileInputStream fis = new FileInputStream(f1)) {
            bytes1 = fis.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileInputStream fis = new FileInputStream(f2)) {
            bytes2 = fis.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bytes1.length != bytes2.length) {
            System.out.println("The two files have different sizes.");
            System.out.format("Size of file 1: %d bytes\n", bytes1.length);
            System.out.format("Size of file 2: %d bytes\n", bytes2.length);
            return;
        }
        System.out.println("The two files have the same length");
        int numBytesDifferent = 0;
        final int MAX_DIFF = 100;
        final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
        for (int i = 0; i < bytes1.length; i++) {
            if (bytes1[i] != bytes2[i]) {
                if (numBytesDifferent == MAX_DIFF) {
                    System.out.println("...");
                    System.out.format("(Only printing first %d mismatched bytes)", MAX_DIFF);
                } else if (numBytesDifferent < MAX_DIFF) {
                    int i1 = bytes1[i] & 0xFF, i2 = bytes2[i] & 0xFF;
                    System.out.format("Byte %s is different (%s vs. %s)\n", "0x" + Integer.toHexString(i),
                            "0x" + HEX_CHARS[i1 / 16] + HEX_CHARS[i1 % 16],
                            "0x" + HEX_CHARS[i2 / 16] + HEX_CHARS[i2 % 16]);
                    numBytesDifferent++;
                }
            }
        }
        if (numBytesDifferent == 0) {
            System.out.println("The two files match");
        } else {
            double percent = 100.0 * numBytesDifferent / bytes1.length;
            System.out.format("The two files differ in %d / %d bytes (%.1f%%)\n", numBytesDifferent, bytes1.length,
                    percent);
        }
    }

    /**
     * <pre>
     * Random r = new Random();
     * for (int i = 0; i < 20; i++) {
     *     int i1 = r.nextInt(),
     *             i2 = r.nextInt();
     *     long l = SUtil.hilo(i1, i2);
     *     System.out.format("Original     : %08x, %08x\n", i1, i2);
     *     System.out.format("Reconstructed: %08x, %08x\n\n", SUtil.hi(l), SUtil.lo(l));
     * }
     * </pre>
     */
    public static long hilo(int hi, int lo) {
        return ((hi & 0xFFFFFFFFL) << 32) | (lo & 0xFFFFFFFFL);
    }

    public static int hi(long l) {
        return (int) ((l >> 32) & 0xFFFFFFFFL);
    }

    public static int lo(long l) {
        return (int) (l & 0xFFFFFFFFL);
    }

    public static int[][] intLine(int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
        int[][] ret = new int[Math.max(dx, dy) + 1][2];
        boolean swap = dy > dx;
        if (swap) {
            int temp = x0;
            x0 = y0;
            y0 = temp;

            temp = x1;
            x1 = y1;
            y1 = temp;
        }
        if (x0 > x1) {
            int temp = x0;
            x0 = x1;
            x1 = temp;

            temp = y0;
            y0 = y1;
            y1 = temp;
        }
        for (int x = x0; x <= x1; x++) {
            int y = dx == 0 ? y0 : (int) Math.round(SUtil.map(x, x0, x1, y0, y1));
            ret[x - x0][0] = swap ? y : x;
            ret[x - x0][1] = swap ? x : y;
        }
        return ret;
    }

    public static double map(double x, double xmin, double xmax, double ymin, double ymax) {
        return (x - xmin) / (xmax - xmin) * (ymax - ymin) + ymin;
    }

    public static boolean pointInsideRect(SVector point, SVector pos, SVector size) {
        double x0 = pos.x, x1 = pos.x + size.x;
        double y0 = pos.y, y1 = pos.y + size.y;

        double xmin = Math.min(x0, x1), xmax = Math.max(x0, x1);
        double ymin = Math.min(y0, y1), ymax = Math.max(y0, y1);

        return point.x >= xmin && point.x < xmax
                && point.y >= ymin && point.y < ymax;
    }

    public static boolean pointInsideCube(SVector point, SVector pos, SVector size) {
        double x0 = pos.x, x1 = pos.x + size.x;
        double y0 = pos.y, y1 = pos.y + size.y;
        double z0 = pos.z, z1 = pos.z + size.z;

        double xmin = Math.min(x0, x1), xmax = Math.max(x0, x1);
        double ymin = Math.min(y0, y1), ymax = Math.max(y0, y1);
        double zmin = Math.min(z0, z1), zmax = Math.max(z0, z1);

        return point.x >= xmin && point.x < xmax
                && point.y >= ymin && point.y < ymax
                && point.z >= zmin && point.z < zmax;
    }

    public static boolean rectsOverlap(SVector pos1, SVector size1, SVector pos2, SVector size2) {
        double x0 = pos1.x, x1 = pos1.x + size1.x;
        double y0 = pos1.y, y1 = pos1.y + size1.y;

        double xmin1 = Math.min(x0, x1), xmax1 = Math.max(x0, x1);
        double ymin1 = Math.min(y0, y1), ymax1 = Math.max(y0, y1);

        x0 = pos2.x;
        x1 = pos2.x + size2.x;
        y0 = pos2.y;
        y1 = pos2.y + size2.y;

        double xmin2 = Math.min(x0, x1), xmax2 = Math.max(x0, x1);
        double ymin2 = Math.min(y0, y1), ymax2 = Math.max(y0, y1);

        return xmax1 >= xmin2 && xmin1 < xmax2
                && ymax1 >= ymin2 && ymin1 < ymax2;
    }

    public static boolean cuboidsOverlap(SVector pos1, SVector size1, SVector pos2, SVector size2) {
        double x0 = pos1.x, x1 = pos1.x + size1.x;
        double y0 = pos1.y, y1 = pos1.y + size1.y;
        double z0 = pos1.z, z1 = pos1.z + size1.z;

        double xmin1 = Math.min(x0, x1), xmax1 = Math.max(x0, x1);
        double ymin1 = Math.min(y0, y1), ymax1 = Math.max(y0, y1);
        double zmin1 = Math.min(z0, z1), zmax1 = Math.max(z0, z1);

        x0 = pos2.x;
        x1 = pos2.x + size2.x;
        y0 = pos2.y;
        y1 = pos2.y + size2.y;
        z0 = pos2.z;
        z1 = pos2.z + size2.z;

        double xmin2 = Math.min(x0, x1), xmax2 = Math.max(x0, x1);
        double ymin2 = Math.min(y0, y1), ymax2 = Math.max(y0, y1);
        double zmin2 = Math.min(z0, z1), zmax2 = Math.max(z0, z1);

        return xmax1 >= xmin2 && xmin1 < xmax2
                && ymax1 >= ymin2 && ymin1 < ymax2
                && zmax1 >= zmin2 && zmin1 < zmax2;
    }

    public static double lerp(double x0, double x1, double t) {
        return (1 - t) * x0 + t * x1;
    }

    public static <T> int indexOf(T t, T[] array) {
        for (int i = 0; i < array.length; i++) {
            if (t == array[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Adds {@code element} to {@code list} while maintaining the list's order,
     * which can be either ascending or descending.
     * The list is assumed to already be sorted according to the specified order.
     * 
     * @param <T>
     * @param list
     * @param element
     * @param descending
     */
    public static <T extends Comparable<T>> void addSorted(List<T> list, T element, boolean descending) {
        // edge case: list is empty
        if (list.isEmpty()) {
            list.add(element);
            return;
        }

        // edge cases: element is less than smallest / greater than largest element in
        // the list
        int sign = descending ? -1 : 1;
        if (sign * element.compareTo(list.getFirst()) < 0) {
            list.addFirst(element);
            return;
        }
        if (sign * element.compareTo(list.getLast()) > 0) {
            list.addLast(element);
            return;
        }

        // base case: element is somewhere in the middle
        int index;
        int left = 0, right = list.size() - 1;
        while (true) {
            if (right - left <= 1) {
                index = right;
                break;
            }
            int middle = (left + right) / 2;
            T middleElement = list.get(middle);
            int compare = sign * element.compareTo(middleElement);
            if (compare < 0) {
                right = middle;
            } else if (compare > 0) {
                left = middle;
            } else {
                index = middle;
                break;
            }
        }
        list.add(index, element);
    }

    public static <T> Supplier<T> ifThenElse(BooleanSupplier predicate, Supplier<? extends T> option1,
            Supplier<? extends T> option2) {

        return () -> predicate.getAsBoolean() ? option1.get() : option2.get();
    }
}
