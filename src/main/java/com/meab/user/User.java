package com.meab.user;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.auto.value.AutoValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.meab.DatastoreConstants;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * User info from GitHub:
 * {
 *     "organizations_url":"https://api.github.com/users/kchodorow/orgs",
 *     "received_events_url":"https://api.github.com/users/kchodorow/received_events",
 *     "disk_usage":49945,
 *     "avatar_url":"https://avatars1.githubusercontent.com/u/17042?v=3",
 *     "two_factor_authentication":true,
 *     "private_gists":7,
 *     "gravatar_id":"",
 *     "public_gists":18,
 *     "location":"New York City",
 *     "owned_private_repos":1,
 *     "site_admin":false,
 *     "type":"User",
 *     "blog":"http://www.kchodorow.com",
 *     "total_private_repos":1,
 *     "id":17042,
 *     "following":19,
 *     "followers":211,
 *     "public_repos":64,
 *     "name":"Kristina",
 *     "created_at":"2008-07-14T19:27:34Z",
 *     "login":"kchodorow",
 *     "url":"https://api.github.com/users/kchodorow",
 *     "html_url":"https://github.com/kchodorow",
 *     "collaborators":0,
 *     "hireable":null,
 *     "updated_at":"2017-02-28T12:18:49Z",
 *     "plan":{
 *         "collaborators":0,
 *         "private_repos":0,
 *         "name":"free",
 *         "space":976562499
 *     },
 *     "bio":null,
 *     "email":"k.chodorow@gmail.com",
 *     "company":"Google",
 * }
 */
@AutoValue
public abstract class User {
  private static final Logger log = Logger.getLogger(User.class.getName());

  // The name that cookies coming from the client are under.
  // TODO: change this so the value doesn't match the ID field of the user.
  private static final String COOKIE_NAME = "id";

  public static User create(String accessToken, JSONObject userInfo) throws IOException {
    try {
      return new AutoValue_User(
        userInfo.getInt("id") + "", UUID.randomUUID().toString(), accessToken, new Date(0),
        Sets.<String>newHashSet(), userInfo);
    } catch (JSONException e) {
      log.warning(e.getMessage() + ": " + userInfo.toString());
      throw new IOException(e.getMessage());
    }
  }

  static User fromEntity(Entity entity) {
    Set<String> repos = Sets.newHashSet();
    if (entity.getProperty(DatastoreConstants.User.TRACKED_REPOSITORIES) != null) {
      ArrayList<String> possibleRepos = (ArrayList<String>) entity.getProperty(
        DatastoreConstants.User.TRACKED_REPOSITORIES);
      for (String repo : possibleRepos) {
        if (repo != null) {
          repos.add(repo);
        }
      }
    }

    String cookieId = entity.getProperty(DatastoreConstants.User.COOKIE) == null
      ? UUID.randomUUID().toString()
      : entity.getProperty(DatastoreConstants.User.COOKIE).toString();
    return new AutoValue_User(
      entity.getKey().getName(),
      cookieId,
      entity.getProperty(DatastoreConstants.User.ACCESS_TOKEN).toString(),
      (Date) entity.getProperty(DatastoreConstants.User.LAST_UPDATED),
      repos,
      new JSONObject(entity.getProperty(DatastoreConstants.User.USER_INFO).toString()));
  }

  public Entity getEntity() {
    Entity entity = new Entity(KeyFactory.createKey(DatastoreConstants.User.DATASTORE, id()));
    entity.setProperty(DatastoreConstants.User.COOKIE, cookieId());
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

  public static String getCookieId(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    String cookieId = null;
    for (Cookie cookie : cookies) {
      if (cookie.getName().equals(COOKIE_NAME)) {
        cookieId = cookie.getValue();
      }
    }
    return cookieId;
  }

  public void setCookie(HttpServletResponse response) {
    response.addCookie(new Cookie(COOKIE_NAME, cookieId()));
    response.addCookie(new Cookie("username", getUsername()));
  }

  public static void unsetCookie(String cookieId, HttpServletResponse response) {
    Cookie cookie = new Cookie(COOKIE_NAME, cookieId);
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }

  public abstract String id();
  public abstract String cookieId();
  public abstract String accessToken();
  public abstract Date lastUpdated();
  public abstract Set<String> trackedRepositories();
  public abstract JSONObject userInfo();

  public String getLastUpdated() {
    return DatastoreConstants.GITHUB_DATE_FORMAT.format(lastUpdated());
  }
}
