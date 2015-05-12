package cn.jpush.im.android.demo.view;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.jpush.im.android.demo.R;

import java.io.File;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;

import cn.jpush.im.android.demo.tools.BitmapLoader;

public class MeView extends LinearLayout {

    private TextView mTitleBarTitle;
    private PullScrollView mScrollView;
    private ImageView mAvatarIv;
    private ImageButton mTakePhotoBtn;
    private LinearLayout mContentLl;
    private RelativeLayout mUserInfoRl;
    private TextView mUserNameTv;
    private RelativeLayout mSettingRl;
    private RelativeLayout mLogoutRl;
    private TextView mLogoutTv;
    private Context mContext;


    public MeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void initModule() {
        UserInfo userInfo = JMessageClient.getMyInfo();
        mTitleBarTitle = (TextView) findViewById(R.id.title_bar_title);
        mTitleBarTitle.setText(mContext.getString(R.string.actionbar_me));
        mScrollView = (PullScrollView) findViewById(R.id.scroll_view);
        mContentLl = (LinearLayout) findViewById(R.id.content_list_ll);
        mAvatarIv = (ImageView) findViewById(R.id.my_avatar_iv);
        mTakePhotoBtn = (ImageButton) findViewById(R.id.take_photo_iv);
        mUserInfoRl = (RelativeLayout) findViewById(R.id.user_info_rl);
        mUserNameTv = (TextView) findViewById(R.id.user_name_tv);
        mSettingRl = (RelativeLayout) findViewById(R.id.setting_rl);
        mLogoutRl = (RelativeLayout) findViewById(R.id.logout_rl);
        mScrollView.setHeader(mAvatarIv);
        if(userInfo != null){
            mUserNameTv.setText(userInfo.getUserName());
            File file = userInfo.getAvatar();
            if(file != null && file.isFile()){
                Log.i("MeView", "file.getAbsolutePath() " + file.getAbsolutePath());
                DisplayMetrics dm = new DisplayMetrics();
                ((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
                double density = dm.density;
                Bitmap bitmap = BitmapLoader.getBitmapFromFile(file.getAbsolutePath(), dm.widthPixels, (int)(density * 300));
                mAvatarIv.setImageBitmap(bitmap);
            }else mAvatarIv.setImageResource(R.drawable.friends_sends_pictures_no);
        }
    }


    public void setListeners(OnClickListener onClickListener) {
        mTakePhotoBtn.setOnClickListener(onClickListener);
        mUserInfoRl.setOnClickListener(onClickListener);
        mSettingRl.setOnClickListener(onClickListener);
        mLogoutRl.setOnClickListener(onClickListener);
        mAvatarIv.setOnClickListener(onClickListener);
    }

    public void setOnTouchListener(OnTouchListener listener){
        mUserInfoRl.setOnTouchListener(listener);
        mSettingRl.setOnTouchListener(listener);
        mLogoutRl.setOnTouchListener(listener);
    }

    public void showPhoto(final String path) {
//        Picasso.with(getContext()).load(new File(path)).into(mAvatarIv);
        Log.i("MeView", "updated path:  " + path);
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAvatarIv.setImageBitmap(BitmapLoader.getBitmapFromFile(path, mAvatarIv.getWidth(), mAvatarIv.getHeight()));
            }
        });
    }

    public boolean touchEvent(MotionEvent e) {
        return mScrollView.onTouchEvent(e);
    }

}
