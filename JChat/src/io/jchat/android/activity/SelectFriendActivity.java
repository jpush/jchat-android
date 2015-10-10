package io.jchat.android.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;

import io.jchat.android.R;
import io.jchat.android.controller.SelectFriendController;
import io.jchat.android.view.SelectFriendView;

/**
 * Created by jpush on 2015/10/9.
 */
public class SelectFriendActivity extends Activity {

    private SelectFriendView mView;
    private SelectFriendController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);
        mView = (SelectFriendView) findViewById(R.id.select_friend_view);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        float ratioWidth = (float)screenWidth / 720;
        float ratioHeight = (float)screenHeight / 1280;
        float ratio = Math.min(ratioWidth, ratioHeight);
        mView.initModule(ratio);
        mController = new SelectFriendController(mView, this);
        mView.setListeners(mController);
        mView.setSideBarTouchListener(mController);
        mView.setTextWatcher(mController);
    }
}
