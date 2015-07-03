package io.jchat.android.view;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import io.jchat.android.R;

public class CreateGroupView extends LinearLayout{
	
	private ImageButton mReturnBtn;
	private Button mCommitBtn;
	private EditText mGroupName;

	public CreateGroupView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public void initModule(){
		mReturnBtn = (ImageButton) findViewById(R.id.creat_group_return_btn);
		mCommitBtn = (Button) findViewById(R.id.commit_btn);
		mGroupName = (EditText) findViewById(R.id.input_group_id);
	}
	
	public void setListeners(OnClickListener onClickListener){
		mReturnBtn.setOnClickListener(onClickListener);
		mCommitBtn.setOnClickListener(onClickListener);
	}
	
	public String getGroupName(){
		return mGroupName.getText().toString().trim();
	}

	public void groupNameError(Context context) {
		Toast.makeText(context, "群名不能为空", Toast.LENGTH_SHORT).show();
	}

}
