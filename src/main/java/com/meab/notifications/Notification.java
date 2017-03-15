package com.meab.notifications;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.meab.DatastoreConstants;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
 *          "id":20773773,"name":"bazel","full_name":"bazelbuild/bazel",
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
  private static final SimpleDateFormat GITHUB_DATE_FORMAT = new SimpleDateFormat(
    "yyyy-MM-dd'T'HH:mm:ss'Z'");

  private final boolean unread;
  // author, state_change, mention
  private final String reason;
  private Date date;
  private final JSONObject object;

  public Notification(JSONObject jsonObject) {
    this.unread = jsonObject.getBoolean("unread");
    this.reason = jsonObject.getString("reason");
    this.object = jsonObject;
    String updatedAt = jsonObject.getString("updated_at");
    try {
      this.date = GITHUB_DATE_FORMAT.parse(updatedAt);
    } catch (ParseException e) {
      log.warning("Unparsed date: " + updatedAt + " parsed date: " + e.getMessage());
      this.date = new Date();
    }
  }

  static Notification fromEntity(Entity entity) {
    Text fullText = (Text) entity.getProperty(DatastoreConstants.Notifications.FULL_TEXT);
    if (fullText == null) {
      log.warning("No text for " + entity);
      return null;
    }
    JSONObject object;
    try {
      object = new JSONObject(fullText.getValue());
    } catch (JSONException e) {
      log.warning(e.getMessage() + " entity: " + fullText.getValue());
      return null;
    }
    return new Notification(object);
  }

  public Entity getEntity() {
    Key key = KeyFactory.createKey(
      DatastoreConstants.Notifications.DATASTORE, object.getString("id"));
    Entity entity = new Entity(key);
    entity.setProperty("unread", unread);
    entity.setProperty("date", date);
    entity.setProperty(DatastoreConstants.Notifications.FULL_TEXT, new Text(object.toString()));
    return entity;
  }

  String getHtml() {
    return "<p><a href=\"" + object.getJSONObject("subject").getString("latest_comment_url")
      + "\">" + object.getJSONObject("subject").getString("title") + "</a></p>";
  }
}
