package io.jchat.android.activity;

<<<<<<< HEAD
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
import android.widget.AdapterView;
=======
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
>>>>>>> master
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

<<<<<<< HEAD
import java.util.ArrayList;
=======
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
>>>>>>> master
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
<<<<<<< HEAD
import io.jchat.android.tools.DialogCreator;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.tools.HanziToPinyin;
=======
import io.jchat.android.chatting.utils.DialogCreator;
import io.jchat.android.chatting.utils.HandleResponseCode;
import io.jchat.android.tools.HanyuPinyin;
>>>>>>> master

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
<<<<<<< HEAD
=======
    private List<ItemModel> mShowUserList = new ArrayList<ItemModel>();
    private List<String> mPinyinList = new ArrayList<String>();
    private UIHandler mUIHandler = new UIHandler(this);
    private BackgroundHandler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private static final int PROCESS_USER_INFO_TO_BEANS = 0x1000;
    private static final int SEARCH_MEMBER = 0x1001;
    private static final int SEARCH_MEMBER_SUCCESS = 0x1002;
    private static final int INIT_ADAPTER = 0x1003;
    private static final int ADD_ALL_MEMBER = 0x1004;
>>>>>>> master
    private AllMembersAdapter mAdapter;
    private Dialog mLoadingDialog;
    private long mGroupId;
    private boolean mIsDeleteMode;
<<<<<<< HEAD
=======
    private boolean mIsCreator;
    private String mSearchText;
>>>>>>> master

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

<<<<<<< HEAD
=======
        mBackgroundThread = new HandlerThread("Work on MembersInChatActivity");
        mBackgroundThread.start();
        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());
>>>>>>> master
        mGroupId = getIntent().getLongExtra(JChatDemoApplication.GROUP_ID, 0);
        mIsDeleteMode = getIntent().getBooleanExtra(JChatDemoApplication.DELETE_MODE, false);
        final Conversation conv = JMessageClient.getGroupConversation(mGroupId);
        GroupInfo groupInfo = (GroupInfo) conv.getTargetInfo();
        mMemberInfoList = groupInfo.getGroupMembers();
        String groupOwnerId = groupInfo.getGroupOwner();
        final UserInfo myInfo = JMessageClient.getMyInfo();
<<<<<<< HEAD
        final boolean isCreator = groupOwnerId != null && groupOwnerId.equals(myInfo.getUserName());
        mAdapter = new AllMembersAdapter(this, mMemberInfoList, mIsDeleteMode);
        mListView.setAdapter(mAdapter);
        mListView.requestFocus();

        String title = this.getString(R.string.combine_title);
        mTitle.setText(String.format(title, mMemberInfoList.size()));
        if (mIsDeleteMode) {
            mRightBtn.setText(this.getString(R.string.delete));
=======
        mIsCreator = groupOwnerId != null && groupOwnerId.equals(myInfo.getUserName());

        mBackgroundHandler.sendEmptyMessage(PROCESS_USER_INFO_TO_BEANS);

        String title = this.getString(R.string.combine_title);
        mTitle.setText(String.format(title, mMemberInfoList.size() + ""));
        if (mIsDeleteMode) {
            mRightBtn.setText(this.getString(R.string.jmui_delete));
>>>>>>> master
        } else {
            mRightBtn.setText(this.getString(R.string.add));
        }

        mReturnBtn.setOnClickListener(listener);
        mRightBtn.setOnClickListener(listener);
        mSearchEt.addTextChangedListener(watcher);
<<<<<<< HEAD

        //单机ListView item，跳转到个人详情界面
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
                                    mLoadingDialog = DialogCreator.createLoadingDialog(mContext,
                                            mContext.getString(R.string.deleting_hint));
                                    mLoadingDialog.show();
                                    List<String> list = new ArrayList<String>();
                                    list.add(mMemberInfoList.get(position).getUserName());
                                    JMessageClient.removeGroupMembers(mGroupId, list, new BasicCallback() {
                                        @Override
                                        public void gotResult(int status, String desc) {
                                            mLoadingDialog.dismiss();
                                            mDialog.dismiss();
                                            if (status == 0) {
                                                mAdapter.refreshMemberList(mGroupId);
                                                refreshMemberList();
                                                mTitle.setText("(" + mMemberInfoList.size() + ")");
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
                    mDialog.show();
                }
                return true;
            }
        });
=======
>>>>>>> master
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.return_btn:
                    Intent intent = new Intent();
<<<<<<< HEAD
                    intent.putExtra(JChatDemoApplication.MEMBERS_COUNT, mMemberInfoList.size());
=======
>>>>>>> master
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
<<<<<<< HEAD
            filterData(s.toString());
=======
            mSearchText = s.toString().trim();
            mBackgroundHandler.removeMessages(SEARCH_MEMBER);
            mBackgroundHandler.sendMessageDelayed(mBackgroundHandler.obtainMessage(SEARCH_MEMBER), 200);
>>>>>>> master
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
<<<<<<< HEAD
                    case R.id.cancel_btn:
                        mDialog.dismiss();
                        break;
                    case R.id.commit_btn:
=======
                    case R.id.jmui_cancel_btn:
                        mDialog.dismiss();
                        break;
                    case R.id.jmui_commit_btn:
                        mDialog.dismiss();
>>>>>>> master
                        mLoadingDialog = DialogCreator.createLoadingDialog(mContext,
                                mContext.getString(R.string.deleting_hint));
                        mLoadingDialog.show();
                        JMessageClient.removeGroupMembers(mGroupId, list, new BasicCallback() {
                            @Override
                            public void gotResult(int status, String desc) {
                                mLoadingDialog.dismiss();
<<<<<<< HEAD
                                mDialog.dismiss();
                                if (status == 0) {
                                    Intent intent = new Intent();
                                    intent.putExtra(JChatDemoApplication.MEMBERS_COUNT,
                                            mMemberInfoList.size());
=======
                                if (status == 0) {
                                    Intent intent = new Intent();
>>>>>>> master
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
<<<<<<< HEAD
=======
        mDialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
>>>>>>> master
        mDialog.show();
    }

    //点击添加按钮触发事件
    private void addMemberToGroup() {
<<<<<<< HEAD
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final View view = LayoutInflater.from(mContext)
                .inflate(R.layout.dialog_add_friend_to_conv_list, null);
        builder.setView(view);
        final Dialog dialog = builder.create();
=======
        final Dialog dialog = new Dialog(this, R.style.jmui_default_dialog_style);
        final View view = LayoutInflater.from(mContext)
                .inflate(R.layout.dialog_add_friend_to_conv_list, null);
        dialog.setContentView(view);
        dialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
>>>>>>> master
        dialog.show();
        TextView title = (TextView) view.findViewById(R.id.dialog_name);
        title.setText(mContext.getString(R.string.add_friend_to_group_title));
        final EditText userNameEt = (EditText) view.findViewById(R.id.user_name_et);
<<<<<<< HEAD
        final Button cancel = (Button) view.findViewById(R.id.cancel_btn);
        final Button commit = (Button) view.findViewById(R.id.commit_btn);
=======
        final Button cancel = (Button) view.findViewById(R.id.jmui_cancel_btn);
        final Button commit = (Button) view.findViewById(R.id.jmui_commit_btn);
>>>>>>> master
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
<<<<<<< HEAD
                    case R.id.cancel_btn:
                        dialog.cancel();
                        break;
                    case R.id.commit_btn:
                        final String targetId = userNameEt.getText().toString().trim();
                        if (TextUtils.isEmpty(targetId)) {
                            Toast.makeText(mContext, mContext.getString(R.string.username_not_null_toast),
                                    Toast.LENGTH_SHORT).show();
=======
                    case R.id.jmui_cancel_btn:
                        dialog.cancel();
                        break;
                    case R.id.jmui_commit_btn:
                        final String targetId = userNameEt.getText().toString().trim();
                        if (TextUtils.isEmpty(targetId)) {
                            HandleResponseCode.onHandle(mContext, 801001, true);
>>>>>>> master
                            break;
                            //检查群组中是否包含该用户
                        } else if (checkIfNotContainUser(targetId)) {
                            mLoadingDialog = DialogCreator.createLoadingDialog(mContext,
                                    mContext.getString(R.string.searching_user));
                            mLoadingDialog.show();
                            getUserInfo(targetId, dialog);
                        } else {
<<<<<<< HEAD
                            dialog.cancel();
                            Toast.makeText(mContext, mContext.getString(R.string.user_already_exist_toast),
                                    Toast.LENGTH_SHORT).show();
=======
                            HandleResponseCode.onHandle(mContext, 1002, true);
>>>>>>> master
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
<<<<<<< HEAD
                if (status == 0) {
                    if (mLoadingDialog != null) {
                        mLoadingDialog.dismiss();
                    }
=======
                if (mLoadingDialog != null) {
                    mLoadingDialog.dismiss();
                }
                if (status == 0) {
>>>>>>> master
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
<<<<<<< HEAD
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
                        refreshMemberList();
                        mAdapter.refreshMemberList(mGroupId);
                        mListView.setSelection(mListView.getBottom());
                        mTitle.setText("(" + mMemberInfoList.size() + ")");
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
        GroupInfo groupInfo = (GroupInfo) conv.getTargetInfo();
        mMemberInfoList = groupInfo.getGroupMembers();
=======
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
                    Toast.makeText(mContext, mContext.getString(R.string.added), Toast.LENGTH_SHORT).show();
                } else {
                    HandleResponseCode.onHandle(mContext, status, true);
                }
            }
        });
    }

    //添加或者删除成员后重新获得MemberInfoList
    public void refreshMemberList() {
        mSearchText = "";
        mSearchEt.setText(mSearchText);
        Conversation conv = JMessageClient.getGroupConversation(mGroupId);
        GroupInfo groupInfo = (GroupInfo) conv.getTargetInfo();
        mMemberInfoList = groupInfo.getGroupMembers();
//        addAll(true);
        mTitle.setText(String.format(mContext.getString(R.string.combine_title), mMemberInfoList.size() + ""));
        mBackgroundHandler.sendEmptyMessage(ADD_ALL_MEMBER);
>>>>>>> master
    }

    /**
     * 根据输入框输入的字符过滤群成员
<<<<<<< HEAD
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
                if (!TextUtils.isEmpty(sb)) {
                    String sortString = sb.toString().substring(0, 1).toUpperCase();
                    if (displayName.contains(data) || displayName.startsWith(data)
                            || sortString.equals(data.substring(0, 1).toUpperCase())) {
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
        intent.putExtra(JChatDemoApplication.MEMBERS_COUNT, mMemberInfoList.size());
=======
     */
    private void filterData() {
        if (TextUtils.isEmpty(mSearchText)) {
            addAll();
        } else {
            String nickname, pinyin;
            int sort;
            SpannableString result;
            ItemModel model;
            UserInfo userInfo;
            for (int i = 0; i < mPinyinList.size(); i++) {
                sort = 0;
                userInfo = mMemberInfoList.get(i);
                nickname = userInfo.getNickname();
                if (TextUtils.isEmpty(nickname)) {
                    nickname = userInfo.getUserName();
                }
                result = new SpannableString(nickname);
                //先进行拼音匹配
                pinyin = mPinyinList.get(i).toLowerCase();
                int offset = pinyin.indexOf(mSearchText.toLowerCase());
                if (offset != -1) {
                    model = new ItemModel();
                    sort += mSearchText.length();
                    result.setSpan(new ForegroundColorSpan(Color.RED), offset,
                            offset + mSearchText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //进行直接匹配
                    int index = nickname.indexOf(mSearchText);
                    if (index != -1) {
                        sort += mSearchText.length();
                        result.setSpan(new ForegroundColorSpan(Color.RED), index,
                                index + mSearchText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        model.data = userInfo;
                        model.highlight = result;
                        model.sortIndex = sort;
                        mShowUserList.add(model);
                        continue;
                    }
                    model.data = userInfo;
                    model.highlight = result;
                    model.sortIndex = sort;
                    mShowUserList.add(model);
                    //进行直接匹配
                } else {
                    int index = nickname.indexOf(mSearchText);
                    if (index != -1) {
                        sort += mSearchText.length();
                        result.setSpan(new ForegroundColorSpan(Color.RED), index,
                                index + mSearchText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        model = new ItemModel();
                        model.data = userInfo;
                        model.highlight = result;
                        model.sortIndex = sort;
                        mShowUserList.add(model);
                    }
                }
            }
            Collections.sort(mShowUserList, searchComparator);

        }

        mUIHandler.sendEmptyMessage(SEARCH_MEMBER_SUCCESS);
    }

    static class UIHandler extends Handler {

        private final WeakReference<MembersInChatActivity> mActivity;

        public UIHandler(MembersInChatActivity activity) {
            mActivity = new WeakReference<MembersInChatActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MembersInChatActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case INIT_ADAPTER:
                        activity.mAdapter = new AllMembersAdapter(activity, activity.mShowUserList,
                                activity.mIsDeleteMode, activity.mIsCreator, activity.mGroupId, activity.mWidth);
                        activity.mListView.setAdapter(activity.mAdapter);
                        activity.mListView.requestFocus();
                        //单击ListView item，跳转到个人详情界面
                        activity.mListView.setOnItemClickListener(activity.mAdapter);

                        //如果是群主，长按ListView item可以删除群成员
                        activity.mListView.setOnItemLongClickListener(activity.mAdapter);
                        break;
                    case SEARCH_MEMBER_SUCCESS:
                        if (activity.mAdapter != null) {
                            activity.mAdapter.updateListView(activity.mShowUserList);
                        }
                        break;
                }
            }
        }
    }

    private class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SEARCH_MEMBER:
                    if (mShowUserList != null) {
                        mShowUserList.clear();
                    }
                    filterData();
                    break;
                case PROCESS_USER_INFO_TO_BEANS:
                    addAll();
                    mUIHandler.sendEmptyMessage(INIT_ADAPTER);
                    break;
                case ADD_ALL_MEMBER:
                    addAll();
                    break;
            }
        }
    }

    private void addAll() {
        String nickname, pinyin;
        ItemModel itemModel;
        mPinyinList.clear();
        mShowUserList.clear();
        for (UserInfo userInfo: mMemberInfoList) {
            itemModel = new ItemModel();
            itemModel.data = userInfo;
            nickname = userInfo.getNickname();
            if (TextUtils.isEmpty(nickname)) {
                nickname = userInfo.getUserName();
            }
            pinyin = HanyuPinyin.getInstance().getStringPinYin(nickname);
            mPinyinList.add(pinyin);
            itemModel.highlight = new SpannableString(nickname);
            mShowUserList.add(itemModel);
        }
        mUIHandler.sendEmptyMessage(SEARCH_MEMBER_SUCCESS);
    }

    public class ItemModel {
        public UserInfo data;
        public SpannableString highlight;
        public int sortIndex;
    }

    Comparator<ItemModel> searchComparator = new Comparator<ItemModel>() {
        @Override
        public int compare(ItemModel m1, ItemModel m2) {
            return m2.sortIndex - m1.sortIndex;
        }
    };

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
>>>>>>> master
        setResult(JChatDemoApplication.RESULT_CODE_ALL_MEMBER, intent);
        finish();
        super.onBackPressed();
    }
<<<<<<< HEAD
=======

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUIHandler.removeCallbacksAndMessages(null);
        mBackgroundHandler.removeCallbacksAndMessages(null);
        mBackgroundThread.getLooper().quit();
    }
>>>>>>> master
}
