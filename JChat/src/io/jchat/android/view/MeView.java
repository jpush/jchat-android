package io.jchat.android.view;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.jchat.android.R;

import java.io.File;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;

import io.jchat.android.tools.BitmapLoader;

public class MeView extends LinearLayout {

    private TextView mTitleBarTitle;
    private ImageView mAvatarIv;
    private RoundImageView mTakePhotoBtn;
    private LinearLayout mContentLl;
    private RelativeLayout mUserInfoRl;
    private TextView mUserNameTv;
    private TextView mNickNameTv;
    private RelativeLayout mSettingRl;
    private RelativeLayout mLogoutRl;
    private Context mContext;
    private boolean mLoadAvatarSuccess = false;
    private int mWidth;
    private int mHeight;

    public MeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void initModule() {
        UserInfo userInfo = JMessageClient.getMyInfo();
        mTitleBarTitle = (TextView) findViewById(R.id.title_bar_title);
        mTitleBarTitle.setText(mContext.getString(R.string.actionbar_me));
        mContentLl = (LinearLayout) findViewById(R.id.content_list_ll);
        mAvatarIv = (ImageView) findViewById(R.id.my_avatar_iv);
        mTakePhotoBtn = (RoundImageView) findViewById(R.id.take_photo_iv);
        mNickNameTv = (TextView) findViewById(R.id.nick_name_tv);
        mUserInfoRl = (RelativeLayout) findViewById(R.id.user_info_rl);
        mUserNameTv = (TextView) findViewById(R.id.user_name_tv);
        mSettingRl = (RelativeLayout) findViewById(R.id.setting_rl);
        mLogoutRl = (RelativeLayout) findViewById(R.id.logout_rl);
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
        double density = dm.density;
        mWidth = dm.widthPixels;
        mHeight = (int)(190 * density);
        if(userInfo != null){
            mUserNameTv.setText(userInfo.getUserName());
            File file = userInfo.getAvatarFile();
            if(file != null && file.isFile()){
                Log.i("MeView", "file.getAbsolutePath() " + file.getAbsolutePath());
                showPhoto(file.getAbsolutePath());
                loadAvatarSuccess(true);
            }else {
                loadAvatarSuccess(false);
            }
            if(!TextUtils.isEmpty(userInfo.getNickname()))
                mNickNameTv.setText(userInfo.getNickname());
        }
    }

    private void loadAvatarSuccess(boolean value) {
        mLoadAvatarSuccess = value;
    }

    public boolean getAvatarFlag(){
        return mLoadAvatarSuccess;
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
        Log.i("MeView", "updated path:  " + path);
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = BitmapLoader.getBitmapFromFile(path, mWidth, mHeight);
                mAvatarIv.setImageBitmap(BitmapLoader.BoxBlurFilter(bitmap));
                mAvatarIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mTakePhotoBtn.setImageBitmap(bitmap);
            }
        });
    }

    public void showNickName(String nickname) {
        mNickNameTv.setText(nickname);
    }
}
