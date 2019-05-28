package jiguang.chat.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.model.GroupInfo;
import jiguang.chat.R;
import jiguang.chat.utils.DialogCreator;

/**
 * Created by ${chenyn} on 2017/11/6.
 */

public class GroupInfoActivity extends BaseActivity {

    private ImageView mIv_groupAvatar;
    private TextView mTv_groupName;
    private TextView mTv_groupId;
    private TextView mTv_groupOwner;
    private TextView mTv_groupMember;
    private TextView mTv_groupDesc;
    private Button mBtn_apply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group_info);
        initTitle(true, true, "群资料", "", false, "");
        mIv_groupAvatar = (ImageView) findViewById(R.id.iv_groupAvatar);
        mTv_groupName = (TextView) findViewById(R.id.tv_groupName);
        mTv_groupId = (TextView) findViewById(R.id.tv_groupId);
        mTv_groupOwner = (TextView) findViewById(R.id.tv_groupOwner);
        mTv_groupMember = (TextView) findViewById(R.id.tv_groupMember);
        mTv_groupDesc = (TextView) findViewById(R.id.tv_groupDesc);
        mBtn_apply = (Button) findViewById(R.id.btn_apply);

        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent.getFlags() != 1) {
            if (intent.getStringExtra("groupAvatar") != null) {
                mIv_groupAvatar.setImageBitmap(BitmapFactory.decodeFile(intent.getStringExtra("groupAvatar")));
            }
            mTv_groupName.setText(intent.getStringExtra("groupName"));
            mTv_groupId.setText(intent.getLongExtra("groupId", 0) + "");
            mTv_groupOwner.setText(intent.getStringExtra("groupOwner"));
            mTv_groupMember.setText(intent.getStringExtra("groupMember") + "人");
            if (TextUtils.isEmpty(intent.getStringExtra("groupDesc"))) {
                mTv_groupDesc.setText("暂无描述");
            } else {
                mTv_groupDesc.setText(intent.getStringExtra("groupDesc"));
            }
            mBtn_apply.setOnClickListener(v -> {
                //申请入群
                Intent start = new Intent(GroupInfoActivity.this, VerificationGroupActivity.class);
                //传群组id过去申请入群
                start.putExtra("openGroupID", intent.getLongExtra("groupId", 0));
                startActivity(start);

            });
        } else {
            Dialog loadingDialog = DialogCreator.createLoadingDialog(GroupInfoActivity.this, "正在加载...");
            loadingDialog.show();
            String groupId = intent.getStringExtra("groupId");
            mBtn_apply.setVisibility(View.GONE);
            JMessageClient.getGroupInfo(Long.parseLong(groupId), new GetGroupInfoCallback() {
                @Override
                public void gotResult(int i, String s, GroupInfo groupInfo) {
                    loadingDialog.dismiss();
                    if (i == 0) {
                        groupInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                            @Override
                            public void gotResult(int i, String s, Bitmap bitmap) {
                                if (i == 0) {
                                    mIv_groupAvatar.setImageBitmap(bitmap);
                                } else {
                                    mIv_groupAvatar.setImageResource(R.drawable.group);
                                }
                            }
                        });

                        mTv_groupName.setText(groupInfo.getGroupName());
                        mTv_groupId.setText(groupInfo.getGroupID() + "");
                        mTv_groupOwner.setText(groupInfo.getGroupOwner());
                        mTv_groupMember.setText(groupInfo.getGroupMembers().size() + "");
                        if (!TextUtils.isEmpty(groupInfo.getGroupDescription())) {
                            mTv_groupDesc.setText(groupInfo.getGroupDescription());
                        } else {
                            mTv_groupDesc.setText("暂无描述");
                        }
                    }
                }
            });

        }

    }
}
