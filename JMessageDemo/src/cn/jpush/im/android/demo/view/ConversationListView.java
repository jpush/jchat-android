package cn.jpush.im.android.demo.view;


import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import cn.jpush.im.android.demo.R;


public class ConversationListView {
	
	private View mMsgView;
	private ListView mConvListView = null;
	private TextView mTitle;
	private ImageButton mCreateGroup;
    private Context mContext;

	public ConversationListView(View view, Context context) {
		this.mMsgView = view;
        this.mContext = context;
	}


	public void initModule(){
		mTitle = (TextView) mMsgView.findViewById(R.id.main_title_bar_title);
		mTitle.setText(mContext.getString(R.string.actionbar_conversation));
		mConvListView = (ListView) mMsgView.findViewById(R.id.conv_list_view);
		mCreateGroup = (ImageButton) mMsgView.findViewById(R.id.create_group_btn);
	}
	
	public void setConvListAdapter(ListAdapter adapter){
		mConvListView.setAdapter(adapter);
	}
	
	public void setListener(OnClickListener onClickListener){
		mCreateGroup.setOnClickListener(onClickListener);
	}
	
	public void setItemListeners(OnItemClickListener onClickListener) {
		mConvListView.setOnItemClickListener(onClickListener);
	}
	
	public void setLongClickListener(OnItemLongClickListener listener){
		mConvListView.setOnItemLongClickListener(listener);
	}




}
