package com.weinsim.sutil.ui;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import org.lwjglx.util.vector.Vector4f;

import com.weinsim.sutil.ui.elements.UIElement;

public class UIStyle {

    private Supplier<Vector4f> backgroundColor;
    /**
     * This is the checkerboard background that is drawn behind the background
     */
    private CheckerboardInfo backgroundCheckerboard;

    /**
     * if stroke checkerboard is active, this field goes unused
     */
    private Supplier<Vector4f> strokeColor;
    private DoubleSupplier strokeWeight;
    /**
     * This is the checkerboard that is drawn as the outline (not behind it)
     */
    private CheckerboardInfo strokeCheckerboard;

    private Supplier<UIShape> shape;

    public UIStyle(Supplier<Vector4f> backgroundColor, Supplier<Vector4f> strokeColor, DoubleSupplier strokeWeight) {
        setBackgroundColor(backgroundColor);
        setStrokeColor(strokeColor);
        setStrokeWeight(strokeWeight);

        shape = () -> UIShape.RECTANGLE;

        setNoBackgroundCheckerboard();
        setNoStrokeCheckerboard();
    }

    // getters

    public Vector4f backgroundColor() {
        return backgroundColor.get();
    }

    public boolean doBackgroundCheckerboard() {
        return backgroundCheckerboard.active().getAsBoolean();
    }

    public Vector4f backgroundCheckerboardColor1() {
        return backgroundCheckerboard.color1().get();
    }

    public Vector4f backgroundCheckerboardColor2() {
        return backgroundCheckerboard.color2().get();
    }

    public double backgroundCheckerboardSize() {
        return backgroundCheckerboard.size().getAsDouble();
    }

    public Vector4f strokeColor() {
        return strokeColor.get();
    }

    public double strokeWeight() {
        return strokeWeight.getAsDouble();
    }

    public boolean doStrokeCheckerboard() {
        return strokeCheckerboard.active().getAsBoolean();
    }

    public Vector4f strokeCheckerboardColor1() {
        return strokeCheckerboard.color1().get();
    }

    public Vector4f strokeCheckerboardColor2() {
        return strokeCheckerboard.color2().get();
    }

    public double strokeCheckerboardSize() {
        return strokeCheckerboard.size().getAsDouble();
    }

    public UIShape shape() {
        return shape.get();
    }

    // setters

    public UIStyle setBackgroundColor(Vector4f backgroundColor) {
        setBackgroundColor(() -> backgroundColor);
        return this;
    }

    public UIStyle setBackgroundColor(Supplier<Vector4f> backgroundColor) {
        this.backgroundColor = backgroundColor != null
                ? backgroundColor
                : () -> new Vector4f();
        return this;
    }

    public UIStyle setBackgroundCheckerboard(Vector4f color1, Vector4f color2, double size) {
        setBackgroundCheckerboard(() -> color1, () -> color2, () -> size);
        return this;
    }

    public UIStyle setBackgroundCheckerboard(Supplier<Vector4f> color1, Supplier<Vector4f> color2, DoubleSupplier size) {
        setBackgroundCheckerboard(() -> true, color1, color2, size);
        return this;
    }

    public UIStyle setBackgroundCheckerboard(BooleanSupplier active, Supplier<Vector4f> color1, Supplier<Vector4f> color2,
            DoubleSupplier size) {

        backgroundCheckerboard = new CheckerboardInfo(active, color1, color2, size);
        return this;
    }

    public UIStyle setNoBackgroundCheckerboard() {
        setBackgroundCheckerboard(() -> false, () -> new Vector4f(), () -> new Vector4f(), () -> 1.0);
        return this;
    }

    public UIStyle setStrokeCheckerboard(Vector4f color1, Vector4f color2, double size) {
        setStrokeCheckerboard(() -> color1, () -> color2, () -> size);
        return this;
    }

    public UIStyle setStrokeCheckerboard(Supplier<Vector4f> color1, Supplier<Vector4f> color2, DoubleSupplier size) {
        setStrokeCheckerboard(() -> true, color1, color2, size);
        return this;
    }

    public UIStyle setStrokeCheckerboard(BooleanSupplier active, Supplier<Vector4f> color1, Supplier<Vector4f> color2,
            DoubleSupplier size) {

        strokeCheckerboard = new CheckerboardInfo(active, color1, color2, size);
        return this;
    }

    public UIStyle setNoStrokeCheckerboard() {
        setStrokeCheckerboard(() -> false, () -> new Vector4f(), () -> new Vector4f(), () -> 1.0);
        return this;
    }

    public UIStyle setStrokeColor(Vector4f strokeColor) {
        setStrokeColor(() -> strokeColor);
        return this;
    }

    public UIStyle setStrokeColor(Supplier<Vector4f> strokeColor) {
        this.strokeColor = strokeColor != null
                ? strokeColor
                : () -> new Vector4f();
        return this;
    }

    public UIStyle setStrokeWeight(double strokeWeight) {
        setStrokeWeight(() -> strokeWeight);
        return this;
    }

    public UIStyle setStrokeWeight(DoubleSupplier strokeWeight) {
        this.strokeWeight = strokeWeight != null
                ? strokeWeight
                : () -> 0.0;
        return this;
    }

    public UIStyle setShape(UIShape shape) {
        setShape(() -> shape);
        return this;
    }

    public UIStyle setShape(Supplier<UIShape> shape) {
        this.shape = shape;
        return this;
    }

    public static <E extends UIElement> E setSelectableButtonStyle(E element, BooleanSupplier selectedSupplier) {
        Supplier<Vector4f> backgroundColorSupplier = () -> selectedSupplier.getAsBoolean()
                ? UIColors.BACKGROUND_SELECTED.get()
                : null;
        Supplier<Vector4f> outlineColorSupplier = () -> element.mouseAbove()
                ? UIColors.OUTLINE.get()
                : null;
        DoubleSupplier strokeWeightSupplier = UISizes.STROKE_WEIGHT;
        element.setStyle(new UIStyle(backgroundColorSupplier, outlineColorSupplier, strokeWeightSupplier));
        return element;
    }

    private record CheckerboardInfo(BooleanSupplier active, Supplier<Vector4f> color1, Supplier<Vector4f> color2,
            DoubleSupplier size) {

    }

}
