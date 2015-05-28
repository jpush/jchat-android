package io.jchat.android.entity;

public class Member{
	private String nickName;
	private int avatarID;
    private long memberID;
	public Member(){
		
	}
	
	public Member(String name, int id){
		this.nickName = name;
		this.avatarID = id;
	}

    public Member(long memberID, int id){
        this.memberID = memberID;
        this.avatarID = id;
    }

    public long getMemberID(){
        return memberID;
    }

	public String getUserName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public int getAvatarID() {
		return avatarID;
	}
	public void setAvatarID(int avatarID) {
		this.avatarID = avatarID;
	}
	
}