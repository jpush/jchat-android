package io.jchat.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import io.jchat.android.R;

public class SideBar extends View {
    // 触摸事件
    private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
    // 26个字母
    public static String[] b;
    private int choose = -1;// 选中
    private Paint paint = new Paint();

    private TextView mTextDialog;
    private float mRatio;
    private float mDensity;
    private float mTop;
    private float mBottom;
    private int mIndex;

    public void setTextView(TextView mTextDialog) {
        this.mTextDialog = mTextDialog;
    }


    public SideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SideBar(Context context) {
        super(context);
    }

    public void setIndex(String[] sections) {
        b = sections;
        postInvalidate();
    }



    /**
     * 重写这个方法
     */
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (null != b) {
            // 获取焦点改变背景颜色.
            int height = getHeight();// 获取对应高度
            int width = getWidth(); // 获取对应宽度
            int singleHeight = (int) (20 * mDensity);// 获取每一个字母的高度
            mTop = height / 2 + 10 * mDensity;
            mBottom = height / 2 - 10 * mDensity * b.length;

            for (int i = 0; i < b.length; i++) {
                paint.setColor(getResources().getColor(R.color.jmui_jpush_blue));
                paint.setTypeface(Typeface.DEFAULT_BOLD);
                paint.setAntiAlias(true);
                paint.setTextSize((int) (30 * mRatio));
                // 选中的状态3
                if (i == choose) {
                    paint.setColor(getResources().getColor(R.color.white));
                    paint.setFakeBoldText(true);
                }
                // x坐标等于中间-字符串宽度的一半.
                float xPos = width / 2 - paint.measureText(b[i]) / 2;
                float yPos;
                if (b.length / 2 > i) {
                    yPos = height / 2 - singleHeight * (b.length / 2 - i);
                } else {
                    yPos = height / 2 + singleHeight * (i - b.length / 2);
                }

                canvas.drawText(b[i], xPos, yPos, paint);
                paint.reset();// 重置画笔
            }
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (null == b) {
            return true;
        }
        final int action = event.getAction();
        final float y = event.getY();// 点击y坐标
        final int oldChoose = choose;
        final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
        int actualHeight = (int)(b.length * 20 * mDensity);
        final int c = (int) Math.ceil((y - mBottom) * b.length / actualHeight) ;
        // 点击y坐标所占总高度的比例*b数组的长度就等于点击b中的个数.

        switch (action) {
            case MotionEvent.ACTION_UP:
                setBackgroundDrawable(new ColorDrawable(0x00000000));
                choose = -1;//
                invalidate();
                if (mTextDialog != null) {
                    mTextDialog.setVisibility(View.INVISIBLE);
                }
                break;

            default:
//			setBackgroundResource(R.drawable.sidebar_background);
//			setBackgroundColor(Color.TRANSPARENT);
                if (oldChoose != c) {
                    if (c >= 0 && c < b.length) {
                        if (listener != null) {
                            listener.onTouchingLetterChanged(b[c]);
                        }
                        if (mTextDialog != null) {
                            mTextDialog.setText(b[c]);
                            mTextDialog.setVisibility(View.VISIBLE);
                        }
                        choose = c;
                        invalidate();
                    }
                }
                break;
        }
        return true;
    }

    /**
     * 向外公开的方法
     *
     * @param onTouchingLetterChangedListener
     */
    public void setOnTouchingLetterChangedListener(
            OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }

    /**
     * 接口
     *
     * @author coder
     */
    public interface OnTouchingLetterChangedListener {
        public void onTouchingLetterChanged(String s);
    }

    public void setRatioAndDensity(float ratio, float density) {
        this.mRatio = ratio;
        this.mDensity = density;
    }

}