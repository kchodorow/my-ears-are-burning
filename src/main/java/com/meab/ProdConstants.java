package com.meab;

import com.google.appengine.api.utils.SystemProperty;

/**
 * Constants for development and production.
 */
public class ProdConstants {
  public static final String GITHUB_LOGIN_URL;
  public static final String GITHUB_ACCESS_TOKEN_URL;
  public static final String GITHUB_NOTIFICATIONS_URL;
  public static final String GITHUB_USER_URL;

  static {
    if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
      GITHUB_LOGIN_URL = "https://github.com/login/oauth/authorize";
      GITHUB_ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";
      GITHUB_NOTIFICATIONS_URL = "https://api.github.com/notifications";
      GITHUB_USER_URL = "https://api.github.com/user";
    } else {
      GITHUB_LOGIN_URL = "/dev/authorize";
      // Apache HTTP client can't figure out how to get/post "/foo", so it has to be spelled out
      // in its entirety.
      GITHUB_ACCESS_TOKEN_URL = "http://localhost:8080/dev/access-token";
      GITHUB_NOTIFICATIONS_URL = "http://localhost:8080/dev/notifications";
      GITHUB_USER_URL = "http://localhost:8080/dev/user";
    }
  }
}
