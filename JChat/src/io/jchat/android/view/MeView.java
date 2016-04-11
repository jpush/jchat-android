package io.jchat.android.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.lang.ref.WeakReference;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.chatting.CircleImageView;
import io.jchat.android.chatting.utils.BitmapLoader;

public class MeView extends LinearLayout {

    private TextView mTitleBarTitle;
    private ImageView mAvatarIv;
    private CircleImageView mTakePhotoBtn;
    private LinearLayout mContentLl;
    private RelativeLayout mUserInfoRl;
    private TextView mUserNameTv;
    private TextView mNickNameTv;
    private RelativeLayout mSettingRl;
    private RelativeLayout mLogoutRl;
    private Context mContext;
    private int mWidth;
    private int mHeight;
    private final MyHandler myHandler = new MyHandler(this);

    public MeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void initModule(float density, int width) {
        UserInfo userInfo = JMessageClient.getMyInfo();
        mTitleBarTitle = (TextView) findViewById(R.id.title_bar_title);
        mTitleBarTitle.setText(mContext.getString(R.string.actionbar_me));
        mContentLl = (LinearLayout) findViewById(R.id.content_list_ll);
        mAvatarIv = (ImageView) findViewById(R.id.my_avatar_iv);
        mTakePhotoBtn = (CircleImageView) findViewById(R.id.take_photo_iv);
        mNickNameTv = (TextView) findViewById(R.id.nick_name_tv);
        mUserInfoRl = (RelativeLayout) findViewById(R.id.user_info_rl);
        mUserNameTv = (TextView) findViewById(R.id.user_name_tv);
        mSettingRl = (RelativeLayout) findViewById(R.id.setting_rl);
        mLogoutRl = (RelativeLayout) findViewById(R.id.logout_rl);
        mWidth = width;
        mHeight = (int) (190 * density);
        if (userInfo != null) {
            mUserNameTv.setText(userInfo.getUserName());
            if (!TextUtils.isEmpty(userInfo.getNickname())) {
                mNickNameTv.setText(userInfo.getNickname());
            } else {
                mNickNameTv.setText(userInfo.getUserName());
            }
        }
    }

    public void setListeners(OnClickListener onClickListener) {
        mTakePhotoBtn.setOnClickListener(onClickListener);
        mUserInfoRl.setOnClickListener(onClickListener);
        mSettingRl.setOnClickListener(onClickListener);
        mLogoutRl.setOnClickListener(onClickListener);
        mAvatarIv.setOnClickListener(onClickListener);
    }

    public void setOnTouchListener(OnTouchListener listener) {
        mUserInfoRl.setOnTouchListener(listener);
        mSettingRl.setOnTouchListener(listener);
        mLogoutRl.setOnTouchListener(listener);
    }


    public void showPhoto(final Bitmap bitmap) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (bitmap != null) {
                    Bitmap bmp = BitmapLoader.doBlur(bitmap, false);
                    android.os.Message msg = myHandler.obtainMessage();
                    msg.obj = bmp;
                    msg.sendToTarget();
                }
            }
        });
        thread.start();
        mTakePhotoBtn.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mTakePhotoBtn.setImageBitmap(bitmap);
    }

    public void showPhoto(final String path) {
        Log.i("MeView", "updated path:  " + path);
        final Bitmap bitmap = BitmapLoader.getBitmapFromFile(path, mWidth, mHeight);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (bitmap != null) {
                    Bitmap bmp = BitmapLoader.doBlur(bitmap, false);
                    android.os.Message msg = myHandler.obtainMessage();
                    msg.obj = bmp;
                    msg.sendToTarget();
                }
            }
        });
        thread.start();
        mTakePhotoBtn.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mTakePhotoBtn.setImageBitmap(bitmap);
    }

    public void showNickName(String nickname) {
        mNickNameTv.setText(nickname);
    }

    private static class MyHandler extends Handler {
        private final WeakReference<MeView> mMeView;

        public MyHandler(MeView meView) {
            mMeView = new WeakReference<MeView>(meView);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MeView meView = mMeView.get();
            if (null != meView) {
                Bitmap bitmap = (Bitmap) msg.obj;
                meView.mAvatarIv.setImageBitmap(bitmap);
                meView.mAvatarIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        }
    }
}
