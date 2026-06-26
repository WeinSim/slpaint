package com.weinsim.slpaint.settings;

import com.weinsim.sutil.json.values.JSONString;
import com.weinsim.sutil.json.values.JSONValue;

public final class StringSetting extends Setting<String> {

    public StringSetting(String identifier) {
        super(identifier);
    }

    @Override
    public JSONValue getJSONValue() {
        return new JSONString(value);
    }

    @Override
    public void setJSONValue(JSONValue json) {
        if (json instanceof JSONString s) {
            this.value = s.getValue();
        } else {
            handleIncorrectJSONType("JSONString", json.getClass().getName());
        }
    }

}
