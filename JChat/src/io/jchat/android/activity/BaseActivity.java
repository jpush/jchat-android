package io.jchat.android.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created by Ken on 2015/3/13.
 */
public class BaseActivity extends Activity {
    
    protected BaseHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mHandler =  new BaseHandler();

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //    private class NetworkReceiver extends BroadcastReceiver{
//
//    }

    public class BaseHandler extends Handler {

        @Override
        public void handleMessage(android.os.Message msg) {
            handleMsg(msg);
        }
    }

    public void handleMsg(Message message){}

}
