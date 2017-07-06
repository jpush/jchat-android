package jiguang.chat.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ListView;

import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupIDListCallback;
import jiguang.chat.R;
import jiguang.chat.adapter.GroupListAdapter;
import jiguang.chat.utils.DialogCreator;

/**
 * Created by ${chenyn} on 2017/4/26.
 */

public class GroupActivity extends BaseActivity {

    private ListView mGroupList;
    private GroupListAdapter mGroupListAdapter;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
        setContentView(R.layout.activity_group);
        initTitle(true, true, "群组", "", false, "");

        mGroupList = (ListView) findViewById(R.id.group_list);

        initData();
    }

    private void initData() {
        final Dialog dialog = DialogCreator.createLoadingDialog(this, this.getString(R.string.jmui_loading));
        dialog.show();
        JMessageClient.getGroupIDList(new GetGroupIDListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<Long> groupIDList) {
                dialog.dismiss();
                if (responseCode == 0) {
                    mGroupListAdapter = new GroupListAdapter(mContext, groupIDList);
                    mGroupList.setAdapter(mGroupListAdapter);
                }
            }
        });
    }
}
