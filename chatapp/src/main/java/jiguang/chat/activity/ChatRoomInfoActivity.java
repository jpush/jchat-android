package jiguang.chat.activity;

import android.os.Bundle;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import cn.jpush.im.android.api.ChatRoomManager;
import cn.jpush.im.android.api.callback.RequestCallback;
import cn.jpush.im.android.api.model.ChatRoomInfo;
import jiguang.chat.R;

/**
 * Created by ${chenyn} on 2017/11/8.
 */

public class ChatRoomInfoActivity extends BaseActivity {

    private TextView mTv_chatRoomName, tv_chatRoomID, tv_chatRoomMember, tv_chatRoomDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_info);
        initTitle(true, true, "聊天室资料", "", false, "");
        mTv_chatRoomName = (TextView) findViewById(R.id.tv_chatRoomName);
        tv_chatRoomID = (TextView) findViewById(R.id.tv_chatRoomID);
        tv_chatRoomMember = (TextView) findViewById(R.id.tv_chatRoomMember);
        tv_chatRoomDesc = (TextView) findViewById(R.id.tv_chatRoomDesc);


        long roomId = getIntent().getLongExtra("chatRoomId", 0);

        ChatRoomManager.getChatRoomInfos(Collections.singleton(roomId), new RequestCallback<List<ChatRoomInfo>>() {
            @Override
            public void gotResult(int i, String s, List<ChatRoomInfo> chatRoomInfos) {
                if (i == 0) {
                    ChatRoomInfo info = chatRoomInfos.get(0);
                    mTv_chatRoomName.setText(info.getName());
                    tv_chatRoomID.setText(info.getRoomID() + "");
                    tv_chatRoomMember.setText(info.getTotalMemberCount() + "");
                    tv_chatRoomDesc.setText(info.getDescription());

                }
            }
        });

    }
}
