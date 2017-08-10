package jiguang.chat.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import jiguang.chat.R;
import jiguang.chat.pickerimage.utils.AttachmentStore;
import jiguang.chat.pickerimage.utils.StorageUtil;
import jiguang.chat.utils.DialogCreator;
import jiguang.chat.utils.SharePreferenceManager;
import jiguang.chat.utils.ThreadUtil;
import jiguang.chat.utils.ToastUtil;
import jiguang.chat.utils.citychoose.view.SelectAddressDialog;
import jiguang.chat.utils.citychoose.view.myinterface.SelectAddressInterface;
import jiguang.chat.utils.photochoose.ChoosePhoto;
import jiguang.chat.utils.photochoose.PhotoUtils;

import static jiguang.chat.R.id.iv_erWeiMa;

/**
 * Created by ${chenyn} on 2017/2/23.
 */

public class PersonalActivity extends BaseActivity implements SelectAddressInterface, View.OnClickListener {

    public static final int SIGN = 1;
    public static final int FLAGS_SIGN = 2;
    public static final String SIGN_KEY = "sign_key";

    public static final int NICK_NAME = 4;
    public static final int FLAGS_NICK = 3;
    public static final String NICK_NAME_KEY = "nick_name_key";

    private static final String SD_PATH = "/sdcard/dskqxt/pic/";
    private static final String IN_PATH = "/dskqxt/pic/";

    private RelativeLayout mRl_cityChoose;
    private TextView mTv_city;
    private SelectAddressDialog dialog;
    private RelativeLayout mRl_gender;
    private RelativeLayout mRl_birthday;

    private TextView mTv_birthday;
    private TextView mTv_gender;
    private RelativeLayout mSign;
    private TextView mTv_sign;
    private RelativeLayout mRl_nickName;


    Intent intent;
    private TextView mTv_nickName;
    private ImageView mIv_photo;
    private ChoosePhoto mChoosePhoto;
    private UserInfo mMyInfo;
    private TextView mTv_userName;
    private RelativeLayout mRl_zxing;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        Window window = this.getWindow();
        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        window.setStatusBarColor(getResources().getColor(R.color.line_normal));

        initView();
        initListener();
        initData();
    }

    private void initData() {
        final Dialog dialog = DialogCreator.createLoadingDialog(PersonalActivity.this,
                PersonalActivity.this.getString(R.string.jmui_loading));
        dialog.show();
        mMyInfo = JMessageClient.getMyInfo();
        if (mMyInfo != null) {
            mTv_nickName.setText(mMyInfo.getNickname());
            SharePreferenceManager.setRegisterUsername(mMyInfo.getNickname());
            mTv_userName.setText("用户名:" + mMyInfo.getUserName());
            mTv_sign.setText(mMyInfo.getSignature());
            UserInfo.Gender gender = mMyInfo.getGender();
            if (gender != null) {
                if (gender.equals(UserInfo.Gender.male)) {
                    mTv_gender.setText("男");
                } else if (gender.equals(UserInfo.Gender.female)) {
                    mTv_gender.setText("女");
                } else {
                    mTv_gender.setText("保密");
                }
            }
            long birthday = mMyInfo.getBirthday();
            if (birthday == 0) {
                mTv_birthday.setText("");
            } else {
                Date date = new Date(mMyInfo.getBirthday());
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                mTv_birthday.setText(format.format(date));
            }
            mTv_city.setText(mMyInfo.getAddress());
            mMyInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                @Override
                public void gotResult(int responseCode, String responseMessage, Bitmap avatarBitmap) {
                    if (responseCode == 0) {
                        mIv_photo.setImageBitmap(avatarBitmap);
                    } else {
                        mIv_photo.setImageResource(R.drawable.rc_default_portrait);
                    }
                }
            });
            dialog.dismiss();
        }
    }

    private void initListener() {
        mRl_cityChoose.setOnClickListener(this);
        mRl_birthday.setOnClickListener(this);
        mRl_gender.setOnClickListener(this);
        mSign.setOnClickListener(this);
        mRl_nickName.setOnClickListener(this);
        mIv_photo.setOnClickListener(this);
        mRl_zxing.setOnClickListener(this);
    }

    private void initView() {
        initTitle(true, true, "个人信息", "", false, "");
        mRl_cityChoose = (RelativeLayout) findViewById(R.id.rl_cityChoose);
        mTv_city = (TextView) findViewById(R.id.tv_city);
        mRl_gender = (RelativeLayout) findViewById(R.id.rl_gender);
        mRl_birthday = (RelativeLayout) findViewById(R.id.rl_birthday);
        mTv_birthday = (TextView) findViewById(R.id.tv_birthday);
        mTv_gender = (TextView) findViewById(R.id.tv_gender);
        mSign = (RelativeLayout) findViewById(R.id.sign);
        mTv_sign = (TextView) findViewById(R.id.tv_sign);
        mRl_nickName = (RelativeLayout) findViewById(R.id.rl_nickName);
        mTv_nickName = (TextView) findViewById(R.id.tv_nickName);
        mIv_photo = (ImageView) findViewById(R.id.iv_photo);
        mTv_userName = (TextView) findViewById(R.id.tv_userName);
        mRl_zxing = (RelativeLayout) findViewById(R.id.rl_zxing);

        mChoosePhoto = new ChoosePhoto();
        mChoosePhoto.setPortraitChangeListener(PersonalActivity.this, mIv_photo, 2);

    }

    @Override
    public void setAreaString(String area) {
        mTv_city.setText(area);
    }

    @Override
    public void setTime(String time) {
        mTv_birthday.setText(time);
    }

    @Override
    public void setGender(String gender) {
        mTv_gender.setText(gender);
    }

    @Override
    public void onClick(View v) {
        intent = new Intent(PersonalActivity.this, NickSignActivity.class);
        switch (v.getId()) {
            case R.id.iv_photo:
                //头像
                if ((ContextCompat.checkSelfPermission(PersonalActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) ||
                        (ContextCompat.checkSelfPermission(PersonalActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(PersonalActivity.this, "请在应用管理中打开“读写存储”和“相机”访问权限！", Toast.LENGTH_SHORT).show();
                }
                mChoosePhoto.setInfo(PersonalActivity.this, true);
                mChoosePhoto.showPhotoDialog(PersonalActivity.this);
                break;
            case R.id.rl_nickName:
                //昵称
                intent.setFlags(FLAGS_NICK);
                intent.putExtra("old_nick", mMyInfo.getNickname());
                startActivityForResult(intent, NICK_NAME);
                break;
            case R.id.sign:
                //签名
                intent.setFlags(FLAGS_SIGN);
                intent.putExtra("old_sign", mMyInfo.getSignature());
                startActivityForResult(intent, SIGN);
                break;
            case R.id.rl_gender:
                //弹出性别选择器
                dialog = new SelectAddressDialog(PersonalActivity.this);
                dialog.showGenderDialog(PersonalActivity.this, mMyInfo);
                break;
            case R.id.rl_birthday:
                //弹出时间选择器选择生日
                dialog = new SelectAddressDialog(PersonalActivity.this);
                dialog.showDateDialog(PersonalActivity.this, mMyInfo);
                break;
            case R.id.rl_cityChoose:
                //点击选择省市
                dialog = new SelectAddressDialog(PersonalActivity.this,
                        PersonalActivity.this, SelectAddressDialog.STYLE_THREE, null, mMyInfo);
                dialog.showDialog();
                break;
            //二维码
            case R.id.rl_zxing:
                final Dialog dialog = new Dialog(PersonalActivity.this, R.style.jmui_default_dialog_style);
                View erWeiMa = LayoutInflater.from(PersonalActivity.this).inflate(R.layout.dialog_zxing, null);
                dialog.setContentView(erWeiMa);

                ImageView avatar = (ImageView) erWeiMa.findViewById(R.id.iv_avatar);
                TextView nickName = (TextView) erWeiMa.findViewById(R.id.tv_nickName);
                ImageView zxing = (ImageView) erWeiMa.findViewById(iv_erWeiMa);

                avatar.setImageBitmap(BitmapFactory.decodeFile(mMyInfo.getAvatarFile().getAbsolutePath()));
                nickName.setText(mMyInfo.getNickname());
                final Bitmap bitmap = generateBitmap(mMyInfo.getUserName(), 600, 600);
                zxing.setImageBitmap(bitmap);

                //如果需要设置二维码中间logo就用这个
//                Bitmap generateBitmap = generateBitmap(mMyInfo.getUserName(), 600, 600);
//                Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
//                Bitmap bitmap = addLogo(generateBitmap, logoBitmap);
//                zxing.setImageBitmap(bitmap);

                zxing.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        //保存二维码的底部弹窗
                        final Dialog photoDialog = new Dialog(PersonalActivity.this, R.style.jmui_default_dialog_style);
                        LayoutInflater inflater = LayoutInflater.from(PersonalActivity.this);
                        View view = inflater.inflate(R.layout.save_erweima, null);
                        photoDialog.setContentView(view);
                        Window window = photoDialog.getWindow();
                        window.setWindowAnimations(R.style.mystyle); // 添加动画
                        window.setGravity(Gravity.BOTTOM);
                        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        photoDialog.show();
                        photoDialog.setCanceledOnTouchOutside(true);
                        Button savePhoto = (Button) view.findViewById(R.id.btn_save);
                        Button cancel = (Button) view.findViewById(R.id.btn_cancel);

                        View.OnClickListener listener = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (v.getId() == R.id.btn_save) {
                                    //截屏dialog并保存到手机
                                    View view = dialog.getWindow().getDecorView();
                                    String path = screenShotView(view);
                                    savePicture(path, dialog);
                                    photoDialog.dismiss();

                                } else {
                                    photoDialog.cancel();
                                }
                            }
                        };
                        savePhoto.setOnClickListener(listener);
                        cancel.setOnClickListener(listener);
                        return false;
                    }
                });

                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);
                dialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
                dialog.show();

                break;
            default:
                break;
        }
    }


    private String screenShotView(View view) {
        Bitmap temBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(temBitmap);
        view.draw(canvas);

        return saveBitmap(PersonalActivity.this, temBitmap);
    }


    public static String saveBitmap(Context context, Bitmap mBitmap) {
        String savePath;
        File filePic;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            savePath = SD_PATH;
        } else {
            savePath = context.getApplicationContext().getFilesDir()
                    .getAbsolutePath()
                    + IN_PATH;
        }
        try {
            filePic = new File(savePath + generateFileName() + ".jpg");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return filePic.getAbsolutePath();
    }

    private static String generateFileName() {
        return UUID.randomUUID().toString();
    }

    public void savePicture(String path, Dialog dialog) {
        if (path == null) {
            return;
        }

        String picPath = StorageUtil.getSystemImagePath();
        String dstPath = picPath + path;
        if (AttachmentStore.copy(path, dstPath) != -1) {
            try {
                ContentValues values = new ContentValues(2);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.DATA, dstPath);
                getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Toast.makeText(PersonalActivity.this, getString(R.string.picture_save_to), Toast.LENGTH_LONG).show();
                dialog.dismiss();
            } catch (Exception e) {
                dialog.dismiss();
                Toast.makeText(PersonalActivity.this, getString(R.string.picture_save_fail), Toast.LENGTH_LONG).show();
            }
        } else {
            dialog.dismiss();
            Toast.makeText(PersonalActivity.this, getString(R.string.picture_save_fail), Toast.LENGTH_LONG).show();
        }
    }

    //生成二维码
    private Bitmap generateBitmap(String content, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, String> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        try {
            BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (encode.get(j, i)) {
                        pixels[i * width + j] = 0x00000000;
                    } else {
                        pixels[i * width + j] = 0xffffffff;
                    }
                }
            }
            return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    //二维码中间的logo
    private Bitmap addLogo(Bitmap qrBitmap, Bitmap logoBitmap) {
        int qrBitmapWidth = qrBitmap.getWidth();
        int qrBitmapHeight = qrBitmap.getHeight();
        int logoBitmapWidth = logoBitmap.getWidth();
        int logoBitmapHeight = logoBitmap.getHeight();
        Bitmap blankBitmap = Bitmap.createBitmap(qrBitmapWidth, qrBitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(blankBitmap);
        canvas.drawBitmap(qrBitmap, 0, 0, null);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        float scaleSize = 1.0f;
        while ((logoBitmapWidth / scaleSize) > (qrBitmapWidth / 5) || (logoBitmapHeight / scaleSize) > (qrBitmapHeight / 5)) {
            scaleSize *= 2;
        }
        float sx = 1.0f / scaleSize;
        canvas.scale(sx, sx, qrBitmapWidth / 2, qrBitmapHeight / 2);
        canvas.drawBitmap(logoBitmap, (qrBitmapWidth - logoBitmapWidth) / 2, (qrBitmapHeight - logoBitmapHeight) / 2, null);
        canvas.restore();
        return blankBitmap;
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Bundle bundle = data.getExtras();
            switch (resultCode) {
                case SIGN:
                    final String sign = bundle.getString(SIGN_KEY);
                    ThreadUtil.runInThread(new Runnable() {
                        @Override
                        public void run() {
                            mMyInfo.setSignature(sign);
                            JMessageClient.updateMyInfo(UserInfo.Field.signature, mMyInfo, new BasicCallback() {
                                @Override
                                public void gotResult(int responseCode, String responseMessage) {
                                    if (responseCode == 0) {
                                        mTv_sign.setText(sign);
                                        ToastUtil.shortToast(PersonalActivity.this, "更新成功");
                                    } else {
                                        ToastUtil.shortToast(PersonalActivity.this, "更新失败");
                                    }
                                }
                            });
                        }
                    });
                    break;
                case NICK_NAME:
                    final String nick = bundle.getString(NICK_NAME_KEY);
                    ThreadUtil.runInThread(new Runnable() {
                        @Override
                        public void run() {
                            mMyInfo.setNickname(nick);
                            JMessageClient.updateMyInfo(UserInfo.Field.nickname, mMyInfo, new BasicCallback() {
                                @Override
                                public void gotResult(int responseCode, String responseMessage) {
                                    if (responseCode == 0) {
                                        mTv_nickName.setText(nick);
                                        ToastUtil.shortToast(PersonalActivity.this, "更新成功");
                                    } else {
                                        ToastUtil.shortToast(PersonalActivity.this, "更新失败,请正确输入");
                                    }
                                }
                            });
                        }
                    });
                    break;
                default:
                    break;
            }
        }
        switch (requestCode) {
            case PhotoUtils.INTENT_CROP:
            case PhotoUtils.INTENT_TAKE:
            case PhotoUtils.INTENT_SELECT:
                mChoosePhoto.photoUtils.onActivityResult(PersonalActivity.this, requestCode, resultCode, data);
                break;
        }
    }

}
