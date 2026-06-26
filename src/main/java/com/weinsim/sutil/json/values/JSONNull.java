package com.weinsim.sutil.json.values;

public final class JSONNull extends JSONValue {

    public JSONNull() {
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JSONNull;
    }

}
