package com.example.lab7.utils.observer;


import com.example.lab7.utils.events.Event;

public interface Observer<E extends Event> {

    void update(E e);
}
