package io.jchat.android.controller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
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
import cn.jpush.im.android.api.enums.ConversationType;
import io.jchat.android.R;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.api.callback.CreateGroupCallback;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.callback.GetGroupMembersCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import io.jchat.android.activity.ChatDetailActivity;
import io.jchat.android.activity.FriendInfoActivity;
import io.jchat.android.activity.MeInfoActivity;
import io.jchat.android.adapter.GroupMemberGridAdapter;
import io.jchat.android.application.JPushDemoApplication;
import io.jchat.android.tools.BitmapLoader;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.tools.NativeImageLoader;
import io.jchat.android.view.ChatDetailView;
import io.jchat.android.view.DialogCreator;
import cn.jpush.im.api.BasicCallback;

public class ChatDetailController implements OnClickListener, OnItemClickListener, OnItemLongClickListener {

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
    private long mGroupID;
    private String mTargetID;
    private DialogCreator mLD;
    private Dialog mLoadingDialog = null;
    private boolean mIsShowDelete = false;
    private static final int GET_GROUP_MEMBER = 2047;
    private static final int ADD_TO_GRIDVIEW = 2048;
    private static final int DELETE_FROM_GRIDVIEW = 2049;
    private double mDensity;
    private String mGroupName;
    private final MyHandler myHandler = new MyHandler(this);

    public ChatDetailController(ChatDetailView chatDetailView,
                                ChatDetailActivity context) {
        this.mChatDetailView = chatDetailView;
        this.mContext = context;
        initData();
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDensity = dm.density;
    }

    /*
     * 获得群组信息，初始化群组界面，先从本地读取，如果没有再从服务器读取
     */
    private void initData() {
        Intent intent = mContext.getIntent();
        mIsGroup = intent.getBooleanExtra(JPushDemoApplication.IS_GROUP, false);
        mGroupID = intent.getLongExtra(JPushDemoApplication.GROUP_ID, 0);
        Log.i(TAG, "mGroupID" + mGroupID);
        mTargetID = intent.getStringExtra(JPushDemoApplication.TARGET_ID);
        Log.i(TAG, "mTargetID: " + mTargetID);
        // 是群组
        if (mIsGroup) {
            //获得群组基本信息：群主ID、群组名、群组人数
            JMessageClient.getGroupInfo(mGroupID,
                    new GetGroupInfoCallback(false) {
                        @Override
                        public void gotResult(final int status, final String desc, GroupInfo group) {
                            if (status == 0) {
                                android.os.Message msg = myHandler.obtainMessage();
                                msg.what = 0;
                                msg.obj = group;
                                Log.i(TAG, "Group owner is " + group.getGroupOwner());
                                msg.sendToTarget();
                            } else {
                                mContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        HandleResponseCode.onHandle(mContext, status, false);
                                    }
                                });
                            }

                        }
                    }
            );

            String myNickName = JMessageClient.getMyInfo().getNickname();
            // TODO 群名，昵称，群人数等初始化
            mChatDetailView.setMyName(myNickName);
            //获得群组成员ID
            JMessageClient
                    .getGroupMembers(mGroupID, new GetGroupMembersCallback() {
                        @Override
                        public void gotResult(int status, String desc, List<UserInfo> members) {
                            if (status == 0) {
                                android.os.Message msg = myHandler.obtainMessage();
                                mMemberInfoList = members;
                                msg.what = GET_GROUP_MEMBER;
                                msg.sendToTarget();
                            }
                        }
                    });
            // 是单聊
        } else {
            Conversation conv = JMessageClient.getSingleConversation(mTargetID);
            mCurrentNum = 1;
            mGridAdapter = new GroupMemberGridAdapter(mContext, mTargetID, conv.getTitle());
            mChatDetailView.setAdapter(mGridAdapter);
            // 设置单聊界面
            mChatDetailView.setSingleView();
        }
    }


    private void initAdapter() {
        mCurrentNum = mMemberInfoList.size();
        // 初始化头像
        mGridAdapter = new GroupMemberGridAdapter(mContext, mMemberInfoList, mIsCreator);
        mChatDetailView.setAdapter(mGridAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.return_btn:
                Intent intent = new Intent();
                intent.putExtra(JPushDemoApplication.GROUP_NAME, getGroupName());
                intent.putExtra("currentCount", mCurrentNum);
                mContext.setResult(JPushDemoApplication.RESULT_CODE_CHAT_DETAIL, intent);
                mContext.finish();
                break;

            // 设置群组名称
            case R.id.group_name_rl:
                mContext.showGroupNameSettingDialog(1, mGroupID, mGroupName);
                break;

            // 设置我在群组的昵称
            case R.id.group_my_name_ll:
                mContext.showGroupNameSettingDialog(2, mGroupID, mGroupName);
                break;

            // 群组人数
            case R.id.group_num_rl:
                break;

            // 查询聊天记录
            case R.id.group_chat_record_ll:
                break;

            // 删除聊天记录
            case R.id.group_chat_del_rl:
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_reset_password, null);
                builder.setView(view);
                TextView title = (TextView) view.findViewById(R.id.title_tv);
                title.setText(mContext.getString(R.string.clear_history_confirm_title));
                final EditText pwdEt = (EditText) view.findViewById(R.id.password_et);
                pwdEt.setVisibility(View.GONE);
                final Button cancel = (Button) view.findViewById(R.id.cancel_btn);
                final Button commit = (Button) view.findViewById(R.id.commit_btn);
                final Dialog dialog = builder.create();
                dialog.show();
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getId()) {
                            case R.id.cancel_btn:
                                dialog.cancel();
                                break;
                            case R.id.commit_btn:
                                Conversation conv;
                                if (mIsGroup)
                                    conv = JMessageClient.getGroupConversation(mGroupID);
                                else
                                    conv = JMessageClient.getSingleConversation(mTargetID);
                                if (conv != null) {
                                    conv.deleteAllMessage();
                                    Intent intent = new Intent(JPushDemoApplication.CLEAR_MSG_LIST_ACTION);
                                    mContext.sendBroadcast(intent);
                                }
                                dialog.cancel();
                                break;
                        }
                    }
                };
                cancel.setOnClickListener(listener);
                commit.setOnClickListener(listener);
                break;
            case R.id.chat_detail_del_group:
                deleteAndExit();
                break;
        }
    }

    /**
     * 删除并退出
     */
    private void deleteAndExit() {
        mLD = new DialogCreator();
        mLoadingDialog = mLD.createLoadingDialog(mContext, mContext.getString(R.string.exiting_group_toast));
        mLoadingDialog.show();
        JMessageClient.exitGroup(mGroupID, new BasicCallback(false) {
            @Override
            public void gotResult(final int status, final String desc) {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mLoadingDialog != null)
                            mLoadingDialog.dismiss();
                        if (status == 0) {
                            boolean deleted = JMessageClient.deleteGroupConversation(mGroupID);
                            Log.i(TAG, "deleted: " + deleted);
                            mContext.StartMainActivity();
                        } else HandleResponseCode.onHandle(mContext, status, false);
                    }
                });
            }
        });
    }

    // GridView点击事件
    @Override
    public void onItemClick(AdapterView<?> viewAdapter, View view,
                            final int position, long id) {
        // 没有触发delete时
        if (!mIsShowDelete) {
            Intent intent = new Intent();
            //群聊
            if (mIsGroup) {
                // 点击群成员项时
                if (position < mCurrentNum) {
                    if (mMemberInfoList.get(position).getUserName().equals(JMessageClient.getMyInfo().getUserName())) {
                        intent.setClass(mContext, MeInfoActivity.class);
                    } else {
                        intent.putExtra(JPushDemoApplication.TARGET_ID, mMemberInfoList.get(position).getUserName());
                        intent.setClass(mContext, FriendInfoActivity.class);
                    }
                    mContext.startActivity(intent);
                    // 点击添加成员按钮
                } else if (position == mCurrentNum) {
                    addMemberToGroup();
                    // mContext.showContacts();

                    // 是群主, 成员个数大于1并点击删除按钮
                } else if (position == mCurrentNum + 1 && mIsCreator && mCurrentNum > 1) {
                    // delete friend from group
                    mIsShowDelete = true;
                    mGridAdapter.setIsShowDelete(true,
                            mRestArray[mCurrentNum % 4]);
                }
                //单聊
            } else if (position < mCurrentNum) {
                intent.putExtra(JPushDemoApplication.TARGET_ID, mTargetID);
                intent.setClass(mContext, FriendInfoActivity.class);
                mContext.startActivity(intent);
            } else if (position == mCurrentNum) {
                addMemberToGroup();
            }

            // delete状态
        } else {
            // 点击群成员Item时
            if (position < mCurrentNum) {
                //如果群主删除自己
                if (mMemberInfoList.get(position).getUserName().equals(JMessageClient.getMyInfo().getUserName())) {
                    return;
                } else {
                    // 删除某个群成员
                    mLD = new DialogCreator();
                    mLoadingDialog = mLD.createLoadingDialog(mContext, mContext.getString(R.string.deleting_hint));
                    List<String> delList = new ArrayList<String>();
                    //之所以要传一个List，考虑到之后可能支持同时删除多人功能，现在List中只有一个元素
                    delList.add(mMemberInfoList.get(position).getUserName());
                    delMember(delList, position);
                    // 当前成员数为0，退出删除状态
                    if (mMemberInfoList.size() == 0) {
                        mIsShowDelete = false;
                        mGridAdapter.setIsShowDelete(false);
                    }
                }
                // 点击空白项时, 恢复GridView界面
            } else {
                if (mIsShowDelete) {
                    mIsShowDelete = false;
                    mGridAdapter.setIsShowDelete(false,
                            mRestArray[mCurrentNum % 4]);
                }
            }
        }
    }

    //点击添加按钮触发事件
    private void addMemberToGroup() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final View view = LayoutInflater.from(mContext).inflate(
                R.layout.dialog_add_friend_to_conv_list, null);
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
                            Toast.makeText(mContext, mContext.getString(R.string.username_not_null_toast), Toast.LENGTH_SHORT).show();
                            break;
                            //检查群组中是否包含该用户
                        } else if (checkIfNotContainUser(targetID)) {
                            mLD = new DialogCreator();
                            mLoadingDialog = mLD.createLoadingDialog(mContext, mContext.getString(R.string.searching_user));
                            mLoadingDialog.show();
                            JMessageClient.getUserInfo(targetID, new GetUserInfoCallback(false) {
                                @Override
                                public void gotResult(final int status, String desc, UserInfo userInfo) {
                                    if (status == 0) {
                                        //缓存头像
                                        File file = userInfo.getAvatarFile();
                                        if (file != null) {
                                            Bitmap bitmap = BitmapLoader.getBitmapFromFile(file.getAbsolutePath(),
                                                    (int) (50 * mDensity), (int) (50 * mDensity));
                                            if (bitmap != null)
                                                NativeImageLoader.getInstance().updateBitmapFromCache(targetID, bitmap);
                                        }
                                        dialog.cancel();
                                        // add friend to group
                                        // 要增加到群的成员ID集合
                                        ArrayList<String> userIDs = new ArrayList<String>();
                                        userIDs.add(targetID);
                                        android.os.Message msg = myHandler.obtainMessage();
                                        msg.what = 1;
                                        msg.obj = userInfo;
                                        msg.sendToTarget();
                                    } else {
                                        mContext.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (mLoadingDialog != null)
                                                    mLoadingDialog.dismiss();
                                                HandleResponseCode.onHandle(mContext, status, true);
                                            }
                                        });
                                    }
                                }
                            });

                        } else {
                            dialog.cancel();
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mContext, mContext.getString(R.string.user_already_exist_toast), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        break;
                }
            }
        };
        cancel.setOnClickListener(listener);
        commit.setOnClickListener(listener);
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
            mLD = new DialogCreator();
            mLoadingDialog = mLD.createLoadingDialog(mContext, mContext.getString(R.string.adding_hint));
            mLoadingDialog.show();
            ArrayList<String> list = new ArrayList<String>();
            list.add(userInfo.getUserName());
            JMessageClient.addGroupMembers(mGroupID, list,
                    new BasicCallback() {

                        @Override
                        public void gotResult(final int status, final String desc) {
                            if (status == 0) {
                                // 添加群成员
                                ++mCurrentNum;
                                mGridAdapter.addMemberToList(userInfo);
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
            Toast.makeText(mContext, mContext.getString(R.string.unknown_error_toast), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 删除成员
     *
     * @param list     被删除的用户名List
     * @param position 删除的位置
     */
    private void delMember(List<String> list, final int position) {
        mLoadingDialog.show();
        try {
            JMessageClient.removeGroupMembers(mGroupID,
                    list,
                    new BasicCallback() {

                        @Override
                        public void gotResult(final int status, final String desc) {
                            mLoadingDialog.dismiss();
                            if (status == 0) {
                                android.os.Message msg = myHandler.obtainMessage();
                                msg.what = DELETE_FROM_GRIDVIEW;
                                Bundle bundle = new Bundle();
                                bundle.putInt(JPushDemoApplication.POSITION, position);
                                msg.setData(bundle);
                                msg.sendToTarget();
                            } else {
                                mContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        HandleResponseCode.onHandle(mContext, status, false);
                                    }
                                });
                            }
                        }
                    });
        } catch (Exception e) {
            mLoadingDialog.dismiss();
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, mContext.getString(R.string.unknown_error_toast), Toast.LENGTH_SHORT)
                            .show();
                }
            });
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
                    // 初始化群组
                    case 0:
                        GroupInfo groupInfo = (GroupInfo) msg.obj;
                        UserInfo myInfo = JMessageClient.getMyInfo();
                        String groupOwnerID = groupInfo.getGroupOwner();
                        controller.mGroupName = groupInfo.getGroupName();
                        if (TextUtils.isEmpty(controller.mGroupName)) {
                            controller.mChatDetailView.setGroupName(controller.mContext.getString(R.string.unnamed));
                        } else {
                            controller.mChatDetailView.setGroupName(controller.mGroupName);
                        }
                        // 判断是否为群主
                        if (groupOwnerID != null && groupOwnerID.equals(myInfo.getUserName()))
                            controller.mIsCreator = true;
                        controller.mChatDetailView.setMyName(myInfo.getUserName());
                        if (controller.mGridAdapter != null) {
                            controller.mGridAdapter.setCreator(controller.mIsCreator);
                        }
                        break;
                    //点击加人按钮并且用户信息返回正确
                    case 1:
                        Log.i(TAG, "Adding Group Member, got UserInfo");
                        if (controller.mLoadingDialog != null)
                            controller.mLoadingDialog.dismiss();
                        final UserInfo userInfo = (UserInfo) msg.obj;
                        if (controller.mIsGroup)
                            controller.addAMember(userInfo);
                            //在单聊中点击加人按钮并且用户信息返回正确,如果为第三方则创建群聊
                        else {
                            if (userInfo.getUserName().equals(JMessageClient.getMyInfo().getUserName()) || userInfo.getUserName().equals(controller.mTargetID))
                                return;
                            else controller.addMemberAndCreateGroup(userInfo.getUserName());
                        }
                        break;
                    // 获取成员列表，缓存头像，更新GridView
                    case GET_GROUP_MEMBER:
                        controller.mChatDetailView.setTitle(controller.mMemberInfoList.size());
                        controller.initAdapter();
                        break;
                    // 添加成员
                    case ADD_TO_GRIDVIEW:
//                    ++mCurrentNum;
//                    mGridAdapter.addMemberToList(msg.getData().getStringArrayList("memberList"));
//                    Log.i("ADD_TO_GRIDVIEW", "已添加");
                        break;
                    // 删除成员
                    case DELETE_FROM_GRIDVIEW:
                        // 更新GridView
                        --controller.mCurrentNum;
                        int position = msg.getData().getInt(JPushDemoApplication.POSITION);
                        controller.mGridAdapter.remove(position);
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
        mLD = new DialogCreator();
        mLoadingDialog = mLD.createLoadingDialog(mContext, mContext.getString(R.string.creating_hint));
        mLoadingDialog.show();
        JMessageClient.createGroup("", "", new CreateGroupCallback(false) {
            @Override
            public void gotResult(int status, final String desc, final long groupID) {
                if (status == 0) {
                    ArrayList<String> list = new ArrayList<String>();
                    list.add(mTargetID);
                    list.add(newMember);
                    JMessageClient.addGroupMembers(groupID, list, new BasicCallback(false) {
                        @Override
                        public void gotResult(int status, String desc) {
                            if (mLoadingDialog != null)
                                mLoadingDialog.dismiss();
                            Conversation conv = Conversation.createConversation(ConversationType.group, groupID);
                            if (status == 0) {
                                mContext.StartChatActivity(groupID, conv.getTitle());
                            } else {
                                mContext.StartChatActivity(groupID, conv.getTitle());
                                Toast.makeText(mContext, desc, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mLoadingDialog != null)
                            mLoadingDialog.dismiss();
                        Toast.makeText(mContext, desc, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // 群成员变化

    @Override
    public boolean onItemLongClick(AdapterView<?> viewAdapter, View view,
                                   int position, long id) {
        // 是群主并在非删除状态下长按Item触发事件
        if (!mIsShowDelete && mIsCreator) {
            if (position < mCurrentNum && mCurrentNum > 1) {
                mIsShowDelete = true;
                mGridAdapter.setIsShowDelete(true,
                        mRestArray[mCurrentNum % 4]);
            }
        }
        return true;
    }

    public long getGroupID() {
        return mGroupID;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public int getCurrentCount() {
        return mCurrentNum;
    }

    /**
     * 当收到群成员变化的Event后，刷新成员列表
     *
     * @param groupID 群组ID
     */
    public void refresh(long groupID) {
        //当前群聊
        if (mGroupID == groupID) {
            JMessageClient.getGroupMembers(groupID, new GetGroupMembersCallback() {
                @Override
                public void gotResult(int status, String s, List<UserInfo> memberList) {
                    if (status == 0) {
                        mMemberInfoList = memberList;
                        mCurrentNum = mMemberInfoList.size();
                        mChatDetailView.setTitle(mCurrentNum);
                        if (mGridAdapter != null)
                            mGridAdapter.refreshGroupMember(mMemberInfoList);
                    }
                }
            });
            Log.i(TAG, "Group Member Changing");
        }
    }

}
