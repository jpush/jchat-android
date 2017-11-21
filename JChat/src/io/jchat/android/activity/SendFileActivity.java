package io.jchat.android.activity;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;


import io.jchat.android.R;
import io.jchat.android.adapter.DocumentAdapter;
import io.jchat.android.controller.SendFileController;
import io.jchat.android.view.SendFileView;

public class SendFileActivity extends FragmentActivity {

    private SendFileView mView;
    private SendFileController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_file);
        mView = (SendFileView) findViewById(R.id.send_file_view);
        mView.initModule();
        mController = new SendFileController(this, mView);
        mView.setOnClickListener(mController);
        mView.setOnPageChangeListener(mController);
    }

    public FragmentManager getSupportFragmentManger() {
        return getSupportFragmentManager();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
