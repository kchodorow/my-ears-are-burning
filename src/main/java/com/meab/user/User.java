package com.meab.user;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.meab.DatastoreConstants;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
public class User {
  private static final Logger log = Logger.getLogger(User.class.getName());

  // The name that cookies coming from the client are under.
  // TODO: change this so the value doesn't match the ID field of the user.
  private static final String COOKIE_NAME = "id";
  private final Entity entity;

  private User(Entity entity) {
    this.entity = entity;
  }

  public static User create(String accessToken, JSONObject userInfo) throws IOException {
    Entity entity = new Entity(
      KeyFactory.createKey(DatastoreConstants.User.DATASTORE, userInfo.getInt("id")));
    entity.setProperty(DatastoreConstants.User.COOKIE, UUID.randomUUID().toString());
    entity.setProperty(DatastoreConstants.User.ACCESS_TOKEN, accessToken);
    entity.setProperty(DatastoreConstants.User.LAST_UPDATED, new Date(0));
    entity.setProperty(DatastoreConstants.User.TRACKED_REPOSITORIES, Lists.newArrayList());
    entity.setProperty(DatastoreConstants.User.USER_INFO, new Text(userInfo.toString()));
    entity.setProperty(DatastoreConstants.User.MAX_REPOS, 1);
    entity.setProperty(
      DatastoreConstants.User.SUBSCRIPTION_INFO, new Text(new JSONObject().toString()));
    return new User(entity);
  }

  public static User fromEntity(Entity entity) {
    return new User(entity);
  }

  public Entity getEntity() {
    return entity;
  }

  private JSONObject getUserInfo() {
    String userInfo = ((Text) entity.getProperty(DatastoreConstants.User.USER_INFO)).getValue();
    try {
      return new JSONObject(userInfo);
    } catch (JSONException e) {
      log.warning("Unable to parse user info: " + userInfo);
      return new JSONObject();
    }
  }

  public long id() {
    return entity.getKey().getId();
  }

  public String getUsername() {
    return getUserInfo().getString("login");
  }

  @Nullable
  public String accessToken() {
    return (String) entity.getProperty(DatastoreConstants.User.ACCESS_TOKEN);
  }

  public List<String> trackedRepositories() {
    return (List<String>) entity.getProperty(DatastoreConstants.User.TRACKED_REPOSITORIES);
  }

  public int maxRepositories() {
    return (int) entity.getProperty(DatastoreConstants.User.MAX_REPOS);
  }

  public JSONObject subscriptionInfo() {
    String subscriptionInfo =
      ((Text) entity.getProperty(DatastoreConstants.User.SUBSCRIPTION_INFO)).getValue();
    try {
      return new JSONObject(subscriptionInfo);
    } catch (JSONException e) {
      log.warning("Unable to parse json: " + subscriptionInfo);
      return new JSONObject();
    }
  }

  public Date lastUpdated() {
    return (Date) entity.getProperty(DatastoreConstants.User.LAST_UPDATED);
  }

  @Nullable
  public String cookieId() {
    return (String) entity.getProperty(DatastoreConstants.User.COOKIE);
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
    String cookie = (String) entity.getProperty(DatastoreConstants.User.COOKIE);
    Preconditions.checkNotNull(cookie);
    response.addCookie(new Cookie(COOKIE_NAME, cookie));
    response.addCookie(new Cookie("username", getUsername()));
  }

  public static void unsetCookie(String cookieId, HttpServletResponse response) {
    Cookie cookie = new Cookie(COOKIE_NAME, cookieId);
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }
}
