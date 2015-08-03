package io.jchat.android.controller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import io.jchat.android.R;

import io.jchat.android.activity.MeFragment;
import io.jchat.android.tools.NativeImageLoader;
import io.jchat.android.view.MeView;

public class MeController implements OnClickListener {

    private MeView mMeView;
    private MeFragment mContext;
    private float startY = 0;

    public MeController(MeView meView, MeFragment context) {
        this.mMeView = meView;
        this.mContext = context;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_avatar_iv:
                Log.i("MeController", "avatar onClick");
                mContext.startBrowserAvatar();
                break;
            case R.id.take_photo_iv:
                mContext.showSetAvatarDialog();
                break;
            case R.id.user_info_rl:
                mContext.StartMeInfoActivity();
                break;
            case R.id.setting_rl:
                mContext.StartSettingActivity();
                break;
//			//退出登录 清除Notification，清除缓存
            case R.id.logout_rl:
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext.getActivity());
                View view = LayoutInflater.from(mContext.getActivity()).inflate(R.layout.dialog_resend_msg, null);
                builder.setView(view);
                TextView title = (TextView) view.findViewById(R.id.title);
                title.setText(mContext.getString(R.string.logout_confirm));
                title.setTextColor(Color.parseColor("#000000"));
                final Button cancel = (Button) view.findViewById(R.id.cancel_btn);
                final Button commit = (Button) view.findViewById(R.id.resend_btn);
                commit.setText(mContext.getString(R.string.confirm));
                final Dialog dialog = builder.create();
                dialog.show();
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getId()) {
                            case R.id.cancel_btn:
                                dialog.cancel();
                                break;
                            case R.id.resend_btn:
                                mContext.Logout();
                                mContext.cancelNotification();
                                NativeImageLoader.getInstance().releaseCache();
                                mContext.getActivity().finish();
                                dialog.cancel();
                                break;
                        }
                    }
                };
                cancel.setOnClickListener(listener);
                commit.setOnClickListener(listener);
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

//    @Override
//    public boolean onTouch(View view, MotionEvent e) {
//        switch (e.getAction()){
//            case MotionEvent.ACTION_DOWN:
//                startY = e.getY();
//                return false;
//            case MotionEvent.ACTION_MOVE:
//                 return mMeView.touchEvent(e);
//            case MotionEvent.ACTION_UP:
//                float endY = e.getY();
//                if(endY - startY > 10)
//                    return mMeView.touchEvent(e);
//                else return onSingleTapConfirmed(view);
//            default:
//                return false;
//        }
//    }

//    private boolean onSingleTapConfirmed(View v) {
//        switch (v.getId()){
//            case R.id.user_info_rl:
//                mContext.StartMeInfoActivity();
//                break;
//            case R.id.setting_rl:
//                mContext.StartSettingActivity();
//                break;
////			//退出登录 清除Notification，清除缓存
//            case R.id.logout_rl:
//
//                break;
//        }
//        return false;
//    }

}
