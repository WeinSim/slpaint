package com.weinsim.slpaint.settings;

import com.weinsim.sutil.json.values.JSONBoolean;
import com.weinsim.sutil.json.values.JSONValue;

public final class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String identifier) {
        super(identifier);
    }

    @Override
    public JSONValue getJSONValue() {
        return new JSONBoolean(value);
    }

    @Override
    public void setJSONValue(JSONValue json) {
        if (json instanceof JSONBoolean b) {
            value = b.getValue();
        } else {
            handleIncorrectJSONType("JSONBoolean", json.getClass().getName());
        }
    }

}
