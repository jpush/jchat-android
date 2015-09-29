package io.jchat.android.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;

import cn.jpush.im.android.api.JMessageClient;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.tools.DialogCreator;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by Ken on 2015/2/13.
 */

/*
编辑个性签名界面
 */
public class EditSignatureActivity extends BaseActivity implements View.OnClickListener{

    private ImageButton mReturnBtn;
    private TextView mTitle;
    private Button mCommitBtn;
    private EditText mSignatureEt;
    private ImageButton mDeleteBtn;
    private TextView mTextCountTv;
    private DialogCreator mLD;
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
        mDeleteBtn = (ImageButton) findViewById(R.id.delete_iv);
        mSignatureEt = (EditText) findViewById(R.id.signature_et);
        mDeleteBtn.setOnClickListener(this);
        mCommitBtn.setOnClickListener(this);
        mReturnBtn.setOnClickListener(this);
        mSignatureEt.setHint(getIntent().getStringExtra("OldSignature"));
        mSignatureEt.addTextChangedListener(watcher);
        mTitle.setText(this.getString(R.string.edit_signature_title));
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
            if (temp.length() >= 0) {
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

    private void dismissSoftInput() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        //隐藏软键盘
        InputMethodManager imm = ((InputMethodManager) mContext
                .getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.return_btn:
                finish();
                break;
            case R.id.commit_btn:
                final String signature = mSignatureEt.getText().toString().trim();
                if (TextUtils.isEmpty(signature)) {
                    Toast.makeText(mContext, mContext.getString(R.string.input_signature_toast), Toast.LENGTH_SHORT).show();
                    return;
                }
                mLD = new DialogCreator();
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
                                    dismissSoftInput();
                                    HandleResponseCode.onHandle(mContext, status, false);
                                }
                            }
                        });
                    }
                });
                break;
            case R.id.delete_iv:
                mSignatureEt.setText("");
                break;
        }
    }
}
