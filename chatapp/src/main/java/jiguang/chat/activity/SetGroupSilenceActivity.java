package jiguang.chat.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Switch;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.api.BasicCallback;
import jiguang.chat.R;
import jiguang.chat.utils.DialogCreator;
import jiguang.chat.utils.ToastUtil;

/**
 * Created by ${chenyn} on 2017/11/27.
 */

public class SetGroupSilenceActivity extends BaseActivity {
    private GroupInfo mGroupInfo;
    private String mUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_set_group_silence);
        initTitle(true, true, "设置", "", false, "");

        Switch switchButton = (Switch) findViewById(R.id.switchButton);
        Dialog loadingDialog = DialogCreator.createLoadingDialog(SetGroupSilenceActivity.this, "正在加载...");
        loadingDialog.show();

        long groupID = getIntent().getLongExtra("groupID", 0);
        mUserName = getIntent().getStringExtra("userName");

        JMessageClient.getGroupInfo(groupID, new GetGroupInfoCallback() {
            @Override
            public void gotResult(int i, String s, GroupInfo groupInfo) {
                if (i == 0) {
                    mGroupInfo = groupInfo;
                    switchButton.setChecked(groupInfo.isKeepSilence(mUserName, JMessageClient.getMyInfo().getAppKey()));
                }
                loadingDialog.dismiss();
            }
        });

        switchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            //如果是手动设置才走下面.
            if (switchButton.isPressed()) {
                mGroupInfo.setGroupMemSilence(mUserName, JMessageClient.getMyInfo().getAppKey(), isChecked, new BasicCallback() {
                    @Override
                    public void gotResult(int i, String s) {
                        if (i == 0) {
                            ToastUtil.shortToast(SetGroupSilenceActivity.this, "设置成功");
                        } else {
                            ToastUtil.shortToast(SetGroupSilenceActivity.this, "设置失败" + i + s);
                        }
                    }
                });
            }
        });

    }
}
