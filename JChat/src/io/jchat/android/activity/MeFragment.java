package io.jchat.android.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;

import java.io.File;

import cn.jpush.im.android.api.JMessageClient;
import io.jchat.android.application.JPushDemoApplication;
import io.jchat.android.controller.MeController;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.view.MeView;

public class MeFragment extends BaseFragment {

    private static final String TAG = MeFragment.class.getSimpleName();

    private View mRootView;
    private MeView mMeView;
    private MeController mMeController;
    private Context mContext;
    private String mPath;
    private boolean isGetMeInfoFailed = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        mRootView = layoutInflater.inflate(R.layout.fragment_me,
                (ViewGroup) getActivity().findViewById(R.id.main_view),
                false);
        mMeView = (MeView) mRootView.findViewById(R.id.me_view);
        mMeView.initModule();
        mMeController = new MeController(mMeView, this);
        mMeView.setListeners(mMeController);
    }

    private void getMyUserInfo() {
        final ProgressDialog dialog = new ProgressDialog(mContext);
        dialog.setMessage(this.getString(R.string.loading));
        dialog.show();
        UserInfo userInfo = JMessageClient.getMyInfo();
        if (null != userInfo) {
            JMessageClient.getUserInfo(userInfo.getUserName(), new GetUserInfoCallback() {
                @Override
                public void gotResult(int status, String desc, UserInfo userInfo) {
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    });
                    if (status == 0) {
                        isGetMeInfoFailed = false;
                        handler.sendEmptyMessage(1);
                    } else {
                        isGetMeInfoFailed = true;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        ViewGroup p = (ViewGroup) mRootView.getParent();
        if (p != null) {
            p.removeAllViewsInLayout();
        }
        return mRootView;
    }

    @Override
    public void onResume() {
        if (isGetMeInfoFailed && !(mMeView.getAvatarFlag())) {
            getMyUserInfo();
        }else {
            File file = JMessageClient.getMyInfo().getAvatarFile();
            if(file != null && file.isFile()){
                mMeView.showPhoto(file.getAbsolutePath());
            }
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //退出登录
    public void Logout() {
        // TODO Auto-generated method stub
        Intent intent = new Intent();
        UserInfo info = JMessageClient.getMyInfo();
        if (null != info) {
            intent.putExtra("userName", info.getUserName());
            File avatar = info.getAvatarFile();
            if (null != avatar && avatar.exists()) {
                intent.putExtra("userAvatar", avatar.getAbsolutePath());
            }
            Log.i("MeFragment", "userName " + info.getUserName());
            JMessageClient.logout();
            intent.setClass(this.getActivity(), ReloginActivity.class);
            startActivity(intent);
        } else {
            Log.d(TAG, "user info is null!");
        }
    }

    public void StartSettingActivity() {
        Intent intent = new Intent();
        intent.setClass(this.getActivity(), SettingActivity.class);
        startActivity(intent);
    }

    public void StartMeInfoActivity() {
        Intent intent = new Intent();
        intent.setClass(this.getActivity(), MeInfoActivity.class);
        startActivity(intent);
    }

    public void cancelNotification() {
        NotificationManager manager = (NotificationManager) this.getActivity().getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
    }

    public void showSetAvatarDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        final LayoutInflater inflater = LayoutInflater.from(this.getActivity());
        View view = inflater.inflate(R.layout.dialog_set_avatar, null);
        builder.setView(view);
        final Dialog dialog = builder.create();
        dialog.show();
        LinearLayout takePhotoLl = (LinearLayout) view.findViewById(R.id.take_photo_ll);
        LinearLayout pickPictureLl = (LinearLayout) view.findViewById(R.id.pick_picture_ll);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.take_photo_ll:
                        dialog.cancel();
                        takePhoto();
                        break;
                    case R.id.pick_picture_ll:
                        dialog.cancel();
                        selectImageFromLocal();
                        break;
                }
            }
        };
        takePhotoLl.setOnClickListener(listener);
        pickPictureLl.setOnClickListener(listener);
    }

    private void takePhoto() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String dir = "sdcard/JPushDemo/pictures/";
            File destDir = new File(dir);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            File file = new File(dir, JMessageClient.getMyInfo().getUserName() + ".jpg");
            mPath = file.getAbsolutePath();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            try {
                getActivity().startActivityForResult(intent, JPushDemoApplication.REQUESTCODE_TAKE_PHOTO);
            } catch (ActivityNotFoundException anf) {
                Toast.makeText(this.getActivity(), mContext.getString(R.string.camera_not_prepared), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this.getActivity(), mContext.getString(R.string.sdcard_not_exist_toast), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public String getPhotoPath() {
        return mPath;
    }

    public void selectImageFromLocal() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Intent intent;
            if (Build.VERSION.SDK_INT < 19) {
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
            } else {
                intent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            }
            getActivity().startActivityForResult(intent, JPushDemoApplication.REQUESTCODE_SELECT_PICTURE);
        } else {
            Toast.makeText(this.getActivity(), mContext.getString(R.string.sdcard_not_exist_toast), Toast.LENGTH_SHORT).show();
        }

    }

    public void loadUserAvatar(String path) {
        if (null != mMeView) {
            mMeView.showPhoto(path);
        }
    }

    public void startBrowserAvatar() {
        File file = JMessageClient.getMyInfo().getAvatarFile();
        if (file != null && file.isFile()) {
            Log.i("MeFragment", "file.getAbsolutePath() " + file.getAbsolutePath());
            Intent intent = new Intent();
            intent.putExtra("browserAvatar", true);
            intent.putExtra("avatarPath", file.getAbsolutePath());
            intent.setClass(this.getActivity(), BrowserViewPagerActivity.class);
            startActivity(intent);
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if(JMessageClient.getMyInfo() != null){
                        File file = JMessageClient.getMyInfo().getAvatarFile();
                        if (file != null && mMeView != null)
                            mMeView.showPhoto(file.getAbsolutePath());
                    }
                    break;
                case 2:
                    HandleResponseCode.onHandle(mContext, msg.getData().getInt("status"), false);
                    break;
            }
        }
    };
}
