package io.jchat.android.view.shader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;

import io.jchat.android.R;


public class BubbleShader extends ShaderHelper {
    private static final int DEFAULT_HEIGHT_DP = 10;

    private enum ArrowPosition {
        @SuppressLint("RtlHardcoded")
        LEFT,
        RIGHT
    }

    private final Path path = new Path();

    private int radius = 0;
    private int bitmapRadius;

    private int triangleHeightPx;
    private ArrowPosition arrowPosition = ArrowPosition.LEFT;

    public BubbleShader() {
    }

    @Override
    public void init(Context context, AttributeSet attrs, int defStyle) {
        super.init(context, attrs, defStyle);
        borderWidth = 0;
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShaderImageView, defStyle, 0);
            triangleHeightPx = typedArray.getDimensionPixelSize(R.styleable.ShaderImageView_siTriangleHeight, 0);
            int arrowPositionInt = typedArray.getInt(R.styleable.ShaderImageView_siArrowPosition, ArrowPosition.LEFT.ordinal());
            arrowPosition = ArrowPosition.values()[arrowPositionInt];
            radius = typedArray.getDimensionPixelSize(R.styleable.ShaderImageView_siRadius, radius);
            typedArray.recycle();
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
        float resultWidth = bitmapWidth + 2 * translateX;
        float resultHeight = bitmapHeight + 2 * translateY;
        float centerY;
        if(scale < 2){
            if(viewHeight < 400){
                centerY = resultHeight / scale / 4f;
            }else centerY = viewHeight / scale / 8f;
        }else if(viewHeight < 200){
            centerY = resultHeight / 3f;
        }else if(viewHeight < 400){
            centerY = resultHeight / 4f;
        }else centerY = viewHeight / scale / 8f;
        Log.i("Bubble", "centerY: " + centerY);
        Log.i("Bubble", "viewHeight: " + viewHeight + " viewWidth: " + viewWidth);
        Log.i("Bubble", "scale: " + scale + "resultHeight: " + resultHeight);
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
                path.moveTo(rectLeft, 5);
                RectF rectF = new RectF(rectLeft, 0, rectLeft + 10, 10);
                path.arcTo(rectF, 180, 90);
                path.lineTo(rectLeft, 0);
                path.lineTo(rectLeft, 5);
                //画右上圆角
                path.moveTo(resultWidth - 5, 0);
                RectF rectF1 = new RectF(resultWidth - 10, 0, resultWidth, 10);
                path.arcTo(rectF1, 270, 90);
                path.lineTo(resultWidth, 0);
                path.lineTo(resultWidth - 5, 0);
                //画左下圆角
                path.moveTo(rectLeft + 5, resultHeight);
                RectF rectF2 = new RectF(rectLeft, resultHeight - 10, rectLeft + 10, resultHeight);
                path.arcTo(rectF2, 90, 90);
                path.lineTo(rectLeft, resultHeight);
                path.lineTo(rectLeft + 5, resultHeight);
                //画右下圆角
                path.moveTo(resultWidth, resultHeight - 5);
                RectF rectF3 = new RectF(resultWidth - 10, resultHeight - 10, resultWidth, resultHeight);
                path.arcTo(rectF3, 0, 90);
                path.lineTo(resultWidth, resultHeight);
                path.lineTo(resultWidth, resultHeight - 5);
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
                path.moveTo(rectLeft, 5);
                rectF = new RectF(rectLeft, 0, rectLeft + 10, 10);
                path.arcTo(rectF, 180, 90);
                path.lineTo(rectLeft, 0);
                path.lineTo(rectLeft, 5);
                //画右上圆角
                path.moveTo(rectRight - 5, 0);
                rectF1 = new RectF(rectRight - 10, 0, rectRight, 10);
                path.arcTo(rectF1, 270, 90);
                path.lineTo(rectRight, 0);
                path.lineTo(rectRight - 5, 0);
                //画左下圆角
                path.moveTo(rectLeft + 5, resultHeight);
                rectF2 = new RectF(rectLeft, resultHeight - 10, rectLeft + 10, resultHeight);
                path.arcTo(rectF2, 90, 90);
                path.lineTo(rectLeft, resultHeight);
                path.lineTo(rectLeft + 5, resultHeight);
                //画右下圆角
                path.moveTo(rectRight, resultHeight - 5);
                rectF3 = new RectF(rectRight - 10, resultHeight - 10, rectRight, resultHeight);
                path.arcTo(rectF3, 0, 90);
                path.lineTo(rectRight, resultHeight);
                path.lineTo(rectRight, resultHeight - 5);
                break;
        }

    }

    @Override
    public void reset() {
        path.reset();
    }
}