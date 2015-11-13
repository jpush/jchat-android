package io.jchat.android.controller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
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
import io.jchat.android.adapter.GroupMemberGridAdapter;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.entity.Event;
import io.jchat.android.tools.DialogCreator;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.view.ChatDetailView;

public class ChatDetailController implements OnClickListener, OnItemClickListener,
        OnItemLongClickListener{

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
    private boolean mIsShowDelete = false;
    private static final int ADD_TO_GRIDVIEW = 2048;
    private static final int DELETE_FROM_GRIDVIEW = 2049;
    private String mGroupName;
    private final MyHandler myHandler = new MyHandler(this);
    private Dialog mDialog;
    private boolean mDeleteMsg;
    private int mAvatarSize;

    public ChatDetailController(ChatDetailView chatDetailView, ChatDetailActivity context, int size) {
        this.mChatDetailView = chatDetailView;
        this.mContext = context;
        this.mAvatarSize = size;
        initData();
    }

    /*
     * 获得群组信息，初始化群组界面，先从本地读取，如果没有再从服务器读取
     */
    private void initData() {
        Intent intent = mContext.getIntent();
        mIsGroup = intent.getBooleanExtra(JChatDemoApplication.IS_GROUP, false);
        mGroupId = intent.getLongExtra(JChatDemoApplication.GROUP_ID, 0);
        Log.i(TAG, "mGroupId" + mGroupId);
        mTargetId = intent.getStringExtra(JChatDemoApplication.TARGET_ID);
        Log.i(TAG, "mTargetId: " + mTargetId);
        // 是群组
        if (mIsGroup) {
            //获得群组基本信息：群主ID、群组名、群组人数
            Conversation conv = JMessageClient.getGroupConversation(mGroupId);
            GroupInfo groupInfo = (GroupInfo) conv.getTargetInfo();
            mMemberInfoList = groupInfo.getGroupMembers();
            UserInfo myInfo = JMessageClient.getMyInfo();
            String groupOwnerId = groupInfo.getGroupOwner();
            mGroupName = groupInfo.getGroupName();
            if (TextUtils.isEmpty(mGroupName)) {
                mChatDetailView.setGroupName(mContext.getString(R.string.unnamed));
            } else {
                mChatDetailView.setGroupName(mGroupName);
            }
            // 判断是否为群主
            if (groupOwnerId != null && groupOwnerId.equals(myInfo.getUserName())) {
                mIsCreator = true;
            }
            mChatDetailView.setMyName(myInfo.getUserName());
            mChatDetailView.setTitle(mMemberInfoList.size());
            initAdapter();
            if (mGridAdapter != null) {
                mGridAdapter.setCreator(mIsCreator);
            }
            // 是单聊
        } else {
            mCurrentNum = 1;
            mGridAdapter = new GroupMemberGridAdapter(mContext, mTargetId);
            mChatDetailView.setAdapter(mGridAdapter);
            // 设置单聊界面
            mChatDetailView.setSingleView();
        }
    }


    private void initAdapter() {
        mCurrentNum = mMemberInfoList.size();
        // 初始化头像
        mGridAdapter = new GroupMemberGridAdapter(mContext, mMemberInfoList, mIsCreator, mAvatarSize);
        mChatDetailView.setAdapter(mGridAdapter);
        mChatDetailView.getGridView().setFocusable(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.return_btn:
                Intent intent = new Intent();
                intent.putExtra("deleteMsg", mDeleteMsg);
                intent.putExtra(JChatDemoApplication.NAME, getName());
                intent.putExtra("currentCount", mCurrentNum);
                mContext.setResult(JChatDemoApplication.RESULT_CODE_CHAT_DETAIL, intent);
                mContext.finish();
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
                            case R.id.cancel_btn:
                                mDialog.cancel();
                                break;
                            case R.id.commit_btn:
                                Conversation conv;
                                if (mIsGroup) {
                                    conv = JMessageClient.getGroupConversation(mGroupId);
                                }
                                else {
                                    conv = JMessageClient.getSingleConversation(mTargetId);
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
                mDialog.show();
                break;
            case R.id.chat_detail_del_group:
                listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getId()) {
                            case R.id.cancel_btn:
                                mDialog.cancel();
                                break;
                            case R.id.commit_btn:
                                deleteAndExit();
                                mDialog.cancel();
                                break;
                        }
                    }
                };
                mDialog = DialogCreator.createExitGroupDialog(mContext, listener);
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
                    boolean deleted = JMessageClient.deleteGroupConversation(mGroupId);
                    Log.i(TAG, "deleted: " + deleted);
                    mContext.StartMainActivity();
                } else {
                    HandleResponseCode.onHandle(mContext, status, false);
                }
            }
        });
    }

    // GridView点击事件
    @Override
    public void onItemClick(AdapterView<?> viewAdapter, View view, final int position, long id) {
        // 没有触发delete时
        if (!mIsShowDelete) {
            Intent intent = new Intent();
            //群聊
            if (mIsGroup) {
                // 点击群成员项时
                if (position < mCurrentNum) {
                    if (mMemberInfoList.get(position).getUserName()
                            .equals(JMessageClient.getMyInfo().getUserName())) {
                        intent.setClass(mContext, MeInfoActivity.class);
                    } else {
                        intent.putExtra(JChatDemoApplication.TARGET_ID,
                                mMemberInfoList.get(position).getUserName());
                        intent.putExtra(JChatDemoApplication.GROUP_ID, mGroupId);
                        intent.setClass(mContext, FriendInfoActivity.class);
                    }
                    mContext.startActivity(intent);
                    // 点击添加成员按钮
                } else if (position == mCurrentNum) {
                    addMemberToGroup();
//                     mContext.showContacts();

                    // 是群主, 成员个数大于1并点击删除按钮
                } else if (position == mCurrentNum + 1 && mIsCreator && mCurrentNum > 1) {
                    // delete friend from group
                    mIsShowDelete = true;
                    mGridAdapter.setIsShowDelete(true, mRestArray[mCurrentNum % 4]);
                }
                //单聊
            } else if (position < mCurrentNum) {
                intent.putExtra(JChatDemoApplication.TARGET_ID, mTargetId);
                intent.setClass(mContext, FriendInfoActivity.class);
                mContext.startActivityForResult(intent, JChatDemoApplication.REQUEST_CODE_FRIEND_INFO);
            } else if (position == mCurrentNum) {
                addMemberToGroup();
            }

            // delete状态
        } else {
            // 点击群成员Item时
            if (position < mCurrentNum) {
                //如果群主删除自己
                if (mMemberInfoList.get(position).getUserName()
                        .equals(JMessageClient.getMyInfo().getUserName())) {
                    return;
                } else {
                    // 删除某个群成员
                    mLoadingDialog = DialogCreator
                            .createLoadingDialog(mContext, mContext.getString(R.string.deleting_hint));
                    List<String> delList = new ArrayList<String>();
                    //之所以要传一个List，考虑到之后可能支持同时删除多人功能，现在List中只有一个元素
                    delList.add(mMemberInfoList.get(position).getUserName());
                    delMember(delList);
                }
                // 点击空白项时, 恢复GridView界面
            } else {
                mIsShowDelete = false;
                mGridAdapter.setIsShowDelete(false, mRestArray[mCurrentNum % 4]);
            }
        }
    }

    //点击添加按钮触发事件
    private void addMemberToGroup() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final View view = LayoutInflater.from(mContext)
                .inflate(R.layout.dialog_add_friend_to_conv_list, null);
        builder.setView(view);
        final Dialog dialog = builder.create();
        dialog.show();
        TextView title = (TextView) view.findViewById(R.id.dialog_name);
        title.setText(mContext.getString(R.string.add_friend_to_group_title));
        final EditText userNameEt = (EditText) view.findViewById(R.id.user_name_et);
        final Button cancel = (Button) view.findViewById(R.id.cancel_btn);
        final Button commit = (Button) view.findViewById(R.id.commit_btn);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.cancel_btn:
                        dialog.cancel();
                        break;
                    case R.id.commit_btn:
                        final String targetID = userNameEt.getText().toString().trim();
                        Log.i(TAG, "targetID " + targetID);
                        if (TextUtils.isEmpty(targetID)) {
                            Toast.makeText(mContext, mContext.getString(R.string.username_not_null_toast),
                                    Toast.LENGTH_SHORT).show();
                            break;
                            //检查群组中是否包含该用户
                        } else if (checkIfNotContainUser(targetID)) {
                            mLoadingDialog = DialogCreator.createLoadingDialog(mContext,
                                    mContext.getString(R.string.searching_user));
                            mLoadingDialog.show();
                            getUserInfo(targetID, dialog);
                        } else {
                            dialog.cancel();
                            Toast.makeText(mContext, mContext.getString(R.string.user_already_exist_toast),
                                    Toast.LENGTH_SHORT).show();
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
                    msg.what = ADD_TO_GRIDVIEW;
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
        try {
            mLoadingDialog = DialogCreator.createLoadingDialog(mContext,
                    mContext.getString(R.string.adding_hint));
            mLoadingDialog.show();
            ArrayList<String> list = new ArrayList<String>();
            list.add(userInfo.getUserName());
            JMessageClient.addGroupMembers(mGroupId, list, new BasicCallback() {

                @Override
                public void gotResult(final int status, final String desc) {
                    if (status == 0) {
                        // 添加群成员
                        ++mCurrentNum;
                        mGridAdapter.refreshMemberList(mGroupId);
                        refreshMemberList();
                        mChatDetailView.setTitle(mCurrentNum);
                        Log.i("ADD_TO_GRIDVIEW", "已添加");
                        mLoadingDialog.dismiss();
                    } else {
                        mLoadingDialog.dismiss();
                        HandleResponseCode.onHandle(mContext, status, true);
                    }
                }
            });
        } catch (Exception e) {
            mLoadingDialog.dismiss();
            e.printStackTrace();
            Toast.makeText(mContext, mContext.getString(R.string.unknown_error_toast),
                    Toast.LENGTH_SHORT).show();
        }
    }

    //添加或者删除成员后重新获得MemberInfoList
    private void refreshMemberList() {
        Conversation conv = JMessageClient.getGroupConversation(mGroupId);
        GroupInfo groupInfo = (GroupInfo)conv.getTargetInfo();
        mMemberInfoList = groupInfo.getGroupMembers();
    }

    /**
     * 删除成员
     *
     * @param list     被删除的用户名List
     */
    private void delMember(List<String> list) {
        mLoadingDialog.show();
        try {
            JMessageClient.removeGroupMembers(mGroupId, list, new BasicCallback() {

                @Override
                public void gotResult(final int status, final String desc) {
                    mLoadingDialog.dismiss();
                    if (status == 0) {
                        myHandler.sendEmptyMessage(DELETE_FROM_GRIDVIEW);
                    } else {
                        HandleResponseCode.onHandle(mContext, status, false);
                    }
                }
            });
        } catch (Exception e) {
            mLoadingDialog.dismiss();
            Toast.makeText(mContext, mContext.getString(R.string.unknown_error_toast),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void refreshGroupName(String newName) {
        mGroupName = newName;
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
                    //点击加人按钮并且用户信息返回正确
                    case ADD_TO_GRIDVIEW:
                        Log.i(TAG, "Adding Group Member, got UserInfo");
                        if (controller.mLoadingDialog != null) {
                            controller.mLoadingDialog.dismiss();
                        }
                        final UserInfo userInfo = (UserInfo) msg.obj;
                        if (controller.mIsGroup) {
                            controller.addAMember(userInfo);
                        //在单聊中点击加人按钮并且用户信息返回正确,如果为第三方则创建群聊
                        } else {
                            if (userInfo.getUserName().equals(JMessageClient.getMyInfo().getUserName())
                                    || userInfo.getUserName().equals(controller.mTargetId)){
                                return;
                            } else {
                                controller.addMemberAndCreateGroup(userInfo.getUserName());
                            }
                        }
                        break;
                    // 删除成员
                    case DELETE_FROM_GRIDVIEW:
                        // 更新GridView
                        --controller.mCurrentNum;
                        controller.mChatDetailView.setTitle(controller.mCurrentNum);
                        controller.mGridAdapter.refreshMemberList(controller.mGroupId);
                        controller.refreshMemberList();
                        // 当前成员数为1，退出删除状态
                        if (controller.mCurrentNum == 1) {
                            controller.mIsShowDelete = false;
                            controller.mGridAdapter.setIsShowDelete(false);
                        }
                        Log.i("DELETE_FROM_GRIDVIEW", "已删除");
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
                                EventBus.getDefault().post(new Event.LongEvent(groupId));
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
                    Toast.makeText(mContext, desc, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 群成员变化

    @Override
    public boolean onItemLongClick(AdapterView<?> viewAdapter, View view, int position, long id) {
        // 是群主并在非删除状态下长按Item触发事件
        if (!mIsShowDelete && mIsCreator) {
            if (position < mCurrentNum && mCurrentNum > 1) {
                mIsShowDelete = true;
                mGridAdapter.setIsShowDelete(true, mRestArray[mCurrentNum % 4]);
            }
        }
        return true;
    }

    public String getName() {
        if (mIsGroup) {
            return mGroupName;
        }else {
            Conversation conv = JMessageClient.getSingleConversation(mTargetId);
            return ((UserInfo)conv.getTargetInfo()).getNickname();
        }
    }

    public int getCurrentCount() {
        return mCurrentNum;
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
            mCurrentNum = mMemberInfoList.size();
            mChatDetailView.setTitle(mCurrentNum);
            if (mGridAdapter != null) {
                mGridAdapter.refreshMemberList(mGroupId);
            }
            Log.i(TAG, "Group Member Changing");
        }
    }

}
