package com.sj.emoji;

/**
 * Created by sj on 16/3/22.
 */
public class EmojiParse {

    public static String fromChar(char ch) { return Character.toString(ch); }

    public static String fromCodePoint(int codePoint) { return newString(codePoint); }

    public static final String newString(int codePoint) {
        if (Character.charCount(codePoint) == 1) {
            return String.valueOf(codePoint);
        } else {
            return new String(Character.toChars(codePoint));
        }
    }
}