package io.jchat.android.activity;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.jchat.android.R;
import io.jchat.android.adapter.AlbumListAdapter;
import io.jchat.android.entity.ImageBean;
import io.jchat.android.tools.SortPictureList;
import io.jchat.android.view.SendImageView;

public class ImageFragment extends BaseFragment {

    private Activity mContext;
    private View mRootView;
    private SendImageView mSIView;
    private AlbumListAdapter mAdapter;
    private final static int SCAN_OK = 1;
    private final static int SCAN_ERROR = 0;
    private final MyHandler myHandler = new MyHandler(this);
    private ProgressDialog mProgressDialog;
    private HashMap<String, List<String>> mGruopMap = new HashMap<String, List<String>>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getActivity();
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.fragment_send_image,
                (ViewGroup) mContext.findViewById(R.id.send_doc_view), false);
        mSIView = (SendImageView) mRootView.findViewById(R.id.send_image_view);
        mSIView.initModule();
        getImages();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        ViewGroup p = (ViewGroup) mRootView.getParent();
        if (p != null) {
            p.removeAllViewsInLayout();
        }
        return mRootView;
    }

    private void getImages() {
        //显示进度条
        mProgressDialog = ProgressDialog.show(ImageFragment.this.getContext(), null,
                mContext.getString(R.string.jmui_loading));

        new Thread(new Runnable() {

            @Override
            public void run() {
                Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver contentResolver = mContext.getContentResolver();
                String[] projection = new String[]{ MediaStore.Images.ImageColumns.DATA };
                Cursor cursor = contentResolver.query(imageUri, projection, null, null,
                        MediaStore.Images.Media.DATE_MODIFIED);
                if (cursor == null || cursor.getCount() == 0) {
                    myHandler.sendEmptyMessage(SCAN_ERROR);
                } else {
                    while (cursor.moveToNext()) {
                        //获取图片的路径
                        String path = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Images.Media.DATA));

                        try{
                            //获取该图片的父路径名
                            String parentName = new File(path).getParentFile().getName();
                            //根据父路径名将图片放入到mGruopMap中
                            if (!mGruopMap.containsKey(parentName)) {
                                List<String> chileList = new ArrayList<String>();
                                chileList.add(path);
                                mGruopMap.put(parentName, chileList);
                            } else {
                                mGruopMap.get(parentName).add(path);
                            }
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    cursor.close();
                    //通知Handler扫描图片完成
                    myHandler.sendEmptyMessage(SCAN_OK);
                }
            }
        }).start();

    }

    private static class MyHandler extends Handler {
        private final WeakReference<ImageFragment> mFragment;

        public MyHandler(ImageFragment fragment) {
            mFragment = new WeakReference<ImageFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ImageFragment fragment = mFragment.get();
            if (fragment != null) {
                switch (msg.what) {
                    case SCAN_OK:
                        //关闭进度条
                        fragment.mProgressDialog.dismiss();
                        fragment.mAdapter = new AlbumListAdapter(fragment.getActivity(), fragment
                                .subGroupOfImage(fragment.mGruopMap), fragment.mDensity);
                        fragment.mSIView.setAdapter(fragment.mAdapter);
                        break;
                    case SCAN_ERROR:
                        fragment.mProgressDialog.dismiss();
                        Toast.makeText(fragment.getActivity(), fragment.getString(R.string.sdcard_not_prepare_toast), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }

    private List<ImageBean> subGroupOfImage(HashMap<String, List<String>> mGruopMap) {
        if (mGruopMap.size() == 0) {
            return null;
        }
        List<ImageBean> list = new ArrayList<ImageBean>();

        Iterator<Map.Entry<String, List<String>>> it = mGruopMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<String>> entry = it.next();
            ImageBean mImageBean = new ImageBean();
            String key = entry.getKey();
            List<String> value = entry.getValue();
            SortPictureList sortList = new SortPictureList();
            Collections.sort(value, sortList);
            mImageBean.setFolderName(key);
            mImageBean.setImageCounts(value.size());
            mImageBean.setTopImagePath(value.get(0));//获取该组的第一张图片

            list.add(mImageBean);
        }

        //对相册进行排序，最近修改的相册放在最前面
        PickPictureTotalActivity.SortImageBeanComparator sortComparator = new PickPictureTotalActivity.SortImageBeanComparator(list);
        Collections.sort(list, sortComparator);

        return list;

    }
}
