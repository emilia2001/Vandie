package com.example.socialnetworkfinal.domain;

import java.time.LocalDate;

public class Friendship extends Entity<Tuple<Long, Long>>{
    private LocalDate date;
    private User friend1;
    private User friend2;
    private String state;

    /**
     * @param idFriend1 - id of the first friend
     * @param idFriend2 - id of the second friend
     *     together - the id of a friendship
     * @param State - String, the state of a friendship (approved, pending, rejected)
     */
    public Friendship(Long idFriend1, Long idFriend2, String State) {
        setId(new Tuple<>(idFriend1, idFriend2));
        date = LocalDate.now();
        state = State;
    }

    /**
     * @return LocalDate, the date when the friendship was created
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * @param date - LocalDate, the new date of a friendship
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * @return User, the first friend
     */
    public User getFriend1(){
        return friend1;
    }

    /**
     * @return User, the second friend
     */
    public User getFriend2(){
        return friend2;
    }

    /**
     * @param friend1 - User, the first friend of the friendship
     */
    public void setFriend1(User friend1) {
        this.friend1 = friend1;
    }

    /**
     * @param friend2 - User, the second friend of a friendship
     */
    public void setFriend2(User friend2) {
        this.friend2 = friend2;
    }

    /**
     * @return String, the state of a friendship(approved, pending, rejected)
     */
    public String getState() {
        return state;
    }

    /**
     * @param state - string, the state of a friendship (to be set)
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the string to be printed for a friendship
     */
    @Override
    public String toString() {
        String friendship = "";
        friendship += "IdFriend1 : " + getId().getLeft() + "\n";
        friendship += "IdFriend2 : " + getId().getRight() + "\n";
        friendship += "Date : " + date + "\n";
        friendship += "State : " + state + "\n\n";
        return friendship;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Friendship that = (Friendship) o;
        if( this.getId().getLeft().equals(that.getId().getLeft()) && this.getId().getRight().equals(that.getId().getRight()))
            return true;
        if( this.getId().getLeft().equals(that.getId().getRight()) && this.getId().getRight().equals(that.getId().getLeft()) )
            return true;
        return false;
    }
}

