package com.weinsim.slpaint.renderengine;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL30.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import org.lwjglx.util.vector.Matrix3f;
import org.lwjglx.util.vector.Vector3f;

import com.weinsim.slpaint.main.apps.App;
import com.weinsim.slpaint.main.image.Image;
import com.weinsim.slpaint.renderengine.drawcalls.ClipAreaInfo;
import com.weinsim.slpaint.renderengine.drawcalls.DrawCall;
import com.weinsim.slpaint.renderengine.drawcalls.EllipseDrawCall;
import com.weinsim.slpaint.renderengine.drawcalls.HSLDrawCall;
import com.weinsim.slpaint.renderengine.drawcalls.ImageDrawCall;
import com.weinsim.slpaint.renderengine.drawcalls.RectFillDrawCall;
import com.weinsim.slpaint.renderengine.drawcalls.RectOutlineDrawCall;
import com.weinsim.slpaint.renderengine.drawcalls.TextDrawCall;
import com.weinsim.slpaint.renderengine.font.TextFont;
import com.weinsim.slpaint.renderengine.renderers.EllipseRenderer;
import com.weinsim.slpaint.renderengine.renderers.HSLRenderer;
import com.weinsim.slpaint.renderengine.renderers.ImageRenderer;
import com.weinsim.slpaint.renderengine.renderers.RectFillRenderer;
import com.weinsim.slpaint.renderengine.renderers.RectOutlineRenderer;
import com.weinsim.slpaint.renderengine.renderers.ShapeRenderer;
import com.weinsim.slpaint.renderengine.renderers.TextRenderer;
import com.weinsim.sutil.SUtil;
import com.weinsim.sutil.color.Color;
import com.weinsim.sutil.color.SRGBFloat;
import com.weinsim.sutil.math.SVector;

public class UIRenderMaster {

    public static final int MAX_FONT_ATLASSES = 4;
    public static final int MAX_FONT_CHARS = 256;
    public static final int MAX_TEXT_DATA = 256;

    private static final int NONE = 0, NORMAL = 1, CHECKERBOARD = 2;

    private App app;

    private ArrayList<DrawCall> drawCalls;

    private ArrayList<ShapeRenderer<?>> renderers;
    private RectFillRenderer rectFillRenderer;
    private RectOutlineRenderer rectOutlineRenderer;
    private HSLRenderer hslRenderer;
    private EllipseRenderer ellipseRenderer;
    private TextRenderer textRenderer;
    private ImageRenderer imageRenderer;

    private int[] framebufferSize;
    private boolean defaultFramebuffer;

    private Matrix3f uiMatrix;
    private LinkedList<Matrix3f> uiMatrixStack;

    private ClipAreaInfo clipAreaInfo;
    private LinkedList<ClipAreaInfo> clipAreaStack;

    private double depth;

    private Color fill;
    private int fillMode;

    private Color[] checkerboardColors;
    private double checkerboardSize;

    private Color stroke;
    private int strokeMode;
    private double strokeWeight;

    private TextFont textFont;
    private double textSize;

    public UIRenderMaster(App app) {
        this.app = app;

        renderers = new ArrayList<>();

        renderers.add(rectFillRenderer = new RectFillRenderer());
        renderers.add(rectOutlineRenderer = new RectOutlineRenderer());
        renderers.add(hslRenderer = new HSLRenderer());
        renderers.add(ellipseRenderer = new EllipseRenderer());
        renderers.add(textRenderer = new TextRenderer(TextFont.getCurrentFont()));
        renderers.add(imageRenderer = new ImageRenderer());

        uiMatrixStack = new LinkedList<>();
        clipAreaStack = new LinkedList<>();

        fill = Color.sGrey(0);
        stroke = Color.sGrey(0);

        checkerboardColors = new Color[] { Color.sGrey(0), Color.sGrey(0) };

        drawCalls = new ArrayList<>();
    }

    public void start() {
        defaultFramebuffer();

        uiMatrixStack.clear();
        uiMatrix = new Matrix3f();

        clipAreaStack.clear();
        clipAreaInfo = new ClipAreaInfo();

        depth = 0;

        checkerboardFill(new Color[] { Color.sGrey(0), Color.sGrey(0) }, 1);
        fill(Color.sRGB(0.5, 0.5, 0.5, 1.0));

        stroke(Color.sRGB(0.0, 0.0, 0.0, 1.0));
        strokeWeight(1);

        textFont = null;
        textSize = 1;
    }

    // We automatically look for alpha-conflicting shapes, i.e. pairs of shapes that
    // (partially) overlap where the shape in front uses transparency. We have to
    // make sure to first render the shape in the back.
    public void render() {
        Matrix3f viewMatrix = createViewMatrix();
        glViewport(0, 0, framebufferSize[0], framebufferSize[1]);
        // glDisable(GL_FRAMEBUFFER_SRGB);
        glEnable(GL_FRAMEBUFFER_SRGB);
        glEnable(GL_BLEND);
        glBlendFuncSeparate(
                GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, // rgb
                GL_ONE_MINUS_DST_ALPHA, GL_ONE); // alpha
        glEnable(GL_MULTISAMPLE);
        glDisable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glDepthMask(true);

        // drawcall at index LO must come before drawcall at index HI
        ArrayList<Long> orderRequirements = new ArrayList<>();
        DrawCall[] array = new DrawCall[drawCalls.size()];
        for (int i = 0; i < array.length; i++) {
            DrawCall c = drawCalls.get(i);
            array[i] = c;

            for (int j = 0; j < i; j++) {
                int conflict = DrawCall.alphaConflicts(c, array[j]);
                if (conflict == -1)
                    orderRequirements.add(SUtil.hilo(j, i));
                if (conflict == 1)
                    orderRequirements.add(SUtil.hilo(i, j));
            }
        }

        boolean[] ready = new boolean[array.length];
        boolean[] rendered = new boolean[array.length];
        boolean done = false;
        while (!done) {
            // determine all drawcalls that can (should) be handled immediately
            done = orderRequirements.isEmpty();
            Arrays.fill(ready, true);
            for (Long c : orderRequirements) {
                ready[SUtil.hi(c)] = false;
            }
            for (int i = 0; i < array.length; i++) {
                if (ready[i] && !rendered[i]) {
                    addShape(array[i]);
                    rendered[i] = true;
                }
            }
            for (int i = orderRequirements.size() - 1; i >= 0; i--) {
                if (rendered[SUtil.lo(orderRequirements.get(i))])
                    orderRequirements.remove(i);
            }

            for (ShapeRenderer<?> shapeRenderer : renderers)
                shapeRenderer.render(viewMatrix);
        }

        drawCalls.clear();
    }

    private void addShape(DrawCall d) {
        switch (d) {
            case RectFillDrawCall r -> rectFillRenderer.addShape(r);
            case RectOutlineDrawCall r -> rectOutlineRenderer.addShape(r);
            case EllipseDrawCall e -> ellipseRenderer.addShape(e);
            case HSLDrawCall h -> hslRenderer.addShape(h);
            case ImageDrawCall i -> imageRenderer.addShape(i);
            case TextDrawCall t -> textRenderer.addShape(t);
            default -> {
                String message = String.format("Unknown drawcall: %s\n", d.getClass().getName());
                throw new RuntimeException(message);
            }
        }
    }

    public void defaultFramebuffer() {
        framebuffer(null);
    }

    /**
     * Specify which framebuffer to render to. This can either be an {@code Image}'s
     * framebuffer or the default framebuffer (the window).
     * 
     * @param image the image whose framebuffer to render to, or {@code null} to
     *              render to the default framebuffer
     */
    public void framebuffer(Image image) {
        defaultFramebuffer = image == null;
        if (defaultFramebuffer) {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            framebufferSize = app.getDisplaySize();
        } else {
            image.bindFramebuffer();
            framebufferSize = new int[] { image.getWidth(), image.getHeight() };
        }
    }

    public void setBGColor(Color bgColor) {
        // glViewport(0, 0, framebufferSize[0], framebufferSize[1]);
        SRGBFloat sRGB = bgColor.sRGBFloat();
        glClearColor((float) sRGB.red(), (float) sRGB.green(), (float) sRGB.blue(), (float) sRGB.alpha());
        glClearDepth(1.0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void rect(SVector position, SVector size) {
        // this approach is a bit ugly because it creates up to 4 new SVectors
        if (size.x < 0) {
            position = new SVector(position.x + size.x, position.y);
            size = new SVector(-size.x, size.y);
        }
        if (size.y < 0) {
            position = new SVector(position.x, position.y + size.y);
            size = new SVector(size.x, -size.y);
        }

        if (fillMode > 0)
            drawCalls.add(
                    new RectFillDrawCall(position, depth, size,
                            new Matrix3f().load(uiMatrix),
                            new ClipAreaInfo(clipAreaInfo),
                            fillMode == NORMAL ? fill : checkerboardColors[0],
                            checkerboardColors[1], checkerboardSize, fillMode == CHECKERBOARD));

        if (strokeMode > 0)
            drawCalls.add(
                    new RectOutlineDrawCall(position, depth, size,
                            new Matrix3f().load(uiMatrix),
                            new ClipAreaInfo(clipAreaInfo),
                            strokeMode == NORMAL ? stroke : checkerboardColors[0], strokeWeight,
                            checkerboardColors[1], checkerboardSize, strokeMode == CHECKERBOARD));
    }

    public void ellipse(SVector position, SVector size) {
        if (fillMode != NORMAL)
            return;

        drawCalls.add(
                new EllipseDrawCall(position, depth, size, new Matrix3f().load(uiMatrix),
                        new ClipAreaInfo(clipAreaInfo), fill));
    }

    public void text(String text, SVector position) {
        if (text == null)
            return;
        if (textFont == null)
            return;
        if (fillMode != NORMAL)
            return;

        drawCalls.add(
                new TextDrawCall(position, depth, textSize / textFont.size, new Matrix3f().load(uiMatrix),
                        new ClipAreaInfo(clipAreaInfo), fill, text, textFont));
    }

    public void image(int textureID, SVector position, SVector size) {
        drawCalls.add(
                new ImageDrawCall(position, depth, size, new Matrix3f().load(uiMatrix),
                        new ClipAreaInfo(clipAreaInfo), textureID));
    }

    public void hueSatField(SVector position, SVector size, boolean circular, boolean hsl) {
        int flags = (circular ? HSLDrawCall.HUE_SAT_FIELD_CIRC : HSLDrawCall.HUE_SAT_FIELD_RECT)
                | (hsl ? HSLDrawCall.HSL : HSLDrawCall.HSV);
        drawCalls.add(
                new HSLDrawCall(position, depth, size, new Matrix3f().load(uiMatrix),
                        new ClipAreaInfo(clipAreaInfo), new SVector(), flags));
    }

    public void lightnessScale(SVector position, SVector size, double hue, double saturation, boolean vertical,
            boolean hsl) {

        int flags = HSLDrawCall.LIGHTNESS_SCALE
                | (hsl ? HSLDrawCall.HSL : HSLDrawCall.HSV)
                | (vertical ? HSLDrawCall.VERTICAL : HSLDrawCall.HORIZONTAL);
        drawCalls.add(
                new HSLDrawCall(position, depth, size, new Matrix3f().load(uiMatrix),
                        new ClipAreaInfo(clipAreaInfo), new SVector(hue, saturation, 0), flags));
    }

    public void alphaScale(SVector position, SVector size, boolean vertical) {
        int flags = HSLDrawCall.ALPHA_SCALE
                | (vertical ? HSLDrawCall.VERTICAL : HSLDrawCall.HORIZONTAL);
        hslRenderer.addShape(new HSLDrawCall(position, depth, size, new Matrix3f().load(uiMatrix),
                new ClipAreaInfo(clipAreaInfo), fill.sRGBFloat().toSVector(), flags));
    }

    public void clipArea(SVector position, SVector size) {
        clipArea(position, size, true);
    }

    public void clipArea(SVector position, SVector size, boolean clipToPrevClipArea) {
        Vector3f pos3f = new Vector3f((float) position.x, (float) position.y, 1);
        Matrix3f.transform(uiMatrix, pos3f, pos3f);

        Vector3f size3f = new Vector3f((float) size.x, (float) size.y, 0);
        Matrix3f.transform(uiMatrix, size3f, size3f);

        double x = pos3f.x, y = pos3f.y,
                w = size3f.x, h = size3f.y;

        // no need to clip if the previous clip area was disabled
        if (clipToPrevClipArea && clipAreaInfo.isEnabled()) {
            double x0 = clipAreaInfo.getPosition().x,
                    y0 = clipAreaInfo.getPosition().y,
                    w0 = clipAreaInfo.getSize().x,
                    h0 = clipAreaInfo.getSize().y;

            // left
            if (x < x0) {
                w -= x0 - x;
                x = x0;
            }
            // right
            if (x + w > x0 + w0) {
                w = x0 + w0 - x;
            }
            // top
            if (y < y0) {
                h -= y0 - y;
                y = y0;
            }
            // bottom
            if (y + h > y0 + h0) {
                h = y0 + h0 - y;
            }
        }

        clipAreaInfo.set(new SVector(x, y), new SVector(w, h));
    }

    public void noClipArea() {
        clipAreaInfo.clear();
    }

    public void pushClipArea() {
        clipAreaStack.add(new ClipAreaInfo(clipAreaInfo));
    }

    public void popClipArea() {
        clipAreaInfo = clipAreaStack.removeLast();
    }

    public void depth(double depth) {
        this.depth = depth;
    }

    public void translate(SVector translation) {
        Matrix3f matrix = new Matrix3f();
        matrix.m20 = (float) translation.x;
        matrix.m21 = (float) translation.y;
        Matrix3f.mul(uiMatrix, matrix, uiMatrix);
    }

    public void scale(SVector scale) {
        Matrix3f matrix = new Matrix3f();
        matrix.m00 = (float) scale.x;
        matrix.m11 = (float) scale.y;
        Matrix3f.mul(uiMatrix, matrix, uiMatrix);
    }

    public void scale(double s) {
        Matrix3f matrix = new Matrix3f();
        matrix.m00 = (float) s;
        matrix.m11 = (float) s;
        Matrix3f.mul(uiMatrix, matrix, uiMatrix);
    }

    public void rotate(double angle) {
        Matrix3f matrix = new Matrix3f();
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        matrix.m00 = cos;
        matrix.m01 = -sin;
        matrix.m10 = sin;
        matrix.m11 = cos;
        Matrix3f.mul(uiMatrix, matrix, uiMatrix);
    }

    public void resetMatrix() {
        uiMatrix.setIdentity();
    }

    public void pushMatrix() {
        Matrix3f matrix = new Matrix3f();
        matrix.m00 = uiMatrix.m00;
        matrix.m10 = uiMatrix.m10;
        matrix.m20 = uiMatrix.m20;
        matrix.m01 = uiMatrix.m01;
        matrix.m11 = uiMatrix.m11;
        matrix.m21 = uiMatrix.m21;
        matrix.m02 = uiMatrix.m02;
        matrix.m12 = uiMatrix.m12;
        matrix.m22 = uiMatrix.m22;
        uiMatrixStack.push(matrix);
    }

    public void popMatrix() {
        uiMatrix = uiMatrixStack.pop();
    }

    public void fill(Color fill) {
        this.fill = fill;
        fillMode = NORMAL;
    }

    public void noFill() {
        fillMode = NONE;
    }

    public void checkerboardFill(Color[] colors, double size) {
        fillMode = CHECKERBOARD;
        checkerboardColors[0] = colors[0];
        checkerboardColors[1] = colors[1];
        checkerboardSize = size;
    }

    public void stroke(Color stroke) {
        this.stroke = stroke;
        strokeMode = NORMAL;
    }

    public void noStroke() {
        strokeMode = NONE;
    }

    public void checkerboardStroke(Color[] colors, double size) {
        strokeMode = CHECKERBOARD;
        checkerboardColors[0] = colors[0];
        checkerboardColors[1] = colors[1];
        checkerboardSize = size;
    }

    public void strokeWeight(double strokeWeight) {
        this.strokeWeight = strokeWeight;
    }

    public void textFont(TextFont font) {
        textFont = font;
        textSize = font.size;
    }

    public void textSize(double textSize) {
        this.textSize = textSize;
    }

    private Matrix3f createViewMatrix() {
        Matrix3f matrix = new Matrix3f();
        int width = framebufferSize[0],
                height = framebufferSize[1];
        float sign = defaultFramebuffer ? -1 : 1;
        matrix.m00 = 2f / width;
        matrix.m11 = sign * 2f / height;
        matrix.m20 = -1;
        matrix.m21 = -sign;
        return matrix;
    }

    public void cleanUp() {
        for (ShapeRenderer<?> renderer : renderers)
            renderer.cleanUp();
    }

}
