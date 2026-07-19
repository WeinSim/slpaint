package com.weinsim.slpaint.main.image;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.IntBuffer;
import java.util.Arrays;

import org.lwjgl.BufferUtils;

import com.weinsim.slpaint.main.effects.Effect;
import com.weinsim.slpaint.main.effects.EffectInstance;
import com.weinsim.slpaint.renderengine.Cleanable;
import com.weinsim.slpaint.renderengine.RawModel;
import com.weinsim.slpaint.renderengine.bufferobjects.PingPongFBO;
import com.weinsim.slpaint.renderengine.shader.ShaderProgram;
import com.weinsim.sutil.SUtil;
import com.weinsim.sutil.math.SVector;

/**
 * Responsible for syncing a CPU-backed pixel array with an openGL texture.
 */
public class Image implements Cleanable {

    private static final int CLEAN = 0,
            OPENGL_DIRTY = 1,
            PIXEL_ARRAY_DIRTY = 2;

    private BufferedImage bufferedImage;
    private int[] pixelArray; // the backing array of bufferedImage
    private int width, height;

    private final PingPongFBO fbo;

    // TODO: we are not yet considering effect parameters (e.g. the brightness
    // value)
    private Effect currentPreviewEffect;

    private int syncStatus;

    private int dirtyMinX;
    private int dirtyMaxX;
    private int dirtyMinY;
    private int dirtyMaxY;

    public Image(BufferedImage image) {
        fbo = new PingPongFBO();
        setBufferedImage(image);
    }

    public void setBufferedImage(BufferedImage image) {
        setBufferedImage(image, false);
    }

    /**
     * 
     * @param image
     * @param copy  Whether to copy the underlying image data.
     */
    public void setBufferedImage(BufferedImage image, boolean copy) {
        // convert image to ARGB format
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = newImage.createGraphics();
            g.setComposite(AlphaComposite.Src);
            g.drawImage(image, 0, 0, null);
            g.dispose();

            image = newImage;
        } else if (copy) {
            image = copyBufferedImage(image);
        }

        bufferedImage = image;
        pixelArray = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        width = image.getWidth();
        height = image.getHeight();

        syncStatus = PIXEL_ARRAY_DIRTY;
        updateOpenGLTexture(false);
    }

    /**
     * Synchronizes the cpu pixel buffer with the openGL texture.
     */
    public void sync() {
        if (syncStatus == PIXEL_ARRAY_DIRTY)
            updateOpenGLTexture(true);
        else if (syncStatus == OPENGL_DIRTY)
            updatePixelArray();
    }

    public void syncPixelArray() {
        if (syncStatus == OPENGL_DIRTY)
            updatePixelArray();
    }

    public void syncOpenGLTexture() {
        if (syncStatus == PIXEL_ARRAY_DIRTY)
            updateOpenGLTexture(true);
    }

    public void markOpenGLTextureDirty() {
        if (syncStatus == PIXEL_ARRAY_DIRTY)
            System.err.println(
                    "Image sync error: openGL texture marked as dirty while pixel array was also dirty");
        syncStatus = OPENGL_DIRTY;
        currentPreviewEffect = null;
    }

    private void setDirty(int x, int y) {
        if (syncStatus == OPENGL_DIRTY)
            System.err.format(
                    "Image sync error: pixel (%d, %d) marked as dirty while openGL texture was also dirty\n",
                    x, y);
        if (syncStatus == CLEAN) {
            dirtyMinX = x;
            dirtyMaxX = x;
            dirtyMinY = y;
            dirtyMaxY = y;
        } else {
            // syncStatus == PIXEL_ARRAY_DIRTY
            dirtyMinX = Math.min(dirtyMinX, x);
            dirtyMaxX = Math.max(dirtyMaxX, x);
            dirtyMinY = Math.min(dirtyMinY, y);
            dirtyMaxY = Math.max(dirtyMaxY, y);
        }
        syncStatus = PIXEL_ARRAY_DIRTY;
    }

    private void updateOpenGLTexture(boolean subArea) {
        IntBuffer buffer = BufferUtils.createIntBuffer(width * height);
        buffer.put(pixelArray);
        buffer.flip();

        glBindTexture(GL_TEXTURE_2D, fbo.getTextureID());
        if (subArea) {
            int dirtyWidth = dirtyMaxX - dirtyMinX + 1,
                    dirtyHeight = dirtyMaxY - dirtyMinY + 1;

            glPixelStorei(GL_UNPACK_ROW_LENGTH, width);
            glPixelStorei(GL_UNPACK_SKIP_PIXELS, dirtyMinX);
            glPixelStorei(GL_UNPACK_SKIP_ROWS, dirtyMinY);

            glTexSubImage2D(GL_TEXTURE_2D, 0, dirtyMinX, dirtyMinY, dirtyWidth, dirtyHeight, GL_BGRA,
                    GL_UNSIGNED_INT_8_8_8_8_REV, buffer);

            glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
            glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
            glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
        } else {
            glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_BGRA,
                    GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
        }

        glGenerateMipmap(GL_TEXTURE_2D);
        currentPreviewEffect = null;
        syncStatus = CLEAN;
    }

    private void updatePixelArray() {
        if (bufferedImage.getWidth() != width || bufferedImage.getHeight() != height) {
            // TODO: this part is copied from above
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            pixelArray = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
        }
        glBindTexture(GL_TEXTURE_2D, fbo.getTextureID());
        glGetTexImage(GL_TEXTURE_2D, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, pixelArray);
        glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0);
        syncStatus = CLEAN;
    }

    public void crop(int startX, int startY, int newWidth, int newHeight, int backgroundColor) {
        if (newWidth == width && newHeight == height)
            return;

        BufferedImage oldImage = bufferedImage,
                newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        int oldWidth = oldImage.getWidth(),
                oldHeight = oldImage.getHeight();

        int[] oldPixels = ((DataBufferInt) oldImage.getRaster().getDataBuffer()).getData(),
                newPixels = ((DataBufferInt) newImage.getRaster().getDataBuffer()).getData();

        if (startX < 0 || startX + newWidth > oldWidth || startY < 0 || startY + newHeight > oldHeight)
            Arrays.fill(newPixels, backgroundColor);

        // all coordinates are w.r.t. the new image
        int copyStartX = Math.max(0, -startX),
                copyStartY = Math.max(0, -startY);
        int copyEndX = Math.min(oldWidth - startX, newWidth),
                copyEndY = Math.min(oldHeight - startY, newHeight);
        int copyWidth = copyEndX - copyStartX;

        if (copyWidth > 0) {
            for (int y = copyStartY; y < copyEndY; y++) {
                int oldIndex = (y + startY) * oldWidth + (copyStartX + startX),
                        newIndex = y * newWidth + copyStartX;
                System.arraycopy(oldPixels, oldIndex, newPixels, newIndex, copyWidth);
            }
        }

        setBufferedImage(newImage);
    }

    public void setSizeAndClear(int newWidth, int newHeight, int backgroundColor) {
        BufferedImage image = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        int[] imagePixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        // System.arraycopy(pixels, 0, imagePixels, 0, pixels.length);
        Arrays.fill(imagePixels, backgroundColor);
        setBufferedImage(image);
    }

    public void resize(int newWidth, int newHeight) {
        if (width == newWidth && height == newHeight)
            return;

        setSize(newWidth, newHeight);
        applyEffect(Effect.RESIZE.createInstance());
    }

    private void setSize(int newWidth, int newHeight) {
        width = newWidth;
        height = newHeight;
    }

    public void rotateLeft() {
        rotate(false, true);
    }

    public void rotateRight() {
        rotate(true, false);
    }

    private void rotate(boolean invertX, boolean invertY) {
        int newWidth = height,
                newHeight = width;
        BufferedImage image = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        int[] imagePixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                int oldIndex = (invertX ? height - 1 - x : x) * width + (invertY ? width - 1 - y : y);
                imagePixels[y * newWidth + x] = pixelArray[oldIndex];
            }
        }
        setBufferedImage(image);
    }

    public void rotate180() {
        flip(true, true);
    }

    public void flipHorizontal() {
        flip(true, false);
    }

    public void flipVertical() {
        flip(false, true);
    }

    private void flip(boolean flipX, boolean flipY) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] imagePixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int oldIndex = (flipY ? height - 1 - y : y) * width + (flipX ? width - 1 - x : x);
                imagePixels[y * width + x] = pixelArray[oldIndex];
            }
        }
        setBufferedImage(image);
    }

    public int getPixel(int x, int y) {
        checkBounds(x, y);

        return pixelArray[y * width + x];
    }

    /**
     * The {@code setPixel} method does not respect the color's alpha value. The
     * pixels color is simply set to the new color.
     * 
     * @param x
     * @param y
     * @param color
     * @see Image#drawPixel
     */
    public void setPixel(int x, int y, int color) {
        checkBounds(x, y);

        pixelArray[y * width + x] = color;
        setDirty(x, y);
    }

    public void setPixels(int x, int y, int w, int h, int color) {
        checkBounds(x, y, w, h);

        if (w == width) {
            int fromIndex = y * width + x,
                    toIndex = (y + h) * width;
            Arrays.fill(pixelArray, fromIndex, toIndex, color);
        } else {
            for (int row = y; row < y + h; row++) {
                int fromIndex = row * width + x,
                        toIndex = fromIndex + w;
                Arrays.fill(pixelArray, fromIndex, toIndex, color);
            }
        }

        setDirty(x, y);
        setDirty(x + w - 1, y + h - 1);
    }

    /**
     * The {@code drawPixel} method respects the color's alpha value. That means if
     * you draw with an alpha of 0.5, the pixel's color will become a 50/50 mix of
     * the old color and the new color.
     * 
     * @see Image#setPixel
     */
    public void drawPixel(int x, int y, int color) {
        checkBounds(x, y);

        drawPixelUnsafe(x, y, color, false);

        setDirty(x, y);
    }

    private void drawPixelUnsafe(int x, int y, int color, boolean premultipliedAlpha) {
        int srcAlpha = SUtil.alpha(color);

        if (srcAlpha == 0)
            return;

        int c;
        if (srcAlpha == 255) {
            c = color;
        } else {
            int srcRed = SUtil.red(color),
                    srcGreen = SUtil.green(color),
                    srcBlue = SUtil.blue(color);

            int dst = getPixel(x, y);
            int dstRed = SUtil.red(dst),
                    dstGreen = SUtil.green(dst),
                    dstBlue = SUtil.blue(dst),
                    dstAlpha = SUtil.alpha(dst);

            double sFactor = srcAlpha / 255.0,
                    dFactor = (1 - srcAlpha / 255.0) * dstAlpha / 255.0;
            double sum = sFactor + dFactor;
            if (premultipliedAlpha)
                sFactor = 1;

            c = SUtil.toARGB(
                    (sFactor * srcRed + dFactor * dstRed) / sum,
                    (sFactor * srcGreen + dFactor * dstGreen) / sum,
                    (sFactor * srcBlue + dFactor * dstBlue) / sum,
                    255 - (255 - srcAlpha) * (255 - dstAlpha) / 255);

            // c = SUtil.toARGB(
            // ((255 - srcAlpha) * dstRed + srcAlpha * srcRed) / 255,
            // ((255 - srcAlpha) * dstGreen + srcAlpha * srcGreen) / 255,
            // ((255 - srcAlpha) * dstBlue + srcAlpha * srcBlue) / 255,
            // 255 - (255 - srcAlpha) * (255 - dstAlpha) / 255);
        }
        pixelArray[y * width + x] = c;
    }

    public void drawLine(int x0, int y0, int x1, int y1, int size, int color) {
        drawLine(x0, y0, x1, y1, size, color, false);
    }

    public void drawLine(int x0, int y0, int x1, int y1, int size, int color, boolean ignoreAlpha) {
        final int maxOffset = (size - 1) / 2;

        // https://iquilezles.org/articles/distfunctions2d/
        SVector ba = new SVector(x1 - x0, y1 - y0);
        double invBaSq = 1.0 / ba.magSq();

        double maxDistSq = ((double) size * size) / 4;
        int dx = Math.abs(x0 - x1);
        int dy = Math.abs(y0 - y1);
        if (dx > dy) {
            int minx = Math.min(x0, x1),
                    maxx = Math.max(x0, x1);
            for (int x = minx - maxOffset; x <= maxx + maxOffset; x++) {
                int py = x1 == x0 ? y0 : (int) Math.round(SUtil.map(x, x0, x1, y0, y1));
                for (int yoff = -2 * maxOffset; yoff <= 2 * maxOffset; yoff++) {
                    int y = py + yoff;
                    if (!isInside(x, y))
                        continue;
                    SVector pa = new SVector(x - x0, y - y0);
                    double h = Math.clamp(pa.dot(ba) * invBaSq, 0, 1);
                    if (Double.isFinite(h)) {
                        pa.x -= ba.x * h;
                        pa.y -= ba.y * h;
                    }
                    double distSq = pa.magSq();
                    if (distSq < maxDistSq) {
                        if (ignoreAlpha)
                            setPixel(x, y, color);
                        else
                            drawPixel(x, y, color);
                    }
                }
            }
        } else {
            int miny = Math.min(y0, y1),
                    maxy = Math.max(y0, y1);
            for (int y = miny - maxOffset; y <= maxy + maxOffset; y++) {
                int px = y1 == y0 ? x0 : (int) Math.round(SUtil.map(y, y0, y1, x0, x1));
                for (int xoff = -2 * maxOffset; xoff <= 2 * maxOffset; xoff++) {
                    int x = px + xoff;
                    if (!isInside(x, y))
                        continue;
                    SVector pa = new SVector(x - x0, y - y0);
                    double h = Math.clamp(pa.dot(ba) * invBaSq, 0, 1);
                    if (Double.isFinite(h)) {
                        pa.x -= ba.x * h;
                        pa.y -= ba.y * h;
                    }
                    double distSq = pa.magSq();
                    if (distSq < maxDistSq) {
                        if (ignoreAlpha)
                            setPixel(x, y, color);
                        else
                            drawPixel(x, y, color);
                    }
                }
            }
        }
    }

    public void magic(int x0, int y0, int color) {
        final int radius = 11;
        final int margin = 5;
        final int strokeWeight = 2;
        final int xc = x0 + radius, yc = y0 + radius;
        for (int dy = -radius - margin; dy <= margin; dy++) {
            int y = yc + dy;
            for (int dx = -radius - margin; dx <= radius + margin; dx++) {
                int x = xc + dx;
                if (!isInside(x, y))
                    continue;
                double mag = Math.sqrt(dx * dx + dy * dy);
                if (Math.abs(mag - radius) <= strokeWeight / 2)
                    setPixel(x, y, color);
            }
        }
    }

    public BufferedImage getSubImage(int x, int y, int w, int h, Integer backgroundColor) {
        checkBounds(x, y, w, h);

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] outPixels = ((DataBufferInt) out.getRaster().getDataBuffer()).getData();

        int srcStride = width,
                dstStride = w;

        if (backgroundColor == null || backgroundColor == 0) {
            for (int row = 0; row < h; row++) {
                System.arraycopy(pixelArray, (y + row) * srcStride + x, outPixels, row * dstStride, w);
            }
            return out;
        } else {
            for (int row = 0; row < h; row++) {
                for (int col = 0; col < w; col++) {
                    int c = pixelArray[(y + row) * srcStride + (x + col)];
                    outPixels[row * dstStride + col] = (c == backgroundColor) ? 0 : c;
                }
            }
            return out;
        }
    }

    /**
     * The {@code setSubImage} method does not respect the pixels' alpha values. The
     * pixels are simply copied over.
     * 
     * @see Image#drawSubImage(int, int, int, int, int[])
     */
    public void setSubImage(int x, int y, int w, int h, int[] pixels) {
        drawSubImage(x, y, w, h, pixels, false);
    }

    /**
     * The {@code drawSubImage} method respects the pixels' alpha values and dos the
     * appropriate alpha blending the the existing pixels.
     * 
     * @see Image#setSubImage(int, int, int, int, int[])
     */
    public void drawSubImage(int x, int y, int w, int h, int[] pixels) {
        drawSubImage(x, y, w, h, pixels, true);
    }

    /**
     * 
     * @param pixels are expected to have premultiplied alpha
     */
    private void drawSubImage(int x, int y, int w, int h, int[] pixels, boolean doAlphaBlending) {
        if (x >= width || x + w <= 0 || y >= height || y + h <= 0)
            return;

        int x0 = Math.max(0, x),
                y0 = Math.max(0, y),
                x1 = Math.min(x + w, width),
                y1 = Math.min(y + h, height);

        int stride = w;
        int offset = (y0 - y) * stride + (x0 - x);
        int len = x1 - x0;
        int numRows = y1 - y0;

        if (doAlphaBlending) {
            for (int row = 0; row < numRows; row++) {
                for (int col = 0; col < len; col++) {
                    int c = pixels[row * stride + offset + col];
                    drawPixelUnsafe(col + x0, row + y0, c, true);
                }
            }
        } else {
            for (int row = 0; row < numRows; row++)
                System.arraycopy(pixels, row * stride + offset, pixelArray, (y0 + row) * width + x0, len);
        }

        setDirty(x0, y0);
        setDirty(x1 - 1, y1 - 1);
    }

    public void applyEffect(EffectInstance instance) {
        applyEffect(instance, false);
    }

    public void applyEffect(EffectInstance instance, boolean preview) {
        Effect effect = instance.getEffect();
        // make sure the texture we read from (the active texture) has the right data
        syncOpenGLTexture();
        // if we only want a preview but that preview is already rendered to the
        // inactive texture, we don't need to do anything
        if (preview && currentPreviewEffect == effect)
            return;
        // initialize drawing to the inactive texture
        // fbo.initInactiveTexture(width, height);
        fbo.setTextureSize(false, width, height);
        fbo.bind(false);
        // setup rendering
        ShaderProgram shader = effect.getShader();
        RawModel quad = shader.getRawModel();
        shader.start();
        quad.bind();
        quad.enableVBOs();
        glViewport(0, 0, width, height);
        // glClearColor(0, 0, 0, 1);
        // glClear(GL_COLOR_BUFFER_BIT);
        glDisable(GL_BLEND);
        instance.loadUniforms();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, fbo.getTextureID());
        // render
        glDrawArrays(GL_TRIANGLE_STRIP, 0, quad.vertexCount());
        // clean up
        quad.disableVBOs();
        quad.unbind();
        shader.stop();
        fbo.unbind();
        // mark inactive texture either as active or as a valid preview
        if (preview) {
            currentPreviewEffect = effect;
            glBindTexture(GL_TEXTURE_2D, fbo.getInactiveTextureID());
            glGenerateMipmap(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, 0);
        } else {
            currentPreviewEffect = null;
            fbo.swapTextures();
            markOpenGLTextureDirty();
        }
    }

    public void bindFramebuffer() {
        fbo.bind();
    }

    private void checkBounds(int x, int y) {
        if (!isInside(x, y))
            throw new RuntimeException(String.format("Coordinate out of bounds: (%d, %d)", x, y));
    }

    private void checkBounds(int x, int y, int w, int h) {
        checkBounds(x, y);
        checkBounds(x + w - 1, y + h - 1);
    }

    public boolean isInside(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public BufferedImage getBufferedImage() {
        return getBufferedImage(false);
    }

    /**
     * 
     * @param copy Whether to return a copy of the underlying {@code BufferedImage}
     *             or the {@code BufferedImage} itself
     * @return
     */
    public BufferedImage getBufferedImage(boolean copy) {
        syncPixelArray();
        return copy ? copyBufferedImage(bufferedImage) : bufferedImage;
    }

    private static BufferedImage copyBufferedImage(BufferedImage oldImage) {
        int width = oldImage.getWidth(),
                height = oldImage.getHeight();
        int[] oldPixels = ((DataBufferInt) oldImage.getRaster().getDataBuffer()).getData();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] newPixels = ((DataBufferInt) newImage.getRaster().getDataBuffer()).getData();
        System.arraycopy(oldPixels, 0, newPixels, 0, oldPixels.length);
        return newImage;
    }

    @Override
    public void cleanUp() {
        fbo.cleanUp();
    }

    public int getTextureID() {
        return fbo.getTextureID();
    }

    public int getPreviewTextureID() {
        return currentPreviewEffect != null ? fbo.getInactiveTextureID() : fbo.getTextureID();
    }

    /**
     * <b>Only for debugging</b>
     * 
     * @return this image's FBO
     */
    public PingPongFBO getFBO() {
        return fbo;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
