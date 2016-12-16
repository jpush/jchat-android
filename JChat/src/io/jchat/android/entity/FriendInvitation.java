package io.jchat.android.entity;

public enum FriendInvitation {
    ACCEPTED("accepted"),
    INVITING("inviting"),
    INVITED("invited"),
    REFUSED("refused"),
    BE_REFUSED("be_refused");

    private String value;

    private FriendInvitation(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
