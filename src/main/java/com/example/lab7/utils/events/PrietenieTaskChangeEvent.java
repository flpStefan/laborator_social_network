package com.example.lab7.utils.events;


import com.example.lab7.domain.Prietenie;


public class PrietenieTaskChangeEvent extends TaskChangeEvent<Prietenie> {
    private ChangeEventType type;
    private Prietenie data, oldData;

    public PrietenieTaskChangeEvent(ChangeEventType type, Prietenie data) {
        super(type, data);
    }
    public PrietenieTaskChangeEvent(ChangeEventType type, Prietenie data, Prietenie oldData) {
        super(type, data, oldData);
    }
}