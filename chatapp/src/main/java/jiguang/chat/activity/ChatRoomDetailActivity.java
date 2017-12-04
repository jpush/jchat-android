package jiguang.chat.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import cn.jpush.im.android.api.ChatRoomManager;
import cn.jpush.im.android.api.callback.RequestCallback;
import cn.jpush.im.android.api.model.ChatRoomInfo;
import jiguang.chat.R;
import jiguang.chat.utils.dialog.LoadDialog;

/**
 * Created by ${chenyn} on 2017/10/31.
 */

@SuppressLint("Registered")
public class ChatRoomDetailActivity extends BaseActivity {

    private TextView mChatRoomName;
    private TextView mChatRoomID;
    private TextView mChatRoomMember;
    private TextView mChatRoomDesc;
    private Button mEnterChatRoom;
    private ChatRoomInfo mInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat_room_detail);
        initView();
        initData();
    }

    private void initData() {
        LoadDialog.show(ChatRoomDetailActivity.this, "正在加载...");
        long chatRoomId = getIntent().getLongExtra("chatRoomId", 0);
        ChatRoomManager.getChatRoomInfos(Collections.singleton(chatRoomId), new RequestCallback<List<ChatRoomInfo>>() {
            @Override
            public void gotResult(int i, String s, List<ChatRoomInfo> chatRoomInfos) {
                LoadDialog.dismiss(ChatRoomDetailActivity.this);
                if (i == 0) {
                    mInfo = chatRoomInfos.get(0);
                    mChatRoomName.setText(mInfo.getName());
                    mChatRoomID.setText(mInfo.getRoomID() + "");
                    mInfo.getDescription(new RequestCallback<String>() {
                        @Override
                        public void gotResult(int i, String s, String s2) {
                            if (i == 0) {
                                mChatRoomDesc.setText(s2);
                            }
                        }
                    });

                    mInfo.getTotalMemberCount(new RequestCallback<Integer>() {
                        @Override
                        public void gotResult(int i, String s, Integer integer) {
                            if (i == 0) {
                                mChatRoomMember.setText(integer + "");
                            }
                        }
                    });
                }
            }
        });
        mEnterChatRoom.setOnClickListener(v -> {
            Intent intent = new Intent(ChatRoomDetailActivity.this, ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            intent.putExtra("chatRoomId", chatRoomId);
            intent.putExtra("chatRoomName", mInfo.getName());
            startActivity(intent);
        });
    }

    private void initView() {
        initTitle(true, true, "详细资料", "", false, "");
        mChatRoomName = (TextView) findViewById(R.id.tv_chatRoomName);
        mChatRoomID = (TextView) findViewById(R.id.tv_chatRoomID);
        mChatRoomMember = (TextView) findViewById(R.id.tv_chatRoomMember);
        mChatRoomDesc = (TextView) findViewById(R.id.tv_chatRoomDesc);
        mEnterChatRoom = (Button) findViewById(R.id.btn_enterChatRoom);

    }
}
