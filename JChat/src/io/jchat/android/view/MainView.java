package io.jchat.android.view;

import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.RelativeLayout;

import io.jchat.android.R;


public class MainView extends RelativeLayout{
	
	private Button[] mBtnList;
	private int[] mBtnListID;
	List<Fragment> fragments;
	private ScrollControllViewPager mViewContainer;
	
	public MainView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void initModule(){
		mBtnListID = new int[]{R.id.actionbar_msg_btn, R.id.actionbar_contact_btn,
				R.id.actionbar_me_btn};
		mBtnList = new Button[3];
		for (int i = 0; i < 3; i++) {
			mBtnList[i] = (Button)findViewById(mBtnListID[i]);
		}
		mViewContainer = (ScrollControllViewPager) findViewById(R.id.viewpager);
		mBtnList[0].setTextColor(getResources().getColor(R.color.actionbar_pres_color));
        mBtnList[0].setSelected(true);
	}
	
	public void setOnClickListener(OnClickListener onclickListener) {
		for(int i = 0; i<mBtnListID.length; i++){
			mBtnList[i].setOnClickListener(onclickListener);
		}
	}
	
	public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
		mViewContainer.setOnPageChangeListener(onPageChangeListener);
	}
	
	public void setViewPagerAdapter(FragmentPagerAdapter adapter) {
		mViewContainer.setAdapter(adapter);
	}
	
	public void setCurrentItem(int index) {
		mViewContainer.setCurrentItem(index);
	}

	public void setButtonColor(int index) {
//		int[] pictureSel = new int[]{R.drawable.actionbar_msg_sel, R.drawable.actionbar_contact_sel, R.drawable.actionbar_me_sel};
//		int[] pictureDef = new int[]{R.drawable.actionbar_msg, R.drawable.actionbar_contact, R.drawable.actionbar_me};
		for(int i = 0; i < 3; i++){
			if(index == i){
                mBtnList[i].setSelected(true);
                mBtnList[i].setTextColor(getResources().getColor(R.color.actionbar_pres_color));
            }
			else {
                mBtnList[i].setSelected(false);
                mBtnList[i].setTextColor(getResources().getColor(R.color.white));
            }
		}
//        switch (index){
//            case 0:
//                mBtnList[0].setBackgroundResource(pictureSel[0]);
//                mBtnList[1].setBackgroundResource(pictureDef[1]);
//                mBtnList[2].setBackgroundResource(pictureDef[2]);
//                break;
//            case 1:
//                mBtnList[1].setBackgroundResource(pictureSel[1]);
//                mBtnList[0].setBackgroundResource(pictureDef[0]);
//                mBtnList[2].setBackgroundResource(pictureDef[2]);
//                break;
//            case 2:
//                mBtnList[2].setBackgroundResource(pictureSel[2]);
//                mBtnList[0].setBackgroundResource(pictureDef[0]);
//                mBtnList[1].setBackgroundResource(pictureDef[1]);
//        }
	}

	
}
