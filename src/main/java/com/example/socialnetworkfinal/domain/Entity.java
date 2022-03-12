package com.example.socialnetworkfinal.domain;

import java.util.Objects;

public class Entity<ID> {
    private ID id;

    /**
     * @return the unique id of the entity
     */
    public ID getId() {
        return id;
    }

    /**
     * @param id - the new id of the entity
     */
    public void setId(ID id) {
        this.id = id;
    }

    /**
     * @param o - the object to which we compare this entity
     * @return true if o is the entity,
     *         false if o is null or has another type
     *         equality of the ids, that are the unique identifiers of an object
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        return Objects.equals(id, entity.id);
    }


    /**
     * @return the hash of id
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
