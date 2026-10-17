package com.auction.shared.network;

import java.io.Serializable;

public class MessageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String action;     // Lấy từ Actions.java
    private String sender;     // Username hoặc mã client
    private Object payload;    // Dữ liệu kèm theo gói tin
    private long timestamp;    // Thời gian gửi

    public MessageDTO() {
        this.timestamp = System.currentTimeMillis();
    }

    public MessageDTO(String action, String sender, Object payload) {
        this.action = action;
        this.sender = sender;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}