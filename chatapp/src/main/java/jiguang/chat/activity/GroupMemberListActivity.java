package jiguang.chat.activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import jiguang.chat.R;
import jiguang.chat.adapter.GroupMemberListAdapter;
import jiguang.chat.application.JGApplication;
import jiguang.chat.utils.GroupMemberListComparator;
import jiguang.chat.utils.sidebar.SideBar;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by ${chenyn} on 2017/11/3.
 */

public class GroupMemberListActivity extends BaseActivity {

    private StickyListHeadersListView mGroup_listView;
    private long mGroupId;
    private List<UserInfo> mMemberInfoList;
    private GroupInfo mGroupInfo;
    private LinearLayout mLl_groupSearch;
    private ImageView mIv_ownerAvatar;
    private TextView mTv_ownerName;
    private TextView mTv_back;
    private GroupMemberListAdapter mListAdapter;
    private SideBar mSideBar;
    private TextView mLetterHintTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group_member_list);
        initView();
        initData();
    }

    private void initData() {
        mGroupId = getIntent().getLongExtra(JGApplication.GROUP_ID, 0);

        final Conversation conv = JMessageClient.getGroupConversation(mGroupId);
        mGroupInfo = (GroupInfo) conv.getTargetInfo();
        mMemberInfoList = mGroupInfo.getGroupMembers();

        //设置群主头像和名字
        JMessageClient.getUserInfo(mGroupInfo.getGroupOwner(), new GetUserInfoCallback() {
            @Override
            public void gotResult(int i, String s, UserInfo userInfo) {
                if (i == 0) {
                    if (userInfo.getAvatarFile() != null) {
                        mIv_ownerAvatar.setImageBitmap(BitmapFactory.decodeFile(userInfo.getAvatarFile().getAbsolutePath()));
                    }
                    mTv_ownerName.setText(userInfo.getDisplayName());
                }
            }
        });

        Collections.sort(mMemberInfoList, new GroupMemberListComparator());
        mListAdapter = new GroupMemberListAdapter(this, mMemberInfoList, mGroupId);
        mGroup_listView.setAdapter(mListAdapter);

        mLl_groupSearch.setOnClickListener(v -> {
            Intent intent = new Intent(GroupMemberListActivity.this, SearchGroupActivity.class);
            intent.setFlags(1);
            JGApplication.mSearchGroup = null;
            JGApplication.mSearchGroup = mMemberInfoList;
            startActivity(intent);
        });

        mTv_back.setOnClickListener(v -> finish());

        mSideBar.setOnTouchingLetterChangedListener(s -> {
            int position = mListAdapter.getSectionForLetter(s);
            if (position != -1 && position < mListAdapter.getCount()) {
                //设置滑动sidebar时listView滑动到指定位置
                mGroup_listView.setSelection(position);
            }
        });

        //点击群成员列表,跳转详情界面
        mGroup_listView.setOnItemClickListener((parent, view, position, id) -> {
            Object itemAtPosition = parent.getItemAtPosition(position);
            if (itemAtPosition instanceof UserInfo) {
                UserInfo info = (UserInfo) itemAtPosition;
                Intent intent = new Intent();
                intent.setClass(GroupMemberListActivity.this, GroupUserInfoActivity.class);
                intent.putExtra("groupID", mGroupId);
                intent.putExtra("groupOwner", mGroupInfo.getGroupOwner());
                intent.putExtra("groupUserName", info.getUserName());
                startActivity(intent);
            }
        });
    }

    private void initView() {
        mGroup_listView = (StickyListHeadersListView) findViewById(R.id.group_listView);
        View headerOwner = View.inflate(this, R.layout.header_list_group_member, null);
        mLl_groupSearch = headerOwner.findViewById(R.id.ll_groupSearch);
        mIv_ownerAvatar = headerOwner.findViewById(R.id.iv_ownerAvatar);
        mTv_ownerName = headerOwner.findViewById(R.id.tv_ownerName);
        mTv_back = (TextView) findViewById(R.id.tv_back);
        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mGroup_listView.addHeaderView(headerOwner);
        mLetterHintTv = (TextView) findViewById(R.id.letter_hint_tv);
        mSideBar.setTextView(mLetterHintTv);
        mSideBar.bringToFront();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mListAdapter.notifyDataSetChanged();
    }
}
