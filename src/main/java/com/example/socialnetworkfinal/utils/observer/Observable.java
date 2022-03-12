package com.example.socialnetworkfinal.utils.observer;

public interface Observable<E extends EventObserver> {
    void addObserver(Observer<E> e);
    void notifyObservers(E t);
    void removeObserver(Observer<E> e);
    void removeAAllObservers();
}
