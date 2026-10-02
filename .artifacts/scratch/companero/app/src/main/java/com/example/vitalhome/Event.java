package com.example.vitalhome;

public class Event {

    public enum Type {
        FALL, WATER, SOS_MANUAL
    }

    private final Type type;
    private final String title;
    private final String meta;

    public Event(Type type, String title, String meta) {
        this.type = type;
        this.title = title;
        this.meta = meta;
    }

    public Type getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMeta() {
        return meta;
    }
}
