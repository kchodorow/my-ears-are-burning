package com.meab.api;

import com.google.appengine.api.datastore.Entity;
import com.meab.DatastoreConstants;
import com.meab.notifications.Notification;
import com.meab.notifications.NotificationDatastore;
import com.meab.user.User;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;

public class NotificationServlet extends ApiServlet {
  private NotificationDatastore notificationDatastore = new NotificationDatastore();

  @Override
  public void apiGet(User user, HttpServletRequest request, JSONObject response)
    throws ApiException {
    Date oneHourAgo = NotificationServlet.getOneHourAgo();
    if (user.lastUpdated() == null || user.lastUpdated().before(oneHourAgo)) {
      try {
        notificationDatastore.fetchNotifications(user);
      } catch (IOException e) {
        throw new ApiException(e.getMessage());
      }
    }

    JSONObject notificationsByRepository = new JSONObject();
    for (Entity entity : notificationDatastore.getNotifications(user.id())) {
      Notification notification = Notification.fromEntity(entity);
      if (notification == null || notification.isRead()) {
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

    response.put("notifications", notificationsByRepository);
    response.put("api", DatastoreConstants.API_VERSION);
  }

  private static Date getOneHourAgo() {
    // TODO: do this in a less error-prone way.
    return new Date(System.currentTimeMillis() - (60 * 60 * 1000));
  }
}
