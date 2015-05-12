package cn.jpush.im.android.demo.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import cn.jpush.im.android.demo.R;

import cn.jpush.im.android.api.JMessageClient;

import cn.jpush.im.android.demo.tools.HandleResponseCode;
import cn.jpush.im.api.BasicCallback;

public class ResetPasswordActivity extends BaseActivity {

    private ImageButton mReturnBtn;
    private TextView mTitle;
    private ImageButton mMenuBtn;
    private EditText mNewPwdEt;
    private EditText mConfirmPwdEt;
    private Button mCommit;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mContext = this;
        mReturnBtn = (ImageButton) findViewById(R.id.return_btn);
        mTitle = (TextView) findViewById(R.id.title);
        mMenuBtn = (ImageButton) findViewById(R.id.right_btn);
        mNewPwdEt = (EditText) findViewById(R.id.new_password_et);
        mConfirmPwdEt = (EditText) findViewById(R.id.confirm_password_et);
        mCommit = (Button) findViewById(R.id.commit_btn);

        mTitle.setText(this.getString(R.string.change_password));
        mMenuBtn.setVisibility(View.GONE);
        mReturnBtn.setOnClickListener(listener);
        mCommit.setOnClickListener(listener);
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.return_btn:
                    finish();
                    break;
                case R.id.commit_btn:
                    String newPwd = mNewPwdEt.getText().toString().trim();
                    String confirmPwd = mConfirmPwdEt.getText().toString().trim();
                    if(TextUtils.isEmpty(newPwd) || TextUtils.isEmpty(confirmPwd)){
                        Toast.makeText(mContext, mContext.getString(R.string.password_not_null_toast), Toast.LENGTH_SHORT).show();
                    }else {
                        if(newPwd.equals(confirmPwd)){
                            if(JMessageClient.isCurrentUserPasswordValid(newPwd)){
                                Toast.makeText(mContext, mContext.getString(R.string.password_same_to_previous), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            final ProgressDialog dialog = new ProgressDialog(mContext);
                            dialog.setMessage(mContext.getString(R.string.modifying_hint));
                            dialog.show();
                            JMessageClient.updateUserPassword(getIntent().getStringExtra("oldPassword"),
                                    newPwd, new BasicCallback(false) {
                                @Override
                                public void gotResult(final int status, final String desc) {
                                    ResetPasswordActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.dismiss();
                                            if(status == 0){
                                                finish();
                                            }else{
                                                HandleResponseCode.onHandle(mContext, status);
                                            }
                                        }
                                    });
                                }
                            });
                        }
                        else Toast.makeText(mContext, mContext.getString(R.string.password_not_match_toast), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

}
