package com.weinsim.sutil.json.tokens;

public enum JSONLiteralToken implements JSONToken {
    
    /**
     * <code>true</code>
     */
    TRUE,
    
    /**
     * <code>false</code>
     */
    FALSE,
    
    /**
     * <code>null</code>
     */
    NULL;

    public static JSONLiteralToken from(String s) {
        return switch (s) {
            case "true" -> TRUE;
            case "false" -> FALSE;
            case "null" -> NULL;
            default -> null;
        };
    }
}