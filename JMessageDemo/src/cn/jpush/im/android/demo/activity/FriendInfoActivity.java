package cn.jpush.im.android.demo.activity;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.demo.controller.FriendInfoController;
import cn.jpush.im.android.demo.tools.BitmapLoader;
import cn.jpush.im.android.demo.tools.HandleResponseCode;
import cn.jpush.im.android.demo.tools.NativeImageLoader;
import cn.jpush.im.android.demo.view.FriendInfoView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.widget.Toast;

import cn.jpush.im.android.demo.R;

import java.io.File;

public class FriendInfoActivity extends BaseActivity {

    private FriendInfoView mFriendInfoView;
    private FriendInfoController mFriendInfoController;
    private String mTargetID;
    private UserInfo mUserInfo;
    private double mDensity;
    private Context mContext;

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
                        File file = userInfo.getAvatar();
                        if (file != null && file.isFile()) {
                            Bitmap bitmap = BitmapLoader.getBitmapFromFile(file.getAbsolutePath(), (int) (50 * mDensity), (int) (50 * mDensity));
                            //更新头像缓存
                            NativeImageLoader.getInstance().updateBitmapFromCache(mTargetID, bitmap);
                        }
                        android.os.Message msg = handler.obtainMessage();
                        msg.what = 1;
                        msg.obj = userInfo;
                        msg.sendToTarget();
                    } else {
                        android.os.Message msg = handler.obtainMessage();
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

    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    DisplayMetrics dm = new DisplayMetrics();
                    FriendInfoActivity.this.getWindowManager().getDefaultDisplay().getMetrics(dm);
                    double density = dm.density;
                    mUserInfo = (UserInfo) msg.obj;
                    mFriendInfoView.initInfo(mUserInfo, density);
                    break;
                case 2:
                    HandleResponseCode.onHandle(mContext, msg.getData().getInt("status"));
                    break;
            }
        }
    };

    //点击头像预览大图，若此时UserInfo还是空，则再取一次
    public void startBrowserAvatar() {
        if (mUserInfo != null) {
            File file = mUserInfo.getAvatar();
            if (file != null && file.exists()) {
                Intent intent = new Intent();
                intent.putExtra("browserAvatar", true);
                intent.putExtra("avatarPath", mUserInfo.getAvatar().getAbsolutePath());
                intent.setClass(this, BrowserViewPagerActivity.class);
                startActivity(intent);
            }
        } else {
            JMessageClient.getUserInfo(mTargetID, new GetUserInfoCallback(false) {
                @Override
                public void gotResult(int status, String desc, UserInfo userInfo) {
                    if (status == 0) {
                        File file = userInfo.getAvatar();
                        if (file != null && file.isFile()) {
                            Bitmap bitmap = BitmapLoader.getBitmapFromFile(file.getAbsolutePath(), (int) (50 * mDensity), (int) (50 * mDensity));
                            //更新头像缓存
                            NativeImageLoader.getInstance().updateBitmapFromCache(mTargetID, bitmap);
                        }
                        android.os.Message msg = handler.obtainMessage();
                        msg.what = 1;
                        msg.obj = userInfo;
                        msg.sendToTarget();
                    } else {
                        android.os.Message msg = handler.obtainMessage();
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
