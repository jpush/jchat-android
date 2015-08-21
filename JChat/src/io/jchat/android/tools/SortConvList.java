package io.jchat.android.tools;

import java.util.Comparator;

import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;

/**
 * Created by Ken on 2015/1/28.
 */
public class SortConvList implements Comparator {
    @Override
    public int compare(Object o, Object o2) {
        Conversation conv1 = (Conversation) o;
        Conversation conv2 = (Conversation) o2;
        Message lastMsg1 = conv1.getLatestMessage();
        Message lastMsg2 = conv2.getLatestMessage();
        int flag;
        //返回-1为升序，即将最近收到消息的会话放在第一位
        if(lastMsg1.getCreateTime() > lastMsg2.getCreateTime())
            flag = -1;
        else if(lastMsg1.getCreateTime() < lastMsg2.getCreateTime())
            flag = 1;
        else flag = 0;
        return flag;
    }
}
