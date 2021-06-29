package com.movieadvisor.Model;

import java.io.Serializable;

public class Group implements Serializable {

    private String id;
    private int size;
    private int ratings;

    public Group(String id, int size, int ratings) {
        this.id = id;
        this.size = size;
        this.ratings = ratings;
    }

    public String getId() {
        return id;
    }

    public int getSize() {
        return size;
    }

    public int getRatings() {
        return ratings;
    }
}
