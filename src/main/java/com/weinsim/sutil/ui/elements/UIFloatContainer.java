package com.weinsim.sutil.ui.elements;

import java.util.ArrayList;
import java.util.function.Supplier;

import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.UI;

public class UIFloatContainer extends UIContainer {

    public enum Anchor {

        TOP_LEFT(0, 0),
        TOP_CENTER(1, 0),
        TOP_RIGHT(2, 0),
        CENTER_LEFT(0, 1),
        CENTER_CENTER(1, 1),
        CENTER_RIGHT(2, 1),
        BOTTOM_LEFT(0, 2),
        BOTTOM_CENTER(1, 2),
        BOTTOM_RIGHT(2, 2);

        public final int dx, dy;

        private Anchor(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        public static Anchor fromOffsets(int dx, int dy) {
            for (Anchor anchor : values()) {
                if (anchor.dx == dx && anchor.dy == dy)
                    return anchor;
            }
            return null;
        }
    }

    private ArrayList<PositionSupplier> positionSuppliers;

    protected boolean clipToRoot;

    public UIFloatContainer(int orientation, int alignment) {
        super(orientation, alignment);

        withBackground();

        // (these lines are kind of unneccessary since these are already the defaults
        // for UIElement)
        relativeLayer = 0;
        ignoreParentClipArea = false;
        clipToRoot = false;

        positionSuppliers = new ArrayList<>();
    }

    public void clipToRoot(boolean clipToRoot) {
        this.clipToRoot = clipToRoot;
    }

    public UIFloatContainer clearAnchors() {
        positionSuppliers.clear();
        return this;
    }

    public UIFloatContainer addAnchor(Anchor anchor, Anchor parentAnchor) {
        positionSuppliers.add(new PositionSupplier(anchor, parentAnchor));
        return this;
    }

    public UIFloatContainer addAnchor(Anchor anchor, SVector position) {
        positionSuppliers.add(new PositionSupplier(anchor, position));
        return this;
    }

    public UIFloatContainer addAnchor(Anchor anchor, Supplier<SVector> positionSupplier) {
        positionSuppliers.add(new PositionSupplier(anchor, positionSupplier));
        return this;
    }

    public void setPosition() {
        SVector minPos = new SVector(0, 0);
        double minDistSq = Double.POSITIVE_INFINITY;

        UIRoot root = UI.getRoot();
        for (PositionSupplier positionSupplier : positionSuppliers) {
            SVector pos = positionSupplier.getPosition();

            if (!clipToRoot) {
                minPos.set(pos);
                break;
            }

            SVector parentAbsolutePos = parent.getAbsolutePosition();
            SVector absolutePos = new SVector(pos).add(parentAbsolutePos);
            absolutePos.x = Math.max(0, Math.min(root.size.x - size.x, absolutePos.x));
            absolutePos.y = Math.max(0, Math.min(root.size.y - size.y, absolutePos.y));

            double distSq = absolutePos.distSq(pos);
            if (distSq < minDistSq) {
                minDistSq = distSq;
                minPos.set(absolutePos).sub(parentAbsolutePos);
            }

            if (distSq < EPSILON)
                break;
        }

        position.set(minPos);
    }

    @Override
    public UIContainer setFillSize() {
        throw new IllegalArgumentException("A UIFloatContainer's sizeType must be either MINIMAL or FIXED");
    }

    @Override
    public UIContainer setHFillSize() {
        throw new IllegalArgumentException("A UIFloatContainer's sizeType must be either MINIMAL or FIXED");
    }

    @Override
    public UIContainer setVFillSize() {
        throw new IllegalArgumentException("A UIFloatContainer's sizeType must be either MINIMAL or FIXED");
    }

    private class PositionSupplier {

        final Anchor anchor;
        final Anchor parentAnchor;

        final SVector position;
        final Supplier<SVector> positionSupplier;

        PositionSupplier(Anchor anchor, Anchor parentAnchor) {
            this.anchor = anchor;
            this.parentAnchor = parentAnchor;

            position = null;
            positionSupplier = null;
        }

        PositionSupplier(Anchor anchor, SVector position) {
            this.anchor = anchor;
            this.position = new SVector(position);

            positionSupplier = null;
            parentAnchor = null;
        }

        PositionSupplier(Anchor anchor, Supplier<SVector> positionSupplier) {
            this.anchor = anchor;
            this.positionSupplier = positionSupplier;

            position = null;
            parentAnchor = null;
        }

        SVector getPosition() {
            SVector absolutePos;
            if (parentAnchor != null) {
                SVector parentSize = parent.getSize();
                absolutePos = new SVector(
                        parentSize.x * parentAnchor.dx,
                        parentSize.y * parentAnchor.dy).scale(0.5);
            } else {
                absolutePos = position != null ? position : positionSupplier.get();
            }

            absolutePos.x -= size.x * anchor.dx * 0.5;
            absolutePos.y -= size.y * anchor.dy * 0.5;

            return absolutePos;
        }
    }
}