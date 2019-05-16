package jiguang.chat.controller;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.ChatRoomManager;
import cn.jpush.im.android.api.callback.RequestCallback;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.model.ChatRoomInfo;
import jiguang.chat.R;
import jiguang.chat.activity.ChatActivity;
import jiguang.chat.activity.SearchChatRoomActivity;
import jiguang.chat.adapter.ChatRoomAdapter;
import jiguang.chat.application.JGApplication;
import jiguang.chat.utils.DialogCreator;
import jiguang.chat.utils.HandleResponseCode;
import jiguang.chat.view.ChatRoomView;

/**
 * Created by ${chenyn} on 2017/10/31.
 */

public class ChatRoomController implements AdapterView.OnItemClickListener, View.OnClickListener, OnRefreshListener, OnLoadMoreListener {
    private ChatRoomView mChatRoomView;
    private Context mContext;
    private static final int PAGE_COUNT = 15;
    private List<ChatRoomInfo> chatRoomInfos = new ArrayList<>();
    private ChatRoomAdapter chatRoomAdapter;

    public ChatRoomController(ChatRoomView chatRoomView, Context context) {
        this.mChatRoomView = chatRoomView;
        this.mContext = context;
        initChatRoomAdapter();
    }

    private void initChatRoomAdapter() {
        Dialog loadingDialog = DialogCreator.createLoadingDialog(mContext, "正在加载...");
        loadingDialog.show();
        ChatRoomManager.getChatRoomListByApp(0, PAGE_COUNT, new RequestCallback<List<ChatRoomInfo>>() {
            @Override
            public void gotResult(int i, String s, List<ChatRoomInfo> result) {
                loadingDialog.dismiss();
                if (i == 0) {
                    chatRoomInfos.addAll(result);
                } else {
                    HandleResponseCode.onHandle(mContext, i, false);
                }
                chatRoomAdapter = new ChatRoomAdapter(mContext, chatRoomInfos, mChatRoomView);
                mChatRoomView.setChatRoomAdapter(chatRoomAdapter);
            }
        });
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object itemAtPosition = parent.getItemAtPosition(position);
        if (itemAtPosition != null && itemAtPosition instanceof ChatRoomInfo) {
            ChatRoomInfo info = (ChatRoomInfo) itemAtPosition;
            Intent intent = new Intent(mContext, ChatActivity.class);
            intent.putExtra(JGApplication.CONV_TYPE, ConversationType.chatroom);
            intent.putExtra("chatRoomId", info.getRoomID());
            intent.putExtra("chatRoomName", info.getName());
            mContext.startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.search_title) {
            Intent intent = new Intent(mContext, SearchChatRoomActivity.class);
            mContext.startActivity(intent);
        }
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        ChatRoomManager.getChatRoomListByApp(0, PAGE_COUNT, new RequestCallback<List<ChatRoomInfo>>() {
            @Override
            public void gotResult(int i, String s, List<ChatRoomInfo> result) {
                if (i == 0) {
                    chatRoomInfos.clear();
                    chatRoomInfos.addAll(result);
                    if (chatRoomAdapter != null) {
                        chatRoomAdapter.notifyDataSetChanged();
                    }
                } else {
                    HandleResponseCode.onHandle(mContext, i, false);
                }
                refreshLayout.finishRefresh();
            }
        });
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        ChatRoomManager.getChatRoomListByApp(chatRoomInfos.size(), PAGE_COUNT, new RequestCallback<List<ChatRoomInfo>>() {
            @Override
            public void gotResult(int i, String s, List<ChatRoomInfo> result) {
                if (i == 0) {
                    chatRoomInfos.addAll(result);
                    if (chatRoomAdapter != null) {
                        chatRoomAdapter.notifyDataSetChanged();
                    }
                } else {
                    HandleResponseCode.onHandle(mContext, i, false);
                }
                refreshLayout.finishLoadMore();
            }
        });
    }
}
