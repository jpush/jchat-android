package io.jchat.android.activity;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;

import java.util.List;

import io.jchat.android.R;
import io.jchat.android.adapter.FriendRecommendAdapter;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.chatting.utils.DialogCreator;
import io.jchat.android.chatting.utils.SharePreferenceManager;
import io.jchat.android.database.FriendRecommendEntry;
import io.jchat.android.database.UserEntry;

public class FriendRecommendActivity extends BaseActivity {

    private ImageButton mReturnBtn;
    private TextView mTitle;
    private ImageButton mClearBtn;
    private FriendRecommendAdapter mAdapter;
    private ListView mListView;
    private List<FriendRecommendEntry> mList;
    private Dialog mDialog;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
        setContentView(R.layout.activity_friend_recommend);
        final Context context = this;
        mReturnBtn = (ImageButton) findViewById(R.id.return_btn);
        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setText(this.getString(R.string.friend_verify_title));
        mClearBtn = (ImageButton) findViewById(R.id.right_btn);
        mClearBtn.setImageDrawable(this.getResources().getDrawable(R.drawable.delete));
        mListView = (ListView) findViewById(R.id.friend_recommend_list_view);
        UserEntry user = JChatDemoApplication.getUserEntry();
        if (null != user) {
            mList = user.getRecommends();
            mAdapter = new FriendRecommendAdapter(this, mList, mDensity);
            mListView.setAdapter(mAdapter);
        } else {
            Log.e("FriendRecommendActivity", "Unexpected error: User table null");
        }

        mReturnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendRecommendActivity.this.finish();
            }
        });

        mClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActiveAndroid.beginTransaction();
                try {
                    for (FriendRecommendEntry entry : mList) {
                        entry.delete();
                    }
                    ActiveAndroid.setTransactionSuccessful();
                } finally {
                    ActiveAndroid.endTransaction();
                }
                mAdapter.clearAll();
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                FriendRecommendEntry entry = mList.get(position);
                Intent intent = new Intent(mContext, FriendInfoActivity.class);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long id) {
                final FriendRecommendEntry entry = mList.get(position);
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FriendRecommendEntry.deleteEntry(entry);
                        mList.remove(position);
                        mAdapter.notifyDataSetChanged();
                        mDialog.dismiss();
                    }
                };
                mDialog = DialogCreator.createDelRecommendDialog(context, listener);
                mDialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
                mDialog.show();
                return false;
            }
        });

        SharePreferenceManager.setCachedNewFriendNum(0);
    }
}
