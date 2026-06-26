package com.weinsim.sutil.json.tokens;

public enum JSONStructuralToken implements JSONToken {

    /**
     * <code>{</code>
     */
    LEFT_BRACE,

    /**
     * <code>}</code>
     */
    RIGHT_BRACE,

    /**
     * <code>[</code>
     */
    LEFT_BRACKET,

    /**
     * <code>]</code>
     */
    RIGHT_BRACKET,

    /**
     * <code>,</code>
     */
    COMMA,

    /**
     * <code>:</code>
     */
    COLON;

    public static JSONStructuralToken from(char c) throws JSONParseException {
        return switch (c) {
            case '{' -> LEFT_BRACE;
            case '}' -> RIGHT_BRACE;
            case '[' -> LEFT_BRACKET;
            case ']' -> RIGHT_BRACKET;
            case ',' -> COMMA;
            case ':' -> COLON;
            default -> throw new JSONParseException("Invalid structural token");
        };
    }

}
