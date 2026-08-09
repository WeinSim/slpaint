package com.weinsim.slpaint.main.apps;

import com.weinsim.slpaint.renderengine.Window;
import com.weinsim.slpaint.ui.AppUI;
import com.weinsim.slpaint.ui.ResizeUI;
import com.weinsim.sutil.ui.UI;
import com.weinsim.sutil.ui.elements.UINumberInput;

public final class ResizeApp extends App {

    public static final int PIXELS = 0, PERCENTAGE = 1;

    public static final int CROP = 1, SCALE = 0;

    private MainApp mainApp;

    private UINumberInput widthInput;

    private int widthPixels = 0, heightPixels = 0;
    private double widthPercentage = 0, heightPercentage = 0;
    private int resizeMode;

    private boolean lockRatio;

    private final int initialWidth, initialHeight;
    private final double initialRatio;

    private final double minWidthPercentage, minHeightPercentage,
            maxWidthPercentage, maxHeightPercentage;

    public ResizeApp(MainApp mainApp, int resizeMode) {
        this.mainApp = mainApp;
        this.resizeMode = resizeMode;
        super(500, 500, Window.NORMAL, false, true, getTitle(resizeMode), mainApp);

        initialWidth = mainApp.getImage().getWidth();
        initialHeight = mainApp.getImage().getHeight();
        // when setting width and height for the first time, the aspect ratio must be
        // unlocked, otherwise the initial ratio is set to zero
        lockRatio = false;
        setWidthPixels(initialWidth);
        setHeightPixels(initialHeight);
        initialRatio = (double) initialWidth / initialHeight;
        lockRatio = true;

        minWidthPercentage = 100.0 * MainApp.MIN_IMAGE_SIZE / initialWidth;
        minHeightPercentage = 100.0 * MainApp.MIN_IMAGE_SIZE / initialHeight;
        maxWidthPercentage = 100.0 * MainApp.MAX_IMAGE_SIZE / initialWidth;
        maxHeightPercentage = 100.0 * MainApp.MAX_IMAGE_SIZE / initialHeight;

        loadUI();
    }

    private static String getTitle(int resizeMode) {
        return resizeMode == CROP ? "Crop Image" : "Resize Image";
    }

    private String getTitle() {
        return getTitle(resizeMode);
    }

    @Override
    public void childUpdate(double deltaT) {
        if (frameCount == 1)
            UI.select(widthInput);
        setTitle(getTitle());
    }

    @Override
    protected App createChildApp(int dialogType) {
        return null;
    }

    @Override
    protected AppUI<?> createUI() {
        return new ResizeUI(this);
    }

    public void setWidthInput(UINumberInput widthInput) {
        this.widthInput = widthInput;
    }

    public void done() {
        mainApp.queueEvent(
                switch (resizeMode) {
                    case CROP -> () -> mainApp.cropImage(widthPixels, heightPixels);
                    case SCALE -> () -> mainApp.resizeImage(widthPixels, heightPixels);
                    default -> throw new IllegalArgumentException("resizeMode must be either CROP or SCALE");
                });
        requestClose();
    }

    public void setWidthPixels(int widthPixels) {
        this.widthPixels = widthPixels;
        if (lockRatio) {
            heightPixels = (int) (widthPixels / initialRatio);
        }
        setSizePixels();
    }

    public void setHeightPixels(int heightPixels) {
        this.heightPixels = heightPixels;
        if (lockRatio) {
            widthPixels = (int) (heightPixels * initialRatio);
        }
        setSizePixels();
    }

    private void setSizePixels() {
        int[] clamped = clamp(
                widthPixels, heightPixels,
                MainApp.MIN_IMAGE_SIZE, MainApp.MAX_IMAGE_SIZE,
                1 / initialRatio);
        clamped = clamp(
                clamped[1], clamped[0],
                MainApp.MIN_IMAGE_SIZE, MainApp.MAX_IMAGE_SIZE,
                initialRatio);
        widthPixels = clamped[1];
        heightPixels = clamped[0];

        widthPercentage = 100.0 * widthPixels / initialWidth;
        heightPercentage = 100.0 * heightPixels / initialHeight;
    }

    /*
     * Pixels:
     * set px
     * apply ratio (px)
     * limit px
     * update perc
     * 
     * Percentage:
     * set perc
     * apply ratio (perc)
     * limit perc
     * update px
     */

    public void setWidthPercentage(int widthPercentage) {
        this.widthPercentage = widthPercentage;
        if (lockRatio) {
            heightPercentage = widthPercentage;
        }
        setSizePercentage();
    }

    public void setHeightPercentage(int heightPercentage) {
        this.heightPercentage = heightPercentage;
        if (lockRatio) {
            widthPercentage = heightPercentage;
        }
        setSizePercentage();
    }

    private void setSizePercentage() {
        double[] clamped = clamp(
                widthPercentage, heightPercentage,
                minWidthPercentage, maxWidthPercentage,
                1);
        clamped = clamp(
                clamped[1], clamped[0],
                minHeightPercentage, maxHeightPercentage,
                1);
        widthPercentage = clamped[1];
        heightPercentage = clamped[0];

        widthPixels = (int) (widthPercentage / 100 * initialWidth);
        heightPixels = (int) (heightPercentage / 100 * initialHeight);
    }

    private int[] clamp(int dim1, int dim2, int min, int max, double ratio) {
        if (dim1 < min) {
            dim1 = min;
            if (lockRatio)
                dim2 = (int) (dim1 * ratio);
        } else if (dim1 > max) {
            dim1 = max;
            if (lockRatio)
                dim2 = (int) (dim1 * ratio);
        }

        return new int[] { dim1, dim2 };
    }

    private double[] clamp(double dim1, double dim2, double min, double max, double ratio) {
        if (dim1 < min) {
            dim1 = min;
            if (lockRatio)
                dim2 = (dim1 * ratio);
        } else if (dim1 > max) {
            dim1 = max;
            if (lockRatio)
                dim2 = (dim1 * ratio);
        }

        return new double[] { dim1, dim2 };
    }

    public int getWidthPixels() {
        return widthPixels;
    }

    public int getHeightPixels() {
        return heightPixels;
    }

    public int getWidthPercentage() {
        return (int) widthPercentage;
    }

    public int getHeightPercentage() {
        return (int) heightPercentage;
    }

    public int getResizeMode() {
        return resizeMode;
    }

    public void setResizeMode(int resizeMode) {
        this.resizeMode = resizeMode;
    }

    public boolean isLockRatio() {
        return lockRatio;
    }

    public void setLockRatio(boolean lockRatio) {
        this.lockRatio = lockRatio;
    }

}
