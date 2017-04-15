package com.meab.migration;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.meab.DatastoreConstants;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * I was accidentally saving notifications with the user id stored as a string. It is a long.
 */
public class NotificationBug extends HttpServlet {
  private static final Logger log = Logger.getLogger(NotificationBug.class.getName());

  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PreparedQuery preparedQuery =
      datastore.prepare(new Query(DatastoreConstants.Notifications.DATASTORE));
    int count = 0;
    for (Entity entity : preparedQuery.asIterable()) {
      Object oldUserId = entity.getProperty(DatastoreConstants.Notifications.USER_ID);
      if (oldUserId == null) {
        log.warning(entity.getKey() + " had no user id");
      } else if (oldUserId instanceof String) {
        int newUserId = Integer.parseInt(oldUserId.toString());
        entity.setProperty(DatastoreConstants.Notifications.USER_ID, newUserId);
        datastore.put(entity);
        count++;
      } else if (oldUserId instanceof Long) {
        // Already converted.
      } else {
        log.warning("No idea what's going on with " + entity.getKey() + ": " + oldUserId);
      }
    }
    response.getWriter().write(count + " converted.");
  }
}
