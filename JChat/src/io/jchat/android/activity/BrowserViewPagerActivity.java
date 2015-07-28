package io.jchat.android.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.jchat.android.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.callback.ProgressUpdateCallback;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.MessageDirect;
import io.jchat.android.application.JPushDemoApplication;
import io.jchat.android.tools.BitmapLoader;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.view.ImgBrowserViewPager;
import io.jchat.android.view.photoview.PhotoView;

//用于浏览图片
public class BrowserViewPagerActivity extends BaseActivity {

    private static String TAG = BrowserViewPagerActivity.class.getSimpleName();
    private PhotoView photoView;
    private ImgBrowserViewPager mViewPager;
    private ProgressDialog mProgressDialog;
    //存放所有图片的路径
    private List<String> mPathList = new ArrayList<String>();
    //存放图片消息的ID
    private List<Integer> mMsgIDList = new ArrayList<Integer>();
    private TextView mNumberTv;
    private Button mSendBtn;
    private CheckBox mOriginPictureCb;
    private TextView mTotalSizeTv;
    private CheckBox mPictureSelectedCb;
    private Button mLoadBtn;
    private int mPosition;
    private Conversation mConv;
    private Message mMsg;
    private String mTargetID;
    private boolean mFromChatActivity = true;
    private int mWidth;
    private int mHeight;
    private Context mContext;
    private boolean mDownloading = false;
    private boolean mIsGroup;
    private Long mGroupID;
    private int[] mMsgIDs;
    /**
     * 用来存储图片的选中情况
     */
    private SparseBooleanArray mSelectMap = new SparseBooleanArray();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageButton returnBtn;
        RelativeLayout titleBarRl, checkBoxRl;

        mContext = this;
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mWidth = dm.widthPixels;
        mHeight = dm.heightPixels;
        Log.i(TAG, "width height :" + mWidth + mHeight);
        setContentView(R.layout.activity_image_browser);
        mViewPager = (ImgBrowserViewPager) findViewById(R.id.img_browser_viewpager);
        returnBtn = (ImageButton) findViewById(R.id.return_btn);
        mNumberTv = (TextView) findViewById(R.id.number_tv);
        mSendBtn = (Button) findViewById(R.id.pick_picture_send_btn);
        titleBarRl = (RelativeLayout) findViewById(R.id.title_bar_rl);
        checkBoxRl = (RelativeLayout) findViewById(R.id.check_box_rl);
        mOriginPictureCb = (CheckBox) findViewById(R.id.origin_picture_cb);
        mTotalSizeTv = (TextView) findViewById(R.id.total_size_tv);
        mPictureSelectedCb = (CheckBox) findViewById(R.id.picture_selected_cb);
        mLoadBtn = (Button) findViewById(R.id.load_image_btn);

        Intent intent = this.getIntent();
        mIsGroup = intent.getBooleanExtra("isGroup", false);
        if (mIsGroup) {
            mGroupID = intent.getLongExtra("groupID", 0);
            mConv = JMessageClient.getGroupConversation(mGroupID);
        } else {
            mTargetID = intent.getStringExtra("targetID");
            mConv = JMessageClient.getSingleConversation(mTargetID);
        }
        mPosition = intent.getIntExtra("position", 0);
        mFromChatActivity = intent.getBooleanExtra("fromChatActivity", true);
        boolean browserAvatar = intent.getBooleanExtra("browserAvatar", false);

        PagerAdapter pagerAdapter = new PagerAdapter() {

            @Override
            public int getCount() {
                return mPathList.size();
            }

            /**
             * 点击某张图片预览时，系统自动调用此方法加载这张图片左右视图（如果有的话）
             */
            @Override
            public View instantiateItem(ViewGroup container, int position) {
                photoView = new PhotoView(mFromChatActivity, container.getContext());
                photoView.setTag(position);
                String path = mPathList.get(position);
                Bitmap bitmap = BitmapLoader.getBitmapFromFile(path, mWidth, mHeight);
                if (bitmap != null)
                    photoView.setImageBitmap(bitmap);
                else photoView.setImageResource(R.drawable.friends_sends_pictures_no);
                container.addView(photoView, LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT);
                return photoView;
            }

            @Override
            public int getItemPosition(Object object) {
                View view = (View) object;
                int currentPage = mViewPager.getCurrentItem();
                if (currentPage == (Integer) view.getTag()) {
                    return POSITION_NONE;
                } else {
                    return POSITION_UNCHANGED;
                }
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {
                container.removeView((View) object);
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

        };
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setOnPageChangeListener(l);
        returnBtn.setOnClickListener(listener);
        mSendBtn.setOnClickListener(listener);
        mLoadBtn.setOnClickListener(listener);

        // 在聊天界面中点击图片
        if (mFromChatActivity) {
            titleBarRl.setVisibility(View.GONE);
            checkBoxRl.setVisibility(View.GONE);
            //预览头像
            if (browserAvatar) {
                mPathList.add(intent.getStringExtra("avatarPath"));
                photoView = new PhotoView(mFromChatActivity, this);
                mLoadBtn.setVisibility(View.GONE);
                try {
                    photoView.setImageBitmap(BitmapLoader.getBitmapFromFile(mPathList.get(0), mWidth, mHeight));
                } catch (Exception e) {
                    photoView.setImageResource(R.drawable.friends_sends_pictures_no);
                }
                if(mViewPager != null && mViewPager.getAdapter() != null){
                    mViewPager.getAdapter().notifyDataSetChanged();
                }
            //预览聊天界面中的图片
            } else {
                initImgPathList();
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    Toast.makeText(this, this.getString(R.string.local_picture_not_found_toast), Toast.LENGTH_SHORT).show();
                }
                mMsg = mConv.getMessage(intent.getIntExtra("msgID", 0));
                photoView = new PhotoView(mFromChatActivity, this);
                try {
                    ImageContent ic = (ImageContent) mMsg.getContent();
                    //如果发送方上传了原图
                    if(ic.getBooleanExtra("originalPicture")){
                        mLoadBtn.setVisibility(View.VISIBLE);
                        NumberFormat ddf1 = NumberFormat.getNumberInstance();
                        //保留小数点后两位
                        ddf1.setMaximumFractionDigits(2);
                        double size = ic.getFileSize() / 1048576.0;
                        String fileSize = "(" + ddf1.format(size) + "M" + ")";
                        mLoadBtn.setText(mContext.getString(R.string.load_origin_image) + fileSize);
                    }
                    //如果点击的是第一张图片并且图片未下载过，则显示大图
                    if (ic.getLocalPath() == null && mMsgIDList.indexOf(mMsg.getId()) == 0) {
                        downloadImage();
                    }
                    photoView.setImageBitmap(BitmapLoader.getBitmapFromFile(mPathList.get(mMsgIDList.indexOf(mMsg.getId())), mWidth, mHeight));
                    mViewPager.setCurrentItem(mMsgIDList.indexOf(mMsg.getId()));
                } catch (NullPointerException e) {
                    photoView.setImageResource(R.drawable.friends_sends_pictures_no);
                    mViewPager.setCurrentItem(mMsgIDList.indexOf(mMsg.getId()));
                }
            }
            // 在选择图片时点击预览图片
        } else {
            mPathList = intent.getStringArrayListExtra("pathList");
            int[] pathArray = intent.getIntArrayExtra("pathArray");
            //初始化选中了多少张图片
            for (int i = 0; i < pathArray.length; i++) {
                if (pathArray[i] == 1) {
                    mSelectMap.put(i, true);
                }
            }
            showSelectedNum();
            mLoadBtn.setVisibility(View.GONE);
            mViewPager.setCurrentItem(mPosition);
            mNumberTv.setText(mPosition + 1 + "/" + mPathList.size());
            int currentItem = mViewPager.getCurrentItem();
            checkPictureSelected(currentItem);
            checkOriginPictureSelected();
            //第一张特殊处理
            mPictureSelectedCb.setChecked(mSelectMap.get(currentItem));
            showTotalSize();
        }
    }

    /**
     * 在图片预览中发送图片，点击选择CheckBox时，触发事件
     *
     * @param currentItem 当前图片索引
     */
    private void checkPictureSelected(final int currentItem) {
        mPictureSelectedCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (mSelectMap.size() + 1 <= 9) {
                    if (isChecked)
                        mSelectMap.put(currentItem, true);
                    else mSelectMap.delete(currentItem);
                } else if (isChecked) {
                    Toast.makeText(mContext, mContext.getString(R.string.picture_num_limit_toast), Toast.LENGTH_SHORT).show();
                    mPictureSelectedCb.setChecked(mSelectMap.get(currentItem));
                } else {
                    mSelectMap.delete(currentItem);
                }

                showSelectedNum();
                showTotalSize();
            }
        });

    }

    /**
     * 点击发送原图CheckBox，触发事件
     *
     */
    private void checkOriginPictureSelected() {
        mOriginPictureCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    if (mSelectMap.size() < 1)
                        mPictureSelectedCb.setChecked(true);
                }
            }
        });
    }

    //显示选中的图片总的大小
    private void showTotalSize() {
        if (mSelectMap.size() > 0) {
            List<String> pathList = new ArrayList<String>();
            for (int i=0; i < mSelectMap.size(); i++) {
                pathList.add(mPathList.get(mSelectMap.keyAt(i)));
            }
            String totalSize = BitmapLoader.getPictureSize(pathList);
            mTotalSizeTv.setText(mContext.getString(R.string.origin_picture) + "(" + totalSize + ")");
        } else mTotalSizeTv.setText(mContext.getString(R.string.origin_picture));
    }

    //显示选中了多少张图片
    private void showSelectedNum() {
        if (mSelectMap.size() > 0) {
            mSendBtn.setText(mContext.getString(R.string.send) + "(" + mSelectMap.size() + "/" + "9)");
        } else mSendBtn.setText(mContext.getString(R.string.send));
    }

    private ViewPager.OnPageChangeListener l = new ViewPager.OnPageChangeListener() {
        //在滑动的时候更新CheckBox的状态
        @Override
        public void onPageScrolled(final int i, float v, int i2) {
            checkPictureSelected(i);
            checkOriginPictureSelected();

            mPictureSelectedCb.setChecked(mSelectMap.get(i));
        }

        @Override
        public void onPageSelected(final int i) {
            Log.i(TAG, "onPageSelected !");
            if (mFromChatActivity) {
                mMsg = mConv.getMessage(mMsgIDList.get(i));
                ImageContent ic = (ImageContent) mMsg.getContent();
                //每次选择或滑动图片，如果不存在本地图片则下载，显示大图
                if (ic.getLocalPath() == null) {
//                    mLoadBtn.setVisibility(View.VISIBLE);
                    downloadImage();
                } else if(ic.getBooleanExtra("hasDownloaded") != null && !ic.getBooleanExtra("hasDownloaded")){
                    mLoadBtn.setVisibility(View.VISIBLE);
                }
            } else {
                mNumberTv.setText(i + 1 + "/" + mPathList.size());
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    /**
     * 初始化会话中的所有图片路径
     */
    private void initImgPathList() {
        List<Message> msgList = mConv.getAllMessage();
        for (int i = 0; i < msgList.size(); i++) {
            Message msg = msgList.get(i);
            if (msg.getContentType().equals(ContentType.image)) {
                ImageContent ic = (ImageContent) msg.getContent();
                if (msg.getDirect().equals(MessageDirect.send))
                    mPathList.add(ic.getLocalPath());
                else if (ic.getLocalPath() != null) {
                    mPathList.add(ic.getLocalPath());
                } else mPathList.add(ic.getLocalThumbnailPath());
                mMsgIDList.add(msg.getId());
            }
        }
    }

    private OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.return_btn:
                    int pathArray[] = new int[mPathList.size()];
                    for (int i = 0; i < pathArray.length; i++)
                        pathArray[i] = 0;
                    for (int j = 0; j < mSelectMap.size(); j++) {
                        pathArray[mSelectMap.keyAt(j)] = 1;
                    }
                    Intent intent = new Intent();
                    intent.putExtra("pathArray", pathArray);
                    setResult(JPushDemoApplication.RESULTCODE_SELECT_PICTURE, intent);
                    finish();
                    break;
                case R.id.pick_picture_send_btn:
                    mProgressDialog = new ProgressDialog(mContext);
                    mProgressDialog.setMessage(mContext.getString(R.string.sending_hint));
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();
                    mPosition = mViewPager.getCurrentItem();
//                    pathList.add(mPathList.get(mPosition));

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final List<String> pathList = new ArrayList<String>();
                            if (mOriginPictureCb.isChecked()) {
                                Log.i(TAG, "发送原图");
                                mPictureSelectedCb.setChecked(true);
                                getOriginPictures(pathList, mPosition);
                            } else {
                                Log.i(TAG, "发送缩略图");
                                getThumbnailPictures(pathList, mPosition);
                            }
                            handler.sendEmptyMessage(5);
                        }
                    });
                    thread.start();
                    break;
                //点击显示原图按钮，下载原图
                case R.id.load_image_btn:
                    downloadOriginalPicture();
                    break;
            }
        }
    };

    private void downloadOriginalPicture() {
        final ImageContent imgContent = (ImageContent) mMsg.getContent();
        //如果不存在下载进度
        if (!mMsg.isContentDownloadProgressCallbackExists()) {
            mMsg.setOnContentDownloadProgressCallback(new ProgressUpdateCallback() {
                @Override
                public void onProgressUpdate(double progress) {
                    android.os.Message msg = handler.obtainMessage();
                    Bundle bundle = new Bundle();
                    if (progress < 1.0) {
                        msg.what = 6;
                        bundle.putInt("progress", (int) (progress * 100));
                        msg.setData(bundle);
                        msg.sendToTarget();
                    } else {
                        msg.what = 7;
                        msg.sendToTarget();
                    }
                }
            });
            imgContent.downloadOriginImage(mMsg, new DownloadCompletionCallback() {
                @Override
                public void onComplete(int status, String desc, File file) {
                    if(status == 0){
                        imgContent.setBooleanExtra("hasDownloaded", true);
                    }else{
                        imgContent.setBooleanExtra("hasDownloaded", false);
                        android.os.Message msg = handler.obtainMessage();
                        msg.what = 4;
                        Bundle bundle = new Bundle();
                        bundle.putInt("status", status);
                        msg.setData(bundle);
                        msg.sendToTarget();
                    }
                }
            });
        }
    }

    private void createSendMsg(List<String> pathList) {
        mMsgIDs = new int[pathList.size()];
        for (int i = 0; i < pathList.size(); i++) {
            try {
                File file = new File(pathList.get(i));
                ImageContent content = new ImageContent(file);
                Message msg = mConv.createSendMessage(content);
                mMsgIDs[i] = msg.getId();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获得选中图片的原图路径
     *
     * @param pathList 选中图片的原图路径
     * @param position 选中的图片位置
     */
    private void getOriginPictures(List<String> pathList, int position) {
        for (int i = 0; i < mSelectMap.size(); i++) {
            pathList.add(mPathList.get(mSelectMap.keyAt(i)));
        }
        if (pathList.size() < 1)
            pathList.add(mPathList.get(position));

//        createSendMsg(pathList);
        mMsgIDs = new int[pathList.size()];
        for (int i = 0; i < pathList.size(); i++){
            try {
                File file = new File(pathList.get(i));
                ImageContent content = new ImageContent(file);
                content.setBooleanExtra("originalPicture", true);
                Message msg = mConv.createSendMessage(content);
                mMsgIDs[i] = msg.getId();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获得选中图片的缩略图路径
     *
     * @param pathList 选中图片的缩略图路径
     * @param position 选中的图片位置
     */
    private void getThumbnailPictures(List<String> pathList, int position) {
        String tempPath;
        Bitmap bitmap;
        if (mSelectMap.size() < 1)
            mSelectMap.put(position, true);
        for (int i = 0; i < mSelectMap.size(); i++) {
            //验证图片大小，若小于720 * 1280则直接发送原图，否则压缩
            if (BitmapLoader.verifyPictureSize(mPathList.get(mSelectMap.keyAt(i))))
                pathList.add(mPathList.get(mSelectMap.keyAt(i)));
            else {
                bitmap = BitmapLoader.getBitmapFromFile(mPathList.get(mSelectMap.keyAt(i)), 720, 1280);
                tempPath = BitmapLoader.saveBitmapToLocal(bitmap);
                pathList.add(tempPath);
            }
        }
        createSendMsg(pathList);
    }

    @Override
    protected void onDestroy() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mDownloading) {
            mProgressDialog.dismiss();
            //TODO cancel download image
        }
        int pathArray[] = new int[mPathList.size()];
        for (int i = 0; i < pathArray.length; i++)
            pathArray[i] = 0;
        for (int i = 0; i < mSelectMap.size(); i++) {
            pathArray[mSelectMap.keyAt(i)] = 1;
        }
        Intent intent = new Intent();
        intent.putExtra("pathArray", pathArray);
        setResult(JPushDemoApplication.RESULTCODE_SELECT_PICTURE, intent);
        super.onBackPressed();
    }

    //每次在聊天界面点击图片或者滑动图片自动下载大图
    private void downloadImage() {
        ImageContent imgContent = (ImageContent) mMsg.getContent();
        if(imgContent.getLocalPath() == null){
            //如果不存在进度条Callback，重新注册
            if (!mMsg.isContentDownloadProgressCallbackExists()) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setMessage(mContext.getString(R.string.downloading_hint));
                mDownloading = true;
                mProgressDialog.show();
                // 显示下载进度条
                mMsg.setOnContentDownloadProgressCallback(new ProgressUpdateCallback() {

                    @Override
                    public void onProgressUpdate(double progress) {
                        android.os.Message msg = handler.obtainMessage();
                        Bundle bundle = new Bundle();
                        if (progress < 1.0) {
                            msg.what = 2;
                            bundle.putInt("progress", (int) (progress * 100));
                            msg.setData(bundle);
                            msg.sendToTarget();
                        } else {
                            msg.what = 3;
                            msg.sendToTarget();
                        }
                    }
                });
                // msg.setContent(imgContent);
                imgContent.downloadOriginImage(mMsg,
                        new DownloadCompletionCallback() {
                            @Override
                            public void onComplete(int status, String desc, File file) {
                                mDownloading = false;
                                if (status == 0) {
                                    android.os.Message msg = handler.obtainMessage();
                                    msg.what = 1;
                                    Bundle bundle = new Bundle();
                                    bundle.putString("path", file.getAbsolutePath());
                                    bundle.putInt("position",
                                            mViewPager.getCurrentItem());
                                    msg.setData(bundle);
                                    msg.sendToTarget();
                                } else {
                                    android.os.Message msg = handler.obtainMessage();
                                    msg.what = 4;
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("status", status);
                                    msg.setData(bundle);
                                    msg.sendToTarget();
                                }
                            }
                        });
            }
        }
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    //更新图片并显示
                    Bundle bundle = msg.getData();
                    mPathList.set(bundle.getInt("position"), bundle.getString("path"));
                    mViewPager.getAdapter().notifyDataSetChanged();
                    mLoadBtn.setVisibility(View.GONE);
                    break;
                case 2:
                    mProgressDialog.setProgress(msg.getData().getInt("progress"));
                    break;
                case 3:
                    mProgressDialog.dismiss();
                    break;
                case 4:
                    if(mProgressDialog != null){
                        mProgressDialog.dismiss();
                    }
                    HandleResponseCode.onHandle(mContext, msg.getData().getInt("status"), false);
                    break;
                case 5:
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("sendPicture", true);
                    intent.putExtra("targetID", mTargetID);
                    intent.putExtra("isGroup", mIsGroup);
                    intent.putExtra("groupID", mGroupID);
                    intent.putExtra("msgIDs", mMsgIDs);
                    intent.setClass(BrowserViewPagerActivity.this, ChatActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                //显示下载原图进度
                case 6:
                    mLoadBtn.setText(msg.getData().getInt("progress") + "%");
                    break;
                case 7:
                    mLoadBtn.setText(mContext.getString(R.string.download_completed_toast));
                    final Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mLoadBtn.setVisibility(View.GONE);
                        }
                    }, 1000);
                    break;
            }
        }

    };

}
