package com.acme.tvshows.android.service;


public class Link {
    private final String id;
    private final Language language;
    private final String server;
    
    public Link(String id, String server, Language language) {
        this.id = id;
        this.server = server;
        this.language = language;
    }
    
    public String getId() {
        return id;
    }
    
    public String getServer() {
        return server;
    }
    
    public Language getLanguage() {
        return language;
    }
    
    public String toString() {
        return String.format("%s (%s)", server, language.getName());
    }
}
