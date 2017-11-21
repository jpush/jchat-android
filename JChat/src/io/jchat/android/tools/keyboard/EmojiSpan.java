package io.jchat.android.tools.keyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

/**
 * Created by ${chenyn} on 2017/11/14.
 */

public class EmojiSpan extends ImageSpan {
    public EmojiSpan(Drawable drawable) {
        super(drawable);
    }

    public EmojiSpan(Context context, int resourceId) {
        super(context, resourceId);
    }

    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fontMetricsInt) {
        Drawable drawable = this.getDrawable();
        Rect rect = drawable.getBounds();
        if(fontMetricsInt != null) {
            Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
            int fontHeight = fmPaint.bottom - fmPaint.top;
            int drHeight = rect.bottom - rect.top;
            int top = drHeight / 2 - fontHeight / 4;
            int bottom = drHeight / 2 + fontHeight / 4;
            fontMetricsInt.ascent = -bottom;
            fontMetricsInt.top = -bottom;
            fontMetricsInt.bottom = top;
            fontMetricsInt.descent = top;
        }

        return rect.right;
    }

    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        Drawable drawable = this.getDrawable();
        canvas.save();
        int transY = (bottom - top - drawable.getBounds().bottom) / 2 + top;
        canvas.translate(x, (float)transY);
        drawable.draw(canvas);
        canvas.restore();
    }
}
