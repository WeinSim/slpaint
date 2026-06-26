package com.weinsim.sutil.json.values;

public final class JSONInteger extends JSONValue {

    private int value;

    public JSONInteger(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JSONInteger i) {
            return value == i.value;
        }

        return false;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}
