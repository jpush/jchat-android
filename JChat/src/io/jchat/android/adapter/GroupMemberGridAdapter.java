package io.jchat.android.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.DownloadAvatarBitmapCallback;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import io.jchat.android.R;
import io.jchat.android.tools.HandleResponseCode;
import io.jchat.android.view.CircleImageView;

public class GroupMemberGridAdapter extends BaseAdapter {

    private static final String TAG = "GroupMemberGridAdapter";

    private LayoutInflater mInflater;
    //群成员列表
    private List<UserInfo> mMemberList = new ArrayList<UserInfo>();
    private boolean mIsCreator = false;
    private boolean mIsShowDelete;
    //群成员个数
    private int mCurrentNum;
    //记录空白项的数组
    private int[] mRestArray = new int[]{2, 1, 0, 3};
    //用群成员项数余4得到，作为下标查找mRestArray，得到空白项
    private int mRestNum;
    private int mAvatarSize;
    private boolean mIsGroup;
    private String mTargetID;
    private Context mContext;

    //群聊
    public GroupMemberGridAdapter(Context context, List<UserInfo> memberList, boolean isCreator, int size) {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        mIsGroup = true;
        this.mMemberList = memberList;
        mCurrentNum = mMemberList.size();
        this.mIsCreator = isCreator;
        this.mAvatarSize = size;
        mIsShowDelete = false;
        initBlankItem();
    }

    //单聊
    public GroupMemberGridAdapter(Context context, String targetID, int size) {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        this.mTargetID = targetID;
        this.mAvatarSize = size;
    }

    public void initBlankItem() {
        mCurrentNum = mMemberList.size();
        mRestNum = mRestArray[mCurrentNum % 4];
    }

    public void refreshMemberList(long groupID){
        Conversation conv = JMessageClient.getGroupConversation(groupID);
        GroupInfo groupInfo = (GroupInfo)conv.getTargetInfo();
        mMemberList = groupInfo.getGroupMembers();
        mCurrentNum = mMemberList.size();
        mRestNum = mRestArray[mCurrentNum % 4];
        notifyDataSetChanged();
    }

    public void setIsShowDelete(boolean isShowDelete) {
        this.mIsShowDelete = isShowDelete;
        notifyDataSetChanged();
    }

    public void setIsShowDelete(boolean isShowDelete, int restNum) {
        this.mIsShowDelete = isShowDelete;
        mRestNum = restNum;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        //如果是普通成员，并且群组成员余4等于3，特殊处理，隐藏下面一栏空白
        if (mCurrentNum % 4 == 3 && !mIsCreator)
            return mCurrentNum + 1;
        else return mCurrentNum + mRestNum + 2;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ItemViewTag viewTag;
        Bitmap bitmap;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.group_grid_view_item, null);
            viewTag = new ItemViewTag((CircleImageView) convertView.findViewById(R.id.grid_avatar),
                    (TextView) convertView.findViewById(R.id.grid_name),
                    (ImageView) convertView.findViewById(R.id.grid_delete_icon));
            convertView.setTag(viewTag);
        } else {
            viewTag = (ItemViewTag) convertView.getTag();
        }
        //群聊
        if (mIsGroup) {
            //群成员
            if (position < mMemberList.size()) {
                UserInfo userInfo = mMemberList.get(position);
                viewTag.icon.setVisibility(View.VISIBLE);
                viewTag.name.setVisibility(View.VISIBLE);
                bitmap = userInfo.getSmallAvatarBitmap();
                if (bitmap != null)
                    viewTag.icon.setImageBitmap(bitmap);
                //加载头像
                else {
                    Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(),
                            R.drawable.head_icon);
                    loadMemberAvatar(position, bmp, viewTag.icon);
                }

                if (TextUtils.isEmpty(userInfo.getNickname())) {
                    viewTag.name.setText(userInfo.getUserName());
                } else {
                    viewTag.name.setText(userInfo.getNickname());
                }
            }
            //是Delete状态
            if (mIsShowDelete) {
                if (position < mCurrentNum) {
                    UserInfo userInfo = mMemberList.get(position);
                    //群主不能删除自己
                    if (userInfo.getUserName().equals(JMessageClient.getMyInfo().getUserName()))
                        viewTag.deleteIcon.setVisibility(View.GONE);
                    else viewTag.deleteIcon.setVisibility(View.VISIBLE);

                } else {
                    viewTag.deleteIcon.setVisibility(View.INVISIBLE);
                    viewTag.icon.setVisibility(View.INVISIBLE);
                    viewTag.name.setVisibility(View.INVISIBLE);
                }
                //非Delete状态
            } else {
                viewTag.deleteIcon.setVisibility(View.INVISIBLE);
                if (position < mCurrentNum) {
                    viewTag.icon.setVisibility(View.VISIBLE);
                    viewTag.name.setVisibility(View.VISIBLE);
                } else if (position == mCurrentNum) {
                    viewTag.icon.setImageResource(R.drawable.chat_detail_add);
                    viewTag.icon.setVisibility(View.VISIBLE);
                    viewTag.name.setVisibility(View.INVISIBLE);

                    //设置删除群成员按钮
                } else if (position == mCurrentNum + 1) {
                    if (mIsCreator && mCurrentNum > 1) {
                        viewTag.icon.setImageResource(R.drawable.chat_detail_del);
                        viewTag.icon.setVisibility(View.VISIBLE);
                        viewTag.name.setVisibility(View.INVISIBLE);
                    } else {
                        viewTag.icon.setVisibility(View.GONE);
                        viewTag.name.setVisibility(View.GONE);
                    }
                    //空白项
                } else {
                    viewTag.icon.setVisibility(View.INVISIBLE);
                    viewTag.name.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            if (position == 0) {
                Conversation conv = JMessageClient.getSingleConversation(mTargetID);
                UserInfo userInfo = (UserInfo)conv.getTargetInfo();
                bitmap = userInfo.getSmallAvatarBitmap();
                if (bitmap != null) {
                    viewTag.icon.setImageBitmap(bitmap);
                } else {
                    viewTag.icon.setImageResource(R.drawable.head_icon);
                    userInfo.getSmallAvatarBitmapAsync(new DownloadAvatarBitmapCallback() {
                        @Override
                        public void gotResult(int status, String desc, Bitmap bitmap) {
                            if (status == 0) {
                                Log.d(TAG, "Get small avatar success");
                                viewTag.icon.setImageBitmap(bitmap);
                            }else {
                                HandleResponseCode.onHandle(mContext, status, false);
                            }
                        }
                    });
                }
                if (TextUtils.isEmpty(userInfo.getNickname())){
                    viewTag.name.setText(userInfo.getUserName());
                }else {
                    viewTag.name.setText(userInfo.getNickname());
                }
                viewTag.icon.setVisibility(View.VISIBLE);
                viewTag.name.setVisibility(View.VISIBLE);
            } else {
                viewTag.icon.setImageResource(R.drawable.chat_detail_add);
                viewTag.icon.setVisibility(View.VISIBLE);
                viewTag.name.setVisibility(View.INVISIBLE);
            }

        }

        return convertView;
    }

    public void setCreator(boolean isCreator) {
        mIsCreator = isCreator;
        notifyDataSetChanged();
    }

    /**
     * 启动任务加载头像
     * @param position position
     * @param bitmap 默认头像
     * @param imageView 要加载头像的ImageView对象
     */
    private void loadMemberAvatar(int position, Bitmap bitmap, ImageView imageView){
        if (cancelPotentialWork(position, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(mContext.getResources(), bitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(position);
        }
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap>{

        private final WeakReference<ImageView> imageViewReference;
        private int data;

        public BitmapWorkerTask(ImageView imageView){
            imageViewReference = new WeakReference<ImageView>(imageView);
        }


        @Override
        protected Bitmap doInBackground(Integer... params) {
            final Bitmap[] bitmap = new Bitmap[1];
            data = params[0];

            final UserInfo userInfo = mMemberList.get(data);
            //使用一个信号量，当拿到头像后执行countDown
            final CountDownLatch signal = new CountDownLatch(1);
            //睡眠0.5s
            try {
                Thread.sleep(500);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            if (!TextUtils.isEmpty(userInfo.getAvatar())){
                userInfo.getSmallAvatarBitmapAsync(new DownloadAvatarBitmapCallback() {
                    @Override
                    public void gotResult(int status, String desc, Bitmap bmp) {
                        if (status == 0){
                            bitmap[0] = bmp;
                            signal.countDown();
                        }else {
                            HandleResponseCode.onHandle(mContext, status, false);
                        }
                    }
                });
            }

            //设置30s超时
            try {
                signal.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return bitmap[0];
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            final ImageView imageView = imageViewReference.get();
            if (bitmap != null){
                final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null){
                    imageView.setImageBitmap(bitmap);
                    Log.d(TAG, "Post execute position: " + data);
                }
            }else {
                imageView.setImageResource(R.drawable.head_icon);
            }
            super.onPostExecute(bitmap);
        }
    }

    public static boolean cancelPotentialWork(int data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final int bitmapData = bitmapWorkerTask.data;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == 0 || bitmapData != data) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
                Log.d(TAG, "cancel potential work, Position: " + bitmapData);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    class ItemViewTag {

        protected CircleImageView icon;
        protected ImageView deleteIcon;
        protected TextView name;

        public ItemViewTag(CircleImageView icon, TextView name, ImageView deleteIcon) {
            this.icon = icon;
            this.deleteIcon = deleteIcon;
            this.name = name;
        }
    }
}
