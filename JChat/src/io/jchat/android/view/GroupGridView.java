package io.jchat.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

public class GroupGridView extends GridView{

	public GroupGridView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
    public GroupGridView(Context context, AttributeSet attrs) {

        super(context, attrs);

  }

  public GroupGridView(Context context, AttributeSet attrs, int defStyle) {

       super(context, attrs, defStyle);

  }
  
  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {

           if (ev.getAction() == MotionEvent.ACTION_MOVE) {

                return true;  //禁止GridView滑动

           }


           return super.dispatchTouchEvent(ev);

  }
  
  @Override   
  public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {   

      int expandSpec = MeasureSpec.makeMeasureSpec(   
              Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);   
      super.onMeasure(widthMeasureSpec, expandSpec);   
  }
}
