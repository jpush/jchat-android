package io.jchat.android.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import io.jchat.android.R;

/**
 * Created by jpush on 2015/7/30.
 */
public class EditNoteNameActivity extends Activity implements View.OnClickListener {

    private ImageButton mReturnBtn;
    private TextView mTitle;
    private Button mCommitBtn;
    private EditText mNoteNameEt;
    private EditText mFriendInfoEt;
    private ImageButton mResetBtn;
    private TextView mCountTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

                break;
            case R.id.delete_iv:
                mFriendInfoEt.setText("");
                break;
        }
    }
}
