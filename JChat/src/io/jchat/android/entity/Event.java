package io.jchat.android.entity;

public class Event {

    public static class StringEvent{
        private String mTargetId;

        public StringEvent(String targetId){
            this.mTargetId = targetId;
        }

        public String getTargetId(){
            return mTargetId;
        }
    }

    public static class LongEvent{
        private long mGroupId;

        public LongEvent(long groupId){
            this.mGroupId = groupId;
        }

        public long getGroupId(){
            return mGroupId;
        }
    }

    public static class DraftEvent {
        private String mTargetId;
        private long mGroupId;
        private String mDraft;

        public DraftEvent(String targetId, String draft) {
            this.mTargetId = targetId;
            this.mDraft = draft;
        }

        public DraftEvent(long groupId, String draft) {
            this.mGroupId = groupId;
            this.mDraft = draft;
        }

        public String getTargetId() {
            return mTargetId;
        }

        public long getGroupId() {
            return mGroupId;
        }

        public String getDraft() {
            return mDraft;
        }
    }

}
