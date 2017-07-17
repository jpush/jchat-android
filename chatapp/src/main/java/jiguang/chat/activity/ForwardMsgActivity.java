package jiguang.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import jiguang.chat.R;
import jiguang.chat.adapter.ForwardMsgAdapter;
import jiguang.chat.application.JGApplication;
import jiguang.chat.controller.ActivityController;
import jiguang.chat.database.FriendEntry;
import jiguang.chat.database.UserEntry;
import jiguang.chat.utils.DialogCreator;
import jiguang.chat.utils.pinyin.PinyinComparator;
import jiguang.chat.utils.sidebar.SideBar;
import jiguang.chat.view.listview.StickyListHeadersListView;


/**
 * Created by ${chenyn} on 2017/7/16.
 */

public class ForwardMsgActivity extends BaseActivity {

    private SideBar mSideBar;
    private TextView mLetterHintTv;
    private LinearLayout mLl_groupAll;
    private LinearLayout mSearch_title;
    private StickyListHeadersListView mListView;
    private ForwardMsgAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forward_msg);
        ActivityController.addActivity(this);

        initView();
        initData();
    }

    private void initView() {
        initTitle(true, true, "转发", "", false, "");

        mListView = (StickyListHeadersListView) findViewById(R.id.at_member_list_view);
        mLl_groupAll = (LinearLayout) findViewById(R.id.ll_groupAll);
        mSearch_title = (LinearLayout) findViewById(R.id.search_title);
        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mLetterHintTv = (TextView) findViewById(R.id.letter_hint_tv);
        mSideBar.setTextView(mLetterHintTv);
    }

    private void initData() {
        final UserEntry userEntry = JGApplication.getUserEntry();
        List<FriendEntry> friends = userEntry.getFriends();
        Collections.sort(friends, new PinyinComparator());
        mAdapter = new ForwardMsgAdapter(this, friends);
        mListView.setAdapter(mAdapter);

        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = mAdapter.getSectionForLetter(s);
                if (position != -1 && position < mAdapter.getCount()) {
                    mListView.setSelection(position - 1);
                }
            }
        });

        mSearch_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForwardMsgActivity.this, SearchContactsActivity.class);
                intent.setFlags(1);
                startActivity(intent);
            }
        });

        mLl_groupAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForwardMsgActivity.this, GroupActivity.class);
                intent.setFlags(1);
                startActivity(intent);
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Object itemAtPosition = parent.getItemAtPosition(position);
                if (itemAtPosition instanceof FriendEntry) {
                    DialogCreator.createForwardMsg(ForwardMsgActivity.this, mWidth, true, itemAtPosition, null, null, null);
                }
            }
        });
    }
}
