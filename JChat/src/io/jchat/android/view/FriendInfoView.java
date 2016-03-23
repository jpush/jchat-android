package io.jchat.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.tools.HandleResponseCode;

public class FriendInfoView extends LinearLayout {

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
    private SlipButton mBlackListBtn;
    private Context mContext;

    public FriendInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        // TODO Auto-generated constructor stub
    }

    public void initModule() {
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
        mBlackListBtn = (SlipButton) findViewById(R.id.black_list_slip_btn);
    }

    public void initInfo(UserInfo userInfo) {
        if (userInfo != null) {
            if (!TextUtils.isEmpty(userInfo.getAvatar())) {
                userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                    @Override
                    public void gotResult(int status, String desc, Bitmap bitmap) {
                        if (status == 0) {
                            mAvatarIv.setImageBitmap(bitmap);
                        } else {
                            HandleResponseCode.onHandle(mContext, status, false);
                        }
                    }
                });
            }
            if (TextUtils.isEmpty(userInfo.getNickname())) {
                mNickNameTv.setText(userInfo.getUserName());
            } else {
                mNickNameTv.setText(userInfo.getNickname());
            }
            mNoteName.setText(userInfo.getNotename());
            if (userInfo.getGender() == UserInfo.Gender.male) {
                mGenderTv.setText(mContext.getString(R.string.man));
                mGenderIv.setImageResource(R.drawable.sex_man);
            } else if (userInfo.getGender() == UserInfo.Gender.female) {
                mGenderTv.setText(mContext.getString(R.string.woman));
                mGenderIv.setImageResource(R.drawable.sex_woman);
            } else {
                mGenderTv.setText(mContext.getString(R.string.unknown));
            }
            mAreaTv.setText(userInfo.getRegion());
            mSignatureTv.setText(userInfo.getSignature());

            mBlackListBtn.setChecked(1 == userInfo.getBlacklist());
            Log.d("FriendInfoView", "userInfo.getBlacklist(): " + userInfo.getBlacklist());
        } else {
            mGenderTv.setText(mContext.getString(R.string.unknown));
        }

    }

    public void setListeners(OnClickListener onClickListener) {
        mReturnBtn.setOnClickListener(onClickListener);
        mNameRl.setOnClickListener(onClickListener);
        mSendMsgBtn.setOnClickListener(onClickListener);
        mAvatarIv.setOnClickListener(onClickListener);
    }

    public void setOnChangeListener(SlipButton.OnChangedListener listener) {
        mBlackListBtn.setOnChangedListener(R.id.black_list_slip_btn, listener);
    }

    public void setFriendAvatar(Bitmap bitmap) {
        mAvatarIv.setImageBitmap(bitmap);
    }

    public void setCheck(boolean flag) {
        mBlackListBtn.setChecked(flag);
    }
}
