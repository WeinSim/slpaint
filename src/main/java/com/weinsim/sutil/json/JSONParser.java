package com.weinsim.sutil.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.weinsim.sutil.json.tokens.JSONLiteralToken;
import com.weinsim.sutil.json.tokens.JSONNumberToken;
import com.weinsim.sutil.json.tokens.JSONParseException;
import com.weinsim.sutil.json.tokens.JSONStringToken;
import com.weinsim.sutil.json.tokens.JSONStructuralToken;
import com.weinsim.sutil.json.tokens.JSONToken;
import com.weinsim.sutil.json.values.JSONArray;
import com.weinsim.sutil.json.values.JSONBoolean;
import com.weinsim.sutil.json.values.JSONFloat;
import com.weinsim.sutil.json.values.JSONInteger;
import com.weinsim.sutil.json.values.JSONNull;
import com.weinsim.sutil.json.values.JSONObject;
import com.weinsim.sutil.json.values.JSONString;
import com.weinsim.sutil.json.values.JSONValue;

public class JSONParser {

    private JSONParser() {
    }

    public static JSONObject parseObject(String source) throws JSONParseException {
        JSONValue value = parseValue(source);

        if (value instanceof JSONObject obj) {
            return obj;
        }

        throw new JSONParseException("The parsed value is not of type JSONObject.");
    }

    public static JSONValue parseValue(String source) throws JSONParseException {
        ArrayList<JSONToken> tokens = tokenize(source);

        JSONValue val = parseTokens(tokens);

        return val;
    }

    private static ArrayList<JSONToken> tokenize(String source) throws JSONParseException {
        Tokenizer tokenizer = new Tokenizer(source);
        return tokenizer.tokenize();
    }

    private static JSONValue parseTokens(ArrayList<JSONToken> tokens) throws JSONParseException {
        Parser parser = new Parser(tokens);

        return parser.parseValue(true);
    }

    private static class Tokenizer {

        final char[] source;
        final int len;
        int offset;

        public Tokenizer(String source) {
            this.source = source.toCharArray();
            len = this.source.length;
            offset = 0;
        }

        /**
         * Structural: single character { } [ ] , :
         * Literal: fixed strings ture false null
         * Number: starts with - or digit 0 - 9
         * String: starts with "
         * 
         * @return
         */
        ArrayList<JSONToken> tokenize() throws JSONParseException {
            ArrayList<JSONToken> tokens = new ArrayList<>();

            while (offset < len) {
                advance();

                if (offset >= len) {
                    break;
                }

                switch (source[offset]) {
                    // check for structural tokens
                    case '{', '}', '[', ']', ',', ':' -> {
                        tokens.add(JSONStructuralToken.from(source[offset]));
                        offset++;
                        continue;
                    }
                    // check for literal name tokens
                    case 't', 'f', 'n' -> {
                        int literalLength = source[offset] == 'f' ? 5 : 4;
                        String expected = source[offset] == 't' ? "true"
                                : source[offset] == 'f' ? "false"
                                        : "null";
                        if (offset >= len - literalLength) {
                            throw new JSONParseException("Invalid literal name");
                        }
                        for (int i = 0; i < literalLength; i++) {
                            if (source[offset + i] != expected.charAt(i)) {
                                throw new JSONParseException("Invalid literal name");
                            }
                        }
                        offset += literalLength;
                        tokens.add(JSONLiteralToken.from(expected));
                        continue;
                    }
                }

                // string token
                if (source[offset] == '"') {
                    String content = "";
                    offset++;
                    while (offset < len && source[offset] != '"') {
                        if (source[offset] <= 0x1F) {
                            throw new JSONParseException("Invalid character inside of string literal");
                        }
                        if (source[offset] == '\\') {
                            // escape sequence
                            offset++;
                            if (offset >= len) {
                                throw new JSONParseException("Unclosed string literal");
                            }
                            Character nextChar = switch (source[offset]) {
                                case '"', '\\', '/' -> source[offset];
                                case 'b' -> '\b';
                                case 'f' -> '\f';
                                case 'n' -> '\n';
                                case 'r' -> '\r';
                                case 't' -> '\t';
                                default -> null;
                            };
                            if (nextChar != null) {
                                offset++;
                                content += nextChar;
                                continue;
                            }

                            if (source[offset] == 'u') {
                                offset++;
                                char codepoint = 0;
                                for (int i = 0; i < 4; i++) {
                                    if (offset >= len) {
                                        throw new JSONParseException("Unclosed string literal");
                                    }

                                    codepoint *= 16;
                                    char digit = source[offset];
                                    if (digit <= 0x39) {
                                        // digit
                                        if (digit < 0x30) {
                                            throw new JSONParseException("Invalid unicode escape sequence");
                                        }
                                        codepoint += (char) (digit - 0x30);
                                    } else if (digit <= 0x46) {
                                        // uppercase letter
                                        if (digit < 0x41) {
                                            throw new JSONParseException("Invalid unicode escape sequence");
                                        }
                                        codepoint += (char) (digit - 0x37);
                                    } else {
                                        if (digit < 0x61 || digit > 0x66) {
                                            throw new JSONParseException("Invalid unicode escape sequence");
                                        }
                                        codepoint += (char) (digit - 57);
                                    }
                                    offset++;
                                }

                                content += codepoint;
                            } else {
                                throw new JSONParseException("Invalid escape sequence");
                            }
                            continue;
                        }

                        content += source[offset];
                        offset++;
                    }
                    offset++;
                    tokens.add(new JSONStringToken(content));
                    continue;
                }

                // number token
                if (consume('-') || consume('0', '9')) {
                    offset--;
                    int start = offset;

                    // sign part
                    consume('-');

                    // integer part
                    if (!consume('0')) {
                        if (!consume('1', '9')) {
                            throw new JSONParseException("Invalid number literal");
                        }

                        while (consume('0', '9'))
                            ;
                    }

                    // fractional part
                    if (consume('.')) {
                        if (!consume('0', '9')) {
                            throw new JSONParseException("Invalid number literal");
                        }

                        while (consume('0', '9'))
                            ;
                    }

                    // exponent part
                    if (consume('e') || consume('E')) {
                        consume('+');
                        consume('-');

                        if (!consume('0', '9')) {
                            throw new JSONParseException("Invalid number literal");
                        }

                        while (consume('0', '9'))
                            ;
                    }

                    String content = String.valueOf(source, start, offset - start);
                    tokens.add(new JSONNumberToken(content));
                    continue;
                }

                throw new JSONParseException("Invalid token");
            }

            return tokens;
        }

        boolean consume(char expected) {
            if (offset >= len || source[offset] != expected) {
                return false;
            }
            offset++;
            return true;
        }

        boolean consume(char min, char max) {
            if (offset >= len) {
                return false;
            }
            if (source[offset] < min || source[offset] > max) {
                return false;
            }
            offset++;
            return true;
        }

        /**
         * Skip whitespace
         */
        void advance() {
            while (offset < len) {
                boolean isWhitespace = switch (source[offset]) {
                    case '\t', '\r', '\n', ' ' -> true;
                    default -> false;
                };
                if (!isWhitespace) {
                    break;
                }
                offset++;
            }
        }
    }

    private static class Parser {

        final ArrayList<JSONToken> tokens;
        final int size;
        int offset;

        public Parser(ArrayList<JSONToken> tokens) {
            this.tokens = tokens;
            size = tokens.size();
            offset = 0;
        }

        JSONObject parseObject() throws JSONParseException {
            if (!consumeStructural('{')) {
                throw new JSONParseException("Expected '{' token");
            }

            LinkedHashMap<String, JSONValue> values = new LinkedHashMap<>();

            if (consumeStructural('}')) {
                return new JSONObject(values);
            }

            while (true) {
                JSONToken t = consume();
                JSONStringToken stringToken = null;
                if (t instanceof JSONStringToken s_) {
                    stringToken = s_;
                } else {
                    throw new JSONParseException("Expected string token");
                }

                if (!consumeStructural(':')) {
                    throw new JSONParseException("Expected ':' token");
                }

                JSONValue value = parseValue();

                values.put(stringToken.getContent(), value);

                if (consumeStructural(',')) {
                    continue;
                }

                if (consumeStructural('}')) {
                    break;
                }

                throw new JSONParseException("Expected '}' token");
            }

            return new JSONObject(values);
        }

        JSONValue parseValue() throws JSONParseException {
            return parseValue(false);
        }

        JSONValue parseValue(boolean mustEnd) throws JSONParseException {
            if (offset >= size) {
                throw new JSONParseException("Expected JSONValue");
            }

            JSONToken token = tokens.get(offset);

            if (!(token instanceof JSONStructuralToken)) {
                offset++;
            }

            JSONValue result = switch (token) {
                case JSONStructuralToken s ->
                    switch (s) {
                        case JSONStructuralToken.LEFT_BRACE -> parseObject();
                        case JSONStructuralToken.LEFT_BRACKET -> parseArray();
                        // default -> null;
                        default -> throw new JSONParseException("Invalid structural token");
                    };
                case JSONLiteralToken l -> switch (l) {
                    case JSONLiteralToken.TRUE -> new JSONBoolean(true);
                    case JSONLiteralToken.FALSE -> new JSONBoolean(false);
                    case JSONLiteralToken.NULL -> new JSONNull();
                };
                case JSONStringToken s -> new JSONString(s.getContent());
                case JSONNumberToken n -> parseNumber(n.getContent());
            };

            if (mustEnd && offset != size) {
                throw new JSONParseException("End of input expected");
            }

            return result;
        }

        JSONArray parseArray() throws JSONParseException {
            if (!consumeStructural('[')) {
                throw new JSONParseException("Expected '[' token");
            }

            ArrayList<JSONValue> values = new ArrayList<>();

            if (consumeStructural(']')) {
                return new JSONArray(values);
            }

            while (true) {
                JSONValue value = parseValue();

                values.add(value);

                if (consumeStructural(',')) {
                    continue;
                }

                if (consumeStructural(']')) {
                    break;
                }

                throw new JSONParseException("Expected ']' token");
            }

            return new JSONArray(values);
        }

        JSONValue parseNumber(String content) throws JSONParseException {
            Integer i = null;
            try {
                i = Integer.parseInt(content);
            } catch (NumberFormatException e) {
            }
            if (i != null) {
                return new JSONInteger(i);
            }

            Double d = null;
            try {
                d = Double.parseDouble(content);
            } catch (NumberFormatException e) {
            }
            if (d != null) {
                return new JSONFloat(d);
            }

            throw new JSONParseException("Invalid number format");
        }

        JSONToken consume() {
            if (offset >= size) {
                return null;
            }
            return tokens.get(offset++);
        }

        boolean consumeStructural(char c) throws JSONParseException {
            if (offset >= size) {
                return false;
            }
            JSONToken token = tokens.get(offset);

            if (token instanceof JSONStructuralToken s) {
                if (s == JSONStructuralToken.from(c)) {
                    offset++;
                    return true;
                }
            }

            return false;
        }
    }

}
