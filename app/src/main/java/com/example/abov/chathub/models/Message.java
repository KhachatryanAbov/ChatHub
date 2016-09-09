package com.example.abov.chathub.models;


public class Message {
    private String messageText;
    private String receiverName;
    private String receiverId;
    private String userName;
    private String userId;

    public Message(){}

    public Message(String messageText, String receiverName, String receiverId, String userName, String userId) {
        this.messageText = messageText;
        this.receiverName = receiverName;
        this.receiverId = receiverId;
        this.userName = userName;
        this.userId = userId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
