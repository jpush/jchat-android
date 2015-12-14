package io.jchat.android.entity;

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

}
