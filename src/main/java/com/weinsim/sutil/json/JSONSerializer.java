package com.weinsim.sutil.json;

import java.util.Set;

import com.weinsim.sutil.json.values.JSONArray;
import com.weinsim.sutil.json.values.JSONBoolean;
import com.weinsim.sutil.json.values.JSONFloat;
import com.weinsim.sutil.json.values.JSONInteger;
import com.weinsim.sutil.json.values.JSONNull;
import com.weinsim.sutil.json.values.JSONObject;
import com.weinsim.sutil.json.values.JSONString;
import com.weinsim.sutil.json.values.JSONValue;

public class JSONSerializer {

    private static final String TAB_SEQUENCE = "    ";
    private static final String END_OF_LINE = System.getProperty("line.separator");

    private static int indentationLevel = 0;
    private static StringBuilder builder;

    private JSONSerializer() {

    }

    public static String serialize(JSONValue value) {
        indentationLevel = 0;
        builder = new StringBuilder();

        serializeValue(value);

        return builder.toString();
    }

    private static void serializeValue(JSONValue value) {
        if (value == null) {
            serializeNull();
            return;
        }
        switch (value) {
            case JSONObject obj -> serializeObject(obj);
            case JSONArray arr -> serializeArray(arr);
            case JSONString str -> serializeString(str);
            case JSONInteger i -> serializeInteger(i);
            case JSONFloat f -> serializeFloat(f);
            case JSONBoolean b -> serializeBoolean(b);
            case JSONNull _ -> serializeNull();
        }
    }

    private static void serializeObject(JSONObject obj) {
        builder.append("{");

        Set<String> names = obj.getNames();
        int size = names.size();
        if (size == 0) {
            builder.append("}");
            return;
        }

        indentationLevel++;
        newLine();

        int count = 0;
        for (String name : names) {
            serializeString(new JSONString(name));
            builder.append(" : ");
            serializeValue(obj.get(name));

            count++;
            if (count < size) {
                builder.append(',');
                newLine();
            }
        }

        indentationLevel--;
        newLine();
        builder.append("}");
    }

    private static void serializeArray(JSONArray arr) {
        builder.append("[");

        int size = arr.size();
        if (size == 0) {
            builder.append("]");
            return;
        }

        indentationLevel++;
        newLine();

        for (int i = 0; i < size; i++) {
            serializeValue(arr.get(i));

            if (i + 1 < size) {
                builder.append(',');
                newLine();
            }
        }

        indentationLevel--;
        newLine();
        builder.append("]");
    }

    private static void serializeString(JSONString str) {
        builder.append('"');
        char[] chars = str.getValue().toCharArray();
        for (char c : chars) {
            if (c < 0x20) {
                Character escape = switch (c) {
                    case '\b' -> 'b';
                    case '\f' -> 'f';
                    case '\n' -> 'n';
                    case '\r' -> 'r';
                    case '\t' -> 't';
                    default -> null;
                };
                if (escape != null) {
                    builder.append('\\');
                    builder.append(escape);
                } else {
                    builder.append("\\u");
                    builder.append(String.format("%04x", (int) c));
                }
            } else if (c == '"' || c == '\\') {
                builder.append('\\');
                builder.append(c);
            } else {
                builder.append(c);
            }
        }
        builder.append('"');
    }

    private static void serializeFloat(JSONFloat f) {
        double d = f.getValue();
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            serializeNull();
            return;
        }
        builder.append(Double.toString(d));
    }

    private static void serializeInteger(JSONInteger i) {
        builder.append(Integer.toString(i.getValue()));
    }

    private static void serializeBoolean(JSONBoolean b) {
        builder.append(Boolean.toString(b.getValue()));
    }

    private static void serializeNull() {
        builder.append("null");
    }

    private static void newLine() {
        builder.append(END_OF_LINE);
        for (int i = 0; i < indentationLevel; i++) {
            builder.append(TAB_SEQUENCE);
        }
    }

}
