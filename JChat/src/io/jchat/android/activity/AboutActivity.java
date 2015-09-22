package io.jchat.android.activity;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import io.jchat.android.R;

/**
 * Created by jpush on 2015/8/28.
 */
public class AboutActivity extends Activity {

    private TextView mAboutTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        mAboutTv = (TextView) findViewById(R.id.about_tv);
        PackageManager manager = this.getPackageManager();
        try{
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            String demoVersionName = this.getString(R.string.demo_version_name);
            mAboutTv.setText(String.format(demoVersionName, version)
                    + this.getString(R.string.about_version) + this.getString(R.string.about_date));
        }catch (PackageManager.NameNotFoundException e){
            Log.d("AboutActivity", "Name not Found");
        }

    }
}
