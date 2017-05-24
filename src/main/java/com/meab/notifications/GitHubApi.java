package com.meab.notifications;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

class GitHubApi {
  private static final Logger log = Logger.getLogger(GitHubApi.class.getName());
  private final String accessToken;

  GitHubApi(String accessToken) {
    this.accessToken = accessToken;
  }

  JSONArray getArray(String urlString) throws IOException {
    String json = get(urlString);
    try {
      return new JSONArray(json);
    } catch (JSONException e) {
      log.warning("Could not parse array: " + json);
      return new JSONArray();
    }
  }

  JSONObject getObject(String urlString) throws IOException {
    String json = get(urlString);
    try {
      return new JSONObject(json);
    } catch (JSONException e) {
      log.warning("Could not parse: " + json);
      return new JSONObject();
    }
  }

  private String get(String urlString) throws IOException {
    URL url = new URL(urlString);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestProperty("Authorization", "token " + accessToken);
    BufferedReader reader = new BufferedReader(
      new InputStreamReader(connection.getInputStream()));
    StringBuilder json = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      json.append(line);
    }
    reader.close();
    return json.toString();
  }
}
