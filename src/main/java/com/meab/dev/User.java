package com.meab.dev;

import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Mocks GitHub's user info response.
 */
public class User extends DevServlet {
  @Override
  public void get(HttpServletRequest request, HttpServletResponse response) throws IOException {
    JSONObject user = new JSONObject(
      "{\"disk_usage\":50665,\"avatar_url\":\"https://avatars1.githubusercontent.com/u/17042?v=3\",\"two_factor_authentication\":true,\"private_gists\":7,\"gravatar_id\":\"\",\"public_gists\":23,\"location\":\"New York City\",\"owned_private_repos\":1,\"site_admin\":false,\"type\":\"User\",\"blog\":\"http://www.kchodorow.com\",\"total_private_repos\":1,\"id\":17042,\"following\":19,\"followers\":212,\"public_repos\":64,\"name\":\"Kristina\",\"following_url\":\"https://api.github.com/users/kchodorow/following{/other_user}\",\"created_at\":\"2008-07-14T19:27:34Z\",\"events_url\":\"https://api.github.com/users/kchodorow/events{/privacy}\",\"login\":\"kchodorow\",\"subscriptions_url\":\"https://api.github.com/users/kchodorow/subscriptions\",\"repos_url\":\"https://api.github.com/users/kchodorow/repos\",\"gists_url\":\"https://api.github.com/users/kchodorow/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/kchodorow/starred{/owner}{/repo}\",\"url\":\"https://api.github.com/users/kchodorow\",\"html_url\":\"https://github.com/kchodorow\",\"collaborators\":0,\"hireable\":null,\"updated_at\":\"2017-04-05T15:52:49Z\",\"plan\":{\"collaborators\":0,\"private_repos\":0,\"name\":\"free\",\"space\":976562499},\"bio\":null,\"email\":\"k.chodorow@gmail.com\",\"company\":\"Google\"}");
    response.getWriter().write(user.toString());
  }
}
