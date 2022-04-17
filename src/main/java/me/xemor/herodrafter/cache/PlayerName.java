package me.xemor.herodrafter.cache;

import java.io.Serializable;
/* This could not be made a record because the mutability of this class is needed to support the concept of an LRU caching system
and records are incompatible with mutable data fields
*/
public class PlayerName implements Serializable {

    private final long id;
    private String name;
    public PlayerName(String name, long id) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PlayerName pl) {
            return pl.id == this.id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(id).hashCode(); // the alternative is Objects.hash(id)
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }
}
