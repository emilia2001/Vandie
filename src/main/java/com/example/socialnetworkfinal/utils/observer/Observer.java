package com.example.socialnetworkfinal.utils.observer;


public interface Observer<E extends EventObserver> {
    void update(E e);

}
