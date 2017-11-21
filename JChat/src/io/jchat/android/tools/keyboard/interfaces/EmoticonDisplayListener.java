package io.jchat.android.tools.keyboard.interfaces;

import android.view.ViewGroup;

import io.jchat.android.tools.keyboard.adpater.EmoticonsAdapter;


public interface EmoticonDisplayListener<T> {

    void onBindView(int position, ViewGroup parent, EmoticonsAdapter.ViewHolder viewHolder, T t, boolean isDelBtn);
}
