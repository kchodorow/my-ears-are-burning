package com.meab.user;

import com.google.appengine.api.datastore.Entity;
import com.google.auto.value.AutoValue;
import com.meab.DatastoreConstants;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@AutoValue
public abstract class User {

  public static User create(Entity entity) {
    return new AutoValue_User(
      entity,
      entity.getKey().getName(),
      entity.getProperty(DatastoreConstants.User.ACCESS_TOKEN).toString(),
      parseDate(entity.getProperty(DatastoreConstants.User.LAST_UPDATED)));
  }

  private static Date parseDate(Object property) {
    if (property == null) {
      return null;
    }
    // TODO
    return new Date();
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
    Cookie cookie = new Cookie("id", id());
    response.addCookie(cookie);
  }

  public static void unsetCookie(String id, HttpServletResponse response) {
    Cookie cookie = new Cookie("id", id);
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }

  public abstract Entity entity();
  public abstract String id();
  public abstract String accessToken();
  @Nullable
  public abstract Date lastUpdated();
}
