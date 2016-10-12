package io.jchat.android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.chatting.utils.BitmapLoader;
import io.jchat.android.view.CropImageView;

/**
 * Created by Ken on 15/12/3.
 */
public class CropImageActivity extends BaseActivity {

    private CropImageView mImageView;
    private ImageButton mReturnBtn;
    private TextView mTitle;
    private Button mCommitBtn;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_crop_image);
        mImageView = (CropImageView) findViewById(R.id.crop_image_view);
        mReturnBtn = (ImageButton) findViewById(R.id.return_btn);
        mTitle = (TextView) findViewById(R.id.jmui_title_tv);
        mCommitBtn = (Button) findViewById(R.id.jmui_commit_btn);
        mTitle.setText(this.getString(R.string.crop_image_title));
        Intent intent = getIntent();
        String path = intent.getStringExtra("filePath");
        int degree = getBitmapDegree(path);
        Bitmap bitmap = BitmapLoader.getBitmapFromFile(path, mWidth, mHeight);
        bitmap = rotateBitmapByDegree(bitmap, degree);
        Log.i("CropImageActivity", "mWidth * mHeight: " + mWidth + " * " + mHeight);
        mImageView.setDrawable(new BitmapDrawable(getResources(), bitmap), 720, 720);

        mReturnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mCommitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap uploadBitmap = mImageView.getCropImage();
                UserInfo myInfo = JMessageClient.getMyInfo();
                String uploadPath = BitmapLoader.saveBitmapToLocal(uploadBitmap,
                        mContext, myInfo.getUserName());
                Intent data = new Intent();
                data.putExtra("filePath", uploadPath);
                setResult(JChatDemoApplication.REQUEST_CODE_CROP_PICTURE, data);
                finish();
            }
        });
    }

    /**
     * 获取图片的旋转角度
     *
     * @param path 图片绝对路径
     * @return 图片的旋转角度
     */
    public static int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 将图片按照指定的角度进行旋转
     *
     * @param bitmap 需要旋转的图片
     * @param degree 指定的旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bitmap, int degree) {
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        return newBitmap;
    }

}
