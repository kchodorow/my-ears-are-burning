package com.meab.user;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.meab.DatastoreConstants;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

public class UserDatastore {
  private static final Logger log = Logger.getLogger(UserDatastore.class.getName());
  private static final String GITHUB_USER_URL = "https://api.github.com/user";

  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  public User createUser(String accessToken, String uuid) throws IOException {
    User user = User.create(uuid, accessToken, fetchUserInfo(accessToken));
    datastore.put(user.getEntity());
    return user;
  }

  public User getUser(String uuid) {
    Entity entity;
    try {
      entity = datastore.get(getKey(uuid));
    } catch (EntityNotFoundException e) {
      log.info("Got uuid " + uuid + " from cookie, but no user found.");
      return null;
    }
    if (entity == null) {
      return null;
    }
    return User.fromEntity(entity);
  }

  public void update(User user) {
    update(user.getEntity());
  }

  public void update(Entity user) {
    datastore.put(user);
  }

  public void addTrackedRepository(User user, String repository) {
    Entity entity = user.getEntity();
    ArrayList<String> repos = (ArrayList<String>) entity.getProperty(
      DatastoreConstants.User.TRACKED_REPOSITORIES);
    repos.add(repository);
    entity.setProperty(DatastoreConstants.User.TRACKED_REPOSITORIES, repos);
    datastore.put(entity);
  }

  public void setLastUpdated(User user) {
    Entity entity = user.getEntity();
    entity.setProperty(DatastoreConstants.User.LAST_UPDATED, new Date(System.currentTimeMillis()));
    datastore.put(entity);
  }

  private Key getKey(String uuid) {
    return KeyFactory.createKey(DatastoreConstants.User.DATASTORE, uuid);
  }

  private JSONObject fetchUserInfo(String accessToken) throws IOException {
    HttpClient httpClient = new DefaultHttpClient();
    HttpGet getRequest = new HttpGet(GITHUB_USER_URL);
    getRequest.addHeader("Authorization", "token " + accessToken);
    HttpResponse response = httpClient.execute(getRequest);
    HttpEntity entity = response.getEntity();
    String body = EntityUtils.toString(entity, "UTF-8");
    EntityUtils.consumeQuietly(entity);
    return new JSONObject(body);
  }
}
