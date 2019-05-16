package jiguang.chat.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import jiguang.chat.R;
import jiguang.chat.adapter.ChatRoomAdapter;
import jiguang.chat.controller.ChatRoomController;

/**
 * Created by ${chenyn} on 2017/10/31.
 */

public class ChatRoomView extends LinearLayout{
    private ChatRoomController mListener;
    private Context mContext;
    private ListView mChatRoomListView;
    private LayoutInflater mInflater;
    private LinearLayout mSearch_title;
    private SmartRefreshLayout smartRefreshLayout;

    public ChatRoomView(Context context) {
        super(context);
    }

    public ChatRoomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
    }


    public void initModule() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_search, null);
        TextView titleText = view.findViewById(R.id.tv_chatRoomTitle);
        titleText.setText("聊天室ID");
        mSearch_title = view.findViewById(R.id.search_title);
        mChatRoomListView = findViewById(R.id.lv_chatRoom);
        mChatRoomListView.addHeaderView(view);
        smartRefreshLayout = findViewById(R.id.refreshLayout);
    }

    public void setListener(ChatRoomController listener) {
        mChatRoomListView.setOnItemClickListener(listener);
    }

    public void setClickListener(ChatRoomController listener) {
        mSearch_title.setOnClickListener(listener);
    }

    public void setChatRoomAdapter(ChatRoomAdapter chatRoomAdapter) {
        mChatRoomListView.setAdapter(chatRoomAdapter);
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        smartRefreshLayout.setOnRefreshListener(onRefreshListener);
    }

    public void setOnLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        smartRefreshLayout.setOnLoadMoreListener(loadMoreListener);
    }

}
