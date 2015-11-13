package io.jchat.android.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import io.jchat.android.R;
import io.jchat.android.adapter.PickPictureAdapter;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.tools.BitmapLoader;
import io.jchat.android.tools.HandleResponseCode;

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
    private String mTargetId;
    private Conversation mConv;
    private ProgressDialog mDialog;
    private long mGroupId;
    private int[] mMsgIds;
    private static final int SEND_PICTURE = 200;
    private final MyHandler myHandler = new MyHandler(this);
    private int mIndex = 0;
    private Queue<String> mPathQueue = new LinkedList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_picture_detail);
        mSendPictureBtn = (Button) findViewById(R.id.pick_picture_send_btn);
        mReturnBtn = (ImageButton) findViewById(R.id.pick_picture_detail_return_btn);
        mGridView = (GridView) findViewById(R.id.child_grid);

        Intent intent = this.getIntent();
        mIsGroup = intent.getBooleanExtra(JChatDemoApplication.IS_GROUP, false);
        if (mIsGroup) {
            mGroupId = intent.getLongExtra(JChatDemoApplication.GROUP_ID, 0);
            mConv = JMessageClient.getGroupConversation(mGroupId);
        } else {
            mTargetId = intent.getStringExtra(JChatDemoApplication.TARGET_ID);
            mConv = JMessageClient.getSingleConversation(mTargetId);
        }
        mList = intent.getStringArrayListExtra("data");
        mAdapter = new PickPictureAdapter(this, mList, mGridView, mDensity);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(onItemListener);
        mSendPictureBtn.setOnClickListener(listener);
        mReturnBtn.setOnClickListener(listener);
    }

    private OnItemClickListener onItemListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> viewAdapter, View view, int position, long id) {
            Intent intent = new Intent();
            intent.putExtra("fromChatActivity", false);
            if (mIsGroup) {
                intent.putExtra(JChatDemoApplication.GROUP_ID, mGroupId);
            } else {
                intent.putExtra(JChatDemoApplication.TARGET_ID, mTargetId);
            }
            intent.putStringArrayListExtra("pathList", (ArrayList<String>) mList);
            intent.putExtra(JChatDemoApplication.POSITION, position);
            intent.putExtra(JChatDemoApplication.IS_GROUP, mIsGroup);
            intent.putExtra("pathArray", mAdapter.getSelectedArray());
            intent.setClass(PickPictureActivity.this, BrowserViewPagerActivity.class);
            startActivityForResult(intent, JChatDemoApplication.REQUEST_CODE_BROWSER_PICTURE);
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
                    for (int i = 0; i < positionList.size(); i++) {
                        mPickedList.add(mList.get(positionList.get(i)));
                        Log.i("PickPictureActivity", "Picture Path: " + mList.get(positionList.get(i)));
                    }
                    if (mPickedList.size() < 1)
                        return;
                    else {
                        mDialog = new ProgressDialog(PickPictureActivity.this);
                        mDialog.setCanceledOnTouchOutside(false);
                        mDialog.setCancelable(false);
                        mDialog.setMessage(PickPictureActivity.this.getString(R.string.sending_hint));
                        mDialog.show();

                        getThumbnailPictures();
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
     */
    private void getThumbnailPictures() {
        mMsgIds = new int[mPickedList.size()];
        Bitmap bitmap;
        //根据选择的图片路径生成队列
        for (int i = 0; i < mPickedList.size(); i++) {
            mPathQueue.offer(mPickedList.get(i));
            if (BitmapLoader.verifyPictureSize(mPickedList.get(i))) {
                File file = new File(mPickedList.get(i));
                ImageContent.createImageContentAsync(file, new ImageContent.CreateImageContentCallback() {
                    @Override
                    public void gotResult(int status, String desc, ImageContent imageContent) {
                        if (status == 0) {
                            Message msg = mConv.createSendMessage(imageContent);
                            mMsgIds[mIndex] = msg.getId();
                            mIndex++;
                            if (mIndex >= mPickedList.size()) {
                                myHandler.sendEmptyMessage(SEND_PICTURE);
                            }
                        }else {
                            if (mDialog != null) {
                                mDialog.dismiss();
                            }
                        }
                    }
                });
            }else {
                bitmap = BitmapLoader.getBitmapFromFile(mPickedList.get(i), 720, 1280);
                ImageContent.createImageContentAsync(bitmap, new ImageContent.CreateImageContentCallback() {
                    @Override
                    public void gotResult(int status, String desc, ImageContent imageContent) {
                        if (status == 0) {
                            Message msg = mConv.createSendMessage(imageContent);
                            mMsgIds[mIndex] = msg.getId();
                            mIndex++;
                            if (mIndex >= mPickedList.size()) {
                                myHandler.sendEmptyMessage(SEND_PICTURE);
                            }
                        }else {
                            if (mDialog != null) {
                                mDialog.dismiss();
                            }
                            HandleResponseCode.onHandle(PickPictureActivity.this, status, false);
                        }
                    }
                });
            }
        }

        //从队列中取出第一个元素，生成ImageContent
//        String path = mPathQueue.element();
//        createNextImgContent(path);
    }

    /**
     * 根据图片路径创建ImageContent
     * @param path 图片路径
     */
    private void createNextImgContent(final String path) {
        Bitmap bitmap;
        //验证图片大小，若小于720 * 1280则直接发送原图，否则压缩
        if (BitmapLoader.verifyPictureSize(path)) {
            File file = new File(path);
            ImageContent.createImageContentAsync(file, new ImageContent.CreateImageContentCallback() {
                @Override
                public void gotResult(int status, String desc, ImageContent imageContent) {
                    if (status == 0) {
                        Message msg = mConv.createSendMessage(imageContent);
                        mMsgIds[mIndex] = msg.getId();
                        //自增索引，出列
                        mIndex++;
                        mPathQueue.poll();
                        //如果队列不为空， 继续创建下一个ImageContent
                        if (!mPathQueue.isEmpty()) {
                            createNextImgContent(mPathQueue.element());
                        //否则，队列中所有元素创建完毕，通知Handler
                        } else {
                            myHandler.sendEmptyMessage(SEND_PICTURE);
                        }
                    } else {
                        Log.d("PickPictureActivity", "create image content failed! status:" + status);
                        if (mDialog != null) {
                            mDialog.dismiss();
                        }
                        HandleResponseCode.onHandle(PickPictureActivity.this, status, false);
                    }
                }
            });
        } else {
            bitmap = BitmapLoader.getBitmapFromFile(path, 720, 1280);
            ImageContent.createImageContentAsync(bitmap, new ImageContent.CreateImageContentCallback() {
                @Override
                public void gotResult(int status, String desc, ImageContent imageContent) {
                    if (status == 0) {
                        Message msg = mConv.createSendMessage(imageContent);
                        mMsgIds[mIndex] = msg.getId();
                        mIndex++;
                        mPathQueue.poll();
                        if (!mPathQueue.isEmpty()) {
                            createNextImgContent(mPathQueue.element());
                        } else {
                            myHandler.sendEmptyMessage(SEND_PICTURE);
                        }
                    }else {
                        HandleResponseCode.onHandle(PickPictureActivity.this, status, false);
                    }
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == JChatDemoApplication.RESULT_CODE_SELECT_PICTURE) {
            if (data != null) {
                int[] selectedArray = data.getIntArrayExtra("pathArray");
                int sum = 0;
                for (int i : selectedArray) {
                    if (i > 0) {
                        ++sum;
                    }
                }
                if (sum > 0) {
                    String sendText = PickPictureActivity.this.getString(R.string.send) + "(" + sum + "/" + "9)";
                    mSendPictureBtn.setText(sendText);
                }else {
                    mSendPictureBtn.setText(PickPictureActivity.this.getString(R.string.send));
                }
                mAdapter.refresh(selectedArray);
            }

        } else if (resultCode == JChatDemoApplication.RESULT_CODE_BROWSER_PICTURE) {
            setResult(JChatDemoApplication.RESULT_CODE_SELECT_ALBUM, data);
            finish();
        }
    }

    private static class MyHandler extends Handler {
        private final WeakReference<PickPictureActivity> mActivity;

        public MyHandler(PickPictureActivity activity) {
            mActivity = new WeakReference<PickPictureActivity>(activity);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            PickPictureActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case SEND_PICTURE:
                        Intent intent = new Intent();
                        intent.putExtra(JChatDemoApplication.TARGET_ID, activity.mTargetId);
                        intent.putExtra(JChatDemoApplication.GROUP_ID, activity.mGroupId);
                        intent.putExtra(JChatDemoApplication.MsgIDs, activity.mMsgIds);
                        activity.setResult(JChatDemoApplication.RESULT_CODE_SELECT_ALBUM, intent);
                        if (activity.mDialog != null) {
                            activity.mDialog.dismiss();
                        }
                        activity.finish();
                        break;
                }
            }
        }
    }
}
