package com.meab.oauth;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
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
import java.util.logging.Logger;

/**
 * GitHub redirects back to this servlet, which turns around and posts a request for an access
 * token.
 */
public class AccessTokenServlet extends HttpServlet {

  static final String CLIENT_SECRET_KEY = "client_secret";
  static final String CODE_KEY = "code";

  static final String REQUEST_URL = "https://github.com/login/oauth/access_token";
  static final String USER_URL = "https://api.github.com/notifications";

  private static final Logger log = Logger.getLogger(AccessTokenServlet.class.getName());

  private final SecretDatastore secretDatastore = new SecretDatastore();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    Map<String, Object> parameterMap = request.getParameterMap();
    if (parameterMap.containsKey("code")) {
      String accessToken = requestToken(request);
      response.getWriter().write(getUserInfo(accessToken));
    } else if (parameterMap.containsKey("access_token")) {
      saveToken(request, parameterMap);
    } else {
      System.out.println("Unrecognized request! " + parameterMap);
    }
  }

  private void saveToken(HttpServletRequest request, Map<String, Object> parameterMap) {
    log.warning("In save token: " + parameterMap);
  }

  private String requestToken(HttpServletRequest request)
    throws IOException {
    // TODO: check state.
    String code = request.getParameter("code");

    HttpPost httpPost = new HttpPost(REQUEST_URL);
    List<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair(GitHubServlet.CLIENT_ID_KEY, GitHubServlet.CLIENT_ID_VALUE));
    params.add(new BasicNameValuePair(CLIENT_SECRET_KEY, getClientSecretValue()));
    params.add(new BasicNameValuePair(CODE_KEY, code));
    params.add(new BasicNameValuePair(GitHubServlet.STATE_KEY, "QUERY"));
    httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

    HttpClient httpClient = new DefaultHttpClient();
    HttpResponse postResponse = httpClient.execute(httpPost);

    HttpEntity postEntity = postResponse.getEntity();
    String responseContent = EntityUtils.toString(postEntity);
    log.info("Received: " + responseContent);
    String tokens[] = responseContent.split("&");
    String accessToken = null;
    for (String token : tokens) {
      if (token.startsWith("access_token=")) {
        accessToken = token.substring("access_token=".length());
      }
    }
    EntityUtils.consumeQuietly(postEntity);
    return accessToken;
  }

  private String getUserInfo(String accessToken) throws IOException {
    HttpClient httpClient = new DefaultHttpClient();
    HttpGet getRequest = new HttpGet(USER_URL + "?since=2017-01-01T22:01:45Z");
    getRequest.addHeader("Authorization", "token " + accessToken);
    HttpResponse response = httpClient.execute(getRequest);
    HttpEntity entity = response.getEntity();
    String body = EntityUtils.toString(entity, "UTF-8");
    EntityUtils.consumeQuietly(entity);
    return body;
  }

  private String getClientSecretValue() {
    return secretDatastore.get(SecretDatastore.GITHUB_ID);
  }
}
