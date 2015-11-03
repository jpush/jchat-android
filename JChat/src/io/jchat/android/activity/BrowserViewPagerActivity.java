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
import android.text.TextUtils;
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
import com.squareup.picasso.Picasso;
import java.io.File;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.callback.ProgressUpdateCallback;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import io.jchat.android.R;
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
    private List<Integer> mMsgIdList = new ArrayList<Integer>();
    private TextView mNumberTv;
    private Button mSendBtn;
    private CheckBox mOriginPictureCb;
    private TextView mTotalSizeTv;
    private CheckBox mPictureSelectedCb;
    private Button mLoadBtn;
    private int mPosition;
    private Conversation mConv;
    private Message mMsg;
    private String mTargetId;
    private boolean mFromChatActivity = true;
    //当前消息数
    private int mStart;
    private int mOffset = 18;
    private Context mContext;
    private boolean mDownloading = false;
    private Long mGroupId;
    private int[] mMsgIds;
    private int mIndex = 0;
    private Queue<String> mPathQueue = new LinkedList<String>();
    private final MyHandler myHandler = new MyHandler(this);
    private final static int DOWNLOAD_ORIGIN_IMAGE_SUCCEED = 1;
    private final static int DOWNLOAD_PROGRESS = 2;
    private final static int DOWNLOAD_COMPLETED = 3;
    private final static int SEND_PICTURE = 5;
    private final static int DOWNLOAD_ORIGIN_PROGRESS = 6;
    private final static int DOWNLOAD_ORIGIN_COMPLETED = 7;

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

        final Intent intent = this.getIntent();
        mGroupId = intent.getLongExtra(JPushDemoApplication.GROUP_ID, 0);
        if (mGroupId != 0){
            mConv = JMessageClient.getGroupConversation(mGroupId);
        }else {
            mTargetId = intent.getStringExtra(JPushDemoApplication.TARGET_ID);
            if (mTargetId != null){
                mConv = JMessageClient.getSingleConversation(mTargetId);
            }
        }
        mStart = intent.getIntExtra("msgCount", 0);
        mPosition = intent.getIntExtra(JPushDemoApplication.POSITION, 0);
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
                if (bitmap != null){
                    photoView.setImageBitmap(bitmap);
                } else {
                    photoView.setImageResource(R.drawable.friends_sends_pictures_no);
                }
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
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

        };
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setOnPageChangeListener(onPageChangeListener);
        returnBtn.setOnClickListener(listener);
        mSendBtn.setOnClickListener(listener);
        mLoadBtn.setOnClickListener(listener);

        // 在聊天界面中点击图片
        if (mFromChatActivity) {
            titleBarRl.setVisibility(View.GONE);
            checkBoxRl.setVisibility(View.GONE);
            if(mViewPager != null && mViewPager.getAdapter() != null){
                mViewPager.getAdapter().notifyDataSetChanged();
            }
            //预览头像
            if (browserAvatar) {
                mPathList.add(intent.getStringExtra("avatarPath"));
                photoView = new PhotoView(mFromChatActivity, mContext);
                mLoadBtn.setVisibility(View.GONE);
                try {
                    Picasso.with(mContext).load(new File(mPathList.get(0))).into(photoView);
                } catch (Exception e) {
                    photoView.setImageResource(R.drawable.friends_sends_pictures_no);
                    if (mPathList.get(0) == null){
                        HandleResponseCode.onHandle(mContext, 1001, false);
                    }
                }
            //预览聊天界面中的图片
            } else {
                initImgPathList();
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    Toast.makeText(this, this.getString(R.string.local_picture_not_found_toast), Toast.LENGTH_SHORT).show();
                }
                mMsg = mConv.getMessage(intent.getIntExtra("msgId", 0));
                photoView = new PhotoView(mFromChatActivity, this);
                int currentItem = mMsgIdList.indexOf(mMsg.getId());
                try {
                    ImageContent ic = (ImageContent) mMsg.getContent();
                    //如果点击的是第一张图片并且图片未下载过，则显示大图
                    if (ic.getLocalPath() == null && mMsgIdList.indexOf(mMsg.getId()) == 0) {
                        downloadImage();
                    }
                    String path = mPathList.get(mMsgIdList.indexOf(mMsg.getId()));
                    //如果发送方上传了原图
                    if(ic.getBooleanExtra("originalPicture") != null && ic.getBooleanExtra("originalPicture")){
                        mLoadBtn.setVisibility(View.GONE);
                        setLoadBtnText(ic);
                        photoView.setImageBitmap(BitmapLoader.getBitmapFromFile(path, mWidth, mHeight));
                    }else {
                        Picasso.with(mContext).load(new File(path)).into(photoView);
                    }

                    mViewPager.setCurrentItem(currentItem);
                } catch (NullPointerException e) {
                    photoView.setImageResource(R.drawable.friends_sends_pictures_no);
                    mViewPager.setCurrentItem(currentItem);
                }finally {
                    if (currentItem == 0){
                        getImgMsg();
                    }
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
            String numberText = mPosition + 1 + "/" + mPathList.size();
            mNumberTv.setText(numberText);
            int currentItem = mViewPager.getCurrentItem();
            checkPictureSelected(currentItem);
            checkOriginPictureSelected();
            //第一张特殊处理
            mPictureSelectedCb.setChecked(mSelectMap.get(currentItem));
            showTotalSize();
        }
    }

    private void setLoadBtnText(ImageContent ic) {
        NumberFormat ddf1 = NumberFormat.getNumberInstance();
        //保留小数点后两位
        ddf1.setMaximumFractionDigits(2);
        double size = ic.getFileSize() / 1048576.0;
        String loadText = mContext.getString(R.string.load_origin_image) + "(" + ddf1.format(size) + "M" + ")";
        mLoadBtn.setText(loadText);
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
            String totalText = mContext.getString(R.string.origin_picture)
                    + String.format(mContext.getString(R.string.combine_title), totalSize);
            mTotalSizeTv.setText(totalText);
        } else mTotalSizeTv.setText(mContext.getString(R.string.origin_picture));
    }

    //显示选中了多少张图片
    private void showSelectedNum() {
        if (mSelectMap.size() > 0) {
            String sendText = mContext.getString(R.string.send) + "(" + mSelectMap.size() + "/" + "9)";
            mSendBtn.setText(sendText);
        } else mSendBtn.setText(mContext.getString(R.string.send));
    }

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        //在滑动的时候更新CheckBox的状态
        @Override
        public void onPageScrolled(final int i, float v, int i2) {
            checkPictureSelected(i);
            checkOriginPictureSelected();
            mPictureSelectedCb.setChecked(mSelectMap.get(i));
        }

        @Override
        public void onPageSelected(final int i) {
            Log.d(TAG, "onPageSelected current position: " + i);
            if (mFromChatActivity) {
                mMsg = mConv.getMessage(mMsgIdList.get(i));
                Log.d(TAG, "onPageSelected Image Message ID: " + mMsg.getId());
                ImageContent ic = (ImageContent) mMsg.getContent();
                //每次选择或滑动图片，如果不存在本地图片则下载，显示大图
                if (ic.getLocalPath() == null && i != mPosition) {
//                    mLoadBtn.setVisibility(View.VISIBLE);
                    downloadImage();
                } else if(ic.getBooleanExtra("hasDownloaded") != null && !ic.getBooleanExtra("hasDownloaded")){
                    setLoadBtnText(ic);
                    mLoadBtn.setVisibility(View.GONE);
                }else {
                    mLoadBtn.setVisibility(View.GONE);
                }
                if (i == 0){
                    getImgMsg();
                }
            } else {
                String numText = i + 1 + "/" + mPathList.size();
                mNumberTv.setText(numText);
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    /**
     * 滑动到第一张时，加载上一页消息中的图片
     */
    private void getImgMsg() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ImageContent ic;
                final int msgSize = mMsgIdList.size();
                List<Message> msgList = mConv.getMessagesFromNewest(mStart, mOffset);
                mOffset = msgList.size();
                if (mOffset > 0){
                    for (Message msg : msgList){
                        if (msg.getContentType().equals(ContentType.image)){
                            mMsgIdList.add(0, msg.getId());
                            ic = (ImageContent) msg.getContent();
                            if (!TextUtils.isEmpty(ic.getLocalPath())) {
                                mPathList.add(0, ic.getLocalPath());
                            }else {
                                mPathList.add(0, ic.getLocalThumbnailPath());
                            }
                        }
                    }
                    mStart += mOffset;
                    if (msgSize == mMsgIdList.size()){
                        getImgMsg();
                    }else {
                        //加载完上一页图片后，设置当前图片仍为加载前的那一张图片
                        BrowserViewPagerActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mPosition = mMsgIdList.size() - msgSize;
                                mViewPager.setCurrentItem(mPosition);
                                mViewPager.getAdapter().notifyDataSetChanged();
                            }
                        });
                    }
                }
            }
        });
        thread.start();
    }

    /**
     * 初始化会话中的所有图片路径
     */
    private void initImgPathList() {
        mMsgIdList = this.getIntent().getIntegerArrayListExtra(JPushDemoApplication.MsgIDs);
        Message msg;
        ImageContent ic;
        for (int msgID : mMsgIdList){
            msg = mConv.getMessage(msgID);
            if (msg.getContentType().equals(ContentType.image)) {
                ic = (ImageContent) msg.getContent();
                if (!TextUtils.isEmpty(ic.getLocalPath())){
                    mPathList.add(ic.getLocalPath());
                }else {
                    mPathList.add(ic.getLocalThumbnailPath());
                }
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
                    setResult(JPushDemoApplication.RESULT_CODE_SELECT_PICTURE, intent);
                    finish();
                    break;
                case R.id.pick_picture_send_btn:
                    mProgressDialog = new ProgressDialog(mContext);
                    mProgressDialog.setMessage(mContext.getString(R.string.sending_hint));
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();
                    mPosition = mViewPager.getCurrentItem();

                    if (mOriginPictureCb.isChecked()) {
                        Log.i(TAG, "发送原图");
                        getOriginPictures(mPosition);
                    } else {
                        Log.i(TAG, "发送缩略图");
                        getThumbnailPictures(mPosition);
                    }
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
                    android.os.Message msg = myHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    if (progress < 1.0) {
                        msg.what = DOWNLOAD_ORIGIN_PROGRESS;
                        bundle.putInt("progress", (int) (progress * 100));
                        msg.setData(bundle);
                        msg.sendToTarget();
                    } else {
                        msg.what = DOWNLOAD_ORIGIN_COMPLETED;
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
                        if (mProgressDialog != null){
                            mProgressDialog.dismiss();
                        }
                        HandleResponseCode.onHandle(mContext, status, false);
                    }
                }
            });
        }
    }


    /**
     * 获得选中图片的原图路径
     *
     * @param position 选中的图片位置
     */
    private void getOriginPictures(int position) {
        if (mSelectMap.size() < 1)
            mSelectMap.put(position, true);
        mMsgIds = new int[mSelectMap.size()];
        //根据选择的图片路径生成队列
        for (int i = 0; i < mSelectMap.size(); i++) {
            mPathQueue.offer(mPathList.get(mSelectMap.keyAt(i)));
        }

        //从队列中取出第一个元素，生成ImageContent
        String path = mPathQueue.element();
        createNextImgContent(path, true);
    }

    /**
     * 获得选中图片的缩略图路径
     *
     * @param position 选中的图片位置
     */
    private void getThumbnailPictures(int position) {
        if (mSelectMap.size() < 1)
            mSelectMap.put(position, true);
        mMsgIds = new int[mSelectMap.size()];
        for (int i = 0; i < mSelectMap.size(); i++) {
            mPathQueue.offer(mPathList.get(mSelectMap.keyAt(i)));
        }

        String path = mPathQueue.element();
        createNextImgContent(path, false);
    }

    /**
     * 根据图片路径创建ImageContent
     * @param path 图片路径
     * @param isOriginal 是否发送原图
     */
    private void createNextImgContent(final String path, final boolean isOriginal) {
        Bitmap bitmap;
        //若isOriginal为true或者图片大小小于720 * 1280则直接发送原图，否则压缩
        if (isOriginal || BitmapLoader.verifyPictureSize(path)) {
            File file = new File(path);
            ImageContent.createImageContentAsync(file, new ImageContent.CreateImageContentCallback() {
                @Override
                public void gotResult(int status, String desc, ImageContent imageContent) {
                    if (status == 0) {
                        if (isOriginal){
                            imageContent.setBooleanExtra("originalPicture", true);
                        }
                        Message msg = mConv.createSendMessage(imageContent);
                        mMsgIds[mIndex] = msg.getId();
                        //自增索引，出列
                        mIndex++;
                        mPathQueue.poll();
                        //如果队列不为空， 继续创建下一个ImageContent
                        if (!mPathQueue.isEmpty()) {
                            createNextImgContent(mPathQueue.element(), isOriginal);
                        //否则，队列中所有元素创建完毕，通知Handler
                        } else {
                            myHandler.sendEmptyMessage(SEND_PICTURE);
                        }
                    } else {
                        HandleResponseCode.onHandle(mContext, status, false);
                    }
                }
            });
        } else {
            bitmap = BitmapLoader.getBitmapFromFile(path, 720, 1280);
            ImageContent.createImageContentAsync(bitmap, new ImageContent.CreateImageContentCallback() {
                @Override
                public void gotResult(int status, String desc, ImageContent imageContent) {
                    if (status == 0){
                        Message msg = mConv.createSendMessage(imageContent);
                        mMsgIds[mIndex] = msg.getId();
                        mIndex++;
                        mPathQueue.poll();
                        if (!mPathQueue.isEmpty()) {
                            createNextImgContent(mPathQueue.element(), false);
                        } else {
                            myHandler.sendEmptyMessage(SEND_PICTURE);
                        }
                    }else {
                        HandleResponseCode.onHandle(mContext, status, false);
                    }
                }
            });
        }
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
        setResult(JPushDemoApplication.RESULT_CODE_SELECT_PICTURE, intent);
        super.onBackPressed();
    }

    //每次在聊天界面点击图片或者滑动图片自动下载大图
    private void downloadImage() {
        Log.d(TAG, "Downloading image!");
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
                        android.os.Message msg = myHandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        if (progress < 1.0) {
                            msg.what = DOWNLOAD_PROGRESS;
                            bundle.putInt("progress", (int) (progress * 100));
                            msg.setData(bundle);
                            msg.sendToTarget();
                        } else {
                            msg.what = DOWNLOAD_COMPLETED;
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
                                    android.os.Message msg = myHandler.obtainMessage();
                                    msg.what = DOWNLOAD_ORIGIN_IMAGE_SUCCEED;
                                    Bundle bundle = new Bundle();
                                    bundle.putString("path", file.getAbsolutePath());
                                    bundle.putInt(JPushDemoApplication.POSITION,
                                            mViewPager.getCurrentItem());
                                    msg.setData(bundle);
                                    msg.sendToTarget();
                                } else {
                                    if (mProgressDialog != null) {
                                        mProgressDialog.dismiss();
                                    }
                                    HandleResponseCode.onHandle(mContext, status, false);
                                }
                            }
                        });
            }
        }
    }

    private static class MyHandler extends Handler{
        private final WeakReference<BrowserViewPagerActivity> mActivity;

        public MyHandler(BrowserViewPagerActivity activity){
            mActivity = new WeakReference<BrowserViewPagerActivity>(activity);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            BrowserViewPagerActivity activity = mActivity.get();
            if(activity != null){
                switch (msg.what) {
                    case DOWNLOAD_ORIGIN_IMAGE_SUCCEED:
                        //更新图片并显示
                        Bundle bundle = msg.getData();
                        activity.mPathList.set(bundle.getInt(JPushDemoApplication.POSITION), bundle.getString("path"));
                        activity.mViewPager.getAdapter().notifyDataSetChanged();
                        activity.mLoadBtn.setVisibility(View.GONE);
                        break;
                    case DOWNLOAD_PROGRESS:
                        activity.mProgressDialog.setProgress(msg.getData().getInt("progress"));
                        break;
                    case DOWNLOAD_COMPLETED:
                        activity.mProgressDialog.dismiss();
                        break;
                    case SEND_PICTURE:
                        Intent intent = new Intent();
                        intent.putExtra(JPushDemoApplication.TARGET_ID, activity.mTargetId);
                        intent.putExtra(JPushDemoApplication.GROUP_ID, activity.mGroupId);
                        intent.putExtra(JPushDemoApplication.MsgIDs, activity.mMsgIds);
                        activity.setResult(JPushDemoApplication.RESULT_CODE_BROWSER_PICTURE, intent);
                        activity.finish();
                        break;
                    //显示下载原图进度
                    case DOWNLOAD_ORIGIN_PROGRESS:
                        String progress = msg.getData().getInt("progress") + "%";
                        activity.mLoadBtn.setText(progress);
                        break;
                    case DOWNLOAD_ORIGIN_COMPLETED:
                        activity.mLoadBtn.setText(activity.getString(R.string.download_completed_toast));
                        activity.mLoadBtn.setVisibility(View.GONE);
                        break;
                }
            }
        }
    }

}
