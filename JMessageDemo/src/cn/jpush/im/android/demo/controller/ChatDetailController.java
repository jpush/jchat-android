package cn.jpush.im.android.demo.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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

import cn.jpush.im.android.demo.R;

import java.io.File;
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
import cn.jpush.im.android.demo.activity.ChatActivity;
import cn.jpush.im.android.demo.activity.ChatDetailActivity;
import cn.jpush.im.android.demo.activity.FriendInfoActivity;
import cn.jpush.im.android.demo.activity.MeInfoActivity;
import cn.jpush.im.android.demo.adapter.GroupMemberGridAdapter;
import cn.jpush.im.android.demo.tools.BitmapLoader;
import cn.jpush.im.android.demo.tools.HandleResponseCode;
import cn.jpush.im.android.demo.tools.NativeImageLoader;
import cn.jpush.im.android.demo.view.ChatDetailView;
import cn.jpush.im.android.demo.view.LoadingDialog;
import cn.jpush.im.api.BasicCallback;

public class ChatDetailController implements OnClickListener,
        OnItemClickListener, OnItemLongClickListener {

    private static final String TAG = "ChatDetailController";

    private ChatDetailView mChatDetailView;
    private ChatDetailActivity mContext;
    private GroupMemberGridAdapter mGridAdapter;
    private List<String> mMemberIDList = new ArrayList<String>();
    // 当前GridView群成员项数
    private int mCurrentNum;
    // 空白项的项数
    private int[] mRestArray;
    private boolean mIsGroup = false;
    private boolean mIsCreator = false;
    private String mGroupOwnerID;
    private long mGroupID;
    private String mTargetID;
    private LoadingDialog mLD;
    private Dialog mLoadingDialog = null;
    private boolean mIsShowDelete = false;
    private static final int GET_GROUP_MEMBER = 2047;
    private static final int ADD_TO_GRIDVIEW = 2048;
    private static final int DELETE_FROM_GRIDVIEW = 2049;
    private double mDensity;

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
        mIsGroup = intent.getBooleanExtra("isGroup", false);
        mGroupID = intent.getLongExtra("groupID", 0);
        Log.i(TAG, "mGroupID" + mGroupID);
        mTargetID = intent.getStringExtra("targetID");
        Log.i(TAG, "mTargetID: " + mTargetID);
        // 是群组
        if (mIsGroup) {
            mChatDetailView.setGroupName(getGroupName(mGroupID));
            //获得群组基本信息：群主ID、群组名、群组人数
            JMessageClient.getGroupInfo(mGroupID,
                    new GetGroupInfoCallback(false) {
                        @Override
                        public void gotResult(final int status, final String desc, GroupInfo group) {
                            if (status == 0) {
                                Bundle bundle = new Bundle();
                                android.os.Message msg = handler
                                        .obtainMessage();
                                msg.what = 0;
                                Log.i(TAG, "Group owner is " + group.getGroupOwner());
                                bundle.putString("groupOwnerID",
                                        group.getGroupOwner());
                                msg.setData(bundle);
                                msg.sendToTarget();
                            } else {
                                mContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        HandleResponseCode.onHandle(mContext, status);
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
                        public void gotResult(int status, String desc, List<String> members) {
                            if (status == 0) {
                                android.os.Message msg = handler.obtainMessage();
                                msg.what = GET_GROUP_MEMBER;
                                Bundle bundle = new Bundle();
                                bundle.putStringArrayList("memberList", (ArrayList) members);
                                msg.setData(bundle);
                                msg.sendToTarget();
                            }
                        }
                    });
//                }
            // 是单聊
        } else {
            mMemberIDList.add(mTargetID);
            initAdapter();

            // 设置单聊界面
            mChatDetailView.setSingleView();
        }
    }

    private String getGroupName(long groupID){
        Conversation conv = JMessageClient.getGroupConversation(groupID);
        if (conv != null) {
            return conv.getDisplayName();
        }else return null;
    }

    private void initAdapter() {
        mCurrentNum = mMemberIDList.size();
        // 除了群成员Item和添加、删除按钮，剩下的都看成是空白项，
        // 对应的mRestNum[mCurrent%4]的值即为空白项的数目
        mRestArray = new int[]{2, 1, 0, 3};
        // 初始化头像
        mGridAdapter = new GroupMemberGridAdapter(mContext, mMemberIDList, mIsCreator, mIsGroup);
        mChatDetailView.setAdapter(mGridAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.return_btn:
                mContext.finish();
                break;

            // 设置群组名称
            case R.id.group_name_rl:
                mContext.showGroupNameSettingDialog(1, mGroupID, getGroupName(mGroupID));
                break;

            // 设置我在群组的昵称
            case R.id.group_my_name_ll:
                mContext.showGroupNameSettingDialog(2, mGroupID, getGroupName(mGroupID));
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
                title.setTextColor(Color.parseColor("#000000"));
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
                                    conv = JMessageClient.getGroupConversation(Integer.parseInt(mTargetID));
                                else
                                    conv = JMessageClient.getSingleConversation(mTargetID);
                                if (conv != null) {
                                    conv.deleteAllMessage();
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
        mLD = new LoadingDialog();
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
                            ((Activity) ChatActivity.mChatActivity).finish();
                            mContext.StartMainActivity();
                        } else HandleResponseCode.onHandle(mContext, status);
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
            // 点击群成员项时
            if (position < mCurrentNum) {
                Intent intent = new Intent();
                if (mMemberIDList.get(position).equals(JMessageClient.getMyInfo().getUserName())) {
                    intent.setClass(mContext, MeInfoActivity.class);
                } else {
                    intent.putExtra("targetID", mMemberIDList.get(position));
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
                mGridAdapter.setIsShowDelete(mIsShowDelete,
                        mRestArray[mCurrentNum % 4]);
            }
            // delete状态
        } else {
            // 点击群成员Item时
            if (position < mCurrentNum) {
                //如果群主删除自己
                if (mMemberIDList.get(position).equals(JMessageClient.getMyInfo().getUserName())) {
                    return;
                } else {
                    // 删除某个群成员
                    mLD = new LoadingDialog();
                    mLoadingDialog = mLD.createLoadingDialog(mContext, mContext.getString(R.string.deleting_hint));
                    List<String> delList = new ArrayList<String>();
                    //之所以要传一个List，考虑到之后可能支持同时删除多人功能，现在List中只有一个元素
                    delList.add(mMemberIDList.get(position));
                    delMember(delList, position);
                    // 当前成员数为0，退出删除状态
                    if (mMemberIDList.size() == 0) {
                        mIsShowDelete = false;
                        mGridAdapter.setIsShowDelete(mIsShowDelete);
                    }
                }
                // 点击空白项时, 恢复GridView界面
            } else {
                if (mIsShowDelete) {
                    mIsShowDelete = false;
                    mGridAdapter.setIsShowDelete(mIsShowDelete,
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
                        if (targetID == null || targetID.equals("")) {
                            Toast.makeText(mContext, mContext.getString(R.string.username_not_null_toast), Toast.LENGTH_SHORT).show();
                            break;
                        } else if (!mMemberIDList.contains(targetID)) {
                            mLD = new LoadingDialog();
                            mLoadingDialog = mLD.createLoadingDialog(mContext, mContext.getString(R.string.searching_user));
                            mLoadingDialog.show();
                            JMessageClient.getUserInfo(targetID, new GetUserInfoCallback(false) {
                                @Override
                                public void gotResult(final int status, String desc, UserInfo userInfo) {
                                    if (status == 0) {
                                        //缓存头像
                                        File file = userInfo.getAvatar();
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
                                        android.os.Message msg = handler.obtainMessage();
                                        msg.what = 1;
                                        Bundle bundle = new Bundle();
                                        bundle.putStringArrayList("userIDs", userIDs);
                                        msg.setData(bundle);
                                        msg.sendToTarget();
                                    } else {
                                        mContext.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (mLoadingDialog != null)
                                                    mLoadingDialog.dismiss();
                                                HandleResponseCode.onHandle(mContext, status);
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
     * 增加群成员
     * @param list 要增加的成员的用户名，目前一次只能增加一个
     */
    private void addMember(final ArrayList<String> list) {
        try {
            mLD = new LoadingDialog();
            mLoadingDialog = mLD.createLoadingDialog(mContext, mContext.getString(R.string.adding_hint));
            mLoadingDialog.show();
            JMessageClient.addGroupMembers(mGroupID, list,
                    new BasicCallback(false) {

                        @Override
                        public void gotResult(final int status, final String desc) {
                            if (status == 0) {
                                // 更新GridView

                                android.os.Message msg = handler
                                        .obtainMessage();
                                // 添加群成员
                                msg.what = ADD_TO_GRIDVIEW;
                                Bundle bundle = new Bundle();
                                Log.i(TAG, "list.toString() " + list.toString());
                                bundle.putStringArrayList("memberList", list);
                                msg.setData(bundle);
                                msg.sendToTarget();
                                mLoadingDialog.dismiss();
                            } else {
                                mLoadingDialog.dismiss();
                                mContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        HandleResponseCode.onHandle(mContext, status);
                                    }
                                });
                            }
                        }
                    });
        } catch (Exception e) {
            mLoadingDialog.dismiss();
            e.printStackTrace();
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, mContext.getString(R.string.unknown_error_toast), Toast.LENGTH_SHORT)
                            .show();
                }
            });
        }
    }

    /**
     * 删除成员
     * @param list
     * @param position
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
                                android.os.Message msg = handler
                                        .obtainMessage();
                                msg.what = DELETE_FROM_GRIDVIEW;
                                Bundle bundle = new Bundle();
                                bundle.putInt("position", position);
                                msg.setData(bundle);
                                msg.sendToTarget();
                            } else {
                                mContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        HandleResponseCode.onHandle(mContext, status);
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

    Handler handler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                // 初始化群组
                case 0:
                    mGroupOwnerID = msg.getData().getString("groupOwnerID");
                    // 判断是否为群主
                    if (mGroupOwnerID != null && mGroupOwnerID.equals(JMessageClient.getMyInfo().getUserName()))
                        mIsCreator = true;
                    mChatDetailView.setMyName(JMessageClient.getMyInfo().getUserName());
                    if (mGridAdapter != null) {
                        mGridAdapter.setCreator(mIsCreator);
                    }
                    break;
                //点击加人按钮并且用户信息返回正确
                case 1:
                    if (mLoadingDialog != null)
                        mLoadingDialog.dismiss();
                    final String newMember = msg.getData().getStringArrayList("userIDs").get(0);
                    if (mIsGroup)
                        addMember(msg.getData().getStringArrayList("userIDs"));
                        //在单聊中点击加人按钮并且用户信息返回正确,如果为第三方则创建群聊
                    else {
                        if (newMember.equals(JMessageClient.getMyInfo().getUserName()) || newMember.equals(mTargetID))
                            return;
                        else addMemberAndCreateGroup(newMember);
                    }
                    break;
                // 获取成员列表，缓存头像，更新GridView
                case GET_GROUP_MEMBER:
                    mMemberIDList = msg.getData().getStringArrayList("memberList");
                    NativeImageLoader.getInstance().setAvatarCache(mMemberIDList, (int) (50 * mDensity), new NativeImageLoader.cacheAvatarCallBack() {
                        @Override
                        public void onCacheAvatarCallBack(int status) {
                            if (status == 0) {
                                mContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i(TAG, "init group members' avatar succeed");
                                        if (mGridAdapter != null)
                                            mGridAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    });
                    initAdapter();
                    break;
                // 添加成员
                case ADD_TO_GRIDVIEW:
                    ++mCurrentNum;
                    mGridAdapter.addMemberToList(msg.getData().getStringArrayList("memberList"));
                    Log.i("ADD_TO_GRIDVIEW", "已添加");
                    break;
                // 删除成员
                case DELETE_FROM_GRIDVIEW:
                    // 更新GridView
                    --mCurrentNum;
                    int position = msg.getData().getInt("position");
                    mGridAdapter.remove(position);
                    Log.i("DELETE_FROM_GRIDVIEW", "已删除");
                    break;
            }
        }
    };

    /**
     * 在单聊中点击增加按钮触发事件，创建群聊
     * @param newMember 要增加的成员
     */
    private void addMemberAndCreateGroup(final String newMember) {
        mLD = new LoadingDialog();
        mLoadingDialog = mLD.createLoadingDialog(mContext, mContext.getString(R.string.creating_hint));
        final String desc = "";
        mLoadingDialog.show();
        final String groupName = JMessageClient.getMyInfo().getUserName() + "、" + mTargetID
                + "、" + newMember;
        JMessageClient.createGroup(groupName, desc, new CreateGroupCallback(false) {
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
                            if (status == 0) {
                                mContext.StartChatActivity(groupID, groupName);
                            } else {
                                mContext.StartChatActivity(groupID, groupName);
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
                mGridAdapter.setIsShowDelete(mIsShowDelete,
                        mRestArray[mCurrentNum % 4]);
            }
        }
        return true;
    }

    /**
     * 当收到群成员变化的Event后，刷新成员列表
     * @param groupID
     */
    public void refresh(long groupID) {
        //当前群聊
        if (mGroupID == groupID) {
            JMessageClient.getGroupMembers(groupID, new GetGroupMembersCallback() {
                @Override
                public void gotResult(int status, String s, List<String> memberList) {
                    if (status == 0) {
                        mMemberIDList = memberList;
                        mCurrentNum = mMemberIDList.size();
                        mGridAdapter.refreshGroupMember(mMemberIDList);
                    }
                }
            });
            Log.i(TAG, "Group Member Changing");
        }
    }

    public Long getGroupID() {
        return mGroupID;
    }

    //刷新群名称
    public void NotifyGroupInfoChange() {
        if (mGridAdapter != null)
            mGridAdapter.notifyDataSetChanged();
        if (mIsGroup && mGroupID != 0) {
            Conversation conv = JMessageClient.getGroupConversation(mGroupID);
            if (conv != null)
                mChatDetailView.refreshGroupName(conv.getDisplayName());
            Log.i(TAG, "Fresh group name completed");
        }
    }
}
