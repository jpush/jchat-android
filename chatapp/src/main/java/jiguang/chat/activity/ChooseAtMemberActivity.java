package jiguang.chat.activity;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import jiguang.chat.R;
import jiguang.chat.adapter.AtMemberAdapter;
import jiguang.chat.application.JGApplication;

/**
 * @ 功能, 选择群组中成员
 */

public class ChooseAtMemberActivity extends BaseActivity {

    private List<UserInfo> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_at_member);
        ListView listView = (ListView) findViewById(R.id.at_member_list_view);

        initTitle(true, true, "选择回复的人", "", false, "");

        long groupId = getIntent().getLongExtra(JGApplication.GROUP_ID, 0);
        if (0 != groupId) {
            Conversation conv = JMessageClient.getGroupConversation(groupId);
            GroupInfo groupInfo = (GroupInfo) conv.getTargetInfo();
            mList = groupInfo.getGroupMembers();
            mList.remove(JMessageClient.getMyInfo());
            AtMemberAdapter adapter = new AtMemberAdapter(this, mList);
            listView.setAdapter(adapter);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                UserInfo userInfo = mList.get(position);
                Intent intent = new Intent();
                String atName = userInfo.getNotename();
                if (TextUtils.isEmpty(atName)) {
                    atName = userInfo.getNickname();
                    if (TextUtils.isEmpty(atName)) {
                        atName = userInfo.getUserName();
                    }
                }
                intent.putExtra(JGApplication.NAME, atName);
                intent.putExtra(JGApplication.TARGET_ID, userInfo.getUserName());
                intent.putExtra(JGApplication.TARGET_APP_KEY, userInfo.getAppKey());
                setResult(JGApplication.RESULT_CODE_AT_MEMBER, intent);
                finish();
            }
        });
    }


}
