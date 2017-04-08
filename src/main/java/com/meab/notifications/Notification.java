package com.meab.notifications;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.meab.DatastoreConstants;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;
import java.util.logging.Logger;

/**
 * {
 *      "id":"130516831",
 *      "unread":true,
 *      "reason":"state_change",
 *      "updated_at":"2017-03-12T07:41:28Z",
 *      "last_read_at":null,
 *      "subject":{
 *          "title":"[Feature request] Bazel should cache already downloaded repositories and dependencies",
 *          "url":"https://api.github.com/repos/bazelbuild/bazel/issues/1050",
 *          "latest_comment_url":"https://api.github.com/repos/bazelbuild/bazel/issues/comments/285928164",
 *          "type":"Issue"
 *      },
 *      "repository":{
 *          "id":20773773,
 *          "name":"bazel",
 *          "full_name":"bazelbuild/bazel",
 *          "owner":{
 *              "login":"bazelbuild",
 *              "id":11684617,
 *              ...
 *          },
 *          "private":false,
 *          "html_url":"https://github.com/bazelbuild/bazel",
 *      },
 *      "url":"https://api.github.com/notifications/threads/203256144",
 *      "subscription_url":"https://api.github.com/notifications/threads/203256144/subscription"
 *  }
 */
public class Notification {
  private static final Logger log = Logger.getLogger(Notification.class.getName());

  private final Entity entity;
  private final JSONObject object;

  private Notification(Entity entity, JSONObject object) {
    this.entity = entity;
    this.object = object;
  }

  public static Notification createFromGitHubResponse(JSONObject object) {
    Key key = KeyFactory.createKey(
      DatastoreConstants.Notifications.DATASTORE, object.getString("id"));
    Entity entity = new Entity(key);
    // NB: GitHub uses a negative boolean field (tsk) so we reverse it here.
    entity.setProperty("done", !object.getBoolean("unread"));
    entity.setProperty("reason", object.getString("reason"));
    entity.setProperty("date", getDate(object));
    entity.setProperty(DatastoreConstants.Notifications.FULL_TEXT, new Text(object.toString()));
    return new Notification(entity, object);
  }

  public static Notification fromEntity(Entity entity) {
    JSONObject object;
    try {
      object = new JSONObject(
        ((Text) entity.getProperty(DatastoreConstants.Notifications.FULL_TEXT)).getValue());
    } catch (JSONException e) {
      log.warning(
        e.getMessage() + ": "
          + ((Text) entity.getProperty(DatastoreConstants.Notifications.FULL_TEXT)).getValue());
      return null;
    }
    return new Notification(entity, object);
  }

  public Entity getEntity() {
    return entity;
  }

  public JSONObject getJson() throws InvalidJsonException {
    JSONObject response = new JSONObject();
    response.put("id", entity.getKey().getName());
    response.put("reason", entity.getProperty("reason"));
    response.put("title", object.getJSONObject("subject").getString("title"));
    response.put("url", object.getJSONObject("subject").getString("url"));
    response.put("repository", object.getJSONObject("repository").getString("full_name"));
    return response;
  }

  public boolean done() {
    return (Boolean) entity.getProperty("done");
  }

  public String getRepository() {
    return object.getJSONObject("repository").getString("full_name");
  }

  private static Date getDate(JSONObject object) {
    String updatedAt = object.getString("updated_at");
    Date date;
    try {
      date = DatastoreConstants.GITHUB_DATE_FORMAT.parse(updatedAt);
    } catch (ParseException e) {
      log.warning("Unparsed date: " + updatedAt + " parsed date: " + e.getMessage());
      date = new Date();
    }
    return date;
  }

  public static class InvalidJsonException extends Exception {
    public InvalidJsonException(String message) {
      super(message);
    }
  }
}
