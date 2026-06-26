package com.weinsim.sutil.ui.elements;

import java.util.function.BooleanSupplier;

import com.weinsim.sutil.ui.UIColors;
import com.weinsim.sutil.ui.UISizes;
import com.weinsim.sutil.ui.UIStyle;

public class UITabs extends UIContainer {

    private UIContainer selectedContent;

    private final UIContainer titles;
    private final UIContainer mainArea;

    public UITabs() {
        super(VERTICAL, LEFT);
        selectedContent = null;

        noOutline();
        zeroMargin();
        zeroPadding();
        setMinimalSize();

        add(new UIEmpty(UISizes.MARGIN::getWidthHeight));

        titles = new UIContainer(HORIZONTAL, BOTTOM);
        titles.noOutline();
        titles.setVMarginScale(0);
        add(titles);

        add(new UISeparator());

        mainArea = new UIContainer(VERTICAL, LEFT);
        mainArea.zeroMargin().noOutline();
        mainArea.setVFillSize();
        mainArea.setHFillSize();
        add(mainArea);
    }

    public void addTab(String name, UIContainer content) {
        UIButton title = new UIButton(name, () -> selectedContent = content);
        title.setStyle(new UIStyle(
                () -> selectedContent == content ? UIColors.BACKGROUND_2.get() : UIColors.BACKGROUND.get(),
                () -> UIColors.OUTLINE.get(),
                () -> title.mouseAbove() ? 2.0 : 1.0));
        // title.label.setSize(
        //         UISizes.ICON::getWidthHeight,
        //         () -> selectedContent == content ? UISizes.TEXT.get() : UISizes.TEXT_SMALL.get());
        BooleanSupplier active = () -> selectedContent == content;
        UIStyle.setSelectableButtonStyle(title, active);
        title.label.setTextSize(UISizes.TEXT_SMALL);
        titles.add(title);
        content.setVisibilitySupplier(active);
        mainArea.add(content);
        if (selectedContent == null)
            selectedContent = content;
    }

}
