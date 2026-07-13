package com.weinsim.slpaint.main.image;

import java.awt.image.BufferedImage;
import java.util.LinkedList;

public class ImageHistory {

    private static final int MAX_NUM_CHANGES = 64;

    private final Image image;

    private LinkedList<BufferedImage> history;
    /**
     * Points to the current state of the image
     */
    private int index;

    public ImageHistory(Image image) {
        this.image = image;

        history = new LinkedList<>();
        history.add(image.getBufferedImage(true));
        index = 0;

        // System.out.println(this);
    }

    public void addSnapshot() {
        // if prior changes were undone, remove these changes
        int numBefore = history.size() - 1 - index;
        for (int i = 0; i < numBefore; i++)
            history.removeLast();

        // if list is too big, remove oldest snapshot
        if (history.size() == MAX_NUM_CHANGES)
            history.removeFirst();

        history.add(image.getBufferedImage(true));
        index = history.size() - 1;

        // System.out.format("%s: Added snapshot\n", this.toString());
    }

    public void undo() {
        if (!canUndo())
            return;

        index--;
        image.setBufferedImage(history.get(index), true);

        // System.out.format("%s: Undo\n", this.toString());
    }

    public boolean canUndo() {
        return index > 0;
    }

    public void redo() {
        if (!canRedo())
            return;

        index++;
        image.setBufferedImage(history.get(index), true);

        // System.out.format("%s: Redo\n", this.toString());
    }

    public boolean canRedo() {
        return index < history.size() - 1;
    }

    public BufferedImage getCurrentImage() {
        return history.get(index);
    }

    public int size() {
        return history.size();
    }

    @Override
    public String toString() {
        return String.format("ImageHistory [size=%d, index=%d]", history.size(), index);
    }

}
