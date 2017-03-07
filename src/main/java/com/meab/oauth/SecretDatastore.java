package com.meab.oauth;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Stores the secret key in the datastore.
 */
public class SecretDatastore extends HttpServlet {

  static final String GITHUB_ID = "github";
  static final String STATE_MACHINE_ID = "state machine";

  private static final String ENTITY_TYPE = "secret";

  private static final Logger log = Logger.getLogger(SecretDatastore.class.getName());

  private final DatastoreService datastore;

  public SecretDatastore() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    findOrInsert(GITHUB_ID, null);
    findOrInsert(STATE_MACHINE_ID, null);

    response.getWriter().write("Wrote secrets.");
  }

  private void findOrInsert(String key, String value) {
    Entity entity = getEntity(key);
    if (entity != null) {
      // No need to insert.
      return;
    }

    entity = new Entity(ENTITY_TYPE);
    entity.setProperty("key", key);
    entity.setProperty("value", value);
    datastore.put(entity);
  }

  private Entity getEntity(String key) {
    Query.FilterPredicate predicate = new Query.FilterPredicate(
      "key", Query.FilterOperator.EQUAL, key);
    Query query = new Query(ENTITY_TYPE).setFilter(predicate);
    PreparedQuery preparedQuery = datastore.prepare(query);
    try {
      return preparedQuery.asSingleEntity();
    } catch (PreparedQuery.TooManyResultsException e) {
      log.warning("Found too many " + key + " secrets: " + e.getMessage());
      return null;
    }
  }

  public String get(String key) {
    Entity entity = getEntity(key);
    if (entity == null) {
      return null;
    }
    return entity.getProperty("value").toString();
  }
}
