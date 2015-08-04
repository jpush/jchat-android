package io.jchat.android.view;

import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import io.jchat.android.R;


public class MainView extends RelativeLayout{
	
	private Button[] mBtnList;
	private int[] mBtnListID;
	List<Fragment> fragments;
	private ImageView mMsgUnreadiv;
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
		mMsgUnreadiv = (ImageView) findViewById(R.id.msg_unread_iv);
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
		for(int i = 0; i < 3; i++){
			if(index == i){
                mBtnList[i].setSelected(true);
                mBtnList[i].setTextColor(getResources().getColor(R.color.actionbar_pres_color));
            }
			else {
                mBtnList[i].setSelected(false);
                mBtnList[i].setTextColor(getResources().getColor(R.color.action_bar_txt_color));
            }
		}
	}


	public void dismissUnreadFlag() {
		mMsgUnreadiv.setVisibility(View.INVISIBLE);
	}

	public void showNewMsgReceivedFlag() {
		mMsgUnreadiv.setVisibility(View.VISIBLE);
	}
}
