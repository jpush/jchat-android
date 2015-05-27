package cn.jpush.im.android.demo.controller;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import cn.jpush.im.android.demo.R;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.demo.activity.ContactsFragment;
import cn.jpush.im.android.demo.activity.ConversationListFragment;
import cn.jpush.im.android.demo.activity.MainActivity;
import cn.jpush.im.android.demo.activity.MeFragment;
import cn.jpush.im.android.demo.adapter.ViewPagerAdapter;
import cn.jpush.im.android.demo.tools.BitmapLoader;
import cn.jpush.im.android.demo.tools.HandleResponseCode;
import cn.jpush.im.android.demo.view.MainView;
import cn.jpush.im.api.BasicCallback;

public class MainController implements OnClickListener, OnPageChangeListener{
	
	private MeFragment mMeActivity;
	private MainView mMainView;
	private MainActivity mContext;
    private ProgressDialog mDialog;
	
	public MainController(MainView mMainView, MainActivity context){
		this.mMainView = mMainView;
		this.mContext = context;
		setViewPager();
	}

	private void setViewPager() {
        List<Fragment> fragments = new ArrayList<Fragment>();
		// init Fragment
		ConversationListFragment convActivity = new ConversationListFragment();
        ContactsFragment contactsActivity = new ContactsFragment();
		mMeActivity = new MeFragment();
		fragments.add(convActivity);
		fragments.add(contactsActivity);
		fragments.add(mMeActivity);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(
				mContext.getSupportFragmentManger(), fragments);
		mMainView.setViewPagerAdapter(viewPagerAdapter);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.actionbar_msg_btn:
			mMainView.setCurrentItem(0);
			break;
		case R.id.actionbar_contact_btn:
			mMainView.setCurrentItem(1);
			break;
		case R.id.actionbar_me_btn:
			mMainView.setCurrentItem(2);
			break;
		}
	}

    public String getPhotoPath(){
        return mMeActivity.getPhotoPath();
    }

    public void calculateAvatar(final String originPath) {
        mDialog = new ProgressDialog(mContext);
        mDialog.setCancelable(false);
        mDialog.setMessage(mContext.getString(R.string.updating_avatar_hint));
        mDialog.show();
        //验证图片大小，若小于720 * 1280则直接发送原图，否则压缩
        if (BitmapLoader.verifyPictureSize(originPath))
            updateAvatar(originPath, originPath);
        else {
            Bitmap bitmap = BitmapLoader.getBitmapFromFile(originPath, 720, 1280);
            String tempPath = BitmapLoader.saveBitmapToLocal(bitmap);
            updateAvatar(tempPath, originPath);
        }
    }

    private void updateAvatar(final String path, final String originPath) {
        JMessageClient.updateUserAvatar(new File(path), new BasicCallback(false) {
            @Override
            public void gotResult(final int status, final String desc) {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDialog.dismiss();
                        if (status == 0) {
                            Log.i("MeFragment", "Update avatar succeed path " + path);
                            loadUserAvatar(originPath);
                        } else {
                            HandleResponseCode.onHandle(mContext, status);
                        }
                    }
                });
            }
        });
    }

    private void loadUserAvatar(String path){
        if(path != null)
            mMeActivity.loadUserAvatar(path);
    }
	
	@Override
	public void onPageSelected(int index) {
		// TODO Auto-generated method stub
		mMainView.setButtonColor(index);
	}
	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}
}
