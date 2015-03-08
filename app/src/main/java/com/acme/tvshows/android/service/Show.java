package com.acme.tvshows.android.service;


public class Show {
    private final String id;
    private final String name;
    
    public Show(String id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String toString() {
        return name;
    }
}
