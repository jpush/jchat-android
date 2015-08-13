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
    private final MyHandler myHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_picture_detail);
        mSendPictureBtn = (Button) findViewById(R.id.pick_picture_send_btn);
        mReturnBtn = (ImageButton) findViewById(R.id.pick_picture_detail_return_btn);
        mGridView = (GridView) findViewById(R.id.child_grid);

        mIntent = this.getIntent();
        mIsGroup = mIntent.getBooleanExtra("isGroup", false);
        if (mIsGroup){
            mGroupID = mIntent.getLongExtra("groupID", 0);
            Log.i("PickPictureActivity", "groupID : " + mGroupID);
            mConv = JMessageClient.getGroupConversation(mGroupID);
        }
        else {
            mTargetID = mIntent.getStringExtra("targetID");
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
                intent.putExtra("groupID", mGroupID);
            }else intent.putExtra("targetID", mTargetID);
            intent.putStringArrayListExtra("pathList", (ArrayList<String>) mList);
            intent.putExtra("position", position);
            intent.putExtra("isGroup", mIsGroup);
            intent.putExtra("pathArray", mAdapter.getSelectedArray());
            intent.setClass(PickPictureActivity.this, BrowserViewPagerActivity.class);
            startActivityForResult(intent, JPushDemoApplication.REQUESTCODE_SELECT_PICTURE);
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
                    List<Integer> positionList = new ArrayList<Integer>();
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
                                final List<String> pathList = new ArrayList<String>();
                                getThumbnailPictures(pathList);
                                android.os.Message msg = myHandler.obtainMessage();
                                msg.what = 0;
                                Bundle bundle = new Bundle();
                                bundle.putStringArrayList("pathList", (ArrayList<String>) pathList);
                                msg.setData(bundle);
                                msg.sendToTarget();
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
     * @param pathList
     */
    private void getThumbnailPictures(List<String> pathList) {
        String tempPath;
        Bitmap bitmap;
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mMsgIDs = new int[mPickedList.size()];
        for (int i = 0; i < mPickedList.size(); i++) {
            //验证图片大小，若小于720 * 1280则直接发送原图，否则压缩
            if(BitmapLoader.verifyPictureSize(mPickedList.get(i)))
                pathList.add(mPickedList.get(i));
            else {
                bitmap = BitmapLoader.getBitmapFromFile(mPickedList.get(i), 720, 1280);
                tempPath = BitmapLoader.saveBitmapToLocal(bitmap);
                pathList.add(tempPath);
            }

            Log.i("PickPictureActivity", "pathList.get(i) " + pathList.get(i));
            File file = new File(pathList.get(i));
            try {
                    ImageContent content = new ImageContent(file);
                    Message msg = mConv.createSendMessage(content);
                    mMsgIDs[i] = msg.getId();
                } catch (FileNotFoundException e) {
                }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == JPushDemoApplication.RESULTCODE_SELECT_PICTURE) {
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
                    case 0:
                        Intent intent = new Intent();
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("sendPicture", true);
                        intent.putExtra("targetID", activity.mTargetID);
                        intent.putExtra("groupID", activity.mGroupID);
                        intent.putExtra("isGroup", activity.mIsGroup);
                        intent.putExtra("msgIDs", activity.mMsgIDs);
                        intent.setClass(activity, ChatActivity.class);
                        activity.startActivity(intent);
                        if(activity.mDialog != null)
                            activity.mDialog.dismiss();
                        activity.finish();
                        break;
                }
            }
        }
    }
}
