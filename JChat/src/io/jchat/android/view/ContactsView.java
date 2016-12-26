package io.jchat.android.view;

import android.content.Context;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.jchat.android.R;
import io.jchat.android.adapter.StickyListAdapter;
import io.jchat.android.controller.ContactsController;


public class ContactsView extends LinearLayout{

	private TextView mTitle;
    private EditText mSearchEt;
    private ImageButton mDeleteBtn;
    private TextView mHint;
    private Context mContext;
	private StickyListHeadersListView mListView;
    private TextView mNewFriendNum;
    private RelativeLayout mFriendVerifyRl;
    private RelativeLayout mGroupRl;
    private SideBar mSideBar;
    private TextView mLetterHintTv;
    private LayoutInflater mInflater;
	public ContactsView(Context context, AttributeSet attrs) {
		super(context, attrs);
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
		// TODO Auto-generated constructor stub
	}
	
	public void initModule(float ratio, float density){
        mTitle = (TextView) findViewById(R.id.title_bar_title);
        mTitle.setText(mContext.getString(R.string.actionbar_contact));
        mSearchEt = (EditText) findViewById(R.id.search_et);
        mDeleteBtn = (ImageButton) findViewById(R.id.delete_ib);
        mHint = (TextView) findViewById(R.id.contact_hint);
        mListView = (StickyListHeadersListView) findViewById(R.id.sticky_list_view);
        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mLetterHintTv = (TextView) findViewById(R.id.letter_hint_tv);
        mSideBar.setTextView(mLetterHintTv);
        mSideBar.setRatioAndDensity(ratio, density);
        mSideBar.bringToFront();
        View header = mInflater.inflate(R.layout.contact_list_header, null);
        mFriendVerifyRl = (RelativeLayout) header.findViewById(R.id.verify_rl);
        mGroupRl = (RelativeLayout) header.findViewById(R.id.group_chat_rl);
        mNewFriendNum = (TextView) header.findViewById(R.id.friend_verification_num);
        mNewFriendNum.setVisibility(INVISIBLE);
        mListView.addHeaderView(header);
        mListView.setDrawingListUnderStickyHeader(true);
        mListView.setAreHeadersSticky(true);
        mListView.setStickyHeaderTopOffset(0);
	}

	public void setAdapter(StickyListAdapter adapter) {
		mListView.setAdapter(adapter);
        mSideBar.setIndex(adapter.getSections());
	}

    public void setListeners(ContactsController controller) {
        mFriendVerifyRl.setOnClickListener(controller);
        mGroupRl.setOnClickListener(controller);
        mSideBar.setOnClickListener(controller);
        mDeleteBtn.setOnClickListener(controller);
    }

    public void setSideBarTouchListener(SideBar.OnTouchingLetterChangedListener listener) {
        mSideBar.setOnTouchingLetterChangedListener(listener);
    }

    public void setSelection(int position) {
        mListView.setSelection(position);
    }

    public void showNewFriends(int num) {
        mNewFriendNum.setVisibility(VISIBLE);
        mNewFriendNum.setText(String.valueOf(num));
    }

    public void dismissNewFriends() {
        mNewFriendNum.setVisibility(INVISIBLE);
    }

    public String getSearchText() {
        return mSearchEt.getText().toString();
    }

    public void setTextWatcher(TextWatcher watcher) {
        mSearchEt.addTextChangedListener(watcher);
    }

    public void showContact() {
        mSearchEt.setVisibility(VISIBLE);
        mSideBar.setVisibility(VISIBLE);
        mListView.setVisibility(VISIBLE);
        mHint.setVisibility(GONE);
    }

    public void clearSearchText() {
        mSearchEt.setText("");
    }
}
