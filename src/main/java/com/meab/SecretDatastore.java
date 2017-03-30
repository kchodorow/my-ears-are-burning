package com.meab;

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

  public static final String GITHUB_ID = "github";
  private static final String STATE_MACHINE_ID = "state machine";
  static final String[] keys = {GITHUB_ID, STATE_MACHINE_ID};

  private static final String ENTITY_TYPE = "secret";

  private static final Logger log = Logger.getLogger(SecretDatastore.class.getName());

  private final DatastoreService datastore;

  public SecretDatastore() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    String uri = request.getRequestURI();
    int qMark = uri.indexOf("?");
    if (qMark < 0) {
      response.getWriter().write("Nothing to do.");
      return;
    }

    uri = uri.substring(qMark + 1);
    String params[] = uri.split("&");
    String key = getKey(params[0]);
    String value = getValue(params[1]);
    findOrInsert(key, value);

    response.getWriter().write("Wrote secrets.");
  }

  private String getKey(String params) {
    String param[] = params.split("=");
    if (!param[0].equals("key")) {
      return null;
    }
    for (String key : keys) {
      String id = param[1];
      if (key.equals(id)) {
        return id;
      }
    }
    return null;
  }

  private String getValue(String params) {
    String param[] = params.split("=");
    if (param[0].equals("value")) {
      return param[1];
    }
    return null;
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
