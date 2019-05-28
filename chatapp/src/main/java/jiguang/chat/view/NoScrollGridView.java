package jiguang.chat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class NoScrollGridView extends GridView {
    private int mRequestedNumColumns = 0;

    public NoScrollGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoScrollGridView(Context context) {
        super(context);
    }

    public NoScrollGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setNumColumns(int numColumns) {
        super.setNumColumns(numColumns);
        if (numColumns != mRequestedNumColumns) {
            mRequestedNumColumns = numColumns;
        }
    }

    /**
     * 设置gridView不可滑动,并且设置gridView宽度
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
        if (mRequestedNumColumns > 0) {
            int width = (mRequestedNumColumns * getColumnWidth())
                    + ((mRequestedNumColumns-1) * getHorizontalSpacing())
                    + getListPaddingLeft() + getListPaddingRight();

            setMeasuredDimension(width, getMeasuredHeight());
        }
    }
}
