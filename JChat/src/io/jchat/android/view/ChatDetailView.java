package io.jchat.android.view;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.jchat.android.R;
import io.jchat.android.adapter.GroupMemberGridAdapter;

public class ChatDetailView extends LinearLayout{

	private LinearLayout mAllGroupMemberLL;
	private View mSplitLine1;
	private View mSplitLine2;
	private LinearLayout mGroupNameLL;
	private LinearLayout mMyNameLL;
	private LinearLayout mGroupNumLL;
	private LinearLayout mGroupChatRecordLL;
	private LinearLayout mGroupChatDelLL;
	private ImageButton mReturnBtn;
	private TextView mTitle;
    private TextView mMembersNum;
	private ImageButton mMenuBtn;
	private Button mDelGroupBtn;
	private TextView mGroupName;
	private TextView mGroupNum;
	private TextView mMyName;
	private GroupGridView mGridView;
	private SlipButton mNoDisturbBtn;
    private Context mContext;

	public ChatDetailView(Context context, AttributeSet attrs) {
		super(context, attrs);
        this.mContext = context;
		// TODO Auto-generated constructor stub
	}
	
	public void initModule(){
        mAllGroupMemberLL = (LinearLayout) findViewById(R.id.all_member_ll);
		mSplitLine1 = findViewById(R.id.all_member_split_line1);
        mSplitLine2 = findViewById(R.id.all_member_split_line2);
		mGroupNameLL = (LinearLayout) findViewById(R.id.group_name_ll);
		mMyNameLL = (LinearLayout) findViewById(R.id.group_my_name_ll);
		mGroupNumLL = (LinearLayout) findViewById(R.id.group_num_ll);
		mGroupChatRecordLL = (LinearLayout) findViewById(R.id.group_chat_record_ll);
		mGroupChatDelLL = (LinearLayout) findViewById(R.id.group_chat_del_ll);
		mReturnBtn = (ImageButton) findViewById(R.id.return_btn);
		mTitle = (TextView) findViewById(R.id.title);
        mMembersNum = (TextView) findViewById(R.id.members_num);
		mMenuBtn = (ImageButton) findViewById(R.id.right_btn);
		mDelGroupBtn = (Button) findViewById(R.id.chat_detail_del_group);
		mGroupName = (TextView) findViewById(R.id.chat_detail_group_name);
		mGroupNum = (TextView) findViewById(R.id.chat_detail_group_num);
		mMyName = (TextView) findViewById(R.id.chat_detail_my_name);
		mGridView = (GroupGridView) findViewById(R.id.chat_detail_group_gv);
		mNoDisturbBtn = (SlipButton) findViewById(R.id.no_disturb_slip_btn);

		mTitle.setText(mContext.getString(R.string.chat_detail_title));
		mMenuBtn.setVisibility(View.GONE);
		//自定义GridView点击背景为透明色
		mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
	}
	
	public void setListeners(OnClickListener onClickListener) {
        mAllGroupMemberLL.setOnClickListener(onClickListener);
		mGroupNameLL.setOnClickListener(onClickListener);
		mMyNameLL.setOnClickListener(onClickListener);
		mGroupNumLL.setOnClickListener(onClickListener);
		mGroupChatRecordLL.setOnClickListener(onClickListener);
		mGroupChatDelLL.setOnClickListener(onClickListener);
	    mReturnBtn.setOnClickListener(onClickListener);
		mDelGroupBtn.setOnClickListener(onClickListener);
	}

    public void setOnChangeListener(SlipButton.OnChangedListener listener) {
        mNoDisturbBtn.setOnChangedListener(R.id.no_disturb_slip_btn, listener);
    }
	
	public void setItemListener(OnItemClickListener listener) {
		mGridView.setOnItemClickListener(listener);
	}
	
//	public void setLongClickListener(OnItemLongClickListener listener) {
//		mGridView.setOnItemLongClickListener(listener);
//	}

	public void setAdapter(GroupMemberGridAdapter adapter) {
		mGridView.setAdapter(adapter);
	}

	public void setGroupName(String str) {
		mGroupName.setText(str);
	}

	public void setMyName(String str) {
		mMyName.setText(str);
	}
	
	public void setSingleView() {
		mGroupNameLL.setVisibility(View.GONE);
		mGroupNumLL.setVisibility(View.GONE);
		mMyNameLL.setVisibility(View.GONE);
		mDelGroupBtn.setVisibility(View.GONE);
	}

    public void updateGroupName(String newName) {
        mGroupName.setText(newName);
    }

	public void setTitle(int size) {
		String title = mContext.getString(R.string.chat_detail_title)
				+ mContext.getString(R.string.combine_title);
		mTitle.setText(String.format(title, size));
	}

    public GroupGridView getGridView() {
        return mGridView;
    }

    public void setMembersNum(int size) {
        String text = mContext.getString(R.string.all_group_members)
                + mContext.getString(R.string.combine_title);
        mMembersNum.setText(String.format(text, size));
    }

	public void dismissAllMembersBtn() {
        mSplitLine1.setVisibility(View.GONE);
        mSplitLine2.setVisibility(View.GONE);
		mAllGroupMemberLL.setVisibility(View.GONE);
	}

    public void initNoDisturb(int status) {
        mNoDisturbBtn.setChecked(status == 1);
    }

	public void setNoDisturbChecked(boolean flag) {
        mNoDisturbBtn.setChecked(flag);
    }
}
