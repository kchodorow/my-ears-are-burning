package com.meab.api;

import com.google.appengine.api.datastore.Entity;
import com.meab.DatastoreConstants;
import com.meab.notifications.Notification;
import com.meab.notifications.NotificationDatastore;
import com.meab.user.UserDatastore;
import com.meab.user.User;
import org.json.JSONArray;
import org.json.JSONObject;

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

    JSONArray array = new JSONArray();
    for (Entity entity : notificationDatastore.getNotifications(id)) {
      Notification notification = Notification.fromEntity(entity);
      if (notification != null) {
        array.put(notification.getJson());
      }
    }

    JSONObject responseJson = new JSONObject();
    responseJson.put("notifications", array);
    responseJson.put("api", DatastoreConstants.API_VERSION);

    response.setContentType("application/json");
    response.getWriter().write(responseJson.toString());
  }

  private static Date getOneHourAgo() {
    // TODO: do this in a less error-prone way.
    return new Date(System.currentTimeMillis() - (60 * 60 * 1000));
  }
}
