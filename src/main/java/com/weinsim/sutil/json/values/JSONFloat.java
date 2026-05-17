package com.weinsim.sutil.json.values;

public final class JSONFloat extends JSONValue {

    private double value;

    public JSONFloat(double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JSONFloat f) {
            return value == f.value;
        }
        return false;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}