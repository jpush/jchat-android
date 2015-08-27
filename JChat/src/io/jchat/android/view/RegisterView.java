package io.jchat.android.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.jchat.android.R;
import io.jchat.android.activity.RegisterActivity;


public class RegisterView extends LinearLayout {

    private EditText mUserId;
    private EditText mPassword;
    private Button mRegistBtn;
    private ImageButton mReturnBtn;
    private Listener mListener;
    private Context mContext;


    public RegisterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void initModule() {
        // 获取注册所用的用户名、密码、昵称
        mUserId = (EditText) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);
        mRegistBtn = (Button) findViewById(R.id.regist_btn);
        mReturnBtn = (ImageButton) findViewById(R.id.return_btn);
        mUserId.requestFocus();
    }

    public String getUserId() {
        return mUserId.getText().toString().trim();
    }

    public String getPassword() {
        return mPassword.getText().toString().trim();
    }

    public void setListeners(OnClickListener onclickListener) {
        mRegistBtn.setOnClickListener(onclickListener);
        mReturnBtn.setOnClickListener(onclickListener);
    }

    public void userNameError(Context context) {
        Toast.makeText(context, context.getString(R.string.username_not_null_toast), Toast.LENGTH_SHORT).show();
    }

    public void passwordError(Context context) {
        Toast.makeText(context, context.getString(R.string.password_not_null_toast), Toast.LENGTH_SHORT).show();
    }

    public void passwordLengthError(RegisterActivity context) {
        Toast.makeText(context, context.getString(R.string.password_length_illegal), Toast.LENGTH_SHORT).show();
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public interface Listener {
        void onSoftKeyboardShown(int w, int h, int oldw, int oldh);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mListener != null) {
            mListener.onSoftKeyboardShown(w, h, oldw, oldh);
        }
    }
}
