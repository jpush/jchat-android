package io.jchat.android.tools;

import android.util.Log;

import java.util.Comparator;

import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;

/**
 * Created by Ken on 2015/1/28.
 */
public class SortConvList implements Comparator<Conversation> {
    @Override
    public int compare(Conversation o, Conversation o2) {
        if (o.getLastMsgDate() > o2.getLastMsgDate()) {
            return -1;
        } else if (o.getLastMsgDate() < o2.getLastMsgDate()) {
            return 1;
        }
        return 0;
    }
}
