package com.calling.app.chat;

public class MessagesList {

    private  String name , mobile , last_message , profilePic;

    private int unSeenMessages;

    public MessagesList(String name, String mobile, String last_message, String profilePic, int unSeenMessages) {
        this.name = name;
        this.mobile = mobile;
        this.last_message = last_message;
        this.unSeenMessages = unSeenMessages;
        this.profilePic = profilePic;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getLast_message() {
        return last_message;
    }

    public void setLast_message(String last_message) {
        this.last_message = last_message;
    }

    public int getUnSeenMessages() {
        return unSeenMessages;
    }

    public void setUnSeenMessages(int unSeenMessages) {
        this.unSeenMessages = unSeenMessages;
    }
}
