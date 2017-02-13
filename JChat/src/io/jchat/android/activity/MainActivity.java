package io.jchat.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;

import cn.jiguang.api.JCoreInterface;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.controller.MainController;
import io.jchat.android.chatting.utils.FileHelper;
import io.jchat.android.chatting.utils.SharePreferenceManager;
import io.jchat.android.view.MainView;

public class MainActivity extends FragmentActivity {
    private MainController mMainController;
    private MainView mMainView;
    private Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMainView = (MainView) findViewById(R.id.main_view);
        mMainView.initModule();
        mMainController = new MainController(mMainView, this);

        mMainView.setOnClickListener(mMainController);
        mMainView.setOnPageChangeListener(mMainController);
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        JCoreInterface.onPause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        JCoreInterface.onResume(this);
        //第一次登录需要设置昵称
        boolean flag = SharePreferenceManager.getCachedFixProfileFlag();
        UserInfo myInfo = JMessageClient.getMyInfo();
        if (myInfo == null) {
            Intent intent = new Intent();
            if (null != SharePreferenceManager.getCachedUsername()) {
                intent.putExtra("userName", SharePreferenceManager.getCachedUsername());
                intent.putExtra("avatarFilePath", SharePreferenceManager.getCachedAvatarPath());
                intent.setClass(this, ReloginActivity.class);
            } else {
                intent.setClass(this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        } else {
            JChatDemoApplication.setPicturePath(myInfo.getAppKey());
            if (TextUtils.isEmpty(myInfo.getNickname()) && flag) {
                Intent intent = new Intent();
                intent.setClass(this, FixProfileActivity.class);
                startActivity(intent);
                finish();
            }
        }
        mMainController.sortConvList();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public FragmentManager getSupportFragmentManger() {
        // TODO Auto-generated method stub
        return getSupportFragmentManager();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }
        if (requestCode == JChatDemoApplication.REQUEST_CODE_TAKE_PHOTO) {
            String path = mMainController.getPhotoPath();
            if (path != null) {
                File file = new File(path);
                if (file.isFile()) {
                    mUri = Uri.fromFile(file);
                    //拍照后直接进行裁剪
//                mMainController.cropRawPhoto(mUri);
                    Intent intent = new Intent();
                    intent.putExtra("filePath", mUri.getPath());
                    intent.setClass(this, CropImageActivity.class);
                    startActivityForResult(intent, JChatDemoApplication.REQUEST_CODE_CROP_PICTURE);
                }
            }
        } else if (requestCode == JChatDemoApplication.REQUEST_CODE_SELECT_PICTURE) {
            if (data != null) {
                Uri selectedImg = data.getData();
                if (selectedImg != null) {
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };
                    Cursor cursor = this.getContentResolver()
                            .query(selectedImg, filePathColumn, null, null, null);
                    if (null == cursor) {
                        String path = selectedImg.getPath();
                        File file = new File(path);
                        if (file.isFile()) {
                            copyAndCrop(file);
                            return;
                        } else {
                            Toast.makeText(this, this.getString(R.string.picture_not_found),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else if (!cursor.moveToFirst()) {
                        Toast.makeText(this, this.getString(R.string.picture_not_found),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String path = cursor.getString(columnIndex);
                    if (path != null) {
                        File file = new File(path);
                        if (!file.isFile()) {
                            Toast.makeText(this, this.getString(R.string.picture_not_found),
                                    Toast.LENGTH_SHORT).show();
                            cursor.close();
                        } else {
                            //如果是选择本地图片进行头像设置，复制到临时文件，并进行裁剪
                            copyAndCrop(file);
                            cursor.close();
                        }
                    }
                }
            }
        } else if (requestCode == JChatDemoApplication.REQUEST_CODE_CROP_PICTURE) {
//            mMainController.uploadUserAvatar(mUri.getPath());
            String path = data.getStringExtra("filePath");
            if (path != null) {
                mMainController.uploadUserAvatar(path);
            }
        } else if (resultCode == JChatDemoApplication.RESULT_CODE_ME_INFO) {
            String newName = data.getStringExtra("newName");
            if (!TextUtils.isEmpty(newName)) {
                mMainController.refreshNickname(newName);
            }
        }
    }

    /**
     * 复制后裁剪文件
     * @param file 要复制的文件
     */
    private void copyAndCrop(final File file) {
        FileHelper.getInstance().copyFile(file, this, new FileHelper.CopyFileCallback() {
            @Override
            public void copyCallback(Uri uri) {
                mUri = uri;
//                mMainController.cropRawPhoto(mUri);
                Intent intent = new Intent();
                intent.putExtra("filePath", mUri.getPath());
                intent.setClass(MainActivity.this, CropImageActivity.class);
                startActivityForResult(intent, JChatDemoApplication.REQUEST_CODE_CROP_PICTURE);
            }
        });
    }

}
