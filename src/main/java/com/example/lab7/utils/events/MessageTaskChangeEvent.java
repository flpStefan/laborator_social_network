package com.example.lab7.utils.events;


import com.example.lab7.domain.Prietenie;


public class MessageTaskChangeEvent extends TaskChangeEvent<Prietenie> {
    private ChangeEventType type;
    private Prietenie data, oldData;

    public MessageTaskChangeEvent(ChangeEventType type, Prietenie data) {
        super(type, data);
    }
    public MessageTaskChangeEvent(ChangeEventType type, Prietenie data, Prietenie oldData) {
        super(type, data, oldData);
    }
}