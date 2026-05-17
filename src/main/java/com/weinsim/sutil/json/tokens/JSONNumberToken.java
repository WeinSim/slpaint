package com.weinsim.sutil.json.tokens;

public final class JSONNumberToken implements JSONToken {

    private final String content;

    public JSONNumberToken(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}