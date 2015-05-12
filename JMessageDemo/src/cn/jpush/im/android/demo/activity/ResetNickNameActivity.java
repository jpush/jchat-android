package cn.jpush.im.android.demo.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.demo.R;

import cn.jpush.im.android.api.JMessageClient;

import cn.jpush.im.android.demo.tools.HandleResponseCode;
import cn.jpush.im.android.demo.view.LoadingDialog;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by Ken on 2015/2/13.
 */
public class ResetNickNameActivity extends BaseActivity {

    private ImageButton mReturnBtn;
    private TextView mTitleTv;
    private Button mCommitBtn;
    private EditText mNickNameEt;
    private LoadingDialog mLD;
    private Dialog mDialog;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_nick_name);
        mContext = this;
        mReturnBtn = (ImageButton) findViewById(R.id.return_btn);
        mTitleTv = (TextView) findViewById(R.id.title_tv);
        mCommitBtn = (Button) findViewById(R.id.commit_btn);
        mNickNameEt = (EditText) findViewById(R.id.nick_name_et);

        mTitleTv.setText(this.getString(R.string.setting_username_big_hit));
        final String oldNickName = getIntent().getStringExtra("nickName");
        mNickNameEt.setHint(oldNickName);
        mReturnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mCommitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String nickName = mNickNameEt.getText().toString().trim();
                if (TextUtils.isEmpty(nickName)) {
                    Toast.makeText(mContext, mContext.getString(R.string.nickname_not_null_toast), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (oldNickName.equals(nickName)) {
                    return;
                } else {
                    mLD = new LoadingDialog();
                    mDialog = mLD.createLoadingDialog(mContext, mContext.getString(R.string.modifying_hint));
                    mDialog.show();
                    UserInfo myUserInfo = JMessageClient.getMyInfo();
                    myUserInfo.setNickname(nickName);
                    JMessageClient.updateMyInfo(UserInfo.Field.nickname, myUserInfo, new BasicCallback(false) {
                        @Override
                        public void gotResult(final int status, final String desc) {
                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mDialog.dismiss();
                                    if (status == 0) {
                                        Toast.makeText(mContext, ResetNickNameActivity.this.getString(R.string.modify_success_toast), Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent();
                                        intent.putExtra("nickName", nickName);
                                        setResult(0, intent);
                                        finish();
                                    } else HandleResponseCode.onHandle(mContext, status);
                                }
                            });
                        }
                    });
                }
            }
        });
    }
}
