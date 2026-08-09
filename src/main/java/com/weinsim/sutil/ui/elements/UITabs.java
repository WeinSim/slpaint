package com.weinsim.sutil.ui.elements;

import java.util.HashMap;
import java.util.function.BooleanSupplier;

import com.weinsim.sutil.ui.UISizes;
import com.weinsim.sutil.ui.UIStyle;

public class UITabs extends UIContainer {

    private UIContainer selectedContent;
    private HashMap<String, UIContainer> tabs;

    private final UIContainer titles;
    private final UIContainer mainArea;

    public UITabs() {
        super(VERTICAL, LEFT);
        selectedContent = null;
        tabs = new HashMap<>();

        zeroPadding();

        titles = new UIContainer(HORIZONTAL, BOTTOM);
        titles.withMargin();
        titles.setBottomMarginScale(0);
        add(titles);

        addSeparator();

        mainArea = new UIContainer(VERTICAL, LEFT);
        mainArea.setVFillSize();
        mainArea.setHFillSize();
        add(mainArea);
    }

    public void addTab(String name, UIContainer content) {
        UIButton title = new UIButton(name, () -> selectedContent = content);
        BooleanSupplier active = () -> selectedContent == content;
        UIStyle.setSelectableButtonStyle(title, active);
        // title.setMarginScale(0.5);
        title.label.setTextSize(UISizes.TEXT_SMALL);
        titles.add(title);
        content.setVisibilitySupplier(active);
        mainArea.add(content);
        tabs.put(name, content);
        if (selectedContent == null)
            selectedContent = content;
    }

    public void selectTab(String name) {
        UIContainer tab = tabs.get(name);
        if (tab != null)
            selectedContent = tab;
    }

}
