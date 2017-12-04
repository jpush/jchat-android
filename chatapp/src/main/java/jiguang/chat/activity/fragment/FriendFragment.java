package jiguang.chat.activity.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import jiguang.chat.R;
import jiguang.chat.adapter.FriendRecommendAdapter;
import jiguang.chat.application.JGApplication;
import jiguang.chat.database.FriendRecommendEntry;
import jiguang.chat.database.UserEntry;
import jiguang.chat.entity.FriendInvitation;

/**
 * Created by ${chenyn} on 2017/11/7.
 */

public class FriendFragment extends BaseFragment {

    private Activity mContext;
    private ListView mListView;
    private FriendRecommendAdapter mAdapter;
    private List<FriendRecommendEntry> mList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getActivity();
    }

    private void initData() {
        UserEntry user = JGApplication.getUserEntry();
        if (null != user) {
            mList = user.getRecommends();
            mAdapter = new FriendRecommendAdapter(mContext, mList, mDensity, mWidth);
            mListView.setAdapter(mAdapter);
        } else {
            Log.e("FriendRecommendActivity", "Unexpected error: User table null");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return initView();
    }

    private View initView() {
        View view = View.inflate(mContext, R.layout.verification_friend, null);
        mListView = view.findViewById(R.id.friend_recommend_list_view);
        initData();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case JGApplication.RESULT_BUTTON:
                int position = data.getIntExtra("position", -1);
                int btnState = data.getIntExtra("btn_state", -1);
                FriendRecommendEntry entry = mList.get(position);
                if (btnState == 2) {
                    entry.state = FriendInvitation.ACCEPTED.getValue();
                    entry.save();
                } else if (btnState == 1) {
                    entry.state = FriendInvitation.REFUSED.getValue();
                    entry.save();
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }
}
