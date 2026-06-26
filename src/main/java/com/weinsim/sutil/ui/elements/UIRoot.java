package com.weinsim.sutil.ui.elements;

import java.util.ArrayList;
import java.util.HashMap;

import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.KeyboardShortcut;

public class UIRoot extends UIContainer {

    private HashMap<String, KeyboardShortcut> keyboardShortcuts;

    private int minLayer, maxLayer;

    public UIRoot(int orientation, int alignment) {
        super(orientation, alignment);

        keyboardShortcuts = new HashMap<>();

        backgroundNormal = true;
        outlineNormal = false;
    }

    @Override
    public void updateVisibility() {
        super.updateVisibility();

        minLayer = relativeLayer;
        maxLayer = relativeLayer;

        setMinMaxLayer(this, relativeLayer);
    }

    private void setMinMaxLayer(UIContainer parent, int currentLayer) {
        for (UIElement child : parent.getChildren()) {
            int childLayer = currentLayer + child.getRelativeLayer();
            minLayer = Math.min(minLayer, childLayer);
            maxLayer = Math.max(maxLayer, childLayer);

            if (child instanceof UIContainer container)
                setMinMaxLayer(container, childLayer);
        }
    }

    public void updateMouseAbove(boolean valid) {
        int currentLayer = relativeLayer;
        // System.out.format("min = %d, max = %d\n", minLayer, maxLayer);
        for (int targetLayer = maxLayer; targetLayer >= minLayer; targetLayer--) {
            valid &= !super.updateMouseAbove(valid, true, currentLayer, targetLayer);
        }
    }

    public void updateSize() {
        setMinSize();
        setPreferredSize();
        expandAsNeccessary();
        positionChildren();
    }

    public void addKeyboardShortcut(KeyboardShortcut shortcut) {
        keyboardShortcuts.put(shortcut.getIdentifier(), shortcut);
        keyPressActions.add(shortcut);
    }

    public KeyboardShortcut getKeyboardShortcut(String identifier) {
        KeyboardShortcut shortcut = keyboardShortcuts.get(identifier);
        if (shortcut != null)
            return shortcut;
        throw new RuntimeException(String.format("Keyboard shortcut \"%s\" does not exist", identifier));
    }

    public ArrayList<UIElement> getSelectableElements() {
        UIModalDialog modalDialog = getModalDialog();
        return getSelectableElements((modalDialog != null ? modalDialog : this), new ArrayList<UIElement>());
    }

    private static ArrayList<UIElement> getSelectableElements(UIContainer parent, ArrayList<UIElement> elements) {
        for (UIElement child : parent.getChildren()) {
            if (child.isSelectable()) {
                elements.add(child);
            }
            if (child instanceof UIContainer container) {
                getSelectableElements(container, elements);
            }
        }
        return elements;
    }

    public UIModalDialog getModalDialog() {
        for (UIElement child : getChildren()) {
            if (child instanceof UIModalDialog m)
                return m;
        }
        return null;
    }

    public boolean hasModalDialog() {
        return getModalDialog() != null;
    }

    @Override
    public SVector getAbsolutePosition() {
        return new SVector();
    }

}
