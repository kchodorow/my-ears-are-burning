package com.meab.oauth;

import com.google.appengine.api.datastore.Entity;
import com.google.common.net.UrlEscapers;
import com.meab.user.User;
import com.meab.user.UserDatastore;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Authenticate with GitHub.
 */
public class GitHubServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(GitHubServlet.class.getName());

  static final String CLIENT_ID_KEY = "client_id";
  static final String CLIENT_ID_VALUE = "63784a223920d4d5609c";
  static final String SCOPE_KEY = "scope";
  static final String SCOPE_VALUE = "user notifications";
  static final String STATE_KEY = "state";

  static final String REDIRECT_URL = "https://github.com/login/oauth/authorize";

  /**
   * Step 1: Redirect to GitHub.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    String id = User.getIdFromCookie(request);
    if (id != null) {
      response.getWriter().write("Logged in: " + id);
      return;
    }

    String state = UUID.randomUUID().toString();
    String query = "?" + CLIENT_ID_KEY + "=" + CLIENT_ID_VALUE + "&"
      + SCOPE_KEY + "=" + SCOPE_VALUE + "&"
      + STATE_KEY + "=" + state;
    response.sendRedirect(UrlEscapers.urlFragmentEscaper().escape(REDIRECT_URL + query));
  }
}
