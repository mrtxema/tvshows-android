package com.acme.tvshows.android.service;

import java.util.Collections;
import java.util.List;

public class Store {
    private final String code;
    private final List<String> loginParameters;

    public Store(String code, List<String> loginParameters) {
        this.code = code;
        this.loginParameters = Collections.unmodifiableList(loginParameters);
    }

    public String getCode() {
        return code;
    }

    public List<String> getLoginParameters() {
        return loginParameters;
    }

    @Override
    public String toString() {
        return code;
    }
}
