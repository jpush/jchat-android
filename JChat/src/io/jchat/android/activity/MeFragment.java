package io.jchat.android.activity;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.DownloadAvatarCallback;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.application.JPushDemoApplication;
import io.jchat.android.controller.MeController;
import io.jchat.android.tools.DialogCreator;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.view.MeView;

public class MeFragment extends BaseFragment {

    private static final String TAG = MeFragment.class.getSimpleName();

    private View mRootView;
    private MeView mMeView;
    private MeController mMeController;
    private Context mContext;
    private String mPath;
    private boolean mIsShowAvatar = false;

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
        if (!mIsShowAvatar){
            UserInfo myInfo = JMessageClient.getMyInfo();
            File file = myInfo.getSmallAvatarFile();
            //MyInfo存在小头像，直接显示
            if (file != null && file.isFile()) {
                mMeView.showPhoto(file.getAbsolutePath());
                mIsShowAvatar = true;
                //否则下载
            } else {
                myInfo.getSmallAvatarAsync(new DownloadAvatarCallback() {
                    @Override
                    public void gotResult(int status, String desc, File file) {
                        if (status == 0) {
                            mMeView.showPhoto(file.getAbsolutePath());
                            mIsShowAvatar = true;
                        } else {
                            HandleResponseCode.onHandle(mContext, status, false);
                        }
                    }
                });
            }
            mMeView.showNickName(myInfo.getNickname());
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
            File avatar = info.getSmallAvatarFile();
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

    //照相
    public void takePhoto() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String dir = JPushDemoApplication.PICTURE_DIR;
            File destDir = new File(dir);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            File file = new File(dir, JMessageClient.getMyInfo().getUserName() + ".jpg");
            mPath = file.getAbsolutePath();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            try {
                getActivity().startActivityForResult(intent, JPushDemoApplication.REQUEST_CODE_TAKE_PHOTO);
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

    //选择本地图片
    public void selectImageFromLocal() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Intent intent;
            if (Build.VERSION.SDK_INT < 19) {
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
            } else {
                intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            }
            getActivity().startActivityForResult(intent, JPushDemoApplication.REQUEST_CODE_SELECT_PICTURE);
        } else {
            Toast.makeText(this.getActivity(), mContext.getString(R.string.sdcard_not_exist_toast), Toast.LENGTH_SHORT).show();
        }

    }

    public void loadUserAvatar(String path) {
        if (null != mMeView) {
            mMeView.showPhoto(path);
        }
    }

    //预览头像
    public void startBrowserAvatar() {
        File file = JMessageClient.getMyInfo().getAvatarFile();
        //如果MyInfo存在头像，直接预览
        if (file != null && file.isFile()) {
            Log.i("MeFragment", "file.getAbsolutePath() " + file.getAbsolutePath());
            Intent intent = new Intent();
            intent.putExtra("browserAvatar", true);
            intent.putExtra("avatarPath", file.getAbsolutePath());
            intent.setClass(this.getActivity(), BrowserViewPagerActivity.class);
            startActivity(intent);
        //否则下载头像
        } else {
            final Dialog dialog = DialogCreator.createLoadingDialog(mContext, mContext.getString(R.string.loading));
            dialog.show();
            UserInfo myInfo = JMessageClient.getMyInfo();
            myInfo.getAvatarFileAsync(new DownloadAvatarCallback() {
                @Override
                public void gotResult(int status, String desc, File file) {
                    dialog.dismiss();
                    if (status == 0) {
                        Intent intent = new Intent();
                        intent.putExtra("browserAvatar", true);
                        intent.putExtra("avatarPath", file.getAbsolutePath());
                        intent.setClass(mContext, BrowserViewPagerActivity.class);
                        startActivity(intent);
                    } else {
                        HandleResponseCode.onHandle(mContext, status, false);
                    }
                }
            });
        }
    }

}
