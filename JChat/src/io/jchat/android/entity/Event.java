package io.jchat.android.entity;

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
}
