package com.weinsim.sutil.ui.elements;

import static org.lwjgl.glfw.GLFW.*;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import com.weinsim.sutil.ui.KeyboardShortcut;
import com.weinsim.sutil.ui.UI;
import com.weinsim.sutil.ui.UIColors;
import com.weinsim.sutil.ui.UIIcon;
import com.weinsim.sutil.ui.UISizes;

/*
 * Appearances of UIFloatMenu:
 * Child of UIDropdown:
 *   boolean expanded
 *   setCloseAction(expanded = !expanded)
 * Grandhild of UIMenuBar:
 *   UIFloatMenu expandedMenu
 *   setCloseAction(expandedMenu = null)
 * UIContextMenu:
 *   boolean expanded
 *     (in the float menu itself!)
 *   expandedLabel = null;
 *   close();
 */
public class UIFloatMenu extends UIFloatContainer {

    private final UIContainer contents;

    private final DoubleSupplier textSizeUpdater;

    protected CMLabel expandedLabel;

    protected Runnable closeAction;

    public UIFloatMenu(BooleanSupplier visibilitySupplier, Runnable closeAction) {
        this(visibilitySupplier, closeAction, false, UIText.SMALL);
    }

    public UIFloatMenu(BooleanSupplier visibilitySupplier, Runnable closeAction, boolean scroll) {
        this(visibilitySupplier, closeAction, scroll, UIText.SMALL);
    }

    public UIFloatMenu(BooleanSupplier visibilitySupplier, Runnable closeAction, boolean scroll,
            DoubleSupplier textSizeUpdater) {

        super(VERTICAL, LEFT);

        setVisibilitySupplier(visibilitySupplier);
        this.closeAction = closeAction;
        this.textSizeUpdater = textSizeUpdater;

        zeroMargin();
        zeroPadding();

        relativeLayer = 1;
        ignoreParentClipArea = true;
        clipToRoot = true;

        if (scroll) {
            UIContainer scrollArea = new UIContainer(VERTICAL, LEFT, TOP, UI.VERTICAL);
            scrollArea.setVFixedSize(400).zeroMargin().zeroPadding();
            contents = scrollArea;
            add(scrollArea.addScrollbars(), true);
        } else {
            contents = this;
        }

        addMousePressAction(GLFW_MOUSE_BUTTON_LEFT, false, () -> {
            // The first check is neccessary because it prevents the following situation:
            // A UIContextMenu and one of its attached float menues are open.
            // The user clicks somewhere in the parent menu. Because the mouse is not above
            // the child menu, it closes (and also causes the parent to close).
            if (!(parent instanceof UIFloatContainer) && shouldClose(this)) {
                close();
            }
        });
    }

    // returns true if this element or one of its children has mouseAbove set.
    private static boolean shouldClose(UIElement element) {
        if (element.mouseAbove)
            return false;
        if (element instanceof UIContainer container) {
            for (UIElement child : container.getChildren()) {
                if (!shouldClose(child))
                    return false;
            }
        }
        return true;
    }

    private void add(UIElement child, boolean normalAdd) {
        if (normalAdd)
            super.add(child);
        else
            contents.add(child);
    }

    @Override
    public void add(UIElement child) {
        add(child, contents == this || contents == null);
    }

    public void addLabel(String labelText, KeyboardShortcut shortcut) {
        int modifiers = shortcut.getModifiers();
        String rightText = "";
        if ((modifiers & GLFW_MOD_CONTROL) != 0)
            rightText += "Ctrl + ";
        if ((modifiers & GLFW_MOD_SHIFT) != 0)
            rightText += "Shift + ";
        if ((modifiers & GLFW_MOD_ALT) != 0)
            rightText += "Alt + ";
        int key = shortcut.getKey();
        int scancode = glfwGetKeyScancode(key);
        rightText += glfwGetKeyName(key, scancode).toUpperCase();

        addLabel(new UILabel(null, labelText, shortcut.getPossible()), rightText, shortcut::run);
    }

    public void addLabel(String text, Runnable clickAction) {
        addLabel(new UILabel(null, text), null, clickAction);
    }

    public void addLabel(String text, Runnable clickAction, BooleanSupplier active) {
        addLabel(new UILabel(null, text, active), null, clickAction);
    }

    public void addLabel(UILabel label, Runnable clickAction) {
        addLabel(label, null, clickAction);
    }

    private void addLabel(UILabel label, String rightText, Runnable clickAction) {
        CMLabel cmLabel = new CMLabel(label, rightText, null);
        cmLabel.style.setBackgroundColor(
                () -> label.isActive() && cmLabel.mouseAbove()
                        ? UIColors.BACKGROUND_HIGHLIGHT.get()
                        : null);
        if (clickAction != null)
            cmLabel.addLeftClickAction(() -> {
                if (!label.isActive())
                    return;
                clickAction.run();
                close();
            });
        add(cmLabel);
    }

    public UIFloatMenu addNestedMenu(String text) {
        return addNestedMenu(new UILabel(null, text), false);
    }

    public UIFloatMenu addNestedMenu(String text, boolean scroll) {
        return addNestedMenu(new UILabel(null, text), scroll);
    }

    public UIFloatMenu addNestedMenu(UILabel label) {
        return addNestedMenu(label, false);
    }

    public UIFloatMenu addNestedMenu(UILabel label, boolean scroll) {
        CMLabel cmLabel = new CMLabel(label, null, new UIIcon("expand_right"));
        cmLabel.style.setBackgroundColor(
                () -> cmLabel.mouseAbove() || expandedLabel == cmLabel
                        ? UIColors.BACKGROUND_HIGHLIGHT.get()
                        : null);

        UIFloatMenu menu = new UIFloatMenu(
                () -> expandedLabel == cmLabel,
                () -> {
                    expandedLabel = null;
                    close();
                },
                scroll);
        menu.addAnchor(Anchor.TOP_LEFT, Anchor.TOP_RIGHT);
        menu.addAnchor(Anchor.TOP_RIGHT, Anchor.TOP_LEFT);
        menu.addAnchor(Anchor.BOTTOM_LEFT, Anchor.BOTTOM_RIGHT);
        menu.addAnchor(Anchor.BOTTOM_RIGHT, Anchor.BOTTOM_LEFT);
        cmLabel.add(menu);
        add(cmLabel);

        return menu;
    }

    public void addSeparator() {
        UIContainer container = new UIContainer(VERTICAL, 0);
        container.setVMarginScale(1.0).setHMarginScale(0.0).setHFillSize().noOutline();
        container.add(new UISeparator());
        add(container);
    }

    /**
     * @implNote This method is not guaranteed to be called whenever the menu
     *           closes. A close could e.g. also be triggered by a UIMenuBar setting
     *           {@code expandedMenu} to {@code null}.
     */
    public void close() {
        closeAction.run();
    }

    private class CMLabel extends UIContainer {

        CMLabel(UILabel label, String rightText, UIIcon rightIcon) {
            super(HORIZONTAL, LEFT, CENTER);

            noOutline();
            setHFillSize();

            label.setSize(UISizes.ICON_SMALL::getWidthHeight, UISizes.TEXT_SMALL);
            add(label);

            add(new UIContainer(0, 0).setVMarginScale(0).setHFillSize().noOutline());
            if (rightText != null)
                add(new UIText(rightText, textSizeUpdater).setColor(UIColors.TEXT_INVALID));

            if (rightIcon != null)
                add(new UIImage(rightIcon, UISizes.ICON_SMALL::getWidthHeight));
            else
                add(new UIEmpty(UISizes.ICON_SMALL::getWidthHeight));
        }

        @Override
        public void update() {
            super.update();

            if (mouseAbove())
                expandedLabel = this;
        }
    }
}