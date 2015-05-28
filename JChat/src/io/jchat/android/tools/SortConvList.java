package io.jchat.android.tools;

import java.util.Comparator;

import cn.jpush.im.android.api.model.Conversation;

/**
 * Created by Ken on 2015/1/28.
 */
public class SortConvList implements Comparator {
    @Override
    public int compare(Object o, Object o2) {
        Conversation conv1 = (Conversation) o;
        Conversation conv2 = (Conversation) o2;
        int flag;
        //返回-1为升序，即将最近收到消息的会话放在第一位
        if(conv1.getLastMsgDate() > conv2.getLastMsgDate())
            flag = -1;
        else if(conv1.getLastMsgDate() < conv2.getLastMsgDate())
            flag = 1;
        else flag = 0;
        return flag;
    }
}
