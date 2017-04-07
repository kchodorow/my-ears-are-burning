package com.meab;

import java.text.SimpleDateFormat;

public class DatastoreConstants {

  public static final SimpleDateFormat GITHUB_DATE_FORMAT = new SimpleDateFormat(
    "yyyy-MM-dd'T'HH:mm:ss'Z'");
  public static final int API_VERSION = 1;

  public static class User {
    public static final String DATASTORE = "Users";
    public static final String ACCESS_TOKEN = "github token";
    public static final String USER_ID = "id";
    public static final String LAST_UPDATED = "last updated";

    public static final String TRACKED_REPOSITORIES = "repos";
    public static final String USER_INFO = "user info";
    public static final String COOKIE = "cookie";
    public static final String MAX_REPOS = "max repos";
    public static final String SUBSCRIPTION_INFO = "subscription";
  }

  public static class Notifications {
    public static final String DATASTORE = "Notifications";
    public static final String USER_ID = "user id";
    public static final String FULL_TEXT = "full text";
  }

}
