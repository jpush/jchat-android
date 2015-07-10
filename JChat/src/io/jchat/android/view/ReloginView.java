package io.jchat.android.view;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import io.jchat.android.R;

public class ReloginView extends LinearLayout {

    private ScrollView mScrollView;
    private TextView mTitle;
    private EditText mPassword;
    private Button mReloginBtn;
    private RoundImageView mUserAvatarIv;
    private TextView mSwitchBtn;
    private TextView mUserNameTv;
    private Button mRegisterBtn;
    private Context mContext;
    private Listener mListener;

    public ReloginView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void initModule() {
        mScrollView = (ScrollView) findViewById(R.id.scroll_view);
        mTitle = (TextView) findViewById(R.id.title_bar_title);
        mPassword = (EditText) findViewById(R.id.relogin_password);
        mReloginBtn = (Button) findViewById(R.id.relogin_btn);
        mSwitchBtn = (TextView) findViewById(R.id.relogin_switch_user_btn);
        mUserNameTv = (TextView) findViewById(R.id.username_tv);
        mRegisterBtn = (Button) findViewById(R.id.register_btn);
        mUserAvatarIv = (RoundImageView) findViewById(R.id.relogin_head_icon);
        mTitle.setText(mContext.getString(R.string.app_name));
        mRegisterBtn.requestFocus();
    }

    public void setListener(Listener listener){
        this.mListener = listener;
    }

    public interface Listener {
        void onSoftKeyboardShown(int softKeyboardHeight);
    }

    public void setListeners(OnClickListener onClickListener) {
        mReloginBtn.setOnClickListener(onClickListener);
        mSwitchBtn.setOnClickListener(onClickListener);
        mRegisterBtn.setOnClickListener(onClickListener);
        mPassword.setOnClickListener(onClickListener);
    }

    public String getPassword() {
        return mPassword.getText().toString().trim();
    }

    public void passwordError(Context context) {
        Toast.makeText(context, context.getString(R.string.password_not_null_toast), Toast.LENGTH_SHORT).show();
    }

    public void setUserName(String userName) {
        mUserNameTv.setText(userName);
    }

    public void setmUserAvatarIv(Bitmap bitmap) {
        mUserAvatarIv.setImageBitmap(bitmap);
    }

    public void setRegisterBtnVisible(int visibility){
        mRegisterBtn.setVisibility(visibility);
    }

    public void setToBottom() {
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                Log.i("ReloginView", "set to bottom");
                mScrollView.scrollTo(0, 200);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        Rect rect = new Rect();
        Activity activity = (Activity)getContext();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        int statusBarHeight = rect.top;
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeight = dm.heightPixels;
        int diff = (screenHeight - statusBarHeight) - height;
        if(mListener != null){
            mListener.onSoftKeyboardShown(diff);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
