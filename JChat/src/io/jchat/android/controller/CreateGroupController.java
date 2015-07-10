package io.jchat.android.controller;

import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import io.jchat.android.R;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.CreateGroupCallback;
import io.jchat.android.activity.CreateGroupActivity;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.view.CreateGroupView;
import io.jchat.android.view.DialogCreator;

public class CreateGroupController implements OnClickListener {

    private CreateGroupView mCreateGroupView;
    private CreateGroupActivity mContext;
    private Dialog mDialog = null;
    private String mGroupName;

    public CreateGroupController(CreateGroupView view,
                                 CreateGroupActivity context) {
        this.mCreateGroupView = view;
        this.mContext = context;
        initData();
    }

    private void initData() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.creat_group_return_btn:
                mContext.finish();
                break;
            case R.id.commit_btn:
                mGroupName = mCreateGroupView.getGroupName();
                if (mGroupName.equals("")) {
                    mCreateGroupView.groupNameError(mContext);
                    return;
                }
                DialogCreator dialogCreator = new DialogCreator();
                mDialog = dialogCreator.createLoadingDialog(mContext, mContext.getString(R.string.creating_hint));
                final String desc = "";
                mDialog.show();
                JMessageClient.createGroup(
                        mGroupName, desc,
                        new CreateGroupCallback(false) {

                            @Override
                            public void gotResult(final int status, String msg,
                                                  final long groupID) {
                                mContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mDialog.dismiss();
                                        if (status == 0) {
                                            mContext.StartChatActivity(groupID, mGroupName);
                                        } else {
                                            HandleResponseCode.onHandle(mContext, status);
                                            Log.i("CreateGroupController", "status : " + status);
                                        }
                                    }
                                });
                            }
                        });
                break;

        }
    }

}
