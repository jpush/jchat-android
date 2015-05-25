package cn.jpush.im.android.demo.controller;

import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import cn.jpush.im.android.demo.R;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.CreateGroupCallback;
import cn.jpush.im.android.demo.activity.CreateGroupActivity;
import cn.jpush.im.android.demo.tools.HandleResponseCode;
import cn.jpush.im.android.demo.view.CreateGroupView;
import cn.jpush.im.android.demo.view.LoadingDialog;

public class CreateGroupController implements OnClickListener {

    private CreateGroupView mCreateGroupView;
    private CreateGroupActivity mContext;
    private LoadingDialog mLD;
    private Dialog mLoadingDialog = null;
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
                mLD = new LoadingDialog();
                mLoadingDialog = mLD.createLoadingDialog(mContext, mContext.getString(R.string.creating_hint));
                final String desc = "";
                mLoadingDialog.show();
                JMessageClient.createGroup(
                        mGroupName, desc,
                        new CreateGroupCallback(false) {

                            @Override
                            public void gotResult(final int status, String msg,
                                                  final long GroupID) {
                                mContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mLoadingDialog.dismiss();
                                        if (status == 0) {
                                            mContext.StartChatActivity(GroupID, mGroupName);
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
