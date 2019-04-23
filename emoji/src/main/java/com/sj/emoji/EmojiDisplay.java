package com.sj.emoji;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Spannable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sj on 16/3/22.
 */
public class EmojiDisplay {

    public static final int WRAP_DRAWABLE = -1;
    public static final Pattern EMOJI_RANGE = Pattern.compile("[\\u20a0-\\u32ff\\ud83c\\udc00-\\ud83d\\udeff\\udbb9\\udce5-\\udbb9\\udcee]");

    public static Matcher getMatcher(CharSequence matchStr) {
        return EMOJI_RANGE.matcher(matchStr);
    }

    public static Spannable spannableFilter(Context context, Spannable spannable, CharSequence text, int fontSize) {
        return spannableFilter(context, spannable, text, fontSize, null);
    }

    public static Spannable spannableFilter(Context context, Spannable spannable, CharSequence text, int fontSize, EmojiDisplayListener emojiDisplayListener) {
        Matcher m = getMatcher(text);
        if (m != null) {
            while (m.find()) {
                String emojiHex = Integer.toHexString(Character.codePointAt(m.group(), 0));
                if (emojiDisplayListener == null) {
                    emojiDisplay(context, spannable, emojiHex, fontSize, m.start(), m.end());
                } else {
                    emojiDisplayListener.onEmojiDisplay(context, spannable, emojiHex, fontSize, m.start(), m.end());
                }
            }
        }
        return spannable;
    }

    public static void emojiDisplay(Context context, Spannable spannable, String emojiHex, int fontSize, int start, int end) {
        Drawable drawable = getDrawable(context, "emoji_0x" + emojiHex);
        if (drawable != null) {
            int itemHeight;
            int itemWidth;
            if (fontSize == WRAP_DRAWABLE) {
                itemHeight = drawable.getIntrinsicHeight();
                itemWidth = drawable.getIntrinsicWidth();
            } else {
                itemHeight = fontSize;
                itemWidth = fontSize;
            }

            drawable.setBounds(0, 0, itemHeight, itemWidth);
            EmojiSpan imageSpan = new EmojiSpan(drawable);
            spannable.setSpan(imageSpan, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
    }

    public static Drawable getDrawable(Context context, String emojiName) {
        int resID = context.getResources().getIdentifier(emojiName, "mipmap", context.getPackageName());
        if (resID <= 0) {
            resID = context.getResources().getIdentifier(emojiName, "drawable", context.getPackageName());
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return context.getResources().getDrawable(resID, null);
            } else {
                return context.getResources().getDrawable(resID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
