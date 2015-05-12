package cn.jpush.im.android.demo.controller;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;

import cn.jpush.im.android.demo.R;

import cn.jpush.im.android.demo.activity.MeFragment;
import cn.jpush.im.android.demo.tools.NativeImageLoader;
import cn.jpush.im.android.demo.view.MeView;
import cn.jpush.im.android.demo.view.PullScrollView;

public class MeController implements OnClickListener, View.OnTouchListener {

    private MeView mMeView;
    private MeFragment mContext;
    private String mPath;
    private float startY = 0, endY = 0;

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
//            case R.id.user_info_rl:
//                mContext.StartMeInfoActivity();
//                break;
//            case R.id.setting_rl:
//                mContext.StartSettingActivity();
//                break;
////			//退出登录 清除Notification，清除缓存
//            case R.id.logout_rl:
//                mContext.Logout();
//                mContext.cancelNotification();
//                NativeImageLoader.getInstance().releaseCache();
//                mContext.getActivity().finish();
//                break;

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

    @Override
    public boolean onTouch(View view, MotionEvent e) {
        switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:
                startY = e.getY();
                return false;
            case MotionEvent.ACTION_MOVE:
                 return mMeView.touchEvent(e);
            case MotionEvent.ACTION_UP:
                endY = e.getY();
                if(endY - startY > 10)
                    return mMeView.touchEvent(e);
                else return onSingleTapConfirmed(view);
            default:
                return false;
        }
    }

    private boolean onSingleTapConfirmed(View view) {
        switch (view.getId()){
            case R.id.user_info_rl:
                mContext.StartMeInfoActivity();
                break;
            case R.id.setting_rl:
                mContext.StartSettingActivity();
                break;
//			//退出登录 清除Notification，清除缓存
            case R.id.logout_rl:
                mContext.Logout();
                mContext.cancelNotification();
                NativeImageLoader.getInstance().releaseCache();
                mContext.getActivity().finish();
                break;
        }
        return false;
    }

    private void setPath(String path){
        mPath = path;
    }

    public String getPath(){
        return mPath;
    }

}
