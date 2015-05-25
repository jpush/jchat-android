package cn.jpush.im.android.demo.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.jpush.im.android.demo.R;

public class GroupSettingView extends RelativeLayout{
	
	private TextView mDialogName;
	private EditText mEditName;
	private TextView mHint;
	private Button mCancel;
	private Button mCommit;

	public GroupSettingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public void initModule(){
		mDialogName = (TextView) findViewById(R.id.dialog_name);
		mEditName = (EditText) findViewById(R.id.set_group);
		mHint = (TextView) findViewById(R.id.nick_name_hint);
		mCancel = (Button) findViewById(R.id.cancel_btn);
		mCommit = (Button) findViewById(R.id.commit_btn);
	}
	
	public void setListeners(OnClickListener onClickListener){
		mCancel.setOnClickListener(onClickListener);
		mCommit.setOnClickListener(onClickListener);
	}

	public void setTitleText(String string) {
		mDialogName.setText(string);
	}

	public String getResultName() {
		return mEditName.getText().toString().trim();
	}

	public void setEditText(String nickname) {
		mEditName.setHint(nickname);
	}

	public void setVisible() {
		mHint.setVisibility(View.VISIBLE);
	}

}
