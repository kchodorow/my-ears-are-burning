package com.meab.user;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.meab.DatastoreConstants;

import java.util.Date;
import java.util.logging.Logger;

public class UserDatastore {
  private static final Logger log = Logger.getLogger(UserDatastore.class.getName());

  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  public User createUser(String accessToken, String uuid) {
    Entity entity = new Entity(getKey(uuid));
    entity.setProperty(DatastoreConstants.User.ACCESS_TOKEN, accessToken);
    datastore.put(entity);
    return User.create(entity);
  }

  public User getUser(String uuid) {
    Entity entity;
    try {
      entity = datastore.get(getKey(uuid));
    } catch (EntityNotFoundException e) {
      log.info("Got uuid " + uuid + " from cookie, but no user found.");
      return null;
    }
    if (entity == null) {
      return null;
    }
    return User.create(entity);
  }

  public void setLastUpdated(User user) {
    Entity entity = user.entity();
    entity.setProperty(DatastoreConstants.User.LAST_UPDATED, new Date(System.currentTimeMillis()));
    datastore.put(entity);
  }

  private Key getKey(String uuid) {
    return KeyFactory.createKey(DatastoreConstants.User.DATASTORE, uuid);
  }
}
