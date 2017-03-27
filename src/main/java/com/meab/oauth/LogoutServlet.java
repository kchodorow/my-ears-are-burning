package com.meab.oauth;

import com.meab.DatastoreConstants;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LogoutServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    for (Cookie cookie : request.getCookies()) {
      if (cookie.getName().equals(DatastoreConstants.User.COOKIE_NAME)) {
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        break;
      }
    }
    response.sendRedirect("/");
  }
}
