package cn.jpush.im.android.demo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import cn.jpush.im.android.demo.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.api.callback.GetGroupIDListCallback;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.demo.application.JPushDemoApplication;
import cn.jpush.im.android.demo.tools.BitmapLoader;
import cn.jpush.im.android.demo.view.RoundImageView;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by Ken on 2015/1/26.
 */
public class FixProfileActivity extends BaseActivity {

    private ImageView mReturnBtn;
    private Button mFinishBtn;
    private EditText mNickNameEt;
    private ImageView mAvatarIv;
    private String mPath;
    private ProgressDialog mDialog;
    private double mDensity;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fix_profile);
        mContext = this;
        mReturnBtn = (ImageView) findViewById(R.id.return_btn);
        mNickNameEt = (EditText) findViewById(R.id.nick_name_et);
        mAvatarIv = (RoundImageView) findViewById(R.id.avatar_iv);
        mFinishBtn = (Button) findViewById(R.id.finish_btn);
        mReturnBtn.setOnClickListener(listener);
        mAvatarIv.setOnClickListener(listener);
        mFinishBtn.setOnClickListener(listener);
        mNickNameEt.addTextChangedListener(watcher);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDensity = dm.density;

        JMessageClient.getUserInfo(JMessageClient.getMyInfo().getUserName(), null);
    }

    private TextWatcher watcher = new TextWatcher() {
        private CharSequence temp = "";

        @Override
        public void afterTextChanged(Editable arg0) {
            // TODO Auto-generated method stub
            if (temp.length() > 0) {
                mFinishBtn.setClickable(true);
                mFinishBtn.setBackgroundColor(getResources().getColor(R.color.finish_btn_clickable_color));
            } else {
                mFinishBtn.setClickable(false);
                mFinishBtn.setBackgroundColor(getResources().getColor(R.color.finish_btn_unclickable_color));
            }
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int count,
                                  int after) {
            // TODO Auto-generated method stub
            temp = s;
        }

    };

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.return_btn:
                    finish();
                    break;
                case R.id.avatar_iv:
                    showSetAvatarDialog();
                    break;
                case R.id.finish_btn:
                    String nickName = mNickNameEt.getText().toString().trim();
                    if (nickName != null && !nickName.equals("")) {
                        final ProgressDialog dialog = new ProgressDialog(mContext);
                        dialog.setMessage(mContext.getString(R.string.saving_hint));
                        dialog.show();
                        UserInfo myUserInfo = JMessageClient.getMyInfo();
                        myUserInfo.setNickname(nickName);
                        JMessageClient.updateMyInfo(UserInfo.Field.nickname, myUserInfo, new BasicCallback(false) {
                            @Override
                            public void gotResult(final int status, String desc) {
                                FixProfileActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.dismiss();
                                        if (status != 0) {
                                            Toast.makeText(mContext, mContext.getString(R.string.nickname_save_failed),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                        startMainActivity();
                                    }
                                });
                            }
                        });
                    } else {
                        Toast.makeText(FixProfileActivity.this, FixProfileActivity.this
                                .getString(R.string.nickname_not_null_toast), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    break;
            }
        }
    };

    public void showSetAvatarDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final LayoutInflater inflater = LayoutInflater.from(this);
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

    private void startMainActivity() {
        Intent intent = new Intent();
        intent.setClass(FixProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void takePhoto() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String dir = "sdcard/JPushDemo/pictures/";
            File destDir = new File(dir);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            File file = new File(dir, JMessageClient.getMyInfo().getUserName() + ".jpg");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            mPath = file.getAbsolutePath();
            startActivityForResult(intent, JPushDemoApplication.REQUESTCODE_TAKE_PHOTO);
        } else {
            Toast.makeText(this, this.getString(R.string.sdcard_not_exist_toast), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public void selectImageFromLocal() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, JPushDemoApplication.REQUESTCODE_SELECT_PICTURE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }
        if (requestCode == JPushDemoApplication.REQUESTCODE_TAKE_PHOTO) {
            if (mPath != null) {
                calculateAvatar(mPath);
            }
        } else if (requestCode == JPushDemoApplication.REQUESTCODE_SELECT_PICTURE) {
            if (data != null) {
                Uri selectedImg = data.getData();
                if (selectedImg != null) {
                    Cursor cursor = getContentResolver().query(
                            selectedImg, null, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex("_data");
                    String path = cursor.getString(columnIndex);
                    if (path != null) {
                        File file = new File(path);
                        if (file == null || !file.exists()) {
                            Toast.makeText(this, this.getString(R.string.picture_not_found), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    cursor.close();
                    calculateAvatar(path);
                }
            }
        }
    }

    private void calculateAvatar(final String originPath) {
        mDialog = new ProgressDialog(this);
        mDialog.setCancelable(false);
        mDialog.setMessage(this.getString(R.string.updating_avatar_hint));
        mDialog.show();
        //验证图片大小，若小于720 * 1280则直接发送原图，否则压缩
        if (BitmapLoader.verifyPictureSize(originPath))
            updateAvatar(originPath, originPath);
        else {
            Bitmap bitmap = BitmapLoader.getBitmapFromFile(originPath, 720, 1280);
            Log.i("FixProfileActivity", "uploading width height: " + bitmap.getWidth() + " " + bitmap.getHeight());
            String tempPath = BitmapLoader.saveBitmapToLocal(bitmap);
            updateAvatar(tempPath, originPath);
        }
    }

    private void updateAvatar(final String path, final String originPath) {
        JMessageClient.updateUserAvatar(new File(path), new BasicCallback(false) {
            @Override
            public void gotResult(int status, final String desc) {
                mDialog.dismiss();
                if (status == 0) {
                    Log.i("FixProfileActivity", "Update avatar succeed path " + path);
                    loadUserAvatar(originPath);
                    FixProfileActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(FixProfileActivity.this, FixProfileActivity.this.getString(R.string.avatar_modify_succeed_toast), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    FixProfileActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(FixProfileActivity.this, desc, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void loadUserAvatar(String path) {
        final Bitmap bitmap = BitmapLoader.getBitmapFromFile(path, (int) (100 * mDensity), (int) (100 * mDensity));
        Log.i("FixProfileActivity", "bitmap.getWidth() bitmap.getHeight() " + bitmap.getWidth() + bitmap.getHeight());
        Log.i("FixProfileActivity", "file path " + path);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAvatarIv.setImageBitmap(bitmap);
            }
        });
    }

    @Override
    protected void onDestroy() {
//        mAvatarIv.setDrawingCacheEnabled(true);
//        mAvatarIv.getDrawingCache().recycle();
        super.onDestroy();
    }
}
