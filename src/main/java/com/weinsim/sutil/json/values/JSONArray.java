package com.weinsim.sutil.json.values;

import java.util.ArrayList;

public final class JSONArray extends JSONValue {

    private ArrayList<JSONValue> value;

    public JSONArray() {
        this(new ArrayList<JSONValue>());
    }

    public JSONArray(ArrayList<JSONValue> value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JSONArray arr) {
            if (size() != arr.size()) {
                return false;
            }
            for (int i = 0; i < size(); i++) {
                if (!get(i).equals(arr.get(i))) {
                    return false;
                }
            }

            return true;
        }
        return false;
    }

    public int size() {
        return value.size();
    }

    public JSONValue get(int index) {
        return value.get(index);
    }

    public void add(JSONValue value) {
        this.value.add(value == null ? new JSONNull() : value);
    }

    public void add(int i) {
        value.add(new JSONInteger(i));
    }

    public void add(double d) {
        value.add(new JSONFloat(d));
    }

    public void add(boolean b) {
        value.add(new JSONBoolean(b));
    }

    public void add(String s) {
        value.add(new JSONString(s));
    }

    public void clear() {
        value.clear();
    }

    public JSONObject getObject(int index) {
        JSONValue value = this.value.get(index);
        if (value != null && value instanceof JSONObject j) {
            return j;
        }
        return null;
    }

    public JSONArray getArray(int index) {
        JSONValue value = this.value.get(index);
        if (value != null && value instanceof JSONArray j) {
            return j;
        }
        return null;
    }

    public String getString(int index) {
        JSONValue value = this.value.get(index);
        if (value != null && value instanceof JSONString s) {
            return s.getValue();
        }
        return null;
    }

    public Integer getInteger(int index) {
        JSONValue value = this.value.get(index);
        if (value != null && value instanceof JSONInteger s) {
            return s.getValue();
        }
        return null;
    }

    public int getInteger(int index, int defaultValue) {
        Integer i = getInteger(index);
        return i != null ? i : defaultValue;
    }

    public Double getDouble(int index) {
        JSONValue value = this.value.get(index);
        if (value != null && value instanceof JSONFloat s) {
            return s.getValue();
        }
        return null;
    }

    public double getDouble(int index, double defaultValue) {
        Double d = getDouble(index);
        return d != null ? d : defaultValue;
    }

    public Boolean getBoolean(int index) {
        JSONValue value = this.value.get(index);
        if (value != null && value instanceof JSONBoolean s) {
            return s.getValue();
        }
        return null;
    }

    public boolean getBoolean(int index, boolean defaultValue) {
        Boolean b = getBoolean(index);
        return b != null ? b : defaultValue;
    }
}