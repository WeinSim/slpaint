package com.weinsim.slpaint.main.tools;

import java.util.LinkedList;

import com.weinsim.slpaint.main.image.Image;
import com.weinsim.sutil.SUtil;
import com.weinsim.sutil.color.Color;

public final class FillBucketTool extends ImageTool {

    public static final FillBucketTool INSTANCE = new FillBucketTool();

    private static final int[] FILL_XOFF = { 0, -1, 0, 1 };
    private static final int[] FILL_YOFF = { 1, 0, -1, 0 };

    private FillBucketTool() {
        super();
    }

    @Override
    public void createKeyboardShortcuts() {
    }

    @Override
    public void click(int x, int y, int mouseButton) {
        Image image = app.getImage();
        if (!image.isInside(x, y))
            return;
        Color baseColor = image.getPixel(x, y);
        Color replaceColor = mouseButton == 0 ? app.getPrimaryColor() : app.getSecondaryColor();
        if (baseColor.equals(replaceColor))
            return;
        // high 32 bits: x, low 32 bits: y
        LinkedList<Long> boundary = new LinkedList<>();
        boundary.add(SUtil.hilo(x, y));
        // No noticeable performance increase with cached bitmap for discovered pixels.
        // However, disabling it causes a bug where the while loop never finsihes when
        // the fill color matches the initial pixel color.
        boolean[][] discovered = new boolean[image.getWidth()][image.getHeight()];
        while (!boundary.isEmpty()) {
            long point = boundary.removeLast();
            int pointX = SUtil.hi(point),
                    pointY = SUtil.lo(point);
            discovered[pointX][pointY] = true;
            image.setPixel(pointX, pointY, replaceColor);
            for (int i = 0; i < FILL_XOFF.length; i++) {
                int newX = pointX + FILL_XOFF[i],
                        newY = pointY + FILL_YOFF[i];
                if (image.isInside(newX, newY)) {
                    if (discovered[newX][newY])
                        continue;
                    if (image.getPixel(newX, newY).equals(baseColor))
                        boundary.add(SUtil.hilo(newX, newY));
                }
            }
        }
        app.addImageSnapshot();
    }

    @Override
    public void finish() {
    }

    @Override
    public void cancel() {
    }

    @Override
    public String getName() {
        return "FillBucket";
    }

}
