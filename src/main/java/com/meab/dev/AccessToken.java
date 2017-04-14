package com.meab.dev;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Testing servlet mimicking GitHub's access token.
 */
public class AccessToken extends DevServlet {
  @Override
  public void post(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.getWriter().write(
      "access_token=e72e16c7e42f292c6912e7710c838347ae178b4a&scope=user%2Cnotifications");
  }
}
