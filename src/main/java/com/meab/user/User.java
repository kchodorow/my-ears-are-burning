package com.meab.user;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.meab.DatastoreConstants;
import org.json.JSONObject;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;

@AutoValue
public abstract class User {

  public static User create(String uuid, String accessToken, JSONObject userInfo) {
    return new AutoValue_User(uuid, accessToken, new Date(0), ImmutableSet.<String>of(), userInfo);
  }

  static User fromEntity(Entity entity) {
    ImmutableSet<String> repos = ImmutableSet.of();
    if (entity.getProperty(DatastoreConstants.User.TRACKED_REPOSITORIES) != null) {
      ArrayList<String> possibleRepos = (ArrayList<String>) entity.getProperty(
        DatastoreConstants.User.TRACKED_REPOSITORIES);
      ImmutableSet.Builder<String> builder = ImmutableSet.builder();
      for (String repo : possibleRepos) {
        if (repo != null) {
          builder.add(repo);
        }
      }
      repos = builder.build();
    }
    return new AutoValue_User(
      entity.getKey().getName(),
      entity.getProperty(DatastoreConstants.User.ACCESS_TOKEN).toString(),
      (Date) entity.getProperty(DatastoreConstants.User.LAST_UPDATED),
      repos,
      new JSONObject(entity.getProperty(DatastoreConstants.User.USER_INFO).toString()));
  }

  public Entity getEntity() {
    Entity entity = new Entity(KeyFactory.createKey(DatastoreConstants.User.DATASTORE, id()));
    entity.setProperty(DatastoreConstants.User.ACCESS_TOKEN, accessToken());
    entity.setProperty(DatastoreConstants.User.LAST_UPDATED, lastUpdated());
    entity.setProperty(
      DatastoreConstants.User.TRACKED_REPOSITORIES,
      Lists.newArrayList(trackedRepositories().iterator()));
    entity.setProperty(DatastoreConstants.User.USER_INFO, userInfo().toString());
    return entity;
  }

  public String getUsername() {
    return userInfo().getString("login");
  }

  public static String getIdFromCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    String userId = null;
    for (Cookie cookie : cookies) {
      if (cookie.getName().equals(DatastoreConstants.User.USER_ID)) {
        userId = cookie.getValue();
      }
    }
    return userId;
  }

  public void setCookie(HttpServletResponse response) {
    Cookie cookie = new Cookie(DatastoreConstants.User.COOKIE_NAME, id());
    response.addCookie(cookie);
  }

  public static void unsetCookie(String id, HttpServletResponse response) {
    Cookie cookie = new Cookie(DatastoreConstants.User.COOKIE_NAME, id);
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }

  public abstract String id();
  public abstract String accessToken();
  public abstract Date lastUpdated();
  public abstract ImmutableSet<String> trackedRepositories();
  public abstract JSONObject userInfo();

  public String getLastUpdated() {
    return DatastoreConstants.GITHUB_DATE_FORMAT.format(lastUpdated());
  }
}
