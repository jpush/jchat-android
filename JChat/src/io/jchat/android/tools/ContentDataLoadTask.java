package io.jchat.android.tools;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.jchat.android.entity.FileItem;
import io.jchat.android.entity.FileSystemType;


public class ContentDataLoadTask extends AsyncTask<Void, Void, Void> {

    private Context mContext;

    private ContentResolver mContentResolver;

    private OnContentDataLoadListener mOnContentDataLoadListener;

    public ContentDataLoadTask(Context mContext) {
        this.mContext = mContext;
    }

    public OnContentDataLoadListener getOnContentDataLoadListener() {
        return mOnContentDataLoadListener;
    }

    public void setOnContentDataLoadListener(OnContentDataLoadListener mOnContentDataLoadListener) {
        this.mOnContentDataLoadListener = mOnContentDataLoadListener;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (mOnContentDataLoadListener != null) {
            mOnContentDataLoadListener.onStartLoad();
        }


        mContentResolver = mContext.getContentResolver();
    }

    @Override
    protected Void doInBackground(Void... params) {


//        ContentDataControl.setAlbumMap(getAllPhoto());

        ContentDataControl.addFileListByType(FileSystemType.music, getAllMusic());

        ContentDataControl.addFileListByType(FileSystemType.video, getAllVideo());

        ContentDataControl.addFileListByType(FileSystemType.text, getAllDocument());

        ContentDataControl.addFileListByType(FileSystemType.other, getAllOther());


        return null;
    }


    private HashMap<String, List<String>> getAllPhoto() {

        HashMap<String, List<String>> gruopMap = new HashMap<String, List<String>>();
        String[] projection = new String[]{ MediaStore.Images.ImageColumns.DATA };
        Cursor cursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null,
                MediaStore.Images.Media.DATE_MODIFIED);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                //获取图片的路径
                String path = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Images.Media.DATA));

                try {
                    //获取该图片的父路径名
                    String parentName = new File(path).getParentFile().getName();
                    //根据父路径名将图片放入到mGruopMap中
                    if (!gruopMap.containsKey(parentName)) {
                        List<String> chileList = new ArrayList<String>();
                        chileList.add(path);
                        gruopMap.put(parentName, chileList);
                    } else {
                        gruopMap.get(parentName).add(path);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cursor.close();

            return gruopMap;

        }
        return null;
    }


    private List<FileItem> getAllMusic() {

        List<FileItem> musics = new ArrayList<>();


        String[] projection = new String[]{MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.DISPLAY_NAME, MediaStore.Audio.AudioColumns.SIZE,
                MediaStore.Audio.AudioColumns.DATE_MODIFIED};

        Cursor cursor = mContentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, MediaStore.Audio.AudioColumns.DATE_MODIFIED + " desc");

        if (cursor != null) {
            while (cursor.moveToNext()) {

                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME));
                String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));
                String size = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.SIZE));
                String date = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATE_MODIFIED));

                FileItem fileItem = new FileItem(filePath, fileName, size, date);

                musics.add(fileItem);

            }
            cursor.close();
            cursor = null;
        }


        return musics;

    }


    private List<FileItem> getAllVideo() {

        List<FileItem> videos = new ArrayList<>();


        String[] projection = new String[]{MediaStore.Video.VideoColumns.DATA,
                MediaStore.Video.VideoColumns.DISPLAY_NAME, MediaStore.Video.VideoColumns.SIZE,
                MediaStore.Video.VideoColumns.DATE_MODIFIED};


        Cursor cursor = mContentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection,
                null, null, MediaStore.Video.VideoColumns.DATE_MODIFIED + " desc");

        if (cursor != null) {
            while (cursor.moveToNext()) {

                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME));
                String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA));
                String size = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.SIZE));
                String date = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_MODIFIED));

                FileItem fileItem = new FileItem(filePath, fileName, size, date);

                videos.add(fileItem);

            }


            cursor.close();
            cursor = null;
        }

        return videos;

    }

    private List<FileItem> getAllDocument() {

        List<FileItem> texts = new ArrayList<>();

        String[] projection = new String[]{MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.TITLE, MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED};

        String selection = MediaStore.Files.FileColumns.MIME_TYPE + "= ? "
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? ";

        String[] selectionArgs = new String[]{"text/plain", "application/msword", "application/pdf",
                "application/vnd.ms-powerpoint", "application/vnd.ms-excel", "application/vnd.ms-works"};

        Cursor cursor = mContentResolver.query(MediaStore.Files.getContentUri("external"), projection,
                selection, selectionArgs, MediaStore.Files.FileColumns.DATE_MODIFIED + " desc");

        if (cursor != null) {
            while (cursor.moveToNext()) {

                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE));
                String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                String size = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
                String date = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED));

                FileItem fileItem = new FileItem(filePath, fileName, size, date);

                texts.add(fileItem);

            }

            cursor.close();
            cursor = null;

        }


        return texts;

    }

    private List<FileItem> getAllOther() {

        List<FileItem> other = new ArrayList<>();


        String[] projection = new String[]{MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.TITLE, MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED};

        String selection = MediaStore.Files.FileColumns.MIME_TYPE + "= ? or "
                + MediaStore.Files.FileColumns.MIME_TYPE + "= ? or "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "= ? ";

        String[] selectionArgs = new String[]{"application/zip", "application/vnd.android.package-archive",
                "application/x-rar-compressed"};

        Cursor cursor = mContentResolver.query(MediaStore.Files.getContentUri("external"), projection,
                selection, selectionArgs, MediaStore.Files.FileColumns.DATE_MODIFIED + " desc");

        if (cursor != null) {
            while (cursor.moveToNext()) {

                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE));
                String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                String size = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
                String date = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED));
                FileItem fileItem = new FileItem(filePath, fileName, size, date);

                other.add(fileItem);

            }
        }


        return other;

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mOnContentDataLoadListener != null) {
            mOnContentDataLoadListener.onFinishLoad();
        }
    }


    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (mOnContentDataLoadListener != null) {
            mOnContentDataLoadListener.onFinishLoad();
        }
    }

    public interface OnContentDataLoadListener {

        public void onStartLoad();

        public void onFinishLoad();

    }
}
