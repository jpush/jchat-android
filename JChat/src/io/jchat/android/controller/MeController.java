package io.jchat.android.controller;

import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import io.jchat.android.R;
import io.jchat.android.activity.MeFragment;
import io.jchat.android.tools.DialogCreator;
import io.jchat.android.tools.NativeImageLoader;
import io.jchat.android.view.MeView;

public class MeController implements OnClickListener {

    private MeView mMeView;
    private MeFragment mContext;
    private Dialog mDialog;
    private int mWidth;

    public MeController(MeView meView, MeFragment context, int width) {
        this.mMeView = meView;
        this.mContext = context;
        this.mWidth = width;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_avatar_iv:
                Log.i("MeController", "avatar onClick");
                mContext.startBrowserAvatar();
                break;
            case R.id.take_photo_iv:
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (v.getId()) {
                            case R.id.take_photo_btn:
                                mDialog.cancel();
                                mContext.takePhoto();
                                break;
                            case R.id.pick_picture_btn:
                                mDialog.cancel();
                                mContext.selectImageFromLocal();
                                break;
                        }
                    }
                };
                mDialog = DialogCreator.createSetAvatarDialog(mContext.getActivity(), listener);
                mDialog.show();
                mDialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
                break;
            case R.id.user_info_rl:
                mContext.startMeInfoActivity();
                break;
            case R.id.setting_rl:
                mContext.StartSettingActivity();
                break;
//			//退出登录 清除Notification，清除缓存
            case R.id.logout_rl:
                listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getId()) {
                            case R.id.cancel_btn:
                                mDialog.cancel();
                                break;
                            case R.id.commit_btn:
                                mContext.Logout();
                                mContext.cancelNotification();
                                NativeImageLoader.getInstance().releaseCache();
                                mContext.getActivity().finish();
                                mDialog.cancel();
                                break;
                        }
                    }
                };
                mDialog = DialogCreator.createLogoutDialog(mContext.getActivity(), listener);
                mDialog.show();
                break;
//		case R.id.birthday:
//			Calendar calendar = Calendar.getInstance();
//			String dateStr = mBirthday.getText().toString().trim();
//			this.mDatePickerDialog = new DatePickerDialog(this.getActivity(), new OnDateSetListener() {
//
//				@Override
//				public void onDateSet(DatePicker arg0, int year, int month, int dayOfMonth) {
//					final String birthday = year + "-" + (month + 1) + "-" + dayOfMonth;
//					mBirthday.setText(birthday);
//					birthdayStr = birthday;
//				}
//			}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
//			this.mDatePickerDialog.show();
//			break;
//		case R.id.name:
//			mType = ChangeType.NICKNAME;
//			showInput(this.mNameLayout);
//			break;
//		case R.id.sign:
//			mType = ChangeType.SIGNATURE;
//			showInput(this.mSignatureLayout);
//			break;
//		case R.id.location:
//
//			mType = ChangeType.LOCATION;
//			showInput(this.mLocationLayout);
//			break;
//		case R.id.edit_layout:
//			showAllLayout();
//			saveInfo();
//			break;
//		default:
//			break;
        }
    }

}
