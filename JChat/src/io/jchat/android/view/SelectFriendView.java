package io.jchat.android.view;

import android.content.Context;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.jchat.android.R;
import io.jchat.android.adapter.StickyListAdapter;

/**
 * Created by jpush on 2015/10/9.
 */
public class SelectFriendView extends LinearLayout {

    private Context mContext;
    private ImageButton mCancelBtn;
    private EditText mSearchEt;
    private ImageButton mSearchBtn;
    private StickyListHeadersListView mListView;
    private SideBar mSideBar;
    private TextView mLetterHintTv;


    public SelectFriendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void initModule(float ratio){
        mCancelBtn = (ImageButton)findViewById(R.id.cancel_btn);
        mSearchEt = (EditText) findViewById(R.id.search_et);
        mSearchBtn = (ImageButton) findViewById(R.id.search_btn);
        mListView = (StickyListHeadersListView) findViewById(R.id.sticky_list_view);
        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mLetterHintTv = (TextView) findViewById(R.id.letter_hint_tv);
        mSideBar.setTextView(mLetterHintTv);
        mSideBar.setRatio(ratio);

        mListView.setDrawingListUnderStickyHeader(true);
        mListView.setAreHeadersSticky(true);
        mListView.setStickyHeaderTopOffset(0);
    }

    public void setListeners(OnClickListener listener){
        mCancelBtn.setOnClickListener(listener);
        mSearchBtn.setOnClickListener(listener);
    }

    public void setAdapter(StickyListAdapter adapter){
        mListView.setAdapter(adapter);
    }

    public void setSideBarTouchListener(SideBar.OnTouchingLetterChangedListener listener) {
        mSideBar.setOnTouchingLetterChangedListener(listener);
    }

    public void setSelection(int position) {
        mListView.setSelection(position);
    }

    public void setTextWatcher(TextWatcher watcher) {
        mSearchEt.addTextChangedListener(watcher);
    }

}
