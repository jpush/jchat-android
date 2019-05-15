package jiguang.chat.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cn.jpush.im.android.api.ChatRoomManager;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.callback.RequestCallback;
import cn.jpush.im.android.api.event.ChatRoomNotificationEvent;
import cn.jpush.im.android.api.model.ChatRoomInfo;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import jiguang.chat.R;
import jiguang.chat.adapter.ChatRoomKeeperGridAdapter;
import jiguang.chat.application.JGApplication;
import jiguang.chat.utils.DialogCreator;
import jiguang.chat.utils.ToastUtil;
import jiguang.chat.view.NoScrollGridView;

/**
 * Created by ${chenyn} on 2017/11/8.
 */

public class ChatRoomInfoActivity extends BaseActivity implements View.OnClickListener {

    private TextView mTvChatRoomName, mTvChatRoomDesc, mTvChatRoomOwner;
    private NoScrollGridView mGvChatRoomKeeper;
    private ChatRoomKeeperGridAdapter mGridAdapter;
    private List<UserInfo> keeperList = new ArrayList<>();
    private List<UserInfo> keeperListDisplay = new ArrayList<>();
    private ChatRoomInfo mChatRoomInfo;
    private UserInfo ownerInfo;
    private long roomId;
    private boolean isOwner;
    private Dialog exitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_info);
        initTitle(true, true, "聊天室资料", "", false, "");
        initData();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.ll_chat_room_name:
                intent.setClass(ChatRoomInfoActivity.this, NickSignActivity.class);
                intent.putExtra(NickSignActivity.TYPE, NickSignActivity.Type.CHAT_ROOM_NAME);
                intent.putExtra(NickSignActivity.DESC, mChatRoomInfo != null ? mChatRoomInfo.getName() : "");
                startActivity(intent);
                break;
            case R.id.ll_chat_room_desc:
                intent.setClass(ChatRoomInfoActivity.this, NickSignActivity.class);
                intent.putExtra(NickSignActivity.TYPE, NickSignActivity.Type.CHAT_ROOM_DESC);
                intent.putExtra(NickSignActivity.DESC, mChatRoomInfo != null ? mChatRoomInfo.getDescription() : "");
                startActivity(intent);
                break;
            case R.id.ll_chat_room_keeper:
                intent.setClass(ChatRoomInfoActivity.this, ChatRoomKeeperActivity.class);
                intent.putExtra(JGApplication.ROOM_ID, roomId);
                intent.putExtra(ChatRoomKeeperActivity.IS_OWNER, isOwner);
                startActivity(intent);
                break;
            case R.id.ll_chat_room_owner:
                if (isOwner) {
                    intent.setClass(ChatRoomInfoActivity.this, PersonalActivity.class);
                } else {
                    intent.setClass(ChatRoomInfoActivity.this, GroupUserInfoActivity.class);
                    intent.putExtra(GroupUserInfoActivity.IS_FROM_GROUP, false);
                    intent.putExtra(JGApplication.NAME, ownerInfo != null ? ownerInfo.getUserName() : "");
                    intent.putExtra(JGApplication.TARGET_APP_KEY, ownerInfo != null ? ownerInfo.getAppKey() : "");
                }
                startActivity(intent);
                break;
            case R.id.btn_exit_room:
                View.OnClickListener listener = (v1) -> {
                    switch (v1.getId()) {
                        case R.id.jmui_cancel_btn:
                            exitDialog.cancel();
                            break;
                        case R.id.jmui_commit_btn:
                            exitChatRoom();
                            exitDialog.cancel();
                            break;
                    }
                };
                exitDialog = DialogCreator.createBaseDialogWithTitle(ChatRoomInfoActivity.this, "确定退出聊天室", listener);
                exitDialog.show();
            default:
                break;
        }
    }

    private void exitChatRoom() {
        Dialog loadingDialog = DialogCreator.createLoadingDialog(ChatRoomInfoActivity.this,
                "正在处理");
        loadingDialog.show();
        ChatRoomManager.leaveChatRoom(roomId, new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                loadingDialog.dismiss();
                if (i == 0) {
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.setClass(ChatRoomInfoActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    ToastUtil.shortToast(ChatRoomInfoActivity.this, "退出失败");
                }
            }
        });
    }

    private void initData() {
        mTvChatRoomName = (TextView) findViewById(R.id.tv_chatRoomName);
        mTvChatRoomDesc = (TextView) findViewById(R.id.tv_chatRoomDesc);
        mTvChatRoomOwner = (TextView) findViewById(R.id.tv_chatRoomOwner);
        mGvChatRoomKeeper = (NoScrollGridView) findViewById(R.id.grid_chatRommKeeper);
        roomId = getIntent().getLongExtra("chatRoomId", 0);
        findViewById(R.id.ll_chat_room_name).setOnClickListener(this);
        findViewById(R.id.ll_chat_room_desc).setOnClickListener(this);
        findViewById(R.id.ll_chat_room_keeper).setOnClickListener(this);
        findViewById(R.id.ll_chat_room_owner).setOnClickListener(this);
        findViewById(R.id.btn_exit_room).setOnClickListener(this);
        Dialog loadingDialog = DialogCreator.createLoadingDialog(this, "正在加载...");
        loadingDialog.show();
        AtomicInteger taskCount = new AtomicInteger(2);
        ChatRoomManager.getChatRoomInfos(Collections.singleton(roomId), new RequestCallback<List<ChatRoomInfo>>() {
            @Override
            public void gotResult(int i, String s, List<ChatRoomInfo> chatRoomInfos) {
                if (i == 0) {
                    mChatRoomInfo = chatRoomInfos.get(0);
                    mTvChatRoomName.setText(mChatRoomInfo.getName());
                    mTvChatRoomDesc.setText(mChatRoomInfo.getDescription());
                    mChatRoomInfo.getOwnerInfo(new GetUserInfoCallback() {
                        @Override
                        public void gotResult(int i, String s, UserInfo userInfo) {
                            if (i == 0) {
                                ownerInfo = userInfo;
                                isOwner = (ownerInfo.getUserID() == JMessageClient.getMyInfo().getUserID());
                                mTvChatRoomOwner.setText(ownerInfo.getDisplayName());
                            }
                            if (taskCount.decrementAndGet() == 0) {
                                loadingDialog.dismiss();
                            }
                        }
                    });
                } else {
                    if (taskCount.decrementAndGet() == 0) {
                        loadingDialog.dismiss();
                    }
                }
            }
        });
        ChatRoomManager.getChatRoomAdminList(roomId, new RequestCallback<List<UserInfo>>() {
            @Override
            public void gotResult(int i, String s, List<UserInfo> userInfos) {
                if (i == 0) {
                    keeperList.clear();
                    keeperListDisplay.clear();
                    keeperList.addAll(userInfos);
                    if (keeperList.size() > 5) {
                        keeperListDisplay.addAll(userInfos.subList(0, 5)); // 只取5个
                    } else {
                        keeperListDisplay.addAll(userInfos);
                    }
                }
                mGvChatRoomKeeper.setNumColumns(keeperListDisplay.size());
                mGridAdapter = new ChatRoomKeeperGridAdapter(ChatRoomInfoActivity.this, keeperListDisplay);
                mGvChatRoomKeeper.setAdapter(mGridAdapter);
                if (taskCount.decrementAndGet() == 0) {
                    loadingDialog.dismiss();
                }
            }
        });
    }

    public void onEvent(ChatRoomNotificationEvent event) {
        switch (event.getType()) {
            case del_chatroom_admin:
            case add_chatroom_admin:
                ChatRoomManager.getChatRoomAdminList(roomId, new RequestCallback<List<UserInfo>>() {
                    @Override
                    public void gotResult(int i, String s, List<UserInfo> userInfos) {
                        if (i == 0) {
                            keeperList.clear();
                            keeperListDisplay.clear();
                            keeperList.addAll(userInfos);
                            if (keeperList.size() > 5) {
                                keeperListDisplay.addAll(userInfos.subList(0, 5)); // 只取5个
                            } else {
                                keeperListDisplay.addAll(userInfos);
                            }
                        }
                        mGvChatRoomKeeper.setNumColumns(keeperListDisplay.size());
                        mGridAdapter.notifyDataSetChanged();
                    }
                });
                break;
            default:
                break;

        }
    }
}
