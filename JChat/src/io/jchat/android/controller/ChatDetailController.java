package io.jchat.android.controller;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.CreateGroupCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.eventbus.EventBus;
import cn.jpush.im.api.BasicCallback;
import io.jchat.android.R;
import io.jchat.android.activity.ChatDetailActivity;
import io.jchat.android.activity.FriendInfoActivity;
import io.jchat.android.activity.MeInfoActivity;
import io.jchat.android.activity.MembersInChatActivity;
import io.jchat.android.adapter.GroupMemberGridAdapter;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.chatting.utils.SharePreferenceManager;
import io.jchat.android.entity.Event;
import io.jchat.android.chatting.utils.DialogCreator;
import io.jchat.android.chatting.utils.HandleResponseCode;
import io.jchat.android.entity.EventType;
import io.jchat.android.view.ChatDetailView;
import io.jchat.android.view.SlipButton;

public class ChatDetailController implements OnClickListener, OnItemClickListener, SlipButton.OnChangedListener{

    private static final String TAG = "ChatDetailController";

    private ChatDetailView mChatDetailView;
    private ChatDetailActivity mContext;
    private GroupMemberGridAdapter mGridAdapter;
    //GridView的数据源
    private List<UserInfo> mMemberInfoList = new ArrayList<UserInfo>();
    // 当前GridView群成员项数
    private int mCurrentNum;
    // 空白项的项数
    // 除了群成员Item和添加、删除按钮，剩下的都看成是空白项，
    // 对应的mRestNum[mCurrent%4]的值即为空白项的数目
    private int[] mRestArray = new int[]{2, 1, 0, 3};
    private boolean mIsGroup = false;
    private boolean mIsCreator = false;
    private long mGroupId;
    private String mTargetId;
    private Dialog mLoadingDialog = null;
    private static final int ADD_MEMBERS_TO_GRIDVIEW = 2048;
    private static final int ADD_A_MEMBER_TO_GRIDVIEW = 2049;
    private static final int MAX_GRID_ITEM = 40;
    private String mGroupName;
    private final MyHandler myHandler = new MyHandler(this);
    private Dialog mDialog;
    private boolean mDeleteMsg;
    private int mAvatarSize;
    private String mMyUsername;
    private String mTargetAppKey;
    private int mWidth;
    private GroupInfo mGroupInfo;
    private UserInfo mUserInfo;

    public ChatDetailController(ChatDetailView chatDetailView, ChatDetailActivity context, int size,
                                int width) {
        this.mChatDetailView = chatDetailView;
        this.mContext = context;
        this.mAvatarSize = size;
        this.mWidth = width;
        initData();
    }

    /*
     * 获得群组信息，初始化群组界面，先从本地读取，如果没有再从服务器读取
     */
    private void initData() {
        Intent intent = mContext.getIntent();
        mGroupId = intent.getLongExtra(JChatDemoApplication.GROUP_ID, 0);
        Log.i(TAG, "mGroupId" + mGroupId);
        mTargetId = intent.getStringExtra(JChatDemoApplication.TARGET_ID);
        mTargetAppKey = intent.getStringExtra(JChatDemoApplication.TARGET_APP_KEY);
        mMyUsername = JMessageClient.getMyInfo().getUserName();
        Log.i(TAG, "mTargetId: " + mTargetId);
        // 是群组
        if (mGroupId != 0) {
            mIsGroup = true;
            //获得群组基本信息：群主ID、群组名、群组人数
            Conversation conv = JMessageClient.getGroupConversation(mGroupId);
            mGroupInfo = (GroupInfo) conv.getTargetInfo();
            mChatDetailView.initNoDisturb(mGroupInfo.getNoDisturb());
            mMemberInfoList = mGroupInfo.getGroupMembers();
            String groupOwnerId = mGroupInfo.getGroupOwner();
            mGroupName = mGroupInfo.getGroupName();
            if (TextUtils.isEmpty(mGroupName)) {
                mChatDetailView.setGroupName(mContext.getString(R.string.unnamed));
            } else {
                mChatDetailView.setGroupName(mGroupName);
            }
            // 判断是否为群主
            if (groupOwnerId != null && groupOwnerId.equals(mMyUsername)) {
                mIsCreator = true;
            }
            mChatDetailView.setMyName(mMyUsername);
            mChatDetailView.setTitle(mMemberInfoList.size());
            initAdapter();
            if (mGridAdapter != null) {
                mGridAdapter.setCreator(mIsCreator);
            }
            // 是单聊
        } else {
            Conversation conv = JMessageClient.getSingleConversation(mTargetId, mTargetAppKey);
            mUserInfo = (UserInfo) conv.getTargetInfo();
            mChatDetailView.initNoDisturb(mUserInfo.getNoDisturb());
            mCurrentNum = 1;
            mGridAdapter = new GroupMemberGridAdapter(mContext, mTargetId, mTargetAppKey);
            mChatDetailView.setAdapter(mGridAdapter);
            // 设置单聊界面
            mChatDetailView.setSingleView();
            mChatDetailView.dismissAllMembersBtn();
        }
    }


    private void initAdapter() {
        // 初始化头像
        mGridAdapter = new GroupMemberGridAdapter(mContext, mMemberInfoList, mIsCreator, mAvatarSize);
        if (mMemberInfoList.size() > MAX_GRID_ITEM) {
            mCurrentNum = MAX_GRID_ITEM - 1;
        } else {
            mCurrentNum = mMemberInfoList.size();
        }
        mChatDetailView.setMembersNum(mMemberInfoList.size());
        mChatDetailView.setAdapter(mGridAdapter);
        mChatDetailView.getGridView().setFocusable(false);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.return_btn:
                intent.putExtra("deleteMsg", mDeleteMsg);
                intent.putExtra(JChatDemoApplication.CONV_TITLE, getName());
                intent.putExtra(JChatDemoApplication.MEMBERS_COUNT, mMemberInfoList.size());
                mContext.setResult(JChatDemoApplication.RESULT_CODE_CHAT_DETAIL, intent);
                mContext.finish();
                break;
            //显示所有群成员
            case R.id.all_member_ll:
                intent.putExtra(JChatDemoApplication.GROUP_ID, mGroupId);
                intent.putExtra(JChatDemoApplication.DELETE_MODE, false);
                intent.setClass(mContext, MembersInChatActivity.class);
                mContext.startActivityForResult(intent, JChatDemoApplication.REQUEST_CODE_ALL_MEMBER);
                break;

            // 设置群组名称
            case R.id.group_name_ll:
                mContext.showGroupNameSettingDialog(1, mGroupId, mGroupName);
                break;

            // 设置我在群组的昵称
            case R.id.group_my_name_ll:
                mContext.showGroupNameSettingDialog(2, mGroupId, mGroupName);
                break;

            // 群组人数
            case R.id.group_num_ll:
                break;

            // 查询聊天记录
            case R.id.group_chat_record_ll:
                break;

            // 删除聊天记录
            case R.id.group_chat_del_ll:
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getId()) {
                            case R.id.jmui_cancel_btn:
                                mDialog.cancel();
                                break;
                            case R.id.jmui_commit_btn:
                                Conversation conv;
                                if (mIsGroup) {
                                    conv = JMessageClient.getGroupConversation(mGroupId);
                                }
                                else {
                                    conv = JMessageClient.getSingleConversation(mTargetId, mTargetAppKey);
                                }
                                if (conv != null) {
                                    conv.deleteAllMessage();
                                    mDeleteMsg = true;
                                }
                                mDialog.cancel();
                                break;
                        }
                    }
                };
                mDialog = DialogCreator.createDeleteMessageDialog(mContext, listener);
                mDialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
                mDialog.show();
                break;
            case R.id.chat_detail_del_group:
                listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getId()) {
                            case R.id.jmui_cancel_btn:
                                mDialog.cancel();
                                break;
                            case R.id.jmui_commit_btn:
                                deleteAndExit();
                                mDialog.cancel();
                                break;
                        }
                    }
                };
                mDialog = DialogCreator.createExitGroupDialog(mContext, listener);
                mDialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
                mDialog.show();
                break;
        }
    }

    /**
     * 删除并退出
     */
    private void deleteAndExit() {
        mLoadingDialog = DialogCreator.createLoadingDialog(mContext,
                mContext.getString(R.string.exiting_group_toast));
        mLoadingDialog.show();
        JMessageClient.exitGroup(mGroupId, new BasicCallback() {
            @Override
            public void gotResult(final int status, final String desc) {
                if (mLoadingDialog != null)
                    mLoadingDialog.dismiss();
                if (status == 0) {
                    Conversation conv = JMessageClient.getGroupConversation(mGroupId);
                    EventBus.getDefault().post(new Event.Builder().setType(EventType.deleteConversation)
                            .setConversation(conv)
                            .build());
                    JMessageClient.deleteGroupConversation(mGroupId);
                    mContext.startMainActivity();
                } else {
                    HandleResponseCode.onHandle(mContext, status, false);
                }
            }
        });
    }

    // GridView点击事件
    @Override
    public void onItemClick(AdapterView<?> viewAdapter, View view, final int position, long id) {
        Intent intent = new Intent();
        //群聊
        if (mIsGroup) {
            // 点击群成员项时
            if (position < mCurrentNum) {
                if (mMemberInfoList.get(position).getUserName().equals(mMyUsername)) {
                    intent.setClass(mContext, MeInfoActivity.class);
                } else {
                    UserInfo userInfo = mMemberInfoList.get(position);
                    intent.putExtra(JChatDemoApplication.TARGET_ID, userInfo.getUserName());
                    intent.putExtra(JChatDemoApplication.TARGET_APP_KEY, userInfo.getAppKey());
                    intent.putExtra(JChatDemoApplication.GROUP_ID, mGroupId);
                    intent.setClass(mContext, FriendInfoActivity.class);
                }
                mContext.startActivity(intent);
                // 点击添加成员按钮
            } else if (position == mCurrentNum) {
                mContext.showContacts();

                // 是群主, 成员个数大于1并点击删除按钮
            } else if (position == mCurrentNum + 1 && mIsCreator && mCurrentNum > 1) {
                intent.putExtra(JChatDemoApplication.DELETE_MODE, true);
                intent.putExtra(JChatDemoApplication.GROUP_ID, mGroupId);
                intent.setClass(mContext, MembersInChatActivity.class);
                mContext.startActivityForResult(intent, JChatDemoApplication.REQUEST_CODE_ALL_MEMBER);
            }
            //单聊
        } else if (position < mCurrentNum) {
            intent.putExtra(JChatDemoApplication.TARGET_ID, mTargetId);
            intent.putExtra(JChatDemoApplication.TARGET_APP_KEY, mTargetAppKey);
            intent.setClass(mContext, FriendInfoActivity.class);
            mContext.startActivityForResult(intent, JChatDemoApplication.REQUEST_CODE_FRIEND_INFO);
        } else if (position == mCurrentNum) {
            mContext.showContacts();
        }

    }

    public void addMembersToGroup(ArrayList<String> users) {
        ArrayList<String> list = new ArrayList<String>();
        for (String username : users) {
            if (checkIfNotContainUser(username)) {
                list.add(username);
            }
        }
        if (list.size() > 0) {
            mLoadingDialog = DialogCreator.createLoadingDialog(mContext,
                    mContext.getString(R.string.adding_hint));
            mLoadingDialog.show();
            android.os.Message msg = myHandler.obtainMessage();
            msg.what = ADD_MEMBERS_TO_GRIDVIEW;
            msg.obj = list;
            msg.sendToTarget();
        }
    }

    //点击添加按钮触发事件
    private void addMemberToGroup() {
        final Dialog dialog = new Dialog(mContext, R.style.jmui_default_dialog_style);
        final View view = LayoutInflater.from(mContext)
                .inflate(R.layout.dialog_add_friend_to_conv_list, null);
        dialog.setContentView(view);
        dialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
        TextView title = (TextView) view.findViewById(R.id.dialog_name);
        title.setText(mContext.getString(R.string.add_friend_to_group_title));
        final EditText userNameEt = (EditText) view.findViewById(R.id.user_name_et);
        final Button cancel = (Button) view.findViewById(R.id.jmui_cancel_btn);
        final Button commit = (Button) view.findViewById(R.id.jmui_commit_btn);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.jmui_cancel_btn:
                        dialog.cancel();
                        break;
                    case R.id.jmui_commit_btn:
                        final String targetId = userNameEt.getText().toString().trim();
                        Log.i(TAG, "targetID " + targetId);
                        if (TextUtils.isEmpty(targetId)) {
                            HandleResponseCode.onHandle(mContext, 801001, true);
                            break;
                            //检查群组中是否包含该用户
                        } else if (checkIfNotContainUser(targetId)) {
                            mLoadingDialog = DialogCreator.createLoadingDialog(mContext,
                                    mContext.getString(R.string.searching_user));
                            mLoadingDialog.show();
                            getUserInfo(targetId, dialog);
                        } else {
                            userNameEt.setText("");
                            HandleResponseCode.onHandle(mContext, 1002, true);
                        }
                        break;
                }
            }
        };
        cancel.setOnClickListener(listener);
        commit.setOnClickListener(listener);
    }

    private void getUserInfo(final String targetId, final Dialog dialog){
        JMessageClient.getUserInfo(targetId, new GetUserInfoCallback() {
            @Override
            public void gotResult(final int status, String desc, final UserInfo userInfo) {
                if (mLoadingDialog != null) {
                    mLoadingDialog.dismiss();
                }
                if (status == 0) {
                    android.os.Message msg = myHandler.obtainMessage();
                    msg.what = ADD_A_MEMBER_TO_GRIDVIEW;
                    msg.obj = userInfo;
                    msg.sendToTarget();
                    dialog.cancel();
                } else {
                    HandleResponseCode.onHandle(mContext, status, true);
                }
            }
        });
    }

    /**
     * 添加成员时检查是否存在该群成员
     *
     * @param targetID 要添加的用户
     * @return 返回是否存在该用户
     */
    private boolean checkIfNotContainUser(String targetID) {
        if (mMemberInfoList != null) {
            for (UserInfo userInfo : mMemberInfoList) {
                if (userInfo.getUserName().equals(targetID))
                    return false;
            }
            return true;
        }
        return true;
    }

    /**
     * @param userInfo 要增加的成员的用户名，目前一次只能增加一个
     */
    private void addAMember(final UserInfo userInfo) {
        mLoadingDialog = DialogCreator.createLoadingDialog(mContext,
                mContext.getString(R.string.adding_hint));
        mLoadingDialog.show();
        ArrayList<String> list = new ArrayList<String>();
        list.add(userInfo.getUserName());
        JMessageClient.addGroupMembers(mGroupId, list, new BasicCallback() {

            @Override
            public void gotResult(final int status, final String desc) {
                mLoadingDialog.dismiss();
                if (status == 0) {
                    refreshMemberList();
                } else {
                    HandleResponseCode.onHandle(mContext, status, true);
                }
            }
        });
    }

    private void addMembers(ArrayList<String> users) {
        JMessageClient.addGroupMembers(mGroupId, users, new BasicCallback() {

            @Override
            public void gotResult(final int status, final String desc) {
                mLoadingDialog.dismiss();
                if (status == 0) {
                    refreshMemberList();
                } else {
                    HandleResponseCode.onHandle(mContext, status, true);
                }
            }
        });
    }

    //添加或者删除成员后重新获得MemberInfoList
    public void refreshMemberList() {
        mCurrentNum = mMemberInfoList.size() > MAX_GRID_ITEM ? MAX_GRID_ITEM - 1 : mMemberInfoList.size();
        mGridAdapter.refreshMemberList();
        mChatDetailView.setMembersNum(mMemberInfoList.size());
        mChatDetailView.setTitle(mMemberInfoList.size());
    }

    public void refreshGroupName(String newName) {
        mGroupName = newName;
    }

    @Override
    public void onChanged(int id, final boolean checked) {
        final Dialog dialog = DialogCreator.createLoadingDialog(mContext, mContext.getString(R.string.jmui_loading));
        dialog.show();
        //设置免打扰,1为将当前用户或群聊设为免打扰,0为移除免打扰
        if (mIsGroup) {
            mGroupInfo.setNoDisturb(checked ? 1 : 0, new BasicCallback() {
                @Override
                public void gotResult(int status, String desc) {
                    dialog.dismiss();
                    if (status == 0) {
                        if (checked) {
                            Toast.makeText(mContext, mContext.getString(R.string.set_do_not_disturb_success_hint),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, mContext.getString(R.string.remove_from_no_disturb_list_hint),
                                    Toast.LENGTH_SHORT).show();
                        }
                    //设置失败,恢复为原来的状态
                    } else {
                        if (checked) {
                            mChatDetailView.setNoDisturbChecked(false);
                        } else {
                            mChatDetailView.setNoDisturbChecked(true);
                        }
                        HandleResponseCode.onHandle(mContext, status, false);
                    }
                }
            });
        } else {
            mUserInfo.setNoDisturb(checked ? 1 : 0, new BasicCallback() {
                @Override
                public void gotResult(int status, String desc) {
                    dialog.dismiss();
                    if (status == 0) {
                        if (checked) {
                            Toast.makeText(mContext, mContext.getString(R.string.set_do_not_disturb_success_hint),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, mContext.getString(R.string.remove_from_no_disturb_list_hint),
                                    Toast.LENGTH_SHORT).show();
                        }
                        //设置失败,恢复为原来的状态
                    } else {
                        if (checked) {
                            mChatDetailView.setNoDisturbChecked(false);
                        } else {
                            mChatDetailView.setNoDisturbChecked(true);
                        }
                        HandleResponseCode.onHandle(mContext, status, false);
                    }
                }
            });
        }
    }

    private static class MyHandler extends Handler {
        private final WeakReference<ChatDetailController> mController;

        public MyHandler(ChatDetailController controller) {
            mController = new WeakReference<ChatDetailController>(controller);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ChatDetailController controller = mController.get();
            if (controller != null) {
                switch (msg.what) {
                    //无好友模式一次添加一个
                    case ADD_A_MEMBER_TO_GRIDVIEW:
                        if (controller.mLoadingDialog != null) {
                            controller.mLoadingDialog.dismiss();
                        }
                        final UserInfo userInfo = (UserInfo) msg.obj;
                        if (controller.mIsGroup) {
                            controller.addAMember(userInfo);
                            //在单聊中点击加人按钮并且用户信息返回正确,如果为第三方则创建群聊
                        } else {
                            if (userInfo.getUserName().equals(controller.mMyUsername)
                                    || userInfo.getUserName().equals(controller.mTargetId)){
                                HandleResponseCode.onHandle(controller.mContext, 1002, false);
                                return;
                            } else {
                                controller.addMemberAndCreateGroup(userInfo.getUserName());
                            }
                        }
                        break;
                    //好友模式从通讯录中添加好友
                    case ADD_MEMBERS_TO_GRIDVIEW:
                        Log.i(TAG, "Adding Group Members");
                        ArrayList<String> users = (ArrayList<String>) msg.obj;
                        if (controller.mIsGroup) {
                            controller.addMembers(users);
                        //在单聊中点击加人按钮并且用户信息返回正确,如果为第三方则创建群聊
                        } else {
                            if (controller.mLoadingDialog != null) {
                                controller.mLoadingDialog.dismiss();
                            }
                            controller.addMembersAndCreateGroup(users);
                        }
                        break;
                }
            }
        }
    }

    /**
     * 在单聊中点击增加按钮触发事件，创建群聊
     *
     * @param newMember 要增加的成员
     */
    private void addMemberAndCreateGroup(final String newMember) {
        mLoadingDialog = DialogCreator.createLoadingDialog(mContext,
                mContext.getString(R.string.creating_hint));
        mLoadingDialog.show();
        JMessageClient.createGroup("", "", new CreateGroupCallback() {
            @Override
            public void gotResult(int status, final String desc, final long groupId) {
                if (status == 0) {
                    ArrayList<String> list = new ArrayList<String>();
                    list.add(mTargetId);
                    list.add(newMember);
                    JMessageClient.addGroupMembers(groupId, list, new BasicCallback() {
                        @Override
                        public void gotResult(int status, String desc) {
                            if (mLoadingDialog != null) {
                                mLoadingDialog.dismiss();
                            }
                            if (status == 0) {
                                Conversation conv = Conversation.createGroupConversation(groupId);
                                EventBus.getDefault().post(new Event.Builder().setType(EventType.createConversation)
                                        .setConversation(conv).build());
                                mContext.startChatActivity(groupId, conv.getTitle());
                            } else {
                                HandleResponseCode.onHandle(mContext, status, false);
                            }
                        }
                    });
                } else {
                    if (mLoadingDialog != null) {
                        mLoadingDialog.dismiss();
                    }
                    HandleResponseCode.onHandle(mContext, status, false);
                }
            }
        });
    }

    private void addMembersAndCreateGroup(final ArrayList<String> list) {
        mLoadingDialog = DialogCreator.createLoadingDialog(mContext,
                mContext.getString(R.string.creating_hint));
        mLoadingDialog.show();
        JMessageClient.createGroup("", "", new CreateGroupCallback() {
            @Override
            public void gotResult(int status, final String desc, final long groupId) {
                if (status == 0) {
                    JMessageClient.addGroupMembers(groupId, list, new BasicCallback() {
                        @Override
                        public void gotResult(int status, String desc) {
                            if (mLoadingDialog != null) {
                                mLoadingDialog.dismiss();
                            }
                            if (status == 0) {
                                Conversation conv = Conversation.createGroupConversation(groupId);
                                EventBus.getDefault().post(new Event.Builder().setType(EventType.createConversation)
                                        .setConversation(conv).build());
                                mContext.startChatActivity(groupId, conv.getTitle());
                            } else {
                                HandleResponseCode.onHandle(mContext, status, false);
                            }
                        }
                    });
                } else {
                    if (mLoadingDialog != null) {
                        mLoadingDialog.dismiss();
                    }
                    HandleResponseCode.onHandle(mContext, status, false);
                }
            }
        });
    }

    public String getName() {
        if (mIsGroup) {
            return mGroupName;
        }else {
            Conversation conv = JMessageClient.getSingleConversation(mTargetId, mTargetAppKey);
            return conv.getTitle();
        }
    }

    public int getCurrentCount() {
        return mMemberInfoList.size();
    }

    public boolean getDeleteFlag() {
        return mDeleteMsg;
    }

    public GroupMemberGridAdapter getAdapter() {
        return mGridAdapter;
    }

    /**
     * 当收到群成员变化的Event后，刷新成员列表
     *
     * @param groupId 群组Id
     */
    public void refresh(long groupId) {
        //当前群聊
        if (mGroupId == groupId) {
            refreshMemberList();
        }
    }

}
