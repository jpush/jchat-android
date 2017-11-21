package io.jchat.android.tools.keyboard.interfaces;

import android.view.View;
import android.view.ViewGroup;

import io.jchat.android.tools.keyboard.data.PageEntity;


public interface PageViewInstantiateListener<T extends PageEntity> {

    View instantiateItem(ViewGroup container, int position, T pageEntity);
}
