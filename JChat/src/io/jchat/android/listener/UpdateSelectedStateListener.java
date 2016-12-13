package io.jchat.android.listener;


import io.jchat.android.entity.FileType;

public interface UpdateSelectedStateListener {
    public void onSelected(String path, long fileSize, FileType type);
    public void onUnselected(String path, long fileSize, FileType type);
}
