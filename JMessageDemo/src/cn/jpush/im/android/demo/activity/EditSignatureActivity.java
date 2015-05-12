package cn.jpush.im.android.demo.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.demo.R;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.demo.view.LoadingDialog;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by Ken on 2015/2/13.
 */

/*
编辑个性签名界面
 */
public class EditSignatureActivity extends BaseActivity {

    private ImageButton mReturnBtn;
    private TextView mTitle;
    private Button mCommitBtn;
    private EditText mSignatureEt;
    private TextView mTextCountTv;
    private LoadingDialog mLD;
    private Dialog mDialog;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_signature);
        mContext = this;
        mReturnBtn = (ImageButton) findViewById(R.id.return_btn);
        mTitle = (TextView) findViewById(R.id.title_tv);
        mCommitBtn = (Button) findViewById(R.id.commit_btn);
        mTextCountTv = (TextView) findViewById(R.id.text_count_tv);
        mSignatureEt = (EditText) findViewById(R.id.signature_et);
        mSignatureEt.setHint(getIntent().getStringExtra("OldSignature"));
        mSignatureEt.addTextChangedListener(watcher);
        mTitle.setText(this.getString(R.string.edit_signature_title));
        mReturnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mCommitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String signature = mSignatureEt.getText().toString().trim();
                if (TextUtils.isEmpty(signature)) {
                    Toast.makeText(mContext, mContext.getString(R.string.input_signature_toast), Toast.LENGTH_SHORT).show();
                    return;
                }
                mLD = new LoadingDialog();
                mDialog = mLD.createLoadingDialog(mContext, mContext.getString(R.string.modifying_hint));
                mDialog.show();
                UserInfo myUserInfo = JMessageClient.getMyInfo();
                myUserInfo.setSignature(mSignatureEt.getText().toString().trim());
                JMessageClient.updateMyInfo(UserInfo.Field.signature, myUserInfo, new BasicCallback(false) {
                    @Override
                    public void gotResult(final int status, final String desc) {
                        EditSignatureActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mDialog.dismiss();
                                if (status == 0) {
                                    Toast.makeText(mContext, mContext.getString(R.string.modify_success_toast), Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent();
                                    intent.putExtra("signature", signature);
                                    setResult(1, intent);
                                    finish();
                                } else {
                                    Toast.makeText(mContext, desc, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

            }
        });
    }

    TextWatcher watcher = new TextWatcher() {
        private CharSequence temp;
        private int editStart;
        private int editEnd;

        @Override
        public void beforeTextChanged(CharSequence s, int arg1, int arg2,
                                      int arg3) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int count,
                                  int after) {
            // TODO Auto-generated method stub
            temp = s;
        }

        @Override
        public void afterTextChanged(Editable s) {
            editStart = mSignatureEt.getSelectionStart();
            editEnd = mSignatureEt.getSelectionEnd();
            if (temp.length() > 0) {
                mTextCountTv.setText("" + (30 - temp.length()) + "");
            }
            if (temp.length() > 30) {
                s.delete(editStart - 1, editEnd);
                int tempSelection = editStart;
                mSignatureEt.setText(s);
                mSignatureEt.setSelection(tempSelection);
            }
        }

    };
}
