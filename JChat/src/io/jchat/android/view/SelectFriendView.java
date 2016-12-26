package io.jchat.android.view;

import android.content.Context;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import io.jchat.android.R;
import io.jchat.android.adapter.StickyListAdapter;

public class SelectFriendView extends LinearLayout {

    private Context mContext;
    private ImageButton mCancelBtn;
    private EditText mSearchEt;
    private ImageButton mSearchBtn;
    private StickyListHeadersListView mListView;
    private SideBar mSideBar;
    private TextView mLetterHintTv;
    private Button mFinishBtn;


    public SelectFriendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void initModule(float ratio, float density) {
        mCancelBtn = (ImageButton) findViewById(R.id.jmui_cancel_btn);
        mFinishBtn = (Button) findViewById(R.id.finish_btn);
        mSearchEt = (EditText) findViewById(R.id.search_et);
        mSearchBtn = (ImageButton) findViewById(R.id.search_btn);
        mListView = (StickyListHeadersListView) findViewById(R.id.sticky_list_view);
        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mLetterHintTv = (TextView) findViewById(R.id.letter_hint_tv);
        mSideBar.setTextView(mLetterHintTv);
        mSideBar.setRatioAndDensity(ratio, density);

        mListView.setDrawingListUnderStickyHeader(true);
        mListView.setAreHeadersSticky(true);
        mListView.setStickyHeaderTopOffset(0);
    }

    public void setListeners(OnClickListener listener) {
        mCancelBtn.setOnClickListener(listener);
        mSearchBtn.setOnClickListener(listener);
        mFinishBtn.setOnClickListener(listener);
    }

    public void setAdapter(StickyListAdapter adapter) {
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
