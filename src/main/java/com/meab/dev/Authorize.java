package com.meab.dev;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Testing servlet mimicking GitHub's login.
 */
public class Authorize extends DevServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    super.doGet(request, response);
    response.sendRedirect("/github?code=123456&state=67890");
  }
}
