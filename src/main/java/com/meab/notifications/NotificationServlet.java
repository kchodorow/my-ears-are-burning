package com.meab.notifications;

import com.google.appengine.api.datastore.Entity;
import com.meab.user.UserDatastore;
import com.meab.user.User;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class NotificationServlet extends HttpServlet {
  private UserDatastore userDatastore = new UserDatastore();
  private NotificationDatastore notificationDatastore = new NotificationDatastore();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String id = User.getIdFromCookie(request);
    if (id == null) {
      response.getWriter().write("Not logged in.");
      return;
    }
    User user = userDatastore.getUser(id);
    if (user == null) {
      User.unsetCookie(id, response);
      response.getWriter().write("Couldn't find user for " + id);
      return;
    }

    Date oneHourAgo = NotificationServlet.getOneHourAgo();
    if (user.lastUpdated() == null || user.lastUpdated().before(oneHourAgo)) {
      notificationDatastore.fetchNotifications(user);
    }
    for (Entity entity : notificationDatastore.getNotifications(id)) {
      Notification notification = Notification.fromEntity(entity);
      if (notification != null) {
        response.getWriter().write(notification.getHtml());
      }
    }
  }

  private static Date getOneHourAgo() {
    // TODO: do this in a less error-prone way.
    return new Date(System.currentTimeMillis() - (60 * 60 * 1000));
  }
}
