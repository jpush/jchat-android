package jiguang.chat.controller;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;

import cn.jpush.im.android.api.ChatRoomManager;
import cn.jpush.im.android.api.callback.RequestCallback;
import cn.jpush.im.android.api.model.ChatRoomInfo;
import jiguang.chat.R;
import jiguang.chat.activity.ChatRoomDetailActivity;
import jiguang.chat.activity.SearchChatRoomActivity;
import jiguang.chat.adapter.ChatRoomAdapter;
import jiguang.chat.utils.HandleResponseCode;
import jiguang.chat.view.ChatRoomView;

/**
 * Created by ${chenyn} on 2017/10/31.
 */

public class ChatRoomController implements AdapterView.OnItemClickListener, View.OnClickListener{
    private ChatRoomView mChatRoomView;
    private Context mContext;

    public ChatRoomController(ChatRoomView chatRoomView, Context context) {
        this.mChatRoomView = chatRoomView;
        this.mContext = context;
        initChatRoomAdapter();
    }

    private void initChatRoomAdapter() {

        ChatRoomManager.getChatRoomListByApp(0, 10, new RequestCallback<List<ChatRoomInfo>>() {
            @Override
            public void gotResult(int i, String s, List<ChatRoomInfo> chatRoomInfos) {
                if (i == 0) {
                    mChatRoomView.setChatRoomAdapter(new ChatRoomAdapter(mContext, chatRoomInfos, mChatRoomView));
                } else {
                    HandleResponseCode.onHandle(mContext, i, false);
                }
            }
        });
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object itemAtPosition = parent.getItemAtPosition(position);
        if (itemAtPosition != null && itemAtPosition instanceof ChatRoomInfo) {
            ChatRoomInfo info = (ChatRoomInfo) itemAtPosition;
            Intent intent = new Intent(mContext, ChatRoomDetailActivity.class);
            intent.putExtra("chatRoomId", info.getRoomID());
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
}
