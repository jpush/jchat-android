package io.jchat.android.chatting.shader;
/**
 *The MIT License (MIT)
 Copyright (c) 2015 Siyamed Sinir
 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import io.jchat.android.chatting.utils.IdHelper;


public class BubbleShader extends ShaderHelper {
    private static final int DEFAULT_HEIGHT_DP = 10;

    private enum ArrowPosition {
        @SuppressLint("RtlHardcoded")
        LEFT,
        RIGHT
    }

    private final Path path = new Path();

    private int radius = 0;
    private int triangleHeightPx;
    private ArrowPosition arrowPosition = ArrowPosition.LEFT;

    public BubbleShader() {
    }

    @SuppressWarnings("ResourceType")
    @Override
    public void init(Context context, AttributeSet attrs, int defStyle) {
        super.init(context, attrs, defStyle);
        borderWidth = 0;
        if (attrs != null) {
            int[] declareStyleableArray = IdHelper.getResourceDeclareStyleableIntArray(context, "ShaderImageView");
            if (declareStyleableArray != null) {
                TypedArray typedArray = context.obtainStyledAttributes(attrs, declareStyleableArray, defStyle, 0);
                //第一个参数是该属性在R文件中生成的数组的下标(按照字母顺序排列),而不是在attrs文件中声明的顺序.下同
                triangleHeightPx = typedArray.getDimensionPixelSize(7, 0);
                int arrowPositionInt = typedArray.getInt(0, ArrowPosition.LEFT.ordinal());
                arrowPosition = ArrowPosition.values()[arrowPositionInt];
                radius = typedArray.getDimensionPixelSize(6, radius);
                typedArray.recycle();
            }
        }

        if (triangleHeightPx == 0) {
            triangleHeightPx = dpToPx(context.getResources().getDisplayMetrics(), DEFAULT_HEIGHT_DP);
        }
    }

    @Override
    public void draw(Canvas canvas, Paint imagePaint, Paint borderPaint) {
        canvas.save();
        canvas.concat(matrix);
        canvas.drawPath(path, imagePaint);
        canvas.restore();
    }


    @Override
    public void calculate(int bitmapWidth, int bitmapHeight,
                          float width, float height,
                          float scale,
                          float translateX, float translateY) {
        path.reset();
        float x = -translateX;
        float y = -translateY;
        float scaledTriangleHeight = triangleHeightPx / scale;
        float rectCenter = scaledTriangleHeight / 2;
        float resultWidth = bitmapWidth + 2 * translateX;
        float resultHeight = bitmapHeight + 2 * translateY;
        float centerY;
        centerY = 2.5f * scaledTriangleHeight;
        path.setFillType(Path.FillType.EVEN_ODD);
        float rectLeft;
        float rectRight;
        switch (arrowPosition) {
            case LEFT:
                rectLeft = scaledTriangleHeight + x;
                rectRight = resultWidth + rectLeft;
                path.addRect(rectLeft, y, rectRight, resultHeight + y, Path.Direction.CW);
                path.moveTo(x, centerY);
                path.lineTo(rectLeft, centerY - scaledTriangleHeight);
                path.lineTo(rectLeft, centerY + scaledTriangleHeight);
                path.lineTo(x, centerY);
                //画左上圆角
                path.moveTo(rectLeft, rectCenter);
                RectF rectF = new RectF(rectLeft, 0, rectLeft + scaledTriangleHeight, scaledTriangleHeight);
                path.arcTo(rectF, 180, 90);
                path.lineTo(rectLeft, 0);
                path.lineTo(rectLeft, rectCenter);
                //画右上圆角
                path.moveTo(resultWidth - rectCenter , 0);
                rectF = new RectF(resultWidth - scaledTriangleHeight, 0, resultWidth, scaledTriangleHeight);
                path.arcTo(rectF, 270, 90);
                path.lineTo(resultWidth + 1, 0);
                path.lineTo(resultWidth - rectCenter, 0);
                //画左下圆角
                path.moveTo(rectLeft + rectCenter, resultHeight);
                rectF = new RectF(rectLeft, resultHeight - scaledTriangleHeight, rectLeft + scaledTriangleHeight, resultHeight);
                path.arcTo(rectF, 90, 90);
                path.lineTo(rectLeft, resultHeight);
                path.lineTo(rectLeft + rectCenter, resultHeight);
                //画右下圆角
                path.moveTo(resultWidth, resultHeight - rectCenter);
                rectF = new RectF(resultWidth - scaledTriangleHeight, resultHeight - scaledTriangleHeight, resultWidth, resultHeight);
                path.arcTo(rectF, 0, 90);
                path.lineTo(resultWidth + 1, resultHeight);
                path.lineTo(resultWidth, resultHeight - rectCenter);
                break;
            case RIGHT:
                rectLeft = x;
                float imgRight = resultWidth + rectLeft;
                rectRight = imgRight - scaledTriangleHeight;
                path.addRect(rectLeft, y, rectRight, resultHeight + y, Path.Direction.CW);
                path.moveTo(imgRight, centerY);
                path.lineTo(rectRight, centerY - scaledTriangleHeight);
                path.lineTo(rectRight, centerY + scaledTriangleHeight);
                path.lineTo(imgRight, centerY);
                //画左上圆角
                path.moveTo(rectLeft, rectCenter);
                rectF = new RectF(rectLeft, 0, rectLeft + scaledTriangleHeight, scaledTriangleHeight);
                path.arcTo(rectF, 180, 90);
                path.lineTo(rectLeft, 0);
                path.lineTo(rectLeft, rectCenter);
                //画右上圆角
                path.moveTo(rectRight - rectCenter, 0);
                rectF = new RectF(rectRight - scaledTriangleHeight, 0, rectRight, scaledTriangleHeight);
                path.arcTo(rectF, 270, 90);
                path.lineTo(rectRight, 0);
                path.lineTo(rectRight - rectCenter, 0);
                //画左下圆角
                path.moveTo(rectLeft + rectCenter, resultHeight);
                rectF = new RectF(rectLeft, resultHeight - scaledTriangleHeight, rectLeft + scaledTriangleHeight, resultHeight);
                path.arcTo(rectF, 90, 90);
                path.lineTo(rectLeft, resultHeight);
                path.lineTo(rectLeft + rectCenter, resultHeight);
                //画右下圆角
                path.moveTo(rectRight, resultHeight - rectCenter);
                rectF = new RectF(rectRight - scaledTriangleHeight, resultHeight - scaledTriangleHeight, rectRight, resultHeight);
                path.arcTo(rectF, 0, 90);
                path.lineTo(rectRight, resultHeight);
                path.lineTo(rectRight, resultHeight - rectCenter);
                break;
        }

    }

    @Override
    public void reset() {
        path.reset();
    }
}