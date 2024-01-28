package com.example.lab7.repository;

public class Page<E> {
    private Iterable<E> elements;
    private int totalElements;

    public Page(Iterable<E> elements, int totalElements) {
        this.elements = elements;
        this.totalElements = totalElements;
    }

    public Iterable<E> getElements() {
        return elements;
    }

    public int getTotalElements() {
        return totalElements;
    }
}
