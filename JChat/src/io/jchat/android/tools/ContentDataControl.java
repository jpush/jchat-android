package io.jchat.android.tools;


import android.support.v4.util.ArrayMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.jchat.android.activity.PickPictureTotalActivity;
import io.jchat.android.entity.FileItem;
import io.jchat.android.entity.FileSystemType;
import io.jchat.android.entity.ImageBean;

public class ContentDataControl {

    private static final ArrayMap<FileSystemType, List<FileItem>> mAllFileItem = new ArrayMap<>();
    private static final List<ImageBean> mImageList = new ArrayList<>();


    public static void addFileByType(FileSystemType type, FileItem fileItem) {

        if (type == null || fileItem == null) {
            return;
        }

        List<FileItem> fileItemList = mAllFileItem.get(type);

        if (fileItemList == null) {
            fileItemList = new ArrayList<>();
            mAllFileItem.put(type, fileItemList);
        }

        fileItemList.add(fileItem);

    }


    public static void addFileListByType(FileSystemType type, List<FileItem> fileItemList) {


        if (type == null || fileItemList == null) {
            return;
        }

        List<FileItem> fileItems = mAllFileItem.get(type);

        if (fileItems == null) {
            fileItems = new ArrayList<>();
            mAllFileItem.put(type, fileItems);
        }

        fileItems.addAll(fileItemList);

    }

    public static void setAlbumMap(HashMap<String, List<String>> map) {
        if (map.size() == 0) {
            return;
        }

        Iterator<Map.Entry<String, List<String>>> it = map.entrySet().iterator();
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

            mImageList.add(mImageBean);
        }

        //对相册进行排序，最近修改的相册放在最前面
        PickPictureTotalActivity.SortImageBeanComparator sortComparator = new PickPictureTotalActivity.SortImageBeanComparator(mImageList);
        Collections.sort(mImageList, sortComparator);
    }

    public static List<ImageBean> getAlbumList() {
        return mImageList;
    }


    public static List<FileItem> getFileItemListByType(FileSystemType fileSystemType) {

        if (fileSystemType == null) {
            return null;
        }

        return mAllFileItem.get(fileSystemType);

    }


    public static int getTypeCount(FileSystemType fileSystemType) {

        List<FileItem> fileItemList = mAllFileItem.get(fileSystemType);


        return fileItemList == null ? 0 : fileItemList.size();

    }


    public static void destroy() {
        mAllFileItem.clear();
    }
}
