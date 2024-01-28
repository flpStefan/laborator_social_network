package com.example.lab7.utils.events;


import com.example.lab7.domain.FriendRequest;

public class FriendRequestTaskChangeEvent extends TaskChangeEvent<FriendRequest>{
    private ChangeEventType type;
    private FriendRequest data, oldData;

    public FriendRequestTaskChangeEvent(ChangeEventType type, FriendRequest data) {
        super(type, data);
    }
    public FriendRequestTaskChangeEvent(ChangeEventType type, FriendRequest data, FriendRequest oldData) {
        super(type, data, oldData);
    }
}
