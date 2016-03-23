package io.jchat.android.activity;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.jpush.im.android.api.JMessageClient;
import io.jchat.android.R;

public class AboutActivity extends Activity {

    private ImageButton mReturnBtn;
    private TextView mAboutTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        mReturnBtn = (ImageButton) findViewById(R.id.return_btn);
        mAboutTv = (TextView) findViewById(R.id.about_tv);

        mReturnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        PackageManager manager = this.getPackageManager();
        try{
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            String demoVersionName = this.getString(R.string.demo_version_name);
            String sdkVersion = JMessageClient.getSdkVersionString();
            String aboutContent = String.format(demoVersionName, version)
                    + this.getString(R.string.sdk_version) + sdkVersion + this.getString(R.string.about_date);
            mAboutTv.setText(aboutContent);
        }catch (PackageManager.NameNotFoundException e) {
            Log.d("AboutActivity", "Name not Found");
        }

    }
}
