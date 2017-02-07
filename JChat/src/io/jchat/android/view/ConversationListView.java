package io.jchat.android.view;


import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import io.jchat.android.R;


public class ConversationListView {
	
	private View mConvListFragment;
	private ListView mConvListView = null;
	private TextView mTitle;
	private ImageButton mCreateGroup;
	private LinearLayout mHeader;
	private RelativeLayout mLoadingHeader;
    private ImageView mLoadingIv;
    private Context mContext;

	public ConversationListView(View view, Context context) {
		this.mConvListFragment = view;
        this.mContext = context;
	}

	public void initModule() {
		mTitle = (TextView) mConvListFragment.findViewById(R.id.main_title_bar_title);
		mTitle.setText(mContext.getString(R.string.actionbar_conversation));
		mConvListView = (ListView) mConvListFragment.findViewById(R.id.conv_list_view);
		mCreateGroup = (ImageButton) mConvListFragment.findViewById(R.id.create_group_btn);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mHeader = (LinearLayout) inflater.inflate(R.layout.conv_list_head_view, mConvListView, false);
        mLoadingHeader = (RelativeLayout) inflater.inflate(R.layout.jmui_drop_down_list_header, mConvListView, false);
        mLoadingIv = (ImageView) mLoadingHeader.findViewById(R.id.jmui_loading_img);
        mConvListView.addHeaderView(mHeader);
        mConvListView.addHeaderView(mLoadingHeader);
	}
	
	public void setConvListAdapter(ListAdapter adapter) {
		mConvListView.setAdapter(adapter);
	}
	
	public void setListener(OnClickListener onClickListener) {
		mCreateGroup.setOnClickListener(onClickListener);
	}
	
	public void setItemListeners(OnItemClickListener onClickListener) {
		mConvListView.setOnItemClickListener(onClickListener);
	}
	
	public void setLongClickListener(OnItemLongClickListener listener) {
		mConvListView.setOnItemLongClickListener(listener);
	}

    public void showHeaderView() {
        mHeader.findViewById(R.id.network_disconnected_iv).setVisibility(View.VISIBLE);
        mHeader.findViewById(R.id.check_network_hit).setVisibility(View.VISIBLE);
    }

    public void dismissHeaderView() {
		mHeader.findViewById(R.id.network_disconnected_iv).setVisibility(View.GONE);
		mHeader.findViewById(R.id.check_network_hit).setVisibility(View.GONE);
    }


	public void showLoadingHeader() {
        mLoadingIv.setVisibility(View.VISIBLE);
        AnimationDrawable drawable = (AnimationDrawable) mLoadingIv.getDrawable();
        drawable.start();
        mLoadingIv.setVisibility(View.VISIBLE);
	}

    public void dismissLoadingHeader() {
        mLoadingIv.setVisibility(View.GONE);
    }
}
