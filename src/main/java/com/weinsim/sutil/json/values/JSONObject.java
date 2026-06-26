package com.weinsim.sutil.json.values;

import java.util.LinkedHashMap;
import java.util.Set;

public final class JSONObject extends JSONValue {

    private LinkedHashMap<String, JSONValue> value;

    public JSONObject() {
        this(new LinkedHashMap<String, JSONValue>());
    }

    public JSONObject(JSONObject other) {
        this(new LinkedHashMap<String, JSONValue>(other.value));
    }

    public JSONObject(LinkedHashMap<String, JSONValue> value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JSONObject j) {
            if (!getNames().equals(j.getNames())) {
                return false;
            }

            for (String name : getNames()) {
                if (!value.get(name).equals(j.value.get(name))) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    public Set<String> getNames() {
        return value.keySet();
    }

    public JSONValue get(String name) {
        return value.get(name);
    }

    public void put(String name, JSONValue value) {
        this.value.put(name, value == null ? new JSONNull() : value);
    }

    public void put(String name, int i) {
        value.put(name, new JSONInteger(i));
    }

    public void put(String name, double d) {
        value.put(name, new JSONFloat(d));
    }

    public void put(String name, boolean b) {
        value.put(name, new JSONBoolean(b));
    }

    public void put(String name, String s) {
        value.put(name, new JSONString(s));
    }

    public void clear() {
        value.clear();
    }

    public JSONObject getObject(String name) {
        JSONValue value = this.value.get(name);
        if (value != null && value instanceof JSONObject j) {
            return j;
        }
        return null;
    }

    public JSONArray getArray(String name) {
        JSONValue value = this.value.get(name);
        if (value != null && value instanceof JSONArray j) {
            return j;
        }
        return null;
    }

    public String getString(String name) {
        JSONValue value = this.value.get(name);
        if (value != null && value instanceof JSONString s) {
            return s.getValue();
        }
        return null;
    }

    public Integer getInteger(String name) {
        JSONValue value = this.value.get(name);
        if (value != null && value instanceof JSONInteger s) {
            return s.getValue();
        }
        return null;
    }

    public int getInteger(String name, int defaultValue) {
        Integer i = getInteger(name);
        return i != null ? i : defaultValue;
    }

    public Double getDouble(String name) {
        JSONValue value = this.value.get(name);
        if (value != null && value instanceof JSONFloat s) {
            return s.getValue();
        }
        return null;
    }

    public double getDouble(String name, double defaultValue) {
        Double d = getDouble(name);
        return d != null ? d : defaultValue;
    }

    public Boolean getBoolean(String name) {
        JSONValue value = this.value.get(name);
        if (value != null && value instanceof JSONBoolean s) {
            return s.getValue();
        }
        return null;
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        Boolean b = getBoolean(name);
        return b != null ? b : defaultValue;
    }

}
