package com.weinsim.slpaint.main.image;

import static org.lwjgl.util.nfd.NativeFileDialog.*;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.nfd.NFDFilterItem;
import org.lwjgl.util.nfd.NFDOpenDialogArgs;
import org.lwjgl.util.nfd.NFDSaveDialogArgs;

import com.weinsim.slpaint.main.apps.MainApp;
import com.weinsim.sutil.ui.UI;

public class ImageManager {

    public static final int RETRY = -1;

    private static final int DEFAULT_WIDTH = 1280, DEFAULT_HEIGHT = 720;

    private final MainApp app;

    private ImageHistory imageHistory;
    private ImageFile imageFile;
    private Image image;

    public ImageManager(MainApp app) {
        this.app = app;
        setEverything(null, createNewImage());
    }

    public ImageManager(MainApp app, String path) {
        this.app = app;
        try {
            ImageFile imageFile = new ImageFile(path);
            Image image = new Image(imageFile.getLoadedImage());
            setEverything(imageFile, image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (image == null)
            setEverything(null, createNewImage());
    }

    private Image createNewImage() {
        return createNewImage(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 
     * @implNote Has to be called from the main thread.
     */
    private Image createNewImage(int width, int height) {
        Image image = new Image(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
        image.setPixels(0, 0, width, height, app.getSecondaryColor());
        return image;
    }

    /**
     * @implNote Has to be called from the main thread.
     */
    public void newImage(int width, int height) {
        (new Thread(() -> newImageImpl(width, height), "New Image Thread")).start();
    }

    private void newImageImpl(int width, int height) {
        if (checkUnsavedChanges())
            return;

        app.queueEvent(() -> {
            setEverything(null, createNewImage(width, height));
            app.resetImageTransform();
        });
    }

    /**
     * @implNote Has to be called from the main thread.
     */
    public void open() {
        (new Thread(this::openImpl, "Image Opener")).start();
    }

    private void openImpl() {
        if (checkUnsavedChanges())
            return;

        ImageFile newFile = null;
        BufferedImage bufferedImage = null;
        while (true) {
            String newPath;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer pathPointer = stack.mallocPointer(1);
                NFDOpenDialogArgs args = NFDOpenDialogArgs.calloc(stack)
                        .filterList(getFileFilters(stack, true))
                        .parentWindow(it -> it
                                .type(app.getNativeHandleType())
                                .handle(app.getNativeWindowHandle()));
                if (imageFile != null)
                    args.defaultPath(stack.UTF8(imageFile.getPath()));
                int result = NFD_OpenDialog_With(pathPointer, args);
                switch (result) {
                    case NFD_OKAY -> {
                        newPath = pathPointer.getStringUTF8(0);
                        NFD_FreePath(pathPointer.get(0));
                    }
                    case NFD_CANCEL -> {
                        return;
                    }
                    default -> {
                        System.err.format("NFD error while trying to open file: %s\n", NFD_GetError());
                        return;
                    }
                }
            }

            newFile = null;
            try {
                newFile = new ImageFile(newPath);
                bufferedImage = newFile.getLoadedImage();
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (showErrorDialog("open") == UI.OK_OPTION)
                continue;
            else
                return;
        }

        final ImageFile finalNewFile = newFile;
        final BufferedImage finalBufferedImage = bufferedImage;
        app.queueEvent(() -> {
            setEverything(finalNewFile, new Image(finalBufferedImage));
            app.resetImageTransform();
        });
    }

    /**
     * @implNote Has to be called from the main thread.
     */
    public void saveAs() {
        (new Thread(this::saveAsImpl, "Image Saver")).start();
    }

    private boolean saveAsImpl() {
        ImageFile newFile = null;
        while (true) {
            String newPath;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer pathPointer = stack.mallocPointer(1);
                NFDSaveDialogArgs args = NFDSaveDialogArgs.calloc(stack)
                        .filterList(getFileFilters(stack, false))
                        .parentWindow(it -> it
                                .type(app.getNativeHandleType())
                                .handle(app.getNativeWindowHandle()));
                if (imageFile != null) {
                    args.defaultPath(stack.UTF8(imageFile.getPath()));
                    args.defaultName(stack.UTF8(imageFile.getName()));
                }
                int result = NFD_SaveDialog_With(pathPointer, args);
                switch (result) {
                    case NFD_OKAY -> {
                        newPath = pathPointer.getStringUTF8(0);
                        NFD_FreePath(pathPointer.get(0));
                    }
                    case NFD_CANCEL -> {
                        return true;
                    }
                    default -> {
                        System.err.format("NFD error while trying to save file: %s\n", NFD_GetError());
                        return true;
                    }
                }
            }

            String suffix = newPath.substring(newPath.lastIndexOf('.') + 1);
            ImageFormat format = ImageFormat.fromString(suffix);
            if (format == null) {
                // System.err.format("Unable to detect image format. Defaulting to %s
                // instead.\n",
                // ImageFile.DEFAULT_FORMAT.toString());
                format = ImageFile.DEFAULT_FORMAT;
                newPath += "." + format.extensions[0];
            }

            newFile = null;
            try {
                newFile = new ImageFile(newPath, imageHistory.getCurrentImage(), format);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (newFile != null)
                break;

            if (showErrorDialog("save") == UI.OK_OPTION)
                continue;
            else
                return true;
        }

        final ImageFile finalNewFile = newFile;
        app.queueEvent(() -> setEverything(finalNewFile, null));
        return false;
    }

    /**
     * @implNote Has to be called from the main thread.
     */
    public void save() {
        (new Thread(imageFile == null ? this::saveAsImpl : this::saveImpl, "Image Saver")).start();
    }

    private boolean saveImpl() {
        while (true) {
            boolean success = false;
            try {
                imageFile.saveImage(imageHistory.getCurrentImage());
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (success)
                break;

            // TODO: shouldn't this return false when UI.NO_OPTION is selected?
            if (showErrorDialog("save") == UI.OK_OPTION)
                continue;
            else
                return true;
        }
        return false;
    }

    private NFDFilterItem.Buffer getFileFilters(MemoryStack stack, boolean addIndividualFiletypes) {
        ImageFormat[] formats = ImageFormat.values();
        int numFilters = addIndividualFiletypes ? formats.length + 1 : 1;
        NFDFilterItem.Buffer filters = NFDFilterItem.malloc(numFilters);
        int index = 0;
        String spec = "";
        for (ImageFormat format : formats) {
            for (String extension : format.extensions)
                spec += extension + ",";
        }
        spec = spec.substring(0, spec.length() - 1);
        filters.get(index++)
                .name(stack.UTF8("Image Files"))
                .spec(stack.UTF8(spec));
        if (addIndividualFiletypes) {
            for (int i = 0; index < numFilters; index++, i++) {
                filters.get(index)
                        .name(stack.UTF8(formats[i].name))
                        .spec(stack.UTF8(String.join(",", formats[i].extensions)));
            }
        }
        return filters;
    }

    /**
     * Notifies the user that there are unsaved changes. If "Save" is selected, the
     * current file is saved.
     * 
     * @return {@code true} if the user cancels the operation.
     * @implNote Has to be called from a separate thread.
     */
    public boolean checkUnsavedChanges() {
        if (!hasUnsavedChanges())
            return false;

        int returnCode = UI.showModalDialog(
                "Save changes?",
                "There are unsaved changes.\nDo you want to save them?",
                UI.YES_NO_CANCEL_DIALOG);

        switch (returnCode) {
            case UI.CANCEL_OPTION, UI.CLOSED_OPTION, UI.INVALID_OPTION -> {
                return true;
            }
            case UI.NO_OPTION -> {
                return false;
            }
            case UI.YES_OPTION -> {
                return imageFile == null ? saveAsImpl() : saveImpl();
            }
            default -> {
                System.err.format("Unknown return value from UI.showModalDialog: %d\n", returnCode);
                return true;
            }
        }
    }

    public boolean hasUnsavedChanges() {
        // if (imageHistory.size() == 1)
        // return false;
        // if (imageFile == null)
        // return true;
        if (imageFile == null || imageFile.getLastSavedImage() == null)
            return imageHistory.canUndo();
        return imageHistory.getCurrentImage() != imageFile.getLastSavedImage();
    }

    private static int showErrorDialog(String action) {
        String title = String.format("Unable to %s image", action);
        String mainText = String.format("""
                An error occured while attempting to %s the image.
                Please try again.""",
                action);

        return UI.showModalDialog(title, mainText, UI.OK_CANCEL_DIALOG);
    }

    private void setEverything(ImageFile imageFile, Image image) {
        this.imageFile = imageFile;

        if (image != null) {
            this.image = image;
            imageHistory = new ImageHistory(image);
        }
    }

    public void undo() {
        imageHistory.undo();
    }

    public boolean canUndo() {
        return imageHistory.canUndo();
    }

    public void redo() {
        imageHistory.redo();
    }

    public boolean canRedo() {
        return imageHistory.canRedo();
    }

    public void addSnapshot() {
        imageHistory.addSnapshot();
    }

    public long getFilesize() {
        return imageFile == null ? -1 : imageFile.getSize();
    }

    public String getFilename() {
        return imageFile == null ? null : imageFile.getName();
    }

    public ImageFormat getSavedFormat() {
        return imageFile == null ? null : imageFile.getFormat();
    }

    public Image getImage() {
        return image;
    }
}