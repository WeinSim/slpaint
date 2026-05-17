package com.weinsim.slpaint.settings;

import com.weinsim.slpaint.main.ColorPicker;
import com.weinsim.sutil.SUtil;
import com.weinsim.sutil.json.values.JSONArray;
import com.weinsim.sutil.json.values.JSONValue;

public final class ColorSetting extends Setting<ColorPicker> {

    public ColorSetting(String identifier) {
        super(identifier);
    }

    @Override
    public JSONValue getJSONValue() {
        return getJSONValueFromColor(value.getRGB());
    }

    @Override
    public void setJSONValue(JSONValue json) {
        if (value == null) {
            value = new ColorPicker(getColorFromJSONValue(json));
        } else {
            value.setRGB(getColorFromJSONValue(json));
        }
    }

    public static JSONArray getJSONValueFromColor(int color) {
        JSONArray array = new JSONArray();
        array.add(SUtil.red(color));
        array.add(SUtil.green(color));
        array.add(SUtil.blue(color));
        return array;
    }

    public static int getColorFromJSONValue(JSONValue json) {
        if (json instanceof JSONArray array) {
            int r = array.getInteger(0, 0);
            int g = array.getInteger(1, 0);
            int b = array.getInteger(2, 0);
            return SUtil.toARGB(r, g, b);
        } else {
            handleIncorrectJSONType("JSONArray", json.getClass().getName());
            return 0;
        }
    }
}