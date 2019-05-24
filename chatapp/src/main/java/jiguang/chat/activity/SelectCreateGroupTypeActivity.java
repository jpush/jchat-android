package jiguang.chat.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.CreateGroupCallback;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.api.BasicCallback;
import jiguang.chat.R;
import jiguang.chat.application.JGApplication;
import jiguang.chat.entity.Event;
import jiguang.chat.entity.EventType;
import jiguang.chat.utils.DialogCreator;
import jiguang.chat.utils.ToastUtil;
import jiguang.chat.utils.photochoose.ChoosePhoto;
import jiguang.chat.utils.photochoose.PhotoUtils;

/**
 * Created by ${chenyn} on 2017/11/21.
 */

@SuppressLint("Registered")
public class SelectCreateGroupTypeActivity extends BaseActivity implements TextWatcher {

    private Dialog mLoadingDialog;
    private ImageView mIv_groupAvatar;
    private EditText mEt_groupName;
    private LinearLayout mLl_groupType;
    private TextView mTv_groupSelect;
    private TextView tvInGroupDesc;
    private Button mBtn_createGroup;
    private ChoosePhoto mChoosePhoto;
    private File avatarFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_create_group_type);
        initTitle(true, true, "发起群聊", "", false, "");
        JGApplication.groupAvatarPath = null;//清空头像信息

        initView();
        initData();
    }

    private void initData() {
        mEt_groupName.addTextChangedListener(this);
        mLl_groupType.setOnClickListener(v -> showDialog());
        mBtn_createGroup.setOnClickListener(v -> {
            mLoadingDialog = DialogCreator.createLoadingDialog(this, getString(R.string.creating_hint));
            mLoadingDialog.show();
            if (JGApplication.groupAvatarPath != null) {
                avatarFile = new File(JGApplication.groupAvatarPath);
            } else {
                avatarFile = null;
            }

            if (mTv_groupSelect.getText().toString().equals("私有群")) {
                //创建私有群
                JMessageClient.createGroup(mEt_groupName.getText().toString(), "", avatarFile, "", new CreateGroupCallback() {
                    @Override
                    public void gotResult(int responseCode, String responseMsg, final long groupId) {
                        if (responseCode == 0) {
                            if (JGApplication.selectedUser.size() > 0) {
                                JMessageClient.addGroupMembers(groupId, JGApplication.selectedUser, new BasicCallback() {
                                    @Override
                                    public void gotResult(int responseCode, String responseMessage) {
                                        mLoadingDialog.dismiss();
                                        if (responseCode == 0) {
                                            //如果创建群组时添加了人,那么就在size基础上加上自己
                                            createGroup(groupId, JGApplication.selectedUser.size() + 1);
                                        } else if (responseCode == 810007) {
                                            ToastUtil.shortToast(SelectCreateGroupTypeActivity.this, "不能添加自己");
                                        } else {
                                            ToastUtil.shortToast(SelectCreateGroupTypeActivity.this, "添加失败");
                                        }
                                    }
                                });
                            } else {
                                mLoadingDialog.dismiss();
                                //如果创建群组时候没有选择人,那么size就是1
                                createGroup(groupId, 1);
                            }
                            Toast.makeText(SelectCreateGroupTypeActivity.this, "创建成功", Toast.LENGTH_SHORT).show();
                        } else {
                            mLoadingDialog.dismiss();
                            ToastUtil.shortToast(SelectCreateGroupTypeActivity.this, responseMsg);
                        }
                    }
                });
            } else {
                JMessageClient.createPublicGroup(mEt_groupName.getText().toString(), "", avatarFile, "", new CreateGroupCallback() {
                    @Override
                    public void gotResult(int responseCode, String responseMsg, final long groupId) {
                        if (responseCode == 0) {
                            if (JGApplication.selectedUser.size() > 0) {
                                JMessageClient.addGroupMembers(groupId, JGApplication.selectedUser, new BasicCallback() {
                                    @Override
                                    public void gotResult(int responseCode, String responseMessage) {
                                        mLoadingDialog.dismiss();
                                        if (responseCode == 0) {
                                            //如果创建群组时添加了人,那么就在size基础上加上自己
                                            createGroup(groupId, JGApplication.selectedUser.size() + 1);
                                        } else if (responseCode == 810007) {
                                            ToastUtil.shortToast(SelectCreateGroupTypeActivity.this, "不能添加自己");
                                        } else {
                                            ToastUtil.shortToast(SelectCreateGroupTypeActivity.this, "添加失败");
                                        }
                                    }
                                });
                            } else {
                                mLoadingDialog.dismiss();
                                //如果创建群组时候没有选择人,那么size就是1
                                createGroup(groupId, 1);
                            }
                            Toast.makeText(SelectCreateGroupTypeActivity.this, "创建成功", Toast.LENGTH_SHORT).show();
                        } else {
                            mLoadingDialog.dismiss();
                            ToastUtil.shortToast(SelectCreateGroupTypeActivity.this, responseMsg);
                        }
                    }
                });
            }
        });


        mIv_groupAvatar.setOnClickListener(v -> {
            mChoosePhoto = new ChoosePhoto();
            mChoosePhoto.getCreateGroupAvatar(mIv_groupAvatar);
            mChoosePhoto.showPhotoDialog(SelectCreateGroupTypeActivity.this);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PhotoUtils.INTENT_CROP:
            case PhotoUtils.INTENT_TAKE:
            case PhotoUtils.INTENT_SELECT:
                mChoosePhoto.photoUtils.onActivityResult(SelectCreateGroupTypeActivity.this, requestCode, resultCode, data);
                break;
        }
    }

    private void createGroup(long groupId, int groupMembersSize) {
        Conversation groupConversation = JMessageClient.getGroupConversation(groupId);
        if (groupConversation == null) {
            groupConversation = Conversation.createGroupConversation(groupId);
            EventBus.getDefault().post(new Event.Builder()
                    .setType(EventType.createConversation)
                    .setConversation(groupConversation)
                    .build());
        }

        Intent intent = new Intent();
        //设置跳转标志
        intent.putExtra("fromGroup", true);
        intent.putExtra(JGApplication.CONV_TITLE, groupConversation.getTitle());
        intent.putExtra(JGApplication.MEMBERS_COUNT, groupMembersSize);
        intent.putExtra(JGApplication.GROUP_ID, groupId);
        intent.setClass(SelectCreateGroupTypeActivity.this, ChatActivity.class);
        startActivity(intent);
        finish();
    }

    private void initView() {
        mIv_groupAvatar = (ImageView) findViewById(R.id.iv_groupAvatar);
        mEt_groupName = (EditText) findViewById(R.id.et_groupName);
        mLl_groupType = (LinearLayout) findViewById(R.id.ll_groupType);
        mTv_groupSelect = (TextView) findViewById(R.id.tv_groupSelect);
        tvInGroupDesc = (TextView) findViewById(R.id.tvInGroupDesc);
        mBtn_createGroup = (Button) findViewById(R.id.btn_createGroup);

        mBtn_createGroup.setClickable(false);
        mBtn_createGroup.setEnabled(false);
    }


    public void showDialog() {
        final Dialog genderDialog = new Dialog(this, R.style.jmui_default_dialog_style);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_set_sex, null);
        genderDialog.setContentView(view);
        Window window = genderDialog.getWindow();
        window.setWindowAnimations(R.style.mystyle); // 添加动画
        window.setGravity(Gravity.BOTTOM);
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        genderDialog.show();
        genderDialog.setCanceledOnTouchOutside(true);
        Button man = (Button) view.findViewById(R.id.man_rl);
        Button woman = (Button) view.findViewById(R.id.woman_rl);
        Button secrecy = (Button) view.findViewById(R.id.rl_secrecy);
        man.setText("私有群");
        woman.setText("公开群");
        secrecy.setText("取消");

        View.OnClickListener listener = v -> {
            switch (v.getId()) {
                case R.id.man_rl:
                    mTv_groupSelect.setText("私有群");
                    tvInGroupDesc.setText("只能通过群成员邀请入群，无需审核");
                    genderDialog.dismiss();
                    break;
                case R.id.woman_rl:
                    mTv_groupSelect.setText("公开群");
                    tvInGroupDesc.setText("用户可主动申请入群，需群主审核");
                    genderDialog.dismiss();
                    break;
                case R.id.rl_secrecy:
                    genderDialog.dismiss();
                    break;

            }
        };
        man.setOnClickListener(listener);
        woman.setOnClickListener(listener);
        secrecy.setOnClickListener(listener);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String inputString = mEt_groupName.getText().toString();
        if (TextUtils.isEmpty(inputString)) {
            mBtn_createGroup.setEnabled(false);
            mBtn_createGroup.setClickable(false);
            mBtn_createGroup.setBackgroundColor(Color.parseColor("#81E3E2"));
        } else {
            mBtn_createGroup.setClickable(true);
            mBtn_createGroup.setEnabled(true);
            mBtn_createGroup.setBackgroundColor(Color.parseColor("#2DD0CF"));
        }

    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}
