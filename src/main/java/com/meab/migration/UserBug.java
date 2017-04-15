package com.meab.migration;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.common.base.Strings;
import com.meab.DatastoreConstants;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class UserBug extends HttpServlet {
  private static final Logger log = Logger.getLogger(NotificationBug.class.getName());

  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PreparedQuery preparedQuery =
      datastore.prepare(new Query(DatastoreConstants.User.DATASTORE));
    int count = 0;
    for (Entity entity : preparedQuery.asIterable()) {
      Key oldKey = entity.getKey();
      String name = oldKey.getName();
      if (Strings.isNullOrEmpty(name)) {
        continue;
      }
      int id = Integer.parseInt(name);
      Entity newEntity = new Entity(KeyFactory.createKey(DatastoreConstants.User.DATASTORE, id));
      newEntity.setPropertiesFrom(entity);
      datastore.put(newEntity);
      datastore.delete(oldKey);
      count++;
    }
    response.getWriter().write(count + " converted.");
  }
}
