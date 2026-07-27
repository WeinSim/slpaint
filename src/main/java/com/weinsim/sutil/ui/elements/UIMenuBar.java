package com.weinsim.sutil.ui.elements;

import com.weinsim.sutil.ui.UI;
import com.weinsim.sutil.ui.UIColors;
import com.weinsim.sutil.ui.elements.UIFloatContainer.Anchor;

public class UIMenuBar extends UIContainer {

    private MenuLabel expandedMenu;

    public UIMenuBar() {
        super(UI.HORIZONTAL, UI.LEFT, UI.CENTER);

        noOutline();
        setMarginScale(0.5);
        setPaddingScale(0.5);
        setHFillSize();

        // TODO: this is just a hack to prevent parts of the image canvas (e.g. size
        // knobs) to appear above the menu.
        relativeLayer = 4;

        expandedMenu = null;
    }

    public UIFloatMenu addMenu(String name) {
        MenuLabel menuLabel = new MenuLabel(name);
        add(menuLabel);
        return menuLabel.menu;
    }

    private class MenuLabel extends UIContainer {

        final UIFloatMenu menu;

        MenuLabel(String text) {
            super(UI.VERTICAL, UI.CENTER);

            this.menu = new UIFloatMenu(() -> expandedMenu == this, () -> expandedMenu = null);
            menu.addAnchor(Anchor.TOP_LEFT, Anchor.BOTTOM_LEFT);

            noOutline();
            setHMarginScale(1.5);

            style.setBackgroundColor(
                    () -> (mouseAbove || expandedMenu == this)
                            ? UIColors.BACKGROUND_HIGHLIGHT.get()
                            : null);

            addLeftClickAction(() -> {
                if (expandedMenu == null) {
                    expandedMenu = this;
                } else {
                    expandedMenu = null;
                }
            });

            add(new UIText(text, UIText.SMALL));
            add(menu);
        }

        @Override
        public void handleEvents() {
            super.handleEvents();
            if (mouseAbove && expandedMenu != null)
                expandedMenu = this;
        }

    }

}
