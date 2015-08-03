package io.jchat.android.activity;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import io.jchat.android.controller.FriendInfoController;
import io.jchat.android.tools.BitmapLoader;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.tools.NativeImageLoader;
import io.jchat.android.view.FriendInfoView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import io.jchat.android.R;

import java.io.File;
import java.lang.ref.WeakReference;

public class FriendInfoActivity extends BaseActivity {

    private FriendInfoView mFriendInfoView;
    private FriendInfoController mFriendInfoController;
    private String mTargetID;
    private UserInfo mUserInfo;
    private double mDensity;
    private Context mContext;
    private final MyHandler myHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_info);
        mContext = this;
        mFriendInfoView = (FriendInfoView) findViewById(R.id.friend_info_view);
        mTargetID = getIntent().getStringExtra("targetID");
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDensity = dm.density;
        mFriendInfoView.initModule(mTargetID);
        mFriendInfoController = new FriendInfoController(mFriendInfoView, this);
        mFriendInfoView.setListeners(mFriendInfoController);
        if (mTargetID != null) {
            final Bitmap bitmap = NativeImageLoader.getInstance().getBitmapFromMemCache(mTargetID);
            if (bitmap != null) {
                mFriendInfoView.setFriendAvatar(bitmap);
            }
            final ProgressDialog dialog = new ProgressDialog(mContext);
            dialog.setMessage(mContext.getString(R.string.loading));
            dialog.show();
            JMessageClient.getUserInfo(mTargetID, new GetUserInfoCallback() {
                @Override
                public void gotResult(int status, String desc, UserInfo userInfo) {
                    FriendInfoActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    });
                    if (status == 0) {
                        File file = userInfo.getAvatarFile();
                        if (file != null && file.isFile()) {
                            Bitmap bitmap = BitmapLoader.getBitmapFromFile(file.getAbsolutePath(), (int) (50 * mDensity), (int) (50 * mDensity));
                            //更新头像缓存
                            NativeImageLoader.getInstance().updateBitmapFromCache(mTargetID, bitmap);
                        }
                        android.os.Message msg = myHandler.obtainMessage();
                        msg.what = 1;
                        msg.obj = userInfo;
                        msg.sendToTarget();
                    } else {
                        android.os.Message msg = myHandler.obtainMessage();
                        msg.what = 2;
                        Bundle bundle = new Bundle();
                        bundle.putInt("status", status);
                        msg.setData(bundle);
                        msg.sendToTarget();
                    }
                }
            });
        }
    }

    public void StartChatActivity() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("targetID", mTargetID);
        intent.setClass(this, ChatActivity.class);
        startActivity(intent);
        finish();
    }

    private static class MyHandler extends Handler{
        private final WeakReference<FriendInfoActivity> mActivity;

        public  MyHandler(FriendInfoActivity activity){
            mActivity = new WeakReference<FriendInfoActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            FriendInfoActivity activity = mActivity.get();
            if(activity != null){
                switch (msg.what) {
                    case 1:
                        DisplayMetrics dm = new DisplayMetrics();
                        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
                        double density = dm.density;
                        activity.mUserInfo = (UserInfo) msg.obj;
                        activity.mFriendInfoView.initInfo(activity.mUserInfo, density);
                        break;
                    case 2:
                        HandleResponseCode.onHandle(activity, msg.getData().getInt("status"), false);
                        break;
                }
            }
        }
    }


    //点击头像预览大图，若此时UserInfo还是空，则再取一次
    public void startBrowserAvatar() {
        if (mUserInfo != null) {
            File file = mUserInfo.getAvatarFile();
            if (file != null && file.exists()) {
                Intent intent = new Intent();
                intent.putExtra("browserAvatar", true);
                intent.putExtra("avatarPath", mUserInfo.getAvatarFile().getAbsolutePath());
                intent.setClass(this, BrowserViewPagerActivity.class);
                startActivity(intent);
            }
        } else {
            JMessageClient.getUserInfo(mTargetID, new GetUserInfoCallback(false) {
                @Override
                public void gotResult(int status, String desc, UserInfo userInfo) {
                    if (status == 0) {
                        File file = userInfo.getAvatarFile();
                        if (file != null && file.isFile()) {
                            Bitmap bitmap = BitmapLoader.getBitmapFromFile(file.getAbsolutePath(), (int) (50 * mDensity), (int) (50 * mDensity));
                            //更新头像缓存
                            NativeImageLoader.getInstance().updateBitmapFromCache(mTargetID, bitmap);
                        }
                        android.os.Message msg = myHandler.obtainMessage();
                        msg.what = 1;
                        msg.obj = userInfo;
                        msg.sendToTarget();
                    } else {
                        android.os.Message msg = myHandler.obtainMessage();
                        msg.what = 2;
                        Bundle bundle = new Bundle();
                        bundle.putInt("status", status);
                        msg.setData(bundle);
                        msg.sendToTarget();
                    }
                }
            });
        }
    }
}
