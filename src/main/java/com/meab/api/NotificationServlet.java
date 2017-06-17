package com.meab.api;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.meab.DatastoreConstants;
import com.meab.notifications.Notification;
import com.meab.notifications.NotificationDatastore;
import com.meab.user.User;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

public class NotificationServlet extends ApiServlet {
  private static final Logger log = Logger.getLogger(NotificationServlet.class.getName());
  private NotificationDatastore notificationDatastore = new NotificationDatastore();

  @Override
  public void apiGet(User user, HttpServletRequest request, JSONObject response)
    throws ApiException {
    Date oneHourAgo = NotificationServlet.getOneHourAgo();
    if (user.lastUpdated() == null || user.lastUpdated().before(oneHourAgo)) {
      try {
        log.info(
          "Requesting notifications for " + user.getUsername() + ", last updated "
            + user.lastUpdated());
        notificationDatastore.fetchNotifications(user);
      } catch (IOException e) {
        throw new ApiException(e.getMessage());
      }
    }

    JSONObject notificationsByRepository = new JSONObject();
    response.put("notifications", notificationsByRepository);
    response.put("api", DatastoreConstants.API_VERSION);
    response.put("tracked", user.trackedRepositories().size());
    response.put("ok", true);
    if (user.trackedRepositories().size() == 0) {
      return;
    }

    for (Entity entity : notificationDatastore.getNotifications(user.id())) {
      Notification notification = Notification.fromEntity(entity);
      if (!shouldAdd(notification, user)) {
        continue;
      }

      String repository = notification.getRepository();
      if (!notificationsByRepository.has(repository)) {
        notificationsByRepository.put(repository, new JSONArray());
      }
      try {
        ((JSONArray) notificationsByRepository.get(repository)).put(notification.getJson());
      } catch (Notification.InvalidJsonException e) {
        // Skip this element.
      }
    }
  }

  private boolean shouldAdd(Notification notification, User user) {
    return !(notification == null || notification.done())
      && user.trackedRepositories().contains(notification.getRepository())
      && !notification.isStupidReason();
  }

  @Override
  public void apiPost(User user, HttpServletRequest request, JSONObject response) {
    String notificationId = request.getParameter("read");
    Entity notification;
    try {
      notification = notificationDatastore.getNotification(notificationId);
    } catch (EntityNotFoundException e) {
      log.warning("Notification not found: " + e.getMessage());
      return;
    }
    notification.setProperty("done", true);
    notificationDatastore.update(notification);
  }

  private static Date getOneHourAgo() {
    // TODO: do this in a less error-prone way.
    return new Date(System.currentTimeMillis() - (60 * 60 * 1000));
  }
}
