package com.weinsim.sutil.ui;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import com.weinsim.sutil.SUtil;
import com.weinsim.sutil.color.Color;
import com.weinsim.sutil.ui.elements.UIElement;

public class UIStyle {

    private Supplier<Color> backgroundColor;
    /**
     * This is the checkerboard background that is drawn behind the background
     */
    private CheckerboardInfo backgroundCheckerboard;

    /**
     * if stroke checkerboard is active, this field goes unused
     */
    private Supplier<Color> strokeColor;
    private DoubleSupplier strokeWeight;
    /**
     * This is the checkerboard that is drawn as the outline (not behind it)
     */
    private CheckerboardInfo strokeCheckerboard;

    private Supplier<UIShape> shape;

    public UIStyle(Supplier<Color> backgroundColor, Supplier<Color> strokeColor, DoubleSupplier strokeWeight) {
        setBackgroundColor(backgroundColor);
        setStrokeColor(strokeColor);
        setStrokeWeight(strokeWeight);
        shape = () -> UIShape.RECTANGLE;
        setNoBackgroundCheckerboard();
        setNoStrokeCheckerboard();
    }

    // getters

    public Color backgroundColor() {
        return backgroundColor.get();
    }

    public boolean doBackgroundCheckerboard() {
        return backgroundCheckerboard.active().getAsBoolean();
    }

    public Color backgroundCheckerboardColor1() {
        return backgroundCheckerboard.color1().get();
    }

    public Color backgroundCheckerboardColor2() {
        return backgroundCheckerboard.color2().get();
    }

    public double backgroundCheckerboardSize() {
        return backgroundCheckerboard.size().getAsDouble();
    }

    public Color strokeColor() {
        return strokeColor.get();
    }

    public double strokeWeight() {
        return strokeWeight.getAsDouble();
    }

    public boolean doStrokeCheckerboard() {
        return strokeCheckerboard.active().getAsBoolean();
    }

    public Color strokeCheckerboardColor1() {
        return strokeCheckerboard.color1().get();
    }

    public Color strokeCheckerboardColor2() {
        return strokeCheckerboard.color2().get();
    }

    public double strokeCheckerboardSize() {
        return strokeCheckerboard.size().getAsDouble();
    }

    public UIShape shape() {
        return shape.get();
    }

    // setters

    public UIStyle setBackgroundColor(Color backgroundColor) {
        setBackgroundColor(() -> backgroundColor);
        return this;
    }

    public UIStyle setBackgroundColor(Supplier<Color> backgroundColor) {
        this.backgroundColor = backgroundColor != null
                ? backgroundColor
                : () -> Color.sGrey(0);
        return this;
    }

    public UIStyle setBackgroundCheckerboard(Color color1, Color color2, double size) {
        setBackgroundCheckerboard(() -> color1, () -> color2, () -> size);
        return this;
    }

    public UIStyle setBackgroundCheckerboard(Supplier<Color> color1, Supplier<Color> color2, DoubleSupplier size) {
        setBackgroundCheckerboard(() -> true, color1, color2, size);
        return this;
    }

    public UIStyle setBackgroundCheckerboard(BooleanSupplier active, Supplier<Color> color1, Supplier<Color> color2,
            DoubleSupplier size) {

        backgroundCheckerboard = new CheckerboardInfo(active, color1, color2, size);
        return this;
    }

    public UIStyle setNoBackgroundCheckerboard() {
        setBackgroundCheckerboard(() -> false, () -> Color.sGrey(0), () -> Color.sGrey(0), () -> 1.0);
        return this;
    }

    public UIStyle setStrokeCheckerboard(Color color1, Color color2, double size) {
        setStrokeCheckerboard(() -> color1, () -> color2, () -> size);
        return this;
    }

    public UIStyle setStrokeCheckerboard(Supplier<Color> color1, Supplier<Color> color2, DoubleSupplier size) {
        setStrokeCheckerboard(() -> true, color1, color2, size);
        return this;
    }

    public UIStyle setStrokeCheckerboard(BooleanSupplier active, Supplier<Color> color1, Supplier<Color> color2,
            DoubleSupplier size) {

        strokeCheckerboard = new CheckerboardInfo(active, color1, color2, size);
        return this;
    }

    public UIStyle setNoStrokeCheckerboard() {
        setStrokeCheckerboard(() -> false, () -> Color.sGrey(0), () -> Color.sGrey(0), () -> 1.0);
        return this;
    }

    public UIStyle setStrokeColor(Color strokeColor) {
        setStrokeColor(() -> strokeColor);
        return this;
    }

    public UIStyle setStrokeColor(Supplier<Color> strokeColor) {
        this.strokeColor = strokeColor != null
                ? strokeColor
                : () -> Color.sGrey(0);
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
        Supplier<Color> backgroundColorSupplier = SUtil.ifThenElse(
                selectedSupplier,
                UIColors.BACKGROUND_SELECTED,
                () -> null);
        Supplier<Color> outlineColorSupplier = SUtil.ifThenElse(
                element::mouseAbove,
                UIColors.OUTLINE,
                () -> null);
        DoubleSupplier strokeWeightSupplier = UISizes.STROKE_WEIGHT;
        element.setStyle(new UIStyle(backgroundColorSupplier, outlineColorSupplier, strokeWeightSupplier));
        return element;
    }

    private record CheckerboardInfo(BooleanSupplier active, Supplier<Color> color1, Supplier<Color> color2,
            DoubleSupplier size) {

    }

}
