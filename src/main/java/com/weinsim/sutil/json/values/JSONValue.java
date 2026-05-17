package com.weinsim.sutil.json.values;

public sealed abstract class JSONValue permits JSONObject, JSONArray, JSONString, JSONInteger, JSONFloat, JSONBoolean, JSONNull {

}