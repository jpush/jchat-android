package io.jchat.android.entity;

import cn.jpush.im.android.api.model.Message;

/**
 * Created by jpush on 2015/8/26.
 */
public class Event {

    public static class StringEvent{
        private String mTargetID;

        public StringEvent(String targetID){
            this.mTargetID = targetID;
        }

        public String getTargetID(){
            return mTargetID;
        }
    }

    public static class LongEvent{
        private long mGroupID;

        public LongEvent(long groupID){
            this.mGroupID = groupID;
        }

        public long getGroupID(){
            return mGroupID;
        }
    }

    public static class MessageEvent{
        private Message mMessage;

        public MessageEvent(Message msg){
            this.mMessage = msg;
        }

        public Message getMessage(){
            return mMessage;
        }
    }
}
