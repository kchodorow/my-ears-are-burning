package com.meab.oauth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.verify;

@RunWith(JUnit4.class)
public class GitHubServletTest {
  @Test
  public void testGet() throws Exception {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    GitHubServlet servlet = new GitHubServlet();
    servlet.doGet(request, response);
    verify(response).sendRedirect(contains(
      "https://github.com/login/oauth/authorize?client_id=63784a223920d4d5609c"
        + "&scope=user%20notifications&state="));
  }

}
