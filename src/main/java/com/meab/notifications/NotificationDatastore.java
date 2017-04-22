package com.meab.notifications;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import com.google.common.collect.ImmutableList;
import com.meab.DatastoreConstants;
import com.meab.ProdConstants;
import com.meab.user.User;
import com.meab.user.UserDatastore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

/**
 * Stores notifications in the datastore.
 */
public class NotificationDatastore {
  private static final Logger log = Logger.getLogger(NotificationDatastore.class.getName());

  private static final List<String> STUPID_REASONS = ImmutableList.<String>builder()
    .add("invitation").add("author").add("state_change").build();

  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private final UserDatastore userDatastore = new UserDatastore();

  public void fetchNotifications(User user) throws IOException {
    GitHubApi api = new GitHubApi(user.accessToken());
    String lastUpdated = DatastoreConstants.GITHUB_DATE_FORMAT.format(user.lastUpdated());
    JSONArray notifications = api.getArray(
      ProdConstants.GITHUB_NOTIFICATIONS_URL + "?since=" + lastUpdated);
    store(notifications, user);
  }

  public void store(JSONArray notifications, User user) {
    ImmutableList.Builder<Entity> builder = ImmutableList.builder();
    for (int i = 0; i < notifications.length(); ++i) {
      JSONObject jsonObject = notifications.getJSONObject(i);
      String reason = jsonObject.getString("reason");
      if (STUPID_REASONS.contains(reason)) {
        continue;
      }
      Entity notification = Notification.createFromGitHubResponse(jsonObject).getEntity();
      if (reason.equals("mention")) {
        // Get the comment that mentions the user.
        JSONObject mention = getMention(user, jsonObject);
        if (mention != null) {
          notification.setProperty(
            DatastoreConstants.Notifications.MENTION, new Text(mention.toString()));
        }
      }
      notification.setProperty(DatastoreConstants.Notifications.USER_ID, user.id());
      builder.add(notification);
    }
    datastore.put(builder.build());
    userDatastore.setLastUpdated(user);
  }

  private JSONObject getMention(User user, JSONObject jsonObject) {
    GitHubApi api = new GitHubApi(user.accessToken());
    String url = jsonObject.getJSONObject("subject").getString("url") + "/comments";

    JSONArray commentList;
    try {
      commentList = api.getArray(url);
    } catch (IOException e) {
      return null;
    }

    for (int i = commentList.length() - 1; i >= 0; --i) {
      JSONObject comment = commentList.getJSONObject(i);
      String body = comment.getString("body");
      if (body.contains("@" + user.getUsername())) {
        return comment;
      }
    }
    return null;
  }

  public Iterable<Entity> getNotifications(long userId) {
    Query.FilterPredicate predicate = new Query.FilterPredicate(
      DatastoreConstants.Notifications.USER_ID, Query.FilterOperator.EQUAL, userId);
    Query query = new Query(DatastoreConstants.Notifications.DATASTORE).setFilter(predicate);
    PreparedQuery preparedQuery = datastore.prepare(query);
    return preparedQuery.asIterable();
  }

  public Entity getNotification(String notificationId) throws EntityNotFoundException {
    Key key = KeyFactory.createKey(DatastoreConstants.Notifications.DATASTORE, notificationId);
    return datastore.get(key);
  }

  public void update(Entity entity) {
    datastore.put(entity);
  }

  private static class GitHubApi {
    private final String accessToken;

    private GitHubApi(String accessToken) {
      this.accessToken = accessToken;
    }

    private JSONArray getArray(String urlString) throws IOException {
      URL url = new URL(urlString);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestProperty("Authorization", "token " + accessToken);
      BufferedReader reader = new BufferedReader(
        new InputStreamReader(connection.getInputStream()));
      StringBuffer json = new StringBuffer();
      String line;
      while ((line = reader.readLine()) != null) {
        json.append(line);
      }
      reader.close();
      try {
        return new JSONArray(json.toString());
      } catch (JSONException e) {
        log.warning("Could not parse: " + json);
        return new JSONArray();
      }
    }
  }
}
