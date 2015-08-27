package io.jchat.android.activity;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.controller.MeInfoController;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.view.MeInfoView;
import cn.jpush.im.api.BasicCallback;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import io.jchat.android.R;

public class MeInfoActivity extends BaseActivity {

    private MeInfoView mMeInfoView;
    private MeInfoController mMeInfoController;
    private final static int MODIFY_NICKNAME_REQUEST_CODE = 1;
    private final static int SELECT_AREA_REQUEST_CODE = 3;
    private final static int MODIFY_SIGNATURE_REQUEST_CODE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_info);
        mMeInfoView = (MeInfoView) findViewById(R.id.me_info_view);
        mMeInfoView.initModule();
        mMeInfoController = new MeInfoController(mMeInfoView, this);
        mMeInfoView.setListeners(mMeInfoController);
        UserInfo userInfo = JMessageClient.getMyInfo();
        mMeInfoView.refreshUserInfo(userInfo);
    }

    public void startModifyNickNameActivity() {
        String nickname = JMessageClient.getMyInfo().getNickname();
        Intent intent = new Intent();
        intent.putExtra("nickName", nickname);
        intent.setClass(this, ResetNickNameActivity.class);
        startActivityForResult(intent, MODIFY_NICKNAME_REQUEST_CODE);
    }

    public void showSexDialog(final UserInfo.Gender gender) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_set_sex, null);
        builder.setView(view);
        final Dialog dialog = builder.create();
        dialog.show();
        RelativeLayout manRl = (RelativeLayout) view.findViewById(R.id.man_rl);
        RelativeLayout womanRl = (RelativeLayout) view.findViewById(R.id.woman_rl);
        ImageView manSelectedIv = (ImageView) view.findViewById(R.id.man_selected_iv);
        ImageView womanSelectedIv = (ImageView) view.findViewById(R.id.woman_selected_iv);
        if (gender == UserInfo.Gender.male) {
            manSelectedIv.setVisibility(View.VISIBLE);
            womanSelectedIv.setVisibility(View.GONE);
        } else if (gender == UserInfo.Gender.female) {
            manSelectedIv.setVisibility(View.GONE);
            womanSelectedIv.setVisibility(View.VISIBLE);
        } else {
            manSelectedIv.setVisibility(View.GONE);
            womanSelectedIv.setVisibility(View.GONE);
        }
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.man_rl:
                        if (gender != UserInfo.Gender.male) {
                            UserInfo myUserInfo = JMessageClient.getMyInfo();
                            myUserInfo.setGender(UserInfo.Gender.male);
                            JMessageClient.updateMyInfo(UserInfo.Field.gender, myUserInfo, new BasicCallback() {
                                @Override
                                public void gotResult(final int status, final String desc) {
                                    if (status == 0) {
                                        MeInfoActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mMeInfoView.setGender(true);
                                                Toast.makeText(MeInfoActivity.this, MeInfoActivity.this.getString(R.string.modify_success_toast), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else MeInfoActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            HandleResponseCode.onHandle(MeInfoActivity.this, status, false);
                                        }
                                    });
                                }
                            });
                        }
                        dialog.cancel();
                        break;
                    case R.id.woman_rl:
                        if (gender != UserInfo.Gender.female) {
                            UserInfo myUserInfo = JMessageClient.getMyInfo();
                            myUserInfo.setGender(UserInfo.Gender.female);
                            JMessageClient.updateMyInfo(UserInfo.Field.gender, myUserInfo, new BasicCallback() {
                                @Override
                                public void gotResult(final int status, final String desc) {
                                    if (status == 0) {
                                        MeInfoActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mMeInfoView.setGender(false);
                                                Toast.makeText(MeInfoActivity.this, MeInfoActivity.this.getString(R.string.modify_success_toast), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else MeInfoActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            HandleResponseCode.onHandle(MeInfoActivity.this, status, false);
                                        }
                                    });
                                }
                            });
                        }
                        dialog.cancel();
                        break;
                }
            }
        };
        manRl.setOnClickListener(listener);
        womanRl.setOnClickListener(listener);
    }

    public void startSelectAreaActivity() {
        Intent intent = new Intent();
        intent.putExtra("OldRegion", JMessageClient.getMyInfo().getRegion());
        intent.setClass(this, SelectAreaActivity.class);
        startActivityForResult(intent, SELECT_AREA_REQUEST_CODE);
    }

    public void startModifySignatureActivity() {
        Intent intent = new Intent();
        intent.putExtra("OldSignature", JMessageClient.getMyInfo().getSignature());
        intent.setClass(this, EditSignatureActivity.class);
        startActivityForResult(intent, MODIFY_SIGNATURE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == MODIFY_NICKNAME_REQUEST_CODE) {
                mMeInfoView.setNickName(data.getStringExtra("nickName"));
                Log.i("MeInfoActivity", "data.getStringExtra(nickName) " + data.getStringExtra("nickName"));
            } else if (requestCode == SELECT_AREA_REQUEST_CODE)
                mMeInfoView.setRegion(data.getStringExtra("region"));
            else if (requestCode == MODIFY_SIGNATURE_REQUEST_CODE)
                mMeInfoView.setSignature(data.getStringExtra("signature"));
        }
    }
}
