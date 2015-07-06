package io.jchat.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;


import io.jchat.android.R;

import io.jchat.android.controller.ReloginController;
import io.jchat.android.tools.ActivityManager;
import io.jchat.android.tools.NativeImageLoader;
import io.jchat.android.tools.SharePreferenceManager;
import io.jchat.android.view.ReloginView;

public class ReloginActivity extends BaseActivity {

    private ReloginView mReloginView;
    private ReloginController mReloginController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_re_login);
        ActivityManager.addActivity(this);
        mReloginView = (ReloginView) findViewById(R.id.relogin_view);
        mReloginView.initModule();
        fillContent();
        mReloginView.setListeners(mReloginController);
    }

    private void fillContent() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        float mDensity = dm.density;
        String userName = getIntent().getStringExtra("userName");
        String userAvatarPath = getIntent().getStringExtra("userAvatar");
        if (null != userAvatarPath) {
            Bitmap bm = NativeImageLoader.getInstance().loadNativeImage(userAvatarPath, (int) (80 * mDensity), new NativeImageLoader.NativeImageCallBack() {

                @Override
                public void onImageLoader(Bitmap bitmap, String path) {
                    if (bitmap != null) {
                        mReloginView.setmUserAvatarIv(bitmap);
                    }
                }
            });
            if (null != bm) {
                mReloginView.setmUserAvatarIv(bm);
            }
        }
        mReloginView.setUserName(userName);
        mReloginController = new ReloginController(mReloginView, this, userName);

        SharePreferenceManager.setCachedUsername(userName);
        SharePreferenceManager.setCachedAvatarPath(userAvatarPath);
    }


    public void StartRelogin() {
        // TODO Auto-generated method stub
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        startActivity(intent);
        ActivityManager.clearList();
    }


    public void StartSwitchUser() {
        // TODO Auto-generated method stub
        Intent intent = new Intent();
        intent.putExtra("fromSwitch", true);
        intent.setClass(this, LoginActivity.class);
        startActivity(intent);
    }


    public void StartRegisterActivity() {
        Intent intent = new Intent();
        intent.setClass(this, RegisterActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
