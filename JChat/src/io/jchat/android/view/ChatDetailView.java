package io.jchat.android.view;



import io.jchat.android.adapter.GroupMemberGridAdapter;

import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.jchat.android.R;

public class ChatDetailView extends LinearLayout{
	
	private RelativeLayout mGroupNameRL;
	private LinearLayout mMyNameLL;
	private RelativeLayout mGroupNumRL;
	private LinearLayout mGroupChatRecordLL;
	private RelativeLayout mGroupChatDelRL;
	private ImageButton mReturnBtn;
	private TextView mTitle;
	private ImageButton mMenuBtn;
	private Button mDelGroupBtn;
	private TextView mGroupName;
	private TextView mGroupNum;
	private TextView mMyName;
	private GridView mGridView;
    private Context mContext;
	private View mDividingLine;

	public ChatDetailView(Context context, AttributeSet attrs) {
		super(context, attrs);
        this.mContext = context;
		// TODO Auto-generated constructor stub
	}
	
	public void initModule(){
		mGroupNameRL = (RelativeLayout) findViewById(R.id.group_name_rl);
		mMyNameLL = (LinearLayout) findViewById(R.id.group_my_name_ll);
		mGroupNumRL = (RelativeLayout) findViewById(R.id.group_num_rl);
		mGroupChatRecordLL = (LinearLayout) findViewById(R.id.group_chat_record_ll);
		mGroupChatDelRL = (RelativeLayout) findViewById(R.id.group_chat_del_rl);
		mReturnBtn = (ImageButton) findViewById(R.id.return_btn);
		mTitle = (TextView) findViewById(R.id.title);
		mMenuBtn = (ImageButton) findViewById(R.id.right_btn);
		mDelGroupBtn = (Button) findViewById(R.id.chat_detail_del_group);
		mGroupName = (TextView) findViewById(R.id.chat_detail_group_name);
		mGroupNum = (TextView) findViewById(R.id.chat_detail_group_num);
		mDividingLine = findViewById(R.id.group_num_dividing_line);
		mMyName = (TextView) findViewById(R.id.chat_detail_my_name);
		mGridView = (GridView) findViewById(R.id.chat_detail_group_gv);
		
		mTitle.setText(mContext.getString(R.string.chat_detail_title));
		mMenuBtn.setVisibility(View.GONE);
		//自定义GridView点击背景为透明色
		mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
	}
	
	public void setListeners(OnClickListener onClickListener){
		mGroupNameRL.setOnClickListener(onClickListener);
		mMyNameLL.setOnClickListener(onClickListener);
		mGroupNumRL.setOnClickListener(onClickListener);
		mGroupChatRecordLL.setOnClickListener(onClickListener);
		mGroupChatDelRL.setOnClickListener(onClickListener);
		mReturnBtn.setOnClickListener(onClickListener);
		mDelGroupBtn.setOnClickListener(onClickListener);
	}
	
	public void setItemListener(OnItemClickListener listener){
		mGridView.setOnItemClickListener(listener);
	}
	
	public void setLongClickListener(OnItemLongClickListener listener){
		mGridView.setOnItemLongClickListener(listener);
	}
	
	public void setChatDetailInfo(String[] detailInfo){
//		mGroupName.setText(detailInfo[0]);
	}
	
	public void setAdapter(GroupMemberGridAdapter adapter){
		mGridView.setAdapter(adapter);
	}

	public void setGroupName(String str) {
		mGroupName.setText(str);
	}

	public void setMyName(String str) {
		mMyName.setText(str);
	}
	
	public void setGroupNum(int num){
		mGroupNum.setText(num + "");
	}

	public void setSingleView() {
		mGroupNameRL.setVisibility(View.GONE);
		mGroupNumRL.setVisibility(View.GONE);
		mDividingLine.setVisibility(View.GONE);
		mMyNameLL.setVisibility(View.GONE);
		mDelGroupBtn.setVisibility(View.GONE);
	}

    public void updateGroupName(String newName) {
        mGroupName.setText(newName);
    }

    public void refreshGroupName(String groupName) {
        if(groupName != null){
            mGroupName.setText(groupName);
        }
    }
}
