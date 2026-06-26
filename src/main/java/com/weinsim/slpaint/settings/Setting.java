package com.weinsim.slpaint.settings;

import com.weinsim.sutil.json.values.JSONValue;

public abstract sealed class Setting<T> permits BooleanSetting, StringSetting, ColorSetting, ColorArraySetting {

    protected String identifier;
    protected T value;

    public Setting(String identifier) {
        this.identifier = identifier;
        Settings.addSetting(identifier, this);
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    public abstract JSONValue getJSONValue();

    public abstract void setJSONValue(JSONValue json);

    protected static void handleIncorrectJSONType(String expected, String actual) {
        String message = String.format(
                "Incorrect JSON datatype! Expected: %s, actual: %s",
                expected, actual);
        throw new RuntimeException(message);
    }

}
