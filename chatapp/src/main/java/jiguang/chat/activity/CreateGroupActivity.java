package jiguang.chat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;
import jiguang.chat.R;
import jiguang.chat.adapter.CreateGroupAdapter;
import jiguang.chat.adapter.StickyListAdapter;
import jiguang.chat.application.JGApplication;
import jiguang.chat.database.FriendEntry;
import jiguang.chat.database.UserEntry;
import jiguang.chat.utils.keyboard.utils.EmoticonsKeyboardUtils;
import jiguang.chat.utils.pinyin.PinyinComparator;
import jiguang.chat.utils.sidebar.SideBar;
import jiguang.chat.view.listview.StickyListHeadersListView;


/**
 * Created by ${chenyn} on 2017/5/3.
 */

public class CreateGroupActivity extends BaseActivity implements View.OnClickListener, TextWatcher {

    private ImageButton mCancelBtn;
    private EditText mSearchEt;
    private StickyListHeadersListView mListView;
    private SideBar mSideBar;
    private TextView mLetterHintTv;
    private LinearLayout mFinishBtn;
    private StickyListAdapter mAdapter;
    private List<FriendEntry> mData;
    private HorizontalScrollView scrollViewSelected;
    private GridView imageSelectedGridView;
    private CreateGroupAdapter mGroupAdapter;
    private Context mContext;
    private FriendEntry mFriendEntry;
    private TextView mTv_noFriend;
    private TextView mTv_noFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
        setContentView(R.layout.activity_create_group);
        initView();
        initData();
    }

    private void initData() {
        UserEntry userEntry = JGApplication.getUserEntry();
        mData = userEntry.getFriends();
        Collections.sort(mData, new PinyinComparator());
        if (mData.size() > 0) {
            mTv_noFriend.setVisibility(View.GONE);
        } else {
            mTv_noFriend.setVisibility(View.VISIBLE);
        }
        mGroupAdapter = new CreateGroupAdapter(CreateGroupActivity.this);
        imageSelectedGridView.setAdapter(mGroupAdapter);
        mAdapter = new StickyListAdapter(CreateGroupActivity.this, mData, true, scrollViewSelected, imageSelectedGridView, mGroupAdapter);
        mListView.setAdapter(mAdapter);

    }

    private void initView() {
        mTv_noFriend = (TextView) findViewById(R.id.tv_noFriend);
        mTv_noFilter = (TextView) findViewById(R.id.tv_noFilter);
        mCancelBtn = (ImageButton) findViewById(R.id.jmui_cancel_btn);
        mFinishBtn = (LinearLayout) findViewById(R.id.finish_btn);
        mSearchEt = (EditText) findViewById(R.id.search_et);
        mListView = (StickyListHeadersListView) findViewById(R.id.sticky_list_view);
        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mLetterHintTv = (TextView) findViewById(R.id.letter_hint_tv);
        mSideBar.setTextView(mLetterHintTv);
        scrollViewSelected = (HorizontalScrollView) findViewById(R.id.contact_select_area);
        imageSelectedGridView = (GridView) findViewById(R.id.contact_select_area_grid);

        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = mAdapter.getSectionForLetter(s);
                if (position != -1 && position < mAdapter.getCount()) {
                    mListView.setSelection(position - 1);
                }
            }
        });

        mSearchEt.addTextChangedListener(this);

        mListView.setDrawingListUnderStickyHeader(true);
        mListView.setAreHeadersSticky(true);
        mListView.setStickyHeaderTopOffset(0);
        mFinishBtn.setOnClickListener(this);
        mCancelBtn.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (forDelete != null && forDelete.size() > 0) {
            for (FriendEntry e : forDelete) {
                e.delete();
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.jmui_cancel_btn:
                EmoticonsKeyboardUtils.closeSoftKeyboard(this);
                finish();
                break;
            case R.id.finish_btn:
                //拿到所选择的userName
                if (JGApplication.selectedUser != null && JGApplication.selectedUser.size() > 0) {
                    JGApplication.selectedUser.clear();
                }
                JGApplication.selectedUser = mAdapter.getSelectedUser();
                Intent intent = new Intent(mContext, SelectCreateGroupTypeActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        filterData(s.toString());
    }

    List<FriendEntry> filterDateList;

    private void filterData(final String filterStr) {
        filterDateList = new ArrayList<>();
        if (!TextUtils.isEmpty(filterStr)) {
            filterDateList.clear();
            //遍历好友集合进行匹配
            for (FriendEntry entry : mData) {
                String appKey = entry.appKey;

                String userName = entry.username;
                String noteName = entry.noteName;
                String nickName = entry.nickName;
                if (!userName.equals(filterStr) && userName.contains(filterStr) ||
                        !userName.equals(filterStr) && noteName.contains(filterStr) ||
                        !userName.equals(filterStr) && nickName.contains(filterStr) &&
                                appKey.equals(JMessageClient.getMyInfo().getAppKey())) {
                    filterDateList.add(entry);
                }
            }
        } else {
            if (mFriendEntry != null) {
                mFriendEntry.delete();
            }
            filterDateList = mData;
        }
        if (filterDateList.size() > 0) {
            mTv_noFilter.setVisibility(View.GONE);
        }
        // 根据a-z进行排序
        Collections.sort(filterDateList, new PinyinComparator());
        mAdapter.updateListView(filterDateList, true, filterStr);

        //当搜索的人不是好友时全局搜索
        //这个不能放在for中的else中,否则for循环了多少次就会添加多少次搜出来的user
        final UserEntry user = UserEntry.getUser(JMessageClient.getMyInfo().getUserName(),
                JMessageClient.getMyInfo().getAppKey());
        final List<FriendEntry> finalFilterDateList = filterDateList;
        JMessageClient.getUserInfo(filterStr, new GetUserInfoCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, UserInfo info) {
                if (responseCode == 0) {
                    mFriendEntry = new FriendEntry(info.getUserID(), info.getUserName(), info.getNotename(), info.getNickname(), info.getAppKey(), info.getAvatar(),
                            info.getUserName(), filterStr.substring(0, 1).toUpperCase(), user);
                    mFriendEntry.save();
                    forDelete.add(mFriendEntry);
                    finalFilterDateList.add(mFriendEntry);
                    Collections.sort(finalFilterDateList, new PinyinComparator());
                    if (finalFilterDateList.size() > 0) {
                        mTv_noFilter.setVisibility(View.GONE);
                    }
                    mAdapter.updateListView(finalFilterDateList, true, filterStr);
                } else {
                    if (filterDateList.size() > 0) {
                        mTv_noFilter.setVisibility(View.GONE);
                    } else {
                        mTv_noFilter.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

    }

    List<FriendEntry> forDelete = new ArrayList<>();

    @Override
    public void afterTextChanged(Editable s) {

    }
}
