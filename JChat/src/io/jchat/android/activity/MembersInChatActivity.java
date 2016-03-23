package io.jchat.android.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import io.jchat.android.R;
import io.jchat.android.adapter.AllMembersAdapter;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.tools.DialogCreator;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.tools.HanziToPinyin;

/**
 * Created by Ken on 2015/11/25.
 */
public class MembersInChatActivity extends BaseActivity {

    private ListView mListView;
    private Dialog mDialog;
    private Context mContext;
    private ImageButton mReturnBtn;
    private TextView mTitle;
    private Button mRightBtn;
    private EditText mSearchEt;
    private List<UserInfo> mMemberInfoList = new ArrayList<UserInfo>();
    private AllMembersAdapter mAdapter;
    private Dialog mLoadingDialog;
    private long mGroupId;
    private boolean mIsDeleteMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_all_members);
        mListView = (ListView) findViewById(R.id.members_list_view);
        mReturnBtn = (ImageButton) findViewById(R.id.return_btn);
        mTitle = (TextView) findViewById(R.id.number_tv);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mSearchEt = (EditText) findViewById(R.id.search_et);

        mGroupId = getIntent().getLongExtra(JChatDemoApplication.GROUP_ID, 0);
        mIsDeleteMode = getIntent().getBooleanExtra(JChatDemoApplication.DELETE_MODE, false);
        final Conversation conv = JMessageClient.getGroupConversation(mGroupId);
        GroupInfo groupInfo = (GroupInfo) conv.getTargetInfo();
        mMemberInfoList = groupInfo.getGroupMembers();
        String groupOwnerId = groupInfo.getGroupOwner();
        final UserInfo myInfo = JMessageClient.getMyInfo();
        final boolean isCreator = groupOwnerId != null && groupOwnerId.equals(myInfo.getUserName());
        mAdapter = new AllMembersAdapter(this, mMemberInfoList, mIsDeleteMode);
        mListView.setAdapter(mAdapter);
        mListView.requestFocus();

        String title = this.getString(R.string.combine_title);
        mTitle.setText(String.format(title, mMemberInfoList.size()));
        if (mIsDeleteMode) {
            mRightBtn.setText(this.getString(R.string.delete));
        } else {
            mRightBtn.setText(this.getString(R.string.add));
        }

        mReturnBtn.setOnClickListener(listener);
        mRightBtn.setOnClickListener(listener);
        mSearchEt.addTextChangedListener(watcher);

        //单击ListView item，跳转到个人详情界面
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserInfo userInfo = mMemberInfoList.get(position);
                String userName = userInfo.getUserName();
                Intent intent = new Intent();
                if (userName.equals(myInfo.getUserName())) {
                    intent.setClass(mContext, MeInfoActivity.class);
                    startActivity(intent);
                } else {
                    intent.setClass(mContext, FriendInfoActivity.class);
                    intent.putExtra(JChatDemoApplication.TARGET_ID,
                            userInfo.getUserName());
                    intent.putExtra(JChatDemoApplication.GROUP_ID, mGroupId);
                    startActivity(intent);
                }
            }
        });

        //如果是群主，长按ListView item可以删除群成员
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                if (isCreator && !mIsDeleteMode) {
                    View.OnClickListener listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switch (v.getId()) {
                                case R.id.cancel_btn:
                                    mDialog.dismiss();
                                    break;
                                case R.id.commit_btn:
                                    mDialog.dismiss();
                                    mLoadingDialog = DialogCreator.createLoadingDialog(mContext,
                                            mContext.getString(R.string.deleting_hint));
                                    mLoadingDialog.show();
                                    List<String> list = new ArrayList<String>();
                                    list.add(mMemberInfoList.get(position).getUserName());
                                    JMessageClient.removeGroupMembers(mGroupId, list, new BasicCallback() {
                                        @Override
                                        public void gotResult(int status, String desc) {
                                            mLoadingDialog.dismiss();
                                            if (status == 0) {
                                                refreshMemberList();
                                            } else {
                                                HandleResponseCode.onHandle(mContext, status, false);
                                            }
                                        }
                                    });
                                    break;

                            }
                        }
                    };
                    mDialog = DialogCreator.createDeleteMemberDialog(mContext, listener, true);
                    mDialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
                    mDialog.show();
                }
                return true;
            }
        });
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.return_btn:
                    Intent intent = new Intent();
                    setResult(JChatDemoApplication.RESULT_CODE_ALL_MEMBER, intent);
                    finish();
                    break;
                case R.id.right_btn:
                    if (mIsDeleteMode) {
                        List<String> deleteList = mAdapter.getSelectedList();
                        if (deleteList.size() > 0) {
                            showDeleteMemberDialog(deleteList);
                        }
                    } else {
                        addMemberToGroup();
                    }
                    break;
            }
        }
    };

    TextWatcher watcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            filterData(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };


    private void showDeleteMemberDialog(final List<String> list) {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.cancel_btn:
                        mDialog.dismiss();
                        break;
                    case R.id.commit_btn:
                        mDialog.dismiss();
                        mLoadingDialog = DialogCreator.createLoadingDialog(mContext,
                                mContext.getString(R.string.deleting_hint));
                        mLoadingDialog.show();
                        JMessageClient.removeGroupMembers(mGroupId, list, new BasicCallback() {
                            @Override
                            public void gotResult(int status, String desc) {
                                mLoadingDialog.dismiss();
                                if (status == 0) {
                                    Intent intent = new Intent();
                                    setResult(JChatDemoApplication.RESULT_CODE_ALL_MEMBER, intent);
                                    finish();
                                } else {
                                    HandleResponseCode.onHandle(mContext, status, false);
                                }
                            }
                        });
                        break;

                }
            }
        };
        mDialog = DialogCreator.createDeleteMemberDialog(mContext, listener, false);
        mDialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
        mDialog.show();
    }

    //点击添加按钮触发事件
    private void addMemberToGroup() {
        final Dialog dialog = new Dialog(this, R.style.default_dialog_style);
        final View view = LayoutInflater.from(mContext)
                .inflate(R.layout.dialog_add_friend_to_conv_list, null);
        dialog.setContentView(view);
        dialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
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
                        final String targetId = userNameEt.getText().toString().trim();
                        if (TextUtils.isEmpty(targetId)) {
                            Toast.makeText(mContext, mContext.getString(R.string.username_not_null_toast),
                                    Toast.LENGTH_SHORT).show();
                            break;
                            //检查群组中是否包含该用户
                        } else if (checkIfNotContainUser(targetId)) {
                            mLoadingDialog = DialogCreator.createLoadingDialog(mContext,
                                    mContext.getString(R.string.searching_user));
                            mLoadingDialog.show();
                            getUserInfo(targetId, dialog);
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

    /**
     * 添加成员时检查是否存在该群成员
     *
     * @param targetId 要添加的用户
     * @return 返回是否存在该用户
     */
    private boolean checkIfNotContainUser(String targetId) {
        if (mMemberInfoList != null) {
            for (UserInfo userInfo : mMemberInfoList) {
                if (userInfo.getUserName().equals(targetId))
                    return false;
            }
            return true;
        }
        return true;
    }

    private void getUserInfo(String targetId, final Dialog dialog) {
        JMessageClient.getUserInfo(targetId, new GetUserInfoCallback() {
            @Override
            public void gotResult(int status, String desc, UserInfo userInfo) {
                if (mLoadingDialog != null) {
                    mLoadingDialog.dismiss();
                }
                if (status == 0) {
                    addAMember(userInfo);
                    dialog.cancel();
                } else {
                    HandleResponseCode.onHandle(mContext, status, true);
                }
            }
        });
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
                    // 添加群成员
                    refreshMemberList();
                    mListView.setSelection(mListView.getBottom());
                } else {
                    HandleResponseCode.onHandle(mContext, status, true);
                }
            }
        });
    }

    //添加或者删除成员后重新获得MemberInfoList
    private void refreshMemberList() {
        Conversation conv = JMessageClient.getGroupConversation(mGroupId);
        GroupInfo groupInfo = (GroupInfo) conv.getTargetInfo();
        mMemberInfoList = groupInfo.getGroupMembers();
        mAdapter.refreshMemberList(mMemberInfoList);
        mTitle.setText("(" + mMemberInfoList.size() + ")");
    }

    /**
     * 根据输入框输入的字符过滤群成员
     * @param data
     */
    private void filterData(String data) {
        List<UserInfo> filterList = new ArrayList<UserInfo>();
        if (TextUtils.isEmpty(data)) {
            filterList = mMemberInfoList;
        } else {
            filterList.clear();
            for (UserInfo userInfo : mMemberInfoList) {
                String displayName;
                if (TextUtils.isEmpty(userInfo.getNickname())) {
                    displayName = userInfo.getUserName();
                } else {
                    displayName = userInfo.getNickname();
                }
                ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance().get(displayName);
                StringBuilder sb = new StringBuilder();
                if (tokens != null && tokens.size() > 0) {
                    for (HanziToPinyin.Token token : tokens) {
                        if (token.type == HanziToPinyin.Token.PINYIN) {
                            sb.append(token.target);
                        } else {
                            sb.append(token.source);
                        }
                    }
                }

                if (displayName.contains(data) || displayName.startsWith(data)) {
                    filterList.add(userInfo);
                    continue;
                }

                if (!TextUtils.isEmpty(sb)) {
                    String sortString = sb.toString().substring(0, 1).toUpperCase();
                    if (sortString.equals(data.substring(0, 1).toUpperCase())) {
                        filterList.add(userInfo);
                    }
                }
            }
        }

        mAdapter.updateListView(filterList);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(JChatDemoApplication.RESULT_CODE_ALL_MEMBER, intent);
        finish();
        super.onBackPressed();
    }
}
