package com.weinsim.slpaint.settings;

import com.weinsim.sutil.color.Color;
import com.weinsim.sutil.color.SRGBInt;
import com.weinsim.sutil.json.values.JSONArray;
import com.weinsim.sutil.json.values.JSONValue;

public final class ColorSetting extends Setting<Color> {

    public ColorSetting(String identifier) {
        super(identifier);
    }

    @Override
    public JSONValue getJSONValue() {
        return getJSONValueFromColor(value);
    }

    @Override
    public void setJSONValue(JSONValue json) {
        value = getColorFromJSONValue(json);
    }

    public static JSONArray getJSONValueFromColor(Color color) {
        JSONArray array = new JSONArray();
        SRGBInt sRGB = color.sRGBInt();
        array.add(sRGB.red());
        array.add(sRGB.green());
        array.add(sRGB.blue());
        return array;
    }

    public static Color getColorFromJSONValue(JSONValue json) {
        if (json instanceof JSONArray array) {
            int r = array.getInteger(0, 0);
            int g = array.getInteger(1, 0);
            int b = array.getInteger(2, 0);
            return Color.sRGB(r, g, b);
        } else {
            handleIncorrectJSONType("JSONArray", json.getClass().getName());
            return Color.sGrey(0);
        }
    }

}
