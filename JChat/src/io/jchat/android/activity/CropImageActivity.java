package io.jchat.android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
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
        Bitmap bitmap = BitmapLoader.getBitmapFromFile(path, mWidth, mHeight);
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
}
