package com.weinsim.slpaint.ui.components;

import com.weinsim.slpaint.main.ColorPicker;
import com.weinsim.slpaint.main.apps.App;
import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.UIColors;
import com.weinsim.sutil.ui.UISizes;
import com.weinsim.sutil.ui.UIStyle;
import com.weinsim.sutil.ui.elements.UIContainer;
import com.weinsim.sutil.ui.elements.UIDragContainer;
import com.weinsim.sutil.ui.elements.UIElement;
import com.weinsim.sutil.ui.elements.UIFloatContainer;

public class HueSatField extends UIDragContainer {

    /**
     * Relative to slider length
     */
    private static final double CURSOR_LINE_LENGTH = 1;
    private static final double CURSOR_CENTER_GAP = 1;
    /**
     * Relative to slider width
     */
    private static final double CURSOR_WIDTH = 2;

    private ColorPicker colorPicker;

    private double nextX;

    public HueSatField(ColorPicker colorPicker) {
        this.colorPicker = colorPicker;

        noOutline();

        add(new Cursor(colorPicker));
        setFixedSize(UISizes.HUE_SAT_FIELD.getWidthHeight());

        nextX = 0;

        handCursorAbove = true;
    }

    @Override
    protected boolean calculateMouseAbove(SVector mouse) {
        if (App.isCircularHueSatField()) {
            double x = (mouse.x - position.x) / size.x - 0.5,
                    y = (mouse.y - position.y) / size.y - 0.5;
            return x * x + y * y < 0.25;
        } else {
            return super.calculateMouseAbove(mouse);
        }
    }

    @Override
    public double getRelativeX() {
        if (App.isCircularHueSatField()) {
            double radius = App.isHSLColorSpace() ? colorPicker.getHSLSaturation() : colorPicker.getHSVSaturation();
            double angle = colorPicker.getHue() / 180 * Math.PI;
            return Math.cos(angle) * radius / 2 + 0.5;
        } else {
            return colorPicker.getHue() / 360.0;
        }
    }

    @Override
    public double getRelativeY() {
        double saturation = App.isHSLColorSpace() ? colorPicker.getHSLSaturation() : colorPicker.getHSVSaturation();
        if (App.isCircularHueSatField()) {
            double radius = saturation;
            double angle = colorPicker.getHue() / 180 * Math.PI;
            return Math.sin(angle) * radius / 2 + 0.5;
        } else {
            return 1 - saturation;
        }
    }

    @Override
    public void setRelativeX(double x) {
        if (App.isCircularHueSatField()) {
            nextX = x;
        } else {
            x = Math.clamp(x, 0, 1);
            if (App.isHSLColorSpace()) {
                colorPicker.setHSLHue(x * 360.0);
            } else {
                colorPicker.setHSVHue(x * 360.0);
            }
        }
    }

    @Override
    public void setRelativeY(double y) {
        if (App.isCircularHueSatField()) {
            y -= 0.5;
            double x = nextX - 0.5;
            double angle = Math.atan2(y, x) / Math.PI * 180;
            angle = (angle + 360) % 360;
            if (App.isHSLColorSpace()) {
                colorPicker.setHSLHue(angle);
                colorPicker.setHSLSaturation(Math.min(1, 2 * Math.sqrt(x * x + y * y)));
            } else {
                colorPicker.setHSVHue(angle);
                colorPicker.setHSVSaturation(Math.min(1, 2 * Math.sqrt(x * x + y * y)));
            }
        } else {
            y = Math.clamp(y, 0, 1);
            if (App.isHSLColorSpace()) {
                colorPicker.setHSLSaturation(1 - y);
            } else {
                colorPicker.setHSVSaturation(1 - y);
            }
        }
    }

    protected class Cursor extends UIFloatContainer {

        public Cursor(ColorPicker colorPicker) {
            super(0, 0);

            noOutline();

            addAnchor(Anchor.CENTER_CENTER, () -> new SVector(getRelativeX(), getRelativeY()).mult(parent.getSize()));

            setFixedSize(new SVector(0, 0));

            for (int i = 0; i < 4; i++) {
                add(new CursorLine(i % 2 == 0));
            }
        }

        @Override
        public void positionChildren() {
            double w = UISizes.SCALE_SLIDER_WIDTH.get();
            final double a = w * CURSOR_WIDTH / 2;
            double len = UISizes.SCALE_SLIDER_LENGTH.get();
            final double b = len * (CURSOR_LINE_LENGTH + CURSOR_CENTER_GAP / 2);
            final double c = len * CURSOR_CENTER_GAP / 2;

            int i = 0;
            for (UIElement child : getChildren()) {
                child.getPosition().set(switch (i++) {
                    case 0 -> new SVector(-a, -b);
                    case 1 -> new SVector(c, -a);
                    case 2 -> new SVector(-a, c);
                    case 3 -> new SVector(-b, -a);
                    default -> null;
                });
            }
        }
    }

    private static class CursorLine extends UIContainer {

        public CursorLine(boolean vertical) {
            super(0, 0);

            setStyle(new UIStyle(UIColors.HIGHLIGHT, () -> null, UISizes.STROKE_WEIGHT));

            double len = UISizes.SCALE_SLIDER_LENGTH.get() * CURSOR_LINE_LENGTH;
            double w = UISizes.SCALE_SLIDER_WIDTH.get() * CURSOR_WIDTH;

            if (vertical) {
                setFixedSize(new SVector(w, len));
            } else {
                setFixedSize(new SVector(len, w));
            }
        }
    }

}
