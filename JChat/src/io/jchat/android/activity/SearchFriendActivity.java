package io.jchat.android.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.adapter.SearchFriendListAdapter;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.chatting.utils.DialogCreator;
import io.jchat.android.chatting.utils.HandleResponseCode;

public class SearchFriendActivity extends BaseActivity {

    private ImageButton mReturnBtn;
    private TextView mTitle;
    private Button mRightBtn;
    private EditText mSearchEt;
    private ImageButton mSearchBtn;
    private Context mContext;
    private ListView mSearchLV;
    private SearchFriendListAdapter mAdapter;
    private List<UserInfo> mInfoList = new ArrayList<UserInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_search_friend);
        mReturnBtn = (ImageButton) findViewById(R.id.return_btn);
        mReturnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mTitle = (TextView) findViewById(R.id.jmui_title_tv);
        mTitle.setText(mContext.getString(R.string.search_friend_title_bar));
        mRightBtn = (Button) findViewById(R.id.jmui_commit_btn);
        mRightBtn.setVisibility(View.GONE);
        mSearchEt = (EditText) findViewById(R.id.search_et);
        mSearchBtn = (ImageButton) findViewById(R.id.search_btn);
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchStr = mSearchEt.getText().toString();
                if (!TextUtils.isEmpty(searchStr)) {
                    final Dialog dialog = DialogCreator.createLoadingDialog(mContext, mContext.getString(R.string.jmui_loading));
                    dialog.show();
                    JMessageClient.getUserInfo(searchStr, new GetUserInfoCallback() {
                        @Override
                        public void gotResult(int status, String desc, UserInfo userInfo) {
                            dialog.dismiss();
                            if (status == 0) {
                                mInfoList.clear();
                                mInfoList.add(userInfo);
                                mAdapter = new SearchFriendListAdapter(mContext, mInfoList);
                                mSearchLV.setAdapter(mAdapter);
                            } else {
                                HandleResponseCode.onHandle(mContext, status, false);
                            }
                        }
                    });
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.input_friend_username_hint),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        mSearchLV = (ListView) findViewById(R.id.search_list_view);

        mSearchLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent();
                UserInfo userInfo = mInfoList.get(position);
                if (userInfo.isFriend()) {
                    intent.setClass(mContext, FriendInfoActivity.class);
                    intent.putExtra("fromContact", true);
                } else {
                    intent.setClass(mContext, SearchFriendDetailActivity.class);
                }
                intent.putExtra(JChatDemoApplication.TARGET_ID, userInfo.getUserName());
                intent.putExtra(JChatDemoApplication.TARGET_APP_KEY, userInfo.getAppKey());
                mContext.startActivity(intent);
            }
        });
    }
}
