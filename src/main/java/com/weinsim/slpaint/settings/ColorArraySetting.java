package com.weinsim.slpaint.settings;

import com.weinsim.slpaint.main.ColorArray;
import com.weinsim.sutil.json.values.JSONArray;
import com.weinsim.sutil.json.values.JSONObject;
import com.weinsim.sutil.json.values.JSONValue;

public final class ColorArraySetting extends Setting<ColorArray> {

    public ColorArraySetting(String identifier) {
        super(identifier);
    }

    @Override
    public JSONValue getJSONValue() {
        JSONArray array = new JSONArray();
        for (int i = 0; i < value.getCapacity(); i++) {
            Integer color = value.getColor(i);
            if (color == null) {
                break;
            }
            array.add(ColorSetting.getJSONValueFromColor(color));
        }

        JSONObject object = new JSONObject();
        object.put("capacity", value.getCapacity());
        object.put("values", array);

        return object;
    }

    @Override
    public void setJSONValue(JSONValue json) {
        if (json instanceof JSONObject object) {
            int capacity = object.getInteger("capacity", 10);
            if (value == null) {
                value = new ColorArray(capacity);
            } else {
                value.reset(capacity);
            }

            JSONArray array = object.getArray("values");
            for (int i = array.size() - 1; i >= 0; i--) {
                int color = ColorSetting.getColorFromJSONValue(array.get(i));
                this.value.addColor(color);
            }
        } else {
            handleIncorrectJSONType("JSONObject", json.getClass().getName());
        }
    }
}