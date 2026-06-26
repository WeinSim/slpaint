package com.weinsim.sutil.json.values;

public final class JSONBoolean extends JSONValue {

    private boolean value;

    public JSONBoolean(boolean value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JSONBoolean b) {
            return value == b.value;
        }

        return false;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

}
