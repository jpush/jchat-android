package io.jchat.android.tools.keyboard;

/**
 * Created by ${chenyn} on 2017/11/14.
 */

public class EmojiParse {
    public EmojiParse() {
    }

    public static String fromChar(char ch) {
        return Character.toString(ch);
    }

    public static String fromCodePoint(int codePoint) {
        return newString(codePoint);
    }

    public static final String newString(int codePoint) {
        return Character.charCount(codePoint) == 1?String.valueOf(codePoint):new String(Character.toChars(codePoint));
    }
}
