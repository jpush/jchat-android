package io.jchat.android.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import io.jchat.android.R;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.chatting.utils.DialogCreator;
import io.jchat.android.chatting.utils.HandleResponseCode;

public class EditNoteNameActivity extends Activity implements View.OnClickListener {

    private ImageButton mReturnBtn;
    private TextView mTitle;
    private Button mCommitBtn;
    private EditText mNoteNameEt;
    private EditText mFriendInfoEt;
    private ImageButton mResetBtn;
    private TextView mCountTv;
    private Context mContext;
    private Dialog mDialog;
    private static final int UPDATE_NOTE_NAME = 0x1100;
    private String mNotename;
    private MyHandler myHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
        setContentView(R.layout.activity_edit_note_name);
        initModule();
    }

    private void initModule() {
        mReturnBtn = (ImageButton) findViewById(R.id.return_btn);
        mTitle = (TextView) findViewById(R.id.jmui_title_tv);
        mCommitBtn = (Button) findViewById(R.id.jmui_commit_btn);
        mNoteNameEt = (EditText) findViewById(R.id.edit_note_name_et);
        mFriendInfoEt = (EditText) findViewById(R.id.edit_friend_info_et);
        mResetBtn = (ImageButton) findViewById(R.id.delete_iv);
        mCountTv = (TextView) findViewById(R.id.text_count_tv);

        mTitle.setText(getString(R.string.note_name));
        mNoteNameEt.setHint(getIntent().getStringExtra("noteName"));
        mFriendInfoEt.setHint(getIntent().getStringExtra("friendDescription"));
        mFriendInfoEt.addTextChangedListener(watcher);

        mReturnBtn.setOnClickListener(this);
        mCommitBtn.setOnClickListener(this);
        mResetBtn.setOnClickListener(this);
    }

    TextWatcher watcher = new TextWatcher() {
        private CharSequence temp;
        private int editStart;
        private int editEnd;
        private String note;
        @Override
        public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence s, int i, int i1, int i2) {
            temp = s;
        }

        @Override
        public void afterTextChanged(Editable editable) {
            editStart = mFriendInfoEt.getSelectionStart();
            editEnd = mFriendInfoEt.getSelectionEnd();
            if (temp.length() >= 0) {
                note = "" + (temp.length()) + "/" + (200 - temp.length()) + "";
                mCountTv.setText(note);
            }
            if (temp.length() > 200) {
                editable.delete(editStart - 1, editEnd);
                int tempSelection = editStart;
                mFriendInfoEt.setText(editable);
                mFriendInfoEt.setSelection(tempSelection);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.return_btn:
                finish();
                break;
            case R.id.jmui_commit_btn:
                mNotename = mNoteNameEt.getText().toString();
                final String noteText = mFriendInfoEt.getText().toString();
                String targetId = getIntent().getStringExtra(JChatDemoApplication.TARGET_ID);
                String appKey = getIntent().getStringExtra(JChatDemoApplication.TARGET_APP_KEY);
                if (!TextUtils.isEmpty(mNotename)) {
                    mDialog = DialogCreator.createLoadingDialog(mContext, mContext.getString(R.string.saving_hint));
                    mDialog.show();
                    JMessageClient.getUserInfo(targetId, appKey, new GetUserInfoCallback() {
                        @Override
                        public void gotResult(final int status, final String desc, final UserInfo userInfo) {
                            if (status == 0) {
                                userInfo.updateNoteName(mNotename, new BasicCallback() {
                                    @Override
                                    public void gotResult(int status, String s) {
                                        if (status == 0) {
                                            if (!TextUtils.isEmpty(noteText)) {
                                                userInfo.updateNoteText(noteText, new BasicCallback() {
                                                    @Override
                                                    public void gotResult(int status, String s) {
                                                        myHandler.sendEmptyMessage(UPDATE_NOTE_NAME);
                                                        if (status != 0) {
                                                            HandleResponseCode.onHandle(mContext, status, false);
                                                        }
                                                    }
                                                });
                                            } else {
                                                myHandler.sendEmptyMessage(UPDATE_NOTE_NAME);
                                            }
                                        } else {
                                            HandleResponseCode.onHandle(mContext, status, false);
                                        }
                                    }
                                });
                            }
                        }
                    });
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.note_name_is_empty_hint),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.delete_iv:
                mFriendInfoEt.setText("");
                break;
        }
    }

    private static class MyHandler extends Handler {

        private WeakReference<EditNoteNameActivity> mActivity;

        public MyHandler(EditNoteNameActivity activity) {
            mActivity = new WeakReference<EditNoteNameActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            EditNoteNameActivity activity = mActivity.get();
            if (null != activity) {
                switch (msg.what) {
                    case UPDATE_NOTE_NAME:
                        if (null != activity.mDialog) {
                            activity.mDialog.dismiss();
                            Intent intent = new Intent();
                            intent.putExtra(JChatDemoApplication.NOTENAME, activity.mNotename);
                            activity.setResult(JChatDemoApplication.RESULT_CODE_EDIT_NOTENAME, intent);
                            activity.finish();
                        }
                        break;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mDialog) {
            mDialog.dismiss();
        }
    }
}
