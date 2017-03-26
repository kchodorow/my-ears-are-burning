package com.meab.notifications;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ImmutableList;
import com.meab.DatastoreConstants;
import com.meab.user.User;
import com.meab.user.UserDatastore;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Stores notifications in the datastore.
 */
public class NotificationDatastore {
  private static final Logger log = Logger.getLogger(NotificationDatastore.class.getName());

  private static final String USER_URL = "https://api.github.com/notifications";

  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private final UserDatastore userDatastore = new UserDatastore();

  public void fetchNotifications(User user) throws IOException {
    HttpClient httpClient = new DefaultHttpClient();
    String lastUpdated = user.getLastUpdated();
    HttpGet getRequest = new HttpGet(USER_URL + "?since=" + lastUpdated);
    getRequest.addHeader("Authorization", "token " + user.accessToken());
    HttpResponse response = httpClient.execute(getRequest);
    HttpEntity entity = response.getEntity();
    String body = EntityUtils.toString(entity, "UTF-8");
    EntityUtils.consumeQuietly(entity);
    JSONArray notifications = new JSONArray(body);
    store(notifications, user);
  }

  public void store(JSONArray notifications, User user) {
    ImmutableList.Builder<Entity> builder = ImmutableList.builder();
    for (int i = 0; i < notifications.length(); ++i) {
      Entity notification = new Notification(notifications.getJSONObject(i)).getEntity();
      notification.setProperty(DatastoreConstants.Notifications.USER_ID, user.id());
      builder.add(notification);
    }
    datastore.put(builder.build());
    userDatastore.setLastUpdated(user);
  }

  public Iterable<Entity> getNotifications(String userId) {
    Query.FilterPredicate predicate = new Query.FilterPredicate(
      DatastoreConstants.Notifications.USER_ID, Query.FilterOperator.EQUAL, userId);
    Query query = new Query(DatastoreConstants.Notifications.DATASTORE).setFilter(predicate);
    PreparedQuery preparedQuery = datastore.prepare(query);
    return preparedQuery.asIterable();
  }
}
