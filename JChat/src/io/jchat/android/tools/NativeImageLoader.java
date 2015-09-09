package io.jchat.android.tools;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.text.TextUtils;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;

/**
 * 本地图片加载器,采用的是异步解析本地图片，单例模式利用getInstance()获取NativeImageLoader实例
 * 调用loadNativeImage()方法加载本地图片，此类可作为一个加载本地图片的工具类
 */
public class NativeImageLoader {
    private LruCache<String, Bitmap> mMemoryCache;
    private static NativeImageLoader mInstance = new NativeImageLoader();
    private ExecutorService mImageThreadPool = Executors.newFixedThreadPool(1);
    private static final int CACHE_AVATAR = 100;
    private final MyHandler myHandler = new MyHandler(this);

    private NativeImageLoader() {
        //获取应用程序的最大内存
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        //用最大内存的1/4来存储图片
        final int cacheSize = maxMemory / 4;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

            //获取每张图片的大小
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };
    }

    /**
     * 初始化用户头像缓存
     *
     * @param userIDList 用户ID List
     * @param length     头像宽高
     * @param callBack   缓存回调
     */
    public void setAvatarCache(final List<String> userIDList, final int length, final CacheAvatarCallBack callBack) {
        if(null == userIDList){
            return;
        }

        UserInfo myInfo = JMessageClient.getMyInfo();
        for (final String userID : userIDList) {
            //若为CurrentUser，直接获取本地的头像（CurrentUser本地头像为最新）
            if (userID.equals(myInfo.getUserName())) {
                //如果有设置头像
                if(!TextUtils.isEmpty(myInfo.getAvatar())){
                    File file = JMessageClient.getMyInfo().getAvatarFile();
                    if (file != null && file.isFile()){
                        Bitmap bitmap = BitmapLoader.getBitmapFromFile(file.getAbsolutePath(), length, length);
                        if (null != bitmap) {
                            mMemoryCache.put(userID, bitmap);
                        }
                    }else {
                        getUserInfo(userID, callBack, length);
                    }
                }
            } else if (mMemoryCache.get(userID) == null) {
                getUserInfo(userID, callBack, length);
            }
        }
    }

    private void getUserInfo(final String userID, final CacheAvatarCallBack callBack, final int length){
        JMessageClient.getUserInfo(userID, new GetUserInfoCallback(false) {
            @Override
            public void gotResult(int i, String s, UserInfo userInfo) {
                if (i == 0) {
                    if (!TextUtils.isEmpty(userInfo.getAvatar())){
                        File file = userInfo.getAvatarFile();
                        if (file != null) {
                            Bitmap bitmap = BitmapLoader.getBitmapFromFile(file.getAbsolutePath(), length, length);
                            addBitmapToMemoryCache(userID, bitmap);
                        }
                        android.os.Message msg = myHandler.obtainMessage();
                        msg.what = CACHE_AVATAR;
                        msg.obj = callBack;
                        Bundle bundle = new Bundle();
                        bundle.putInt("status", 0);
                        msg.setData(bundle);
                        msg.sendToTarget();
                    }
                }
            }
        });
    }

    /**
     * setAvatarCache的重载函数
     * @param userName 用户名
     * @param length 图片宽高
     * @param callBack 回调
     */
    public void setAvatarCache(final String userName, final int length, final CacheAvatarCallBack callBack) {
        getUserInfo(userName, callBack, length);
    }

    /**
     * 此方法直接在LruCache中增加一个键值对
     *
     * @param targetID 用户名
     * @param path     头像路径
     */
    public void putUserAvatar(String targetID, String path, int size) {
        if (path != null) {
            Bitmap bitmap = BitmapLoader.getBitmapFromFile(path, size, size);
            if (bitmap != null) {
                addBitmapToMemoryCache(targetID, bitmap);
            }
        }
    }

    /**
     * 通过此方法来获取NativeImageLoader的实例
     *
     * @return NativeImageLoader
     */
    public static NativeImageLoader getInstance() {
        return mInstance;
    }


    /**
     * 此方法来加载本地图片，我们会根据length来裁剪Bitmap
     *
     * @param path 图片路径
     * @param length 图片宽高
     * @param callBack 回调
     * @return bitmap
     */
    public Bitmap loadNativeImage(final String path, final int length, final NativeImageCallBack callBack) {
        //先获取内存中的Bitmap
        Bitmap bitmap = getBitmapFromMemCache(path);

        final Handler handler = new Handler() {

            @Override
            public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
                callBack.onImageLoader((Bitmap) msg.obj, path);
            }

        };

        //若该Bitmap不在内存缓存中，则启用线程去加载本地的图片，并将Bitmap加入到mMemoryCache中
        if (bitmap == null) {
            mImageThreadPool.execute(new Runnable() {

                @Override
                public void run() {
                    //先获取图片的缩略图
                    Bitmap mBitmap = decodeThumbBitmapForFile(path, length, length);
                    Message msg = handler.obtainMessage();
                    msg.obj = mBitmap;
                    handler.sendMessage(msg);

                    //将图片加入到内存缓存
                    addBitmapToMemoryCache(path, mBitmap);
                }
            });
        }
        return bitmap;

    }

    /**
     * 此方法来加载本地图片，这里的mPoint是用来封装ImageView的宽和高，我们会根据ImageView控件的大小来裁剪Bitmap
     *
     * @param path 路径
     * @param point ImageView的Point
     * @param mCallBack 回调
     * @return bitmap
     */
    public Bitmap loadNativeImage(final String path, final Point point, final NativeImageCallBack mCallBack) {
        //先获取内存中的Bitmap
        Bitmap bitmap = getBitmapFromMemCache(path);

        final Handler mHander = new Handler() {

            @Override
            public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
                mCallBack.onImageLoader((Bitmap) msg.obj, path);
            }

        };

        //若该Bitmap不在内存缓存中，则启用线程去加载本地的图片，并将Bitmap加入到mMemoryCache中
        if (bitmap == null) {
            mImageThreadPool.execute(new Runnable() {

                @Override
                public void run() {
                    //先获取图片的缩略图
                    Bitmap mBitmap = decodeThumbBitmapForFile(path, point.x == 0 ? 0 : point.x, point.y == 0 ? 0 : point.y);
                    Message msg = mHander.obtainMessage();
                    msg.obj = mBitmap;
                    mHander.sendMessage(msg);

                    //将图片加入到内存缓存
                    addBitmapToMemoryCache(path, mBitmap);
                }
            });
        }
        return bitmap;

    }


    /**
     * 往内存缓存中添加Bitmap
     *
     * @param key UserName
     * @param bitmap bitmap
     */
    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null && bitmap != null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public void updateBitmapFromCache(String key, Bitmap bitmap) {
        if (null != bitmap) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public void releaseCache() {
        mMemoryCache.evictAll();
    }

    /**
     * 根据key来获取内存中的图片
     *
     * @param key Username
     * @return bitmap
     */
    public Bitmap getBitmapFromMemCache(String key) {
        if (key == null) {
            return null;
        } else {
            return mMemoryCache.get(key);
        }
    }


    /**
     * 根据View(主要是ImageView)的宽和高来获取图片的缩略图
     *
     * @param path 路径
     * @param viewWidth 宽
     * @param viewHeight 高
     * @return bitmap
     */
    private Bitmap decodeThumbBitmapForFile(String path, int viewWidth, int viewHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //设置为true,表示解析Bitmap对象，该对象不占内存
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        //设置缩放比例
        options.inSampleSize = calculateInSampleSize(options, viewWidth, viewHeight);

        //设置为false,解析Bitmap对象加入到内存中
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }


    /**
     * 计算压缩比例值
     *
     * @param options   解析图片的配置信息
     * @param reqWidth  所需图片压缩尺寸最小宽度
     * @param reqHeight 所需图片压缩尺寸最小高度
     * @return 压缩比例
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // 保存图片原宽高值
        final int height = options.outHeight;
        final int width = options.outWidth;

        // 初始化压缩比例为1
        int inSampleSize = 1;

        // 当图片宽高值任何一个大于所需压缩图片宽高值时,进入循环计算系统
        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // 压缩比例值每次循环两倍增加,
            // 直到原图宽高值的一半除以压缩值后都~大于所需宽高值为止
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    /**
     * 加载本地图片的回调接口
     *
     * @author xiaanming
     */
    public interface NativeImageCallBack {
        /**
         * 当子线程加载完了本地的图片，将Bitmap和图片路径回调在此方法中
         *
         * @param bitmap bitmap
         * @param path 路径
         */
        void onImageLoader(Bitmap bitmap, String path);
    }

    public interface CacheAvatarCallBack {
        void onCacheAvatarCallBack(int status);
    }

    private static class MyHandler extends Handler{
        private final WeakReference<NativeImageLoader> mLoader;
        public MyHandler(NativeImageLoader loader){
            mLoader = new WeakReference<NativeImageLoader>(loader);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            NativeImageLoader loader = mLoader.get();
            if(loader != null){
                switch (msg.what){
                    case CACHE_AVATAR:
                        CacheAvatarCallBack callBack = (CacheAvatarCallBack) msg.obj;
                        callBack.onCacheAvatarCallBack(msg.getData().getInt("status", -1));
                        break;
                }
            }
        }
    }
}
