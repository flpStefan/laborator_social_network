package com.example.lab7.utils.events;


import com.example.lab7.domain.Utilizator;

public class UserTaskChangeEvent extends TaskChangeEvent<Utilizator> {
    private ChangeEventType type;
    private Utilizator data, oldData;

    public UserTaskChangeEvent(ChangeEventType type, Utilizator data) {
        super(type, data);
    }
    public UserTaskChangeEvent(ChangeEventType type, Utilizator data, Utilizator oldData) {
        super(type, data, oldData);
    }

}