package cn.jpush.im.android.demo.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class ScrollControllViewPager extends ViewPager {
	private final String TAG = ScrollControllViewPager.class.getSimpleName();
	private boolean scroll = true;

	public ScrollControllViewPager(Context context) {
		super(context);
	}

	public ScrollControllViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * @param scroll
	 */
	public void setScroll(boolean scroll) {
		this.scroll = scroll;
	}

	@Override
	public void scrollTo(int x, int y) {
		super.scrollTo(x, y);
	}

	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		if (!scroll)
			return false;
		else
			return super.onTouchEvent(arg0);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		if (!scroll)
			return false;
		else
			return super.onInterceptTouchEvent(arg0);
	}

	@Override
	public void setCurrentItem(int item, boolean smoothScroll) {
		super.setCurrentItem(item, smoothScroll);
	}

	@Override
	public void setCurrentItem(int item) {
		super.setCurrentItem(item);
	}
}
