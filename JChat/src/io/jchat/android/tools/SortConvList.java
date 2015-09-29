package io.jchat.android.tools;

import android.util.Log;

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
        //返回-1为升序，即将最近收到消息的会话放在第一位
        int flag;
        Message msg1 = conv1.getLatestMessage();
        Message msg2 = conv2.getLatestMessage();
        long compareTime1;
        long compareTime2;
        if (msg1 != null && msg2 != null){
            compareTime1 = msg1.getCreateTime();
            compareTime2 = msg2.getCreateTime();
        }else if (msg1 == null && msg2 != null){
            compareTime1 = conv1.getLastMsgDate();
            compareTime2 = msg2.getCreateTime();
        }else if (msg1 != null && msg2 == null){
            compareTime1 = msg1.getCreateTime();
            compareTime2 = conv2.getLastMsgDate();
        }else {
            compareTime1 = conv1.getLastMsgDate();
            compareTime2 = conv2.getLastMsgDate();
        }
        if(compareTime1 > compareTime2)
            flag = -1;
        else if(compareTime1 < compareTime2)
            flag = 1;
        else flag = 0;
        return flag;
    }
}
