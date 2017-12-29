package jiguang.chat.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.api.BasicCallback;
import jiguang.chat.R;
import jiguang.chat.utils.HandleResponseCode;

/**
 * Created by ${chenyn} on 2017/11/6.
 */

public class VerificationGroupActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_verification);
        initTitle(true, true, "验证信息", "", true, "发送");
        EditText et_reason = (EditText) findViewById(R.id.et_reason);
        et_reason.setHint("请填写验证信息");

        mJmui_commit_btn.setOnClickListener(v -> {
            //发送加入群组验证信息
            String verification = "";
            if (!TextUtils.isEmpty(et_reason.getText())) {
                verification = et_reason.getText().toString();
            }

            long openGroupID = getIntent().getLongExtra("openGroupID", 0);

            JMessageClient.applyJoinGroup(openGroupID, verification, new BasicCallback() {
                @Override
                public void gotResult(int i, String s) {
                    if (i == 0) {
                        Toast.makeText(VerificationGroupActivity.this, "申请已发出,等待审核", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        HandleResponseCode.onHandle(VerificationGroupActivity.this, i, false);
                    }
                }
            });


        });


    }
}
