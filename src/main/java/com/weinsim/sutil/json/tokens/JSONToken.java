package com.weinsim.sutil.json.tokens;

public sealed interface JSONToken permits JSONLiteralToken, JSONStructuralToken, JSONStringToken, JSONNumberToken {

    // private final JSONTokenType type;
    // private final String content;

    // public JSONToken(JSONTokenType type, String content) {
    //     this.type = type;
    //     this.content = content;
    // }

    // public JSONTokenType getType() {
    //     return type;
    // }

    // public String getContent() {
    //     return content;
    // }
}
