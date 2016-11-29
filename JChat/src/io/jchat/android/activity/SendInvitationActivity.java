package io.jchat.android.activity;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import cn.jpush.im.android.api.ContactManager;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import io.jchat.android.R;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.chatting.utils.DialogCreator;
import io.jchat.android.chatting.utils.HandleResponseCode;
import io.jchat.android.database.FriendRecommendEntry;
import io.jchat.android.database.UserEntry;
import io.jchat.android.entity.FriendInvitation;

public class SendInvitationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_invitation);
        final Context context = this;
        ImageButton returnBtn = (ImageButton) findViewById(R.id.return_btn);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        TextView title = (TextView) findViewById(R.id.jmui_title_tv);
        title.setText(this.getString(R.string.add_friend));
        Button rightBtn = (Button) findViewById(R.id.jmui_commit_btn);
        rightBtn.setVisibility(View.GONE);
        final EditText editText = (EditText) findViewById(R.id.verify_et);
        final UserInfo myInfo = JMessageClient.getMyInfo();
        String nickname = myInfo.getNickname();
        String hint;
        if (null != nickname && !TextUtils.isEmpty(nickname)) {
            hint = this.getString(R.string.friend_request_input_hit) + nickname;
            editText.setText(hint);
        } else {
            hint = this.getString(R.string.friend_request_input_hit) + myInfo.getUserName();
            editText.setText(hint);
        }
        editText.setSelection(editText.getText().length());
        ImageButton deleteBtn = (ImageButton) findViewById(R.id.delete_iv);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
            }
        });
        Button sendBtn = (Button) findViewById(R.id.send_invitation_btn);
        final String targetUsername = getIntent().getStringExtra("targetUsername");
        final String targetAppKey = getIntent().getStringExtra(JChatDemoApplication.TARGET_APP_KEY);
        final String targetAvatar = getIntent().getStringExtra(JChatDemoApplication.AVATAR);
        final String displayName = getIntent().getStringExtra(JChatDemoApplication.NICKNAME);

        final Dialog dialog = DialogCreator.createLoadingDialog(context, context.getString(R.string.jmui_loading));
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                final String reason = editText.getText().toString();
                ContactManager.sendInvitationRequest(targetUsername, targetAppKey, reason, new BasicCallback() {
                    @Override
                    public void gotResult(int status, String desc) {
                        dialog.dismiss();
                        if (status == 0) {
                            UserEntry userEntry = UserEntry.getUser(myInfo.getUserName(), myInfo.getAppKey());
                            FriendRecommendEntry entry = new FriendRecommendEntry(targetUsername, targetAppKey,
                                    targetAvatar, displayName, reason, FriendInvitation.INVITING.getValue(), userEntry);
                            entry.save();
                            Toast.makeText(context, context.getString(R.string.sent_request),
                                    Toast.LENGTH_SHORT).show();
                            SendInvitationActivity.this.finish();
                        } else {
                            HandleResponseCode.onHandle(context, status, false);
                        }
                    }
                });
            }
        });
    }
}
