package com.acme.tvshows.android.service;


public class ShowServiceException extends Exception {
    
    public ShowServiceException(String message) {
        super(message);
    }
    
    public ShowServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
