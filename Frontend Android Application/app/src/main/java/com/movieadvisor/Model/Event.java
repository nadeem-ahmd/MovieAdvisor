package com.movieadvisor.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class Event implements Serializable {

    private String id;
    private String name;
    private String start;
    private String end;
    private String place;
    private ArrayList<String> tags;
    private String rational;

    public Event(String id, String name, String start, String end, String place, ArrayList<String> tags, String rational) {
        this.id = id;
        this.name = name;
        this.start = start;
        this.end = end;
        this.place = place;
        this.tags = tags;
        this.rational = rational;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public String getPlace() {
        return place;
    }

    public String getTags() {
        if (tags.isEmpty())
            return "none";
        StringBuilder s = new StringBuilder();
        for (String tag : tags)
            s.append(tag).append(", ");
        return s.substring(0, s.length() - 2);
    }

    public String getRational() {
        return "Based on your interest in " + rational;
    }
}
