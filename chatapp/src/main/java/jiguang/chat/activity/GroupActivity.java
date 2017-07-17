package jiguang.chat.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupIDListCallback;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.model.GroupInfo;
import jiguang.chat.R;
import jiguang.chat.adapter.GroupListAdapter;
import jiguang.chat.controller.ActivityController;
import jiguang.chat.utils.DialogCreator;

/**
 * Created by ${chenyn} on 2017/4/26.
 */

public class GroupActivity extends BaseActivity {

    private ListView mGroupList;
    private GroupListAdapter mGroupListAdapter;
    private Context mContext;
    private boolean isFromForward = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
        setContentView(R.layout.activity_group);
        initTitle(true, true, "群组", "", false, "");
        ActivityController.addActivity(this);

        mGroupList = (ListView) findViewById(R.id.group_list);

        initData();
    }

    private void initData() {
        final Dialog dialog = DialogCreator.createLoadingDialog(this, this.getString(R.string.jmui_loading));
        dialog.show();
        final List<GroupInfo> infoList = new ArrayList<>();
        JMessageClient.getGroupIDList(new GetGroupIDListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, final List<Long> groupIDList) {
                if (responseCode == 0) {
                    final int[] groupSize = {groupIDList.size()};
                    for (Long id : groupIDList) {
                        JMessageClient.getGroupInfo(id, new GetGroupInfoCallback() {
                            @Override
                            public void gotResult(int i, String s, GroupInfo groupInfo) {
                                if (i == 0) {
                                    groupSize[0] = groupSize[0] - 1;
                                    infoList.add(groupInfo);
                                    if (groupSize[0] == 0) {
                                        setAdapter(infoList, dialog);
                                    }

                                }
                            }
                        });
                    }

                }
            }
        });
    }

    public void setAdapter(List<GroupInfo> infoList, Dialog dialog) {
        dialog.dismiss();
        //来自转发时flag是1
        if (getIntent().getFlags() == 1) {
            isFromForward = true;
        }
        mGroupListAdapter = new GroupListAdapter(mContext,infoList, isFromForward, mWidth);
        mGroupList.setAdapter(mGroupListAdapter);
    }
}
