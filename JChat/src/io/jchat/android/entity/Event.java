package io.jchat.android.entity;

public class Event {

    /**
     * 传递到ConversationListFragment的创建单聊事件
     * mTargetId 单聊Id
     * mAppKey 可以得到创建单聊者的AppKey
     */
    public static class StringEvent{
        private String mTargetId;
<<<<<<< HEAD

        public StringEvent(String targetId){
            this.mTargetId = targetId;
=======
        private String mAppKey;

        public StringEvent(String targetId, String appKey) {
            this.mTargetId = targetId;
            this.mAppKey = appKey;
>>>>>>> master
        }

        public String getTargetId(){
            return mTargetId;
<<<<<<< HEAD
=======
        }

        public String getAppKey() {
            return mAppKey;
>>>>>>> master
        }
    }

    /**
     * 传递到ConversationListFragment的创建或删除群聊事件
     * mGroupId 群聊Id
     * mIsAddEvent 是否为创建群聊的标志
     */
    public static class LongEvent{
        private long mGroupId;
<<<<<<< HEAD

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

=======
        private boolean mIsAddEvent;

        public LongEvent(boolean isAddEvent, long groupId){
            this.mIsAddEvent = isAddEvent;
            this.mGroupId = groupId;
        }

        public long getGroupId(){
            return mGroupId;
        }

        public boolean getFlag() {
            return mIsAddEvent;
        }
    }

    /**
     * 传递到ConversationListFragment的保存为草稿的事件
     * mDraft 草稿内容
     */
    public static class DraftEvent {
        private String mTargetId;
        private long mGroupId;
        private String mTargetAppKey;
        private String mDraft;

        public DraftEvent(String targetId, String appKey, String draft) {
            this.mTargetId = targetId;
            this.mTargetAppKey = appKey;
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

        public String getTargetAppKey() {
            return mTargetAppKey;
        }

>>>>>>> master
        public String getDraft() {
            return mDraft;
        }
    }

}
