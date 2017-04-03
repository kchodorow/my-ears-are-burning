package com.meab.oauth;

import com.google.common.base.Strings;
import com.meab.SecretDatastore;
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

  static final String CLIENT_SECRET_KEY = "client_secret";
  static final String CODE_KEY = "code";

  static final String REQUEST_URL = "https://github.com/login/oauth/access_token";

  private final SecretDatastore secretDatastore = new SecretDatastore();
  private final UserDatastore userDatastore = new UserDatastore();
  private final NotificationDatastore notificationDatastore = new NotificationDatastore();
  private final String githubSecret;

  public AccessTokenServlet() {
    githubSecret = secretDatastore.get(SecretDatastore.GITHUB_ID);
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
    User user;
    try {
      user = getUser(tokenResponse, state);
    } catch (LoginException e) {
      throw new IOException(e.getMessage());
    }
    user.setCookie(response);
    response.sendRedirect("/user");
  }

  private String requestToken(String code, String state)
    throws IOException {
    HttpPost httpPost = new HttpPost(REQUEST_URL);
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

  private User getUser(String responseContent, String uuid) throws LoginException {
    String tokens[] = responseContent.split("&");
    String accessToken = null;
    for (String token : tokens) {
      if (token.startsWith("access_token=")) {
        accessToken = token.substring("access_token=".length());
        break;
      }
    }
    if (accessToken == null) {
      log.warning("No access token returned: " + responseContent);
      throw new LoginException("Did not receive access token");
    }

    if (Strings.isNullOrEmpty(uuid)) {
      log.info("state was empty for access token request");
      uuid = UUID.randomUUID().toString();
    }
    User user = userDatastore.getUser(uuid);
    if (user == null) {
      try {
        user = userDatastore.createUser(accessToken);
        // Pre-load user notifications.
        notificationDatastore.fetchNotifications(user);
      } catch (IOException e) {
        throw new LoginException("Couldn't create user " + uuid + ": " + e.getMessage());
      }
    }
    return user;
  }
}
