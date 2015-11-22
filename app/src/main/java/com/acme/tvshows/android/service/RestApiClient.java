package com.acme.tvshows.android.service;

import org.androidannotations.annotations.EBean;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EBean
public class RestApiClient {
    private final HttpClient httpClient = new DefaultHttpClient();

    private RestApiResponse callUrl(String url) throws IOException {
        final HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("content-type", "application/json");
        final HttpResponse resp = httpClient.execute(httpGet);
        return new RestApiResponse(EntityUtils.toString(resp.getEntity()), resp.getStatusLine().getStatusCode());
    }

    JSONArray callUrlAsJson(String url) throws RestApiException, IOException, JSONException {
        RestApiResponse response = callUrl(url);
        if (response.getStatus() == HttpStatus.SC_BAD_REQUEST) {
            throw new RestApiException("Error calling url: " + url, new JSONObject(response.getContent()));
        }
        final String responseContent = (response.getContent().charAt(0) == '[') ? response.getContent() : String.format("[%s]", response.getContent());
        return new JSONArray(responseContent);
    }

    List<String> asStringList(JSONArray array) throws JSONException {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            result.add(array.getString(i));
        }
        return result;
    }

    String buildQueryString(Map<String, String> parameters) {
        if (parameters.isEmpty()) {
            return "";
        } else {
            StringBuffer result = new StringBuffer();
            for (Map.Entry<String,String> entry : parameters.entrySet()) {
                result.append(result.length() == 0 ? "?" : "&");
                result.append(entry.getKey());
                result.append("=");
                result.append(entry.getValue());
            }
            return result.toString();
        }
    }

    private static class RestApiResponse {
        private final String content;
        private final int status;

        public RestApiResponse(String content, int status) {
            this.content = content;
            this.status = status;
        }

        public String getContent() {
            return content;
        }

        public int getStatus() {
            return status;
        }
    }
}
