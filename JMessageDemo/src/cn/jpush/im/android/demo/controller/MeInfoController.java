package cn.jpush.im.android.demo.controller;

import android.view.View;
import android.view.View.OnClickListener;

import cn.jpush.im.android.demo.R;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.demo.activity.MeInfoActivity;
import cn.jpush.im.android.demo.view.MeInfoView;

public class MeInfoController implements OnClickListener {

    private MeInfoView mMeInfoView;
    private MeInfoActivity mContext;


    public MeInfoController(MeInfoView view, MeInfoActivity context) {
        this.mMeInfoView = view;
        this.mContext = context;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.return_btn:
                mContext.finish();
                break;
            case R.id.nick_name_rl:
                mContext.StartModifyNickNameActivity();
                break;
            case R.id.sex_rl:
                UserInfo userInfo = JMessageClient.getMyInfo();
                UserInfo.Gender gender = userInfo.getGender();
                mContext.showSexDialog(gender);
                break;
            case R.id.location_rl:
                mContext.StartSelectAreaActivity();
                break;
            case R.id.sign_rl:
                mContext.StartModifySignatureActivity();
                break;
        }
    }

}
