package com.meab.oauth;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.meab.DatastoreConstants;
import com.meab.SecretDatastore;
import com.meab.ProdConstants;
import com.meab.notifications.NotificationDatastore;
import com.meab.user.User;
import com.meab.user.UserDatastore;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * GitHub redirects back to this servlet, which turns around and posts a request for an access
 * token.
 */
public class AccessTokenServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(AccessTokenServlet.class.getName());

  private static final String CLIENT_SECRET_KEY = "client_secret";
  private static final String CODE_KEY = "code";

  private final UserDatastore userDatastore;
  private final NotificationDatastore notificationDatastore;
  private final String githubSecret;

  public AccessTokenServlet() {
    this(new UserDatastore(), new NotificationDatastore(), new SecretDatastore());
  }

  AccessTokenServlet(
    UserDatastore userDatastore, NotificationDatastore notificationDatastore,
    SecretDatastore secretDatastore) {
    this.userDatastore = userDatastore;
    this.notificationDatastore = notificationDatastore;
    SecretDatastore secretDatastore1 = secretDatastore;
    this.githubSecret = secretDatastore1.get(SecretDatastore.GITHUB_ID);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Map parameterMap = request.getParameterMap();
    if (!parameterMap.containsKey("code")) {
      log.warning("No code received: " + request);
      response.sendRedirect("/");
      return;
    }

    String code = request.getParameter("code");
    String state = request.getParameter("state");
    String tokenResponse = requestToken(code, state);

    String tokens[] = tokenResponse.split("&");
    String accessToken = null;
    for (String token : tokens) {
      if (token.startsWith("access_token=")) {
        accessToken = token.substring("access_token=".length());
        break;
      }
    }
    if (accessToken == null) {
      log.warning("No access token returned: " + tokenResponse);
      throw new IOException("Did not receive access token");
    }

    User user;
    try {
      user = getUser(accessToken);
    } catch (LoginException e) {
      throw new IOException(e.getMessage());
    }
    user.setCookie(response);
    response.sendRedirect("/user");
  }

  private String requestToken(String code, String state)
    throws IOException {
    HttpPost httpPost = new HttpPost(ProdConstants.GITHUB_ACCESS_TOKEN_URL);
    List<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair(GitHubServlet.CLIENT_ID_KEY, GitHubServlet.CLIENT_ID_VALUE));
    params.add(new BasicNameValuePair(CLIENT_SECRET_KEY, githubSecret));
    params.add(new BasicNameValuePair(CODE_KEY, code));
    params.add(new BasicNameValuePair(GitHubServlet.STATE_KEY, state));
    httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

    HttpClient httpClient = new DefaultHttpClient();
    HttpResponse postResponse = httpClient.execute(httpPost);

    HttpEntity postEntity = postResponse.getEntity();
    String responseContent = EntityUtils.toString(postEntity);
    EntityUtils.consumeQuietly(postEntity);
    return responseContent;
  }

  User getUser(String accessToken) throws LoginException {
    Entity userEntity;
    JSONObject object;
    try {
      object = userDatastore.getGitHubUserByAccessToken(accessToken);
    } catch (IOException e) {
      throw new LoginException(
        "Couldn't get GitHub user with access token " + accessToken + ": " + e.getMessage());
    }
    try {
      userEntity = userDatastore.getEntityById(object.getInt("id"));
    } catch (EntityNotFoundException e) {
      try {
        User newUser = User.create(accessToken, object);
        userEntity = newUser.getEntity();
      } catch (IOException e1) {
        throw new LoginException(
          "Unable to parse json: " + object.toString() + ": " + e1.getMessage());
      }
    }

    String cookie = (String) userEntity.getProperty(DatastoreConstants.User.COOKIE);
    if (cookie == null) {
      userEntity.setProperty(DatastoreConstants.User.COOKIE, UUID.randomUUID().toString());
    }
    userDatastore.update(userEntity);
    User user = User.fromEntity(userEntity);

    try {
      // Pre-load user notifications.
      notificationDatastore.fetchNotifications(user);
    } catch (IOException e) {
      log.warning("Failed to fetch notifications: " + e.getMessage());
      // Ignore for now, hopefully it was a transient error.
    }
    return user;
  }
}
