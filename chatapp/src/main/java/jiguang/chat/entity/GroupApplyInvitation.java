package jiguang.chat.entity;

public enum GroupApplyInvitation {
    ACCEPTED("accepted"),//已接受
    INVITING("inviting"),
    INVITED("invited"),
    REFUSED("refused"),
    BE_REFUSED("be_refused"),
    DEFAULT("default");


    private String value;

    GroupApplyInvitation(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
