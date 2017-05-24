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
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Stores notifications in the datastore.
 */
public class NotificationDatastore {
  private static final Logger log = Logger.getLogger(NotificationDatastore.class.getName());

  static final List<String> STUPID_REASONS = ImmutableList.<String>builder()
    .add("invitation").add("author").add("state_change").add("subscribed").build();

  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private final UserDatastore userDatastore = new UserDatastore();

  protected GitHubApi getApi(User user) {
    return new GitHubApi(user.accessToken());
  }

  public void fetchNotifications(User user) throws IOException {
    GitHubApi api = getApi(user);
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
        JSONObject mention = getMention(user, jsonObject, notification);
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

  JSONObject getMention(User user, JSONObject jsonObject, Entity entity) {
    String mention = "@" + user.getUsername();
    String url = jsonObject.getJSONObject("subject").getString("url");
    if (url.contains("/pulls/")) {
      // This is a pull request. The comment thread is the code review. We probably want the issue
      // thread.
      url = url.replace("/pulls/", "/issues/");
    }
    GitHubApi api = getApi(user);
    url = url + "/comments";

    JSONArray commentList;
    try {
      commentList = api.getArray(url);
    } catch (IOException e) {
      log.warning("Couldn't get comments from " + url + ": " + e.getMessage());
      return null;
    }

    boolean userResponded = false;
    for (int i = commentList.length() - 1; i >= 0; --i) {
      JSONObject comment = commentList.getJSONObject(i);
      String body = comment.getString("body");
      if (body.contains(mention)) {
        if (userResponded) {
          entity.setProperty(DatastoreConstants.Notifications.DONE, true);
        }
        int numExtraComments = commentList.length() - i - 1;
        if (numExtraComments > 0) {
          comment.put("num_following", numExtraComments);
        }
        return comment;
      }
      // GitHub still returns an "unread" notification, even if the user's responded to it!
      if (user.getUsername().equals(comment.getJSONObject("user").getString("login"))) {
        userResponded = true;
      }
    }

    // Maybe it was in the original message.
    url = jsonObject.getJSONObject("subject").getString("url");
    JSONObject issue;
    try {
      issue = api.getObject(url);
    } catch (IOException e) {
      log.warning("Couldn't get comments from " + url + ": " + e.getMessage());
      return null;
    }
    String body = issue.getString("body");
    if (body.contains(mention)) {
      if (userResponded) {
        entity.setProperty(DatastoreConstants.Notifications.DONE, true);
      }
      int comments = issue.getInt("comments");
      if (comments > 0) {
        issue.put("num_following", comments);
      }
      return issue;
    }

    log.warning(url + " theoretically mentioned " + user.getUsername()
      + " but could not find mention");
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

  public void delete(User user) {
    for (Entity notification : getNotifications(user.id())) {
      datastore.delete(notification.getKey());
    }
  }

}
