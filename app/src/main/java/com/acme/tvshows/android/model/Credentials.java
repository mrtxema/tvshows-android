package com.acme.tvshows.android.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Credentials {
    private final String store;
    private final Map<String, String> parameters;

    public Credentials(String store, Map<String, String> parameters) {
        this.store = store;
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    public Credentials(String store) {
        this.store = store;
        this.parameters = Collections.emptyMap();
    }

    public String getStore() {
        return store;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public boolean containsParameters(List<String> loginParameters) {
        return parameters.keySet().containsAll(loginParameters);
    }
}
