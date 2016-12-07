package io.jchat.android.chatting.shader;
/**
 *The MIT License (MIT)
 Copyright (c) 2015 Siyamed Sinir
 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import io.jchat.android.chatting.utils.IdHelper;


@SuppressWarnings("WeakerAccess")
public abstract class ShaderHelper {
    private final static int ALPHA_MAX = 255;

    protected int viewWidth;
    protected int viewHeight;

    protected int borderColor = Color.BLACK;
    protected int borderWidth = 0;
    protected float borderAlpha = 1f;
    protected boolean square = false;

    protected final Paint borderPaint;
    protected final Paint imagePaint;
    protected BitmapShader shader;
    protected Drawable drawable;
    protected final Matrix matrix = new Matrix();

    public ShaderHelper() {
        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);

        imagePaint = new Paint();
        imagePaint.setAntiAlias(true);
    }

    public abstract void draw(Canvas canvas, Paint imagePaint, Paint borderPaint);
    public abstract void reset();
    @SuppressWarnings("UnusedParameters")
    public abstract void calculate(int bitmapWidth, int bitmapHeight, float width, float height, float scale, float translateX, float translateY);


    @SuppressWarnings("SameParameterValue")
    protected final int dpToPx(DisplayMetrics displayMetrics, int dp) {
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public boolean isSquare() {
        return square;
    }

    @SuppressWarnings("ResourceType")
    public void init(Context context, AttributeSet attrs, int defStyle) {
        if(attrs != null){
            int[] declareStyleableArray = IdHelper.getResourceDeclareStyleableIntArray(context, "ShaderImageView");
            if (declareStyleableArray != null && declareStyleableArray.length > 0) {
                TypedArray typedArray = context.obtainStyledAttributes(attrs, declareStyleableArray, defStyle, 0);
                //第一个参数是该属性在R文件中生成的数组的下标(按照字母顺序排列),而不是在attrs文件中声明的顺序.下同
                square = typedArray.getBoolean(8, square);
                borderColor = typedArray.getColor(2, borderColor);
                borderWidth = typedArray.getDimensionPixelSize(4, borderWidth);
                borderAlpha = typedArray.getFloat(1, borderAlpha);
                typedArray.recycle();
            }
        }

        borderPaint.setColor(borderColor);
        borderPaint.setAlpha(Float.valueOf(borderAlpha * ALPHA_MAX).intValue());
        borderPaint.setStrokeWidth(borderWidth);
    }

    public boolean onDraw(Canvas canvas) {
        if (shader == null) {
            createShader();
        }
        if (shader != null && viewWidth > 0 && viewHeight > 0) {
//            canvas.clipRect(0, 0, viewWidth, viewHeight);
            draw(canvas, imagePaint, borderPaint);
            return true;
        }

        return false;
    }

    public void onSizeChanged(int width, int height) {
        viewWidth = width;
        viewHeight = height;
        if(isSquare()) {
            viewWidth = viewHeight = Math.min(width, height);
        }
        if(shader != null) {
            calculateDrawableSizes();
        }
    }

    public Bitmap calculateDrawableSizes() {
        Bitmap bitmap = getBitmap();
        if(bitmap != null) {
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();

            if(bitmapWidth > 0 && bitmapHeight > 0) {
                float width = Math.round(viewWidth - 2f * borderWidth);
                float height = Math.round(viewHeight - 2f * borderWidth);

                float scale;
                float translateX = 0;
                float translateY = 0;

                if (bitmapWidth * height > width * bitmapHeight) {
                    scale = height / bitmapHeight;
                    translateX = Math.round((width/scale - bitmapWidth) / 2f);
                } else {
                    scale = width / (float) bitmapWidth;
                    translateY = Math.round((height/scale - bitmapHeight) / 2f);
                }

                matrix.setScale(scale, scale);
                matrix.preTranslate(translateX, translateY);
                matrix.postTranslate(borderWidth, borderWidth);

                calculate(bitmapWidth, bitmapHeight, width, height, scale, translateX, translateY);

                return bitmap;
            }
        }

        reset();
        return null;
    }

    public final void onImageDrawableReset(Drawable drawable) {
        this.drawable = drawable;
        shader = null;
        imagePaint.setShader(null);
    }

    protected void createShader() {
        Bitmap bitmap = calculateDrawableSizes();
        if(bitmap != null && bitmap.getWidth() > 0 && bitmap.getHeight() > 0) {
            shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            imagePaint.setShader(shader);
        }
    }

    protected Bitmap getBitmap() {
        Bitmap bitmap = null;
        if(drawable != null) {
            if(drawable instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) drawable).getBitmap();
            }
        }

        return bitmap;
    }
}