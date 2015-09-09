package io.jchat.android.view;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;

import io.jchat.android.tools.BitmapLoader;
import io.jchat.android.tools.NativeImageLoader;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.jchat.android.R;

import java.io.File;

public class FriendInfoView extends LinearLayout{

    private TextView mNickNameTv;
	private TextView mNoteName;
    private LinearLayout mNameRl;
	private ImageButton mReturnBtn;
    private CircleImageView mAvatarIv;
	private Button mSendMsgBtn;
    private ImageView mGenderIv;
    private TextView mGenderTv;
    private TextView mAreaTv;
    private TextView mSignatureTv;
    private Context mContext;

	public FriendInfoView(Context context, AttributeSet attrs) {
		super(context, attrs);
        mContext = context;
		// TODO Auto-generated constructor stub
	}
	
	public void initModule(){
        mNickNameTv = (TextView) findViewById(R.id.nick_name_tv);
		mNoteName = (TextView) findViewById(R.id.note_name_tv);
        mNameRl = (LinearLayout) findViewById(R.id.name_rl);
		mReturnBtn = (ImageButton) findViewById(R.id.friend_info_return_btn);
        mAvatarIv = (CircleImageView) findViewById(R.id.friend_detail_avatar);
		mSendMsgBtn = (Button) findViewById(R.id.friend_send_msg_btn);
        mGenderIv = (ImageView) findViewById(R.id.gender_iv);
        mGenderTv = (TextView) findViewById(R.id.gender_tv);
        mAreaTv = (TextView) findViewById(R.id.region_tv);
        mSignatureTv = (TextView) findViewById(R.id.signature_tv);
	}

    public void initInfo(UserInfo userInfo, double density){
        File file = userInfo.getAvatarFile();
        if(file != null){
            Bitmap bitmap = BitmapLoader.getBitmapFromFile(file.getAbsolutePath(),
                    (int)(100 * density), (int)(100 * density));
            mAvatarIv.setImageBitmap(bitmap);
        }
        if(TextUtils.isEmpty(userInfo.getNickname())){
            mNickNameTv.setText(userInfo.getUserName());
        }else {
            mNickNameTv.setText(userInfo.getNickname());
        }
        mNoteName.setText(userInfo.getNotename());
        if(userInfo.getGender() == UserInfo.Gender.male){
            mGenderTv.setText(mContext.getString(R.string.man));
            mGenderIv.setImageResource(R.drawable.sex_man);
        }else if(userInfo.getGender() == UserInfo.Gender.female){
            mGenderTv.setText(mContext.getString(R.string.woman));
            mGenderIv.setImageResource(R.drawable.sex_woman);
        }else {
            mGenderTv.setText(mContext.getString(R.string.unknown));
        }
        mAreaTv.setText(userInfo.getRegion());
        mSignatureTv.setText(userInfo.getSignature());
    }

	public void setListeners(OnClickListener onClickListener) {
		mReturnBtn.setOnClickListener(onClickListener);
		mNameRl.setOnClickListener(onClickListener);
		mSendMsgBtn.setOnClickListener(onClickListener);
        mAvatarIv.setOnClickListener(onClickListener);
	}

    public void setFriendAvatar(Bitmap bitmap) {
        mAvatarIv.setImageBitmap(bitmap);
    }
}
