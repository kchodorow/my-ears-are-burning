package com.meab.oauth;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import java.util.logging.Logger;

public class UserDatastore {
  private static final Logger log = Logger.getLogger(UserDatastore.class.getName());

  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  private static final String USERS_DATABASE = "Users";

  void createUser(String accessToken, String uuid) {
    Entity entity = new Entity(USERS_DATABASE, uuid);
    entity.setProperty("github token", accessToken);
    datastore.put(entity);
  }

  Entity getUser(String uuid) {
    Key userKey = KeyFactory.createKey(USERS_DATABASE, uuid);
    Entity entity;
    try {
      entity = datastore.get(userKey);
    } catch (EntityNotFoundException e) {
      log.info("Got uuid " + uuid + " from cookie, but no user found.");
      return null;
    }
    return entity;
  }
}
