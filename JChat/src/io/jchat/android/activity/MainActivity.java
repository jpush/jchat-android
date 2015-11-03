package io.jchat.android.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.im.android.api.JMessageClient;
import io.jchat.android.R;
import io.jchat.android.application.JPushDemoApplication;
import io.jchat.android.controller.MainController;
import io.jchat.android.tools.BitmapLoader;
import io.jchat.android.tools.DialogCreator;
import io.jchat.android.tools.SharePreferenceManager;
import io.jchat.android.view.MainView;

public class MainActivity extends FragmentActivity{
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
        JPushInterface.onPause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        JPushInterface.onResume(this);
        //第一次登录需要设置昵称
        boolean flag = SharePreferenceManager.getCachedFixProfileFlag();
        if (JMessageClient.getMyInfo() == null) {
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
        } else if (TextUtils.isEmpty(JMessageClient.getMyInfo().getNickname()) && flag) {
            Intent intent = new Intent();
            intent.setClass(this, FixProfileActivity.class);
            startActivity(intent);
            finish();
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
        if (requestCode == JPushDemoApplication.REQUEST_CODE_TAKE_PHOTO) {
            String path = mMainController.getPhotoPath();
            File file = new File(path);
            if (file.isFile()){
                mUri = Uri.fromFile(file);
                //拍照后直接进行裁剪
                mMainController.cropRawPhoto(mUri);
            }
//                mMainController.uploadUserAvatar(path);
        } else if (requestCode == JPushDemoApplication.REQUEST_CODE_SELECT_PICTURE) {
            if (data != null) {
                Uri selectedImg = data.getData();
                if (selectedImg != null) {
                    Cursor cursor = this.getContentResolver().query(
                            selectedImg, null, null, null, null);
                    if (null == cursor || !cursor.moveToFirst()) {
                        Toast.makeText(this, this.getString(R.string.picture_not_found), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int columnIndex = cursor.getColumnIndex("_data");
                    String path = cursor.getString(columnIndex);
                    if (path != null) {
                        File file = new File(path);
                        if (!file.isFile()) {
                            Toast.makeText(this, this.getString(R.string.picture_not_found),
                                    Toast.LENGTH_SHORT).show();
                            cursor.close();
                        }else {
                            //如果是选择本地图片进行头像设置，复制到临时文件，并进行裁剪
                            copyAndCrop(file);
//                            mUri = Uri.fromFile(file);
//                            mMainController.cropRawPhoto(mUri);
                            cursor.close();
                        }
                    }
//                    mMainController.uploadUserAvatar(path);
                }
            }
        }else if (requestCode == JPushDemoApplication.REQUEST_CODE_CROP_PICTURE){
            Bitmap bitmap = decodeUriAsBitmap(mUri);
            String path = BitmapLoader.saveBitmapToLocal(bitmap);
            Log.d("MainActivity", "After compress Path: " + path);
            mMainController.uploadUserAvatar(path);
        }else if (resultCode == JPushDemoApplication.RESULT_CODE_ME_INFO){
            String newName = data.getStringExtra("newName");
            if (!TextUtils.isEmpty(newName)){
                mMainController.refreshNickname(newName);
            }
        }
    }

    /**
     * 复制后裁剪文件
     * @param file 要复制的文件
     */
    private void copyAndCrop(final File file) {
        final Dialog dialog = DialogCreator.createLoadingDialog(MainActivity.this,
                MainActivity.this.getString(R.string.loading));
        dialog.show();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileInputStream fis = new FileInputStream(file);
                    File destDir = new File(JPushDemoApplication.PICTURE_DIR);
                    if (!destDir.exists()) {
                        destDir.mkdirs();
                    }
                    final File tempFile = new File(JPushDemoApplication.PICTURE_DIR,
                            JMessageClient.getMyInfo().getUserName() + ".jpg");
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    byte[] bt = new byte[1024];
                    int c;
                    while((c = fis.read(bt)) > 0){
                        fos.write(bt,0,c);
                    }
                    //关闭输入、输出流
                    fis.close();
                    fos.close();

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            mUri = Uri.fromFile(tempFile);
                            mMainController.cropRawPhoto(mUri);
                        }
                    });
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    });
                }
            }
        });
        thread.start();
    }

    private Bitmap decodeUriAsBitmap(Uri uri){
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

}
