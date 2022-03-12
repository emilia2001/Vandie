package com.example.socialnetworkfinal.domain;

import java.util.Objects;

/**
 * Define a Tuple o generic type entities
 * @param <E1> - tuple first entity type
 * @param <E2> - tuple second entity type
 */
public class Tuple<E1, E2> {
    private final E1 e1;
    private final E2 e2;

    /**
     * @param e1 - first entity of tuple
     * @param e2 - second entity of tuple
     */
    public Tuple(E1 e1, E2 e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    /**
     * @return e1 - first entity
     */
    public E1 getLeft() {
        return e1;
    }

    /**
     * @return e2 - second entity
     */
    public E2 getRight() {
        return e2;
    }

    /**
     * @return the string to be printed for a tuple
     */
    @Override
    public String toString() {
        return "" + e1 + "," + e2;

    }

    /**
     * @param obj - teh object to which we test the equality
     * @return true - the entities are the same
     *         false - otherwise
     */
    @Override
    public boolean equals(Object obj) {
        return this.e1.equals(((Tuple) obj).e1) && this.e2.equals(((Tuple) obj).e2);
    }

    /**
     * @return the hash of entities
     */
    @Override
    public int hashCode() {
        return Objects.hash(e1, e2);
    }
}

