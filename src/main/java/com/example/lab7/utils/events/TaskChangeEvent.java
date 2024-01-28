package com.example.lab7.utils.events;

public class TaskChangeEvent<E> implements Event{
    private ChangeEventType type;
    private E data, oldData;

    public TaskChangeEvent(ChangeEventType type, E data) {
        this.type = type;
        this.data = data;
    }
    public TaskChangeEvent(ChangeEventType type, E data, E oldData) {
        this.type = type;
        this.data = data;
        this.oldData=oldData;
    }

    public ChangeEventType getType() {
        return type;
    }

    public E getData() {
        return data;
    }

    public E getOldData() {
        return oldData;
    }
}
