package io.jchat.android.activity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import java.util.List;

import io.jchat.android.R;
import io.jchat.android.adapter.FriendRecommendAdapter;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.database.FriendRecommendEntry;
import io.jchat.android.database.UserEntry;
import io.jchat.android.entity.FriendInvitation;

public class FriendRecommendActivity extends BaseActivity {

    private ListView mListView;
    private FriendRecommendAdapter mAdapter;
    private List<FriendRecommendEntry> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_recommend);

        initView();

        UserEntry user = JChatDemoApplication.getUserEntry();
        if (null != user) {
            mList = user.getRecommends();
            mAdapter = new FriendRecommendAdapter(this, mList, mDensity, mWidth);
            mListView.setAdapter(mAdapter);
        } else {
            Log.e("FriendRecommendActivity", "Unexpected error: User table null");
        }
    }

    private void initView() {
        initTitle(true, true, "新的朋友", "", false, "");
        mListView = (ListView) findViewById(R.id.friend_recommend_list_view);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case JChatDemoApplication.RESULT_BUTTON:
                int position = data.getIntExtra("position", -1);
                int btnState = data.getIntExtra("btn_state", -1);
                FriendRecommendEntry entry = mList.get(position);
                if (btnState == 2) {
                    entry.state = FriendInvitation.ACCEPTED.getValue();
                    entry.save();
                }else if (btnState == 1) {
                    entry.state = FriendInvitation.REFUSED.getValue();
                    entry.save();
                }
                break;
            default:
                break;
        }
    }

    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }
}
