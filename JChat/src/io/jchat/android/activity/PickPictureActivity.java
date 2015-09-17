package io.jchat.android.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.content.ImageContent;
import io.jchat.android.R;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.JMessageClient;

import io.jchat.android.adapter.PickPictureAdapter;
import io.jchat.android.application.JPushDemoApplication;
import io.jchat.android.tools.BitmapLoader;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.tools.SortPictureList;

public class PickPictureActivity extends BaseActivity {
    private GridView mGridView;
    //此相册下所有图片的路径集合
    private List<String> mList;
    //选中图片的路径集合
    private List<String> mPickedList;
    private Button mSendPictureBtn;
    private ImageButton mReturnBtn;
    private boolean mIsGroup;
    private PickPictureAdapter mAdapter;
    private Intent mIntent;
    private String mTargetID;
    private Conversation mConv;
    private ProgressDialog mDialog;
    private long mGroupID;
    private int[] mMsgIDs;
    private static final int SEND_PICTURE = 200;
    private final MyHandler myHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_picture_detail);
        mSendPictureBtn = (Button) findViewById(R.id.pick_picture_send_btn);
        mReturnBtn = (ImageButton) findViewById(R.id.pick_picture_detail_return_btn);
        mGridView = (GridView) findViewById(R.id.child_grid);

        mIntent = this.getIntent();
        mIsGroup = mIntent.getBooleanExtra(JPushDemoApplication.IS_GROUP, false);
        if (mIsGroup){
            mGroupID = mIntent.getLongExtra(JPushDemoApplication.GROUP_ID, 0);
            Log.i("PickPictureActivity", "groupID : " + mGroupID);
            mConv = JMessageClient.getGroupConversation(mGroupID);
        }
        else {
            mTargetID = mIntent.getStringExtra(JPushDemoApplication.TARGET_ID);
            Log.i("PickPictureActivity", "mTargetID" + mTargetID);
            mConv = JMessageClient.getSingleConversation(mTargetID);
        }
        mList = mIntent.getStringArrayListExtra("data");
        if (mList.size() > 1) {
            SortPictureList sortList = new SortPictureList();
            Collections.sort(mList, sortList);
        }
        mAdapter = new PickPictureAdapter(this, mList, mGridView);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(onItemListener);
        mSendPictureBtn.setOnClickListener(listener);
        mReturnBtn.setOnClickListener(listener);
    }

    private OnItemClickListener onItemListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> viewAdapter, View view, int position,
                                long id) {
            Intent intent = new Intent();
            intent.putExtra("fromChatActivity", false);
            if(mIsGroup){
                intent.putExtra(JPushDemoApplication.GROUP_ID, mGroupID);
            }else intent.putExtra(JPushDemoApplication.TARGET_ID, mTargetID);
            intent.putStringArrayListExtra("pathList", (ArrayList<String>) mList);
            intent.putExtra(JPushDemoApplication.POSITION, position);
            intent.putExtra(JPushDemoApplication.IS_GROUP, mIsGroup);
            intent.putExtra("pathArray", mAdapter.getSelectedArray());
            intent.setClass(PickPictureActivity.this, BrowserViewPagerActivity.class);
            startActivityForResult(intent, JPushDemoApplication.REQUEST_CODE_BROWSER_PICTURE);
        }
    };

    private OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                //点击发送按钮，发送选中的图片
                case R.id.pick_picture_send_btn:
                    //存放选中图片的路径
                    mPickedList = new ArrayList<String>();
                    //存放选中的图片的position
                    List<Integer> positionList;
                    positionList = mAdapter.getSelectItems();
                    //拿到选中图片的路径
                    for (int i = 0; i < positionList.size(); i++){
                        mPickedList.add(mList.get(positionList.get(i)));
                        Log.i("PickPictureActivity", "Picture Path: " + mList.get(positionList.get(i)));
                    }
                    if(mPickedList.size() < 1)
                        return;
                    else {
                        mDialog = new ProgressDialog(PickPictureActivity.this);
                        mDialog.setCanceledOnTouchOutside(false);
                        mDialog.setMessage(PickPictureActivity.this.getString(R.string.sending_hint));
                        mDialog.show();

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                getThumbnailPictures();
                                myHandler.sendEmptyMessageDelayed(SEND_PICTURE, 1000);
                            }
                        });
                        thread.start();
                    }
                    break;
                case R.id.pick_picture_detail_return_btn:
                    finish();
                    break;
            }
        }

    };

    /**
     * 获得选中图片的缩略图路径
     *
     */
    private void getThumbnailPictures() {
        Bitmap bitmap;
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mMsgIDs = new int[mPickedList.size()];
        for (int i = 0; i < mPickedList.size(); i++) {
            final int index = i;
            //验证图片大小，若小于720 * 1280则直接发送原图，否则压缩
            if (BitmapLoader.verifyPictureSize(mPickedList.get(i))){
                File file = new File(mPickedList.get(i));
                ImageContent.createImageContentAsync(file, new ImageContent.CreateImageContentCallback() {
                    @Override
                    public void gotResult(int status, String desc, ImageContent imageContent) {
                        if (status == 0){
                            Message msg = mConv.createSendMessage(imageContent);
                            mMsgIDs[index] = msg.getId();
                        }else {
                            Log.d("PickPictureActivity", "create image content failed! status:" + status);
                            HandleResponseCode.onHandle(PickPictureActivity.this, status, false);
                        }
                    }
                });
            } else {
                bitmap = BitmapLoader.getBitmapFromFile(mPickedList.get(i), 720, 1280);
                final String tempPath = BitmapLoader.saveBitmapToLocal(bitmap);
                File file = new File(tempPath);
                ImageContent.createImageContentAsync(file, new ImageContent.CreateImageContentCallback() {
                    @Override
                    public void gotResult(int status, String desc, ImageContent imageContent) {
                        if (status == 0) {
                            imageContent.setStringExtra("localPath", mPickedList.get(index));
                            imageContent.setStringExtra("tempPath", tempPath);
                            Message msg = mConv.createSendMessage(imageContent);
                            mMsgIDs[index] = msg.getId();
                        } else {
                            Log.d("PickPictureActivity", "create image content failed! status:" + status);
                            HandleResponseCode.onHandle(PickPictureActivity.this, status, false);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == JPushDemoApplication.RESULT_CODE_SELECT_PICTURE) {
            if (data != null) {
                int[] selectedArray = data.getIntArrayExtra("pathArray");
                int sum = 0;
                for(int i=0; i < selectedArray.length; i++){
                    if(selectedArray[i] > 0)
                        ++sum;
                }
                if(sum > 0)
                    mSendPictureBtn.setText(PickPictureActivity.this.getString(R.string.send) + "(" + sum + "/" + "9)");
                mAdapter.refresh(selectedArray);
            }

        }else if (resultCode == JPushDemoApplication.RESULT_CODE_BROWSER_PICTURE){
            setResult(JPushDemoApplication.RESULT_CODE_SELECT_ALBUM, data);
            finish();
        }
    }

    private static class MyHandler extends Handler{
        private final WeakReference<PickPictureActivity> mActivity;

        public MyHandler(PickPictureActivity activity){
            mActivity = new WeakReference<PickPictureActivity>(activity);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            PickPictureActivity activity = mActivity.get();
            if(activity != null){
                switch (msg.what) {
                    case SEND_PICTURE:
                        Intent intent = new Intent();
                        intent.putExtra(JPushDemoApplication.TARGET_ID, activity.mTargetID);
                        intent.putExtra(JPushDemoApplication.GROUP_ID, activity.mGroupID);
                        intent.putExtra(JPushDemoApplication.MsgIDs, activity.mMsgIDs);
                        activity.setResult(JPushDemoApplication.RESULT_CODE_SELECT_ALBUM, intent);
                        if(activity.mDialog != null)
                            activity.mDialog.dismiss();
                        activity.finish();
                        break;
                }
            }
        }
    }
}
