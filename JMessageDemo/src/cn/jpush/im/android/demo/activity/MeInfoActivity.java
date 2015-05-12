package cn.jpush.im.android.demo.activity;

import cn.jpush.im.android.api.JMessageClient;

import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.demo.controller.MeInfoController;
import cn.jpush.im.android.demo.tools.BitmapLoader;
import cn.jpush.im.android.demo.tools.HandleResponseCode;
import cn.jpush.im.android.demo.tools.NativeImageLoader;
import cn.jpush.im.android.demo.view.MeInfoView;
import cn.jpush.im.api.BasicCallback;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;

import cn.jpush.im.android.demo.R;

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

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(this.getString(R.string.loading));
        dialog.show();
        JMessageClient.getUserInfo(JMessageClient.getMyInfo().getUserName(), new GetUserInfoCallback() {
            @Override
            public void gotResult(int status, String desc, UserInfo userInfo) {
                MeInfoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });
                if (status == 0) {
                    android.os.Message msg = handler.obtainMessage();
                    msg.what = 1;
                    msg.obj = userInfo;
                    msg.sendToTarget();
                } else {
                    android.os.Message msg = handler.obtainMessage();
                    msg.what = 2;
                    Bundle bundle = new Bundle();
                    bundle.putInt("status", status);
                    msg.setData(bundle);
                    msg.sendToTarget();
                }
            }
        });
    }

    public void StartModifyNickNameActivity() {
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
                                            HandleResponseCode.onHandle(MeInfoActivity.this, status);
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
                                            HandleResponseCode.onHandle(MeInfoActivity.this, status);
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

    public void StartSelectAreaActivity() {
        Intent intent = new Intent();
        intent.putExtra("OldRegion", JMessageClient.getMyInfo().getRegion());
        intent.setClass(this, SelectAreaActivity.class);
        startActivityForResult(intent, SELECT_AREA_REQUEST_CODE);
    }

    public void StartModifySignatureActivity() {
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

    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    UserInfo userInfo = (UserInfo) msg.obj;
                    mMeInfoView.refreshUserInfo(userInfo);
                    break;
                case 2:
                    HandleResponseCode.onHandle(MeInfoActivity.this, msg.getData().getInt("status"));
                    break;
            }
        }
    };
}
