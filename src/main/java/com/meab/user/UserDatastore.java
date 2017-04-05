package com.meab.user;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.meab.DatastoreConstants;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

public class UserDatastore {
  private static final Logger log = Logger.getLogger(UserDatastore.class.getName());
  private static final String GITHUB_USER_URL = "https://api.github.com/user";

  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  public User createUser(String accessToken) throws IOException {
    User user = User.create(accessToken, fetchUserInfo(accessToken));
    datastore.put(user.getEntity());
    return user;
  }

  public User getUser(String uuid) {
    Entity entity;
    PreparedQuery pq;
    Query.Filter propertyFilter =
      new Query.FilterPredicate(
        DatastoreConstants.User.COOKIE, Query.FilterOperator.GREATER_THAN_OR_EQUAL, uuid);
    Query q = new Query(DatastoreConstants.User.DATASTORE).setFilter(propertyFilter);
    pq = datastore.prepare(q);
    try {
      entity = pq.asSingleEntity();
      if (entity == null) {
        log.info("Got uuid " + uuid + " from cookie, but no user found.");
        return null;
      }
    } catch (PreparedQuery.TooManyResultsException e) {
      log.warning("Found more than one " + uuid + ".");
      Iterable<Entity> iterator = pq.asIterable();
      for (Entity i : iterator) {
        log.warning("Logging out " + i.getKey() + ".");
        i.setProperty(DatastoreConstants.User.COOKIE, null);
        update(i);
      }
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

  public void setLastUpdated(User user) {
    Entity entity = user.getEntity();
    entity.setProperty(DatastoreConstants.User.LAST_UPDATED, new Date(System.currentTimeMillis()));
    datastore.put(entity);
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

  public void delete(User user) {
    datastore.delete(user.getEntity().getKey());
  }
}
