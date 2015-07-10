package io.jchat.android.view;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.jchat.android.R;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;


public class MeInfoView extends LinearLayout{

	// private LinearLayout mTitleBarContainer;

	private RoundImageView mUserAvatar;
	private TextView mUsername;
	private TextView mNicknameTv;
	private ImageView mGenderIcon;
	private TextView mGenderTv;
    private ImageView mGenderIv;
	private TextView mRegionTv;
	private TextView mSignatureTv;
	private TextView mBirthday;
	private ImageButton mReturnBtn;
	private TextView mTitle;
	private ImageButton mMenuBtn;

    private RelativeLayout mNickNameRl;
    private RelativeLayout mSexRl;
    private RelativeLayout mAreaRl;
    private RelativeLayout mSignatureRl;
	private LinearLayout mBirthdayLayout;
	private LinearLayout mPasswordLayout;
	private LinearLayout mSettingLayout;
	private LinearLayout mLogoutLayout;
	
	private DialogCreator mLD = null;
	private Dialog mLoadingDialog = null;

	private RelativeLayout mEditLayout;
	private EditText mInputEdit;
	private ChangeType mType;

	private DatePickerDialog mDatePickerDialog;

	private AlertDialog mChangAvatarDialog;
	private RelativeLayout mUsePhoto;
	private RelativeLayout mUseCamera;

	private ImageView mAvatarBg;

	private String nicknameStr, birthdayStr, regionStr, signatureStr, genderStr;

	private Bitmap mAvatarBitmap;
    private Context mContext;

    public void setSignature(String signature) {
        mSignatureTv.setText(signature);
    }

    public void setNickName(String nickName) {
        mNicknameTv.setText(nickName);
    }

    public void setGender(boolean isMan){
        if(isMan){
            mGenderTv.setText(mContext.getString(R.string.man));
            mGenderIv.setImageResource(R.drawable.sex_man);
        }else {
            mGenderTv.setText(mContext.getString(R.string.woman));
            mGenderIv.setImageResource(R.drawable.sex_woman);
        }
    }

    public void setRegion(String region) {
        mRegionTv.setText(region);
    }


    private enum ChangeType {
		NICKNAME, LOCATION, SIGNATURE
	}
	
	public MeInfoView(Context context, AttributeSet attrs) {
		super(context, attrs);
        this.mContext = context;
		// TODO Auto-generated constructor stub
	}
	
	public void initModule(){
		mReturnBtn = (ImageButton) findViewById(R.id.return_btn);
		mTitle = (TextView) findViewById(R.id.title);
		mMenuBtn = (ImageButton) findViewById(R.id.right_btn);
        mNickNameRl = (RelativeLayout) findViewById(R.id.nick_name_rl);
        mSexRl = (RelativeLayout) findViewById(R.id.sex_rl);
        mAreaRl = (RelativeLayout) findViewById(R.id.location_rl);
        mRegionTv = (TextView) findViewById(R.id.region_tv);
        mSignatureRl = (RelativeLayout) findViewById(R.id.sign_rl);
		mNicknameTv = (TextView) findViewById(R.id.nick_name_tv);
		mGenderTv = (TextView) findViewById(R.id.gender_tv);
        mGenderIv = (ImageView) findViewById(R.id.sex_icon);
		mRegionTv = (TextView) findViewById(R.id.region_tv);
		mSignatureTv = (TextView) findViewById(R.id.signature_tv);
		mTitle.setText(mContext.getString(R.string.detail_info));
		mMenuBtn.setVisibility(View.GONE);
        refreshUserInfo(JMessageClient.getMyInfo());
	}

    public void refreshUserInfo(UserInfo userInfo) {
        if(userInfo != null){
            if(!TextUtils.isEmpty(userInfo.getNickname()))
                mNicknameTv.setText(userInfo.getNickname());
            if(userInfo.getGender() == UserInfo.Gender.male){
                mGenderTv.setText(mContext.getString(R.string.man));
                mGenderIv.setImageResource(R.drawable.sex_man);
            }else if(userInfo.getGender() == UserInfo.Gender.female){
                mGenderTv.setText(mContext.getString(R.string.woman));
                mGenderIv.setImageResource(R.drawable.sex_woman);
            }else {
                mGenderTv.setText(mContext.getString(R.string.unknown));
            }
            if(!TextUtils.isEmpty(userInfo.getRegion()))
                mRegionTv.setText(userInfo.getRegion());
            if(!TextUtils.isEmpty(userInfo.getSignature()))
                mSignatureTv.setText(userInfo.getSignature());
        }
    }

    public void setListeners(OnClickListener onClickListener) {
		mReturnBtn.setOnClickListener(onClickListener);
        mNickNameRl.setOnClickListener(onClickListener);
        mSexRl.setOnClickListener(onClickListener);
        mAreaRl.setOnClickListener(onClickListener);
        mSignatureRl.setOnClickListener(onClickListener);
////		JPushIMManager.getInstance().registerUserAvatarChangeObserver(this);
	}


}
