package com.weinsim.sutil.json.tokens;

public final class JSONStringToken implements JSONToken {

    private final String content;

    public JSONStringToken(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

}
