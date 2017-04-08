package com.meab.api;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.meab.notifications.NotificationDatastore;
import com.meab.user.User;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.logging.Logger;

public class MuteServlet extends ApiServlet {
  private static final Logger log = Logger.getLogger(MuteServlet.class.getName());

  private NotificationDatastore notificationDatastore = new NotificationDatastore();

  @Override
  public void apiPost(User user, HttpServletRequest request, JSONObject response) {
    String notificationId = request.getParameter("id");
    Entity notification;
    try {
      notification = notificationDatastore.getNotification(notificationId);
    } catch (EntityNotFoundException e) {
      log.warning("Notification not found: " + e.getMessage());
      return;
    }
    notification.setProperty("unread", false);
    notificationDatastore.update(notification);
  }
}
