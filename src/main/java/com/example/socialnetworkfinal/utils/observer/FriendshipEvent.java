package com.example.socialnetworkfinal.utils.observer;

import com.example.socialnetworkfinal.domain.Friendship;

import java.util.Objects;

public class FriendshipEvent implements EventObserver{
    private FriendshipEventsType type;
    private Friendship data;


    public FriendshipEvent(FriendshipEventsType type, Friendship data) {
        this.type = type;
        this.data = data;
    }

    public FriendshipEventsType getType() {
        return type;
    }

    public Friendship getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FriendshipEvent)) return false;
        FriendshipEvent that = (FriendshipEvent) o;
        return getType().equals(that.getType()) && Objects.equals(getData(), that.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getData());
    }
}
