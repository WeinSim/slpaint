package com.weinsim.sutil.json.values;

public final class JSONString extends JSONValue {

    private String value;

    public JSONString(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JSONString s) {
            return value.equals(s.value);
        }

        return false;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
