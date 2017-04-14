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
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    super.doGet(request, response);
    JSONObject user = new JSONObject();
    user.put("id", 12345);
    user.put("login", "k");
    response.getWriter().write(user.toString());
  }
}
