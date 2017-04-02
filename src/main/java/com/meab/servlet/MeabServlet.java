package com.meab.servlet;

import com.meab.user.User;
import com.meab.user.UserDatastore;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * General servlet that knows how to fetch user info.
 */
public class MeabServlet extends HttpServlet {
  protected UserDatastore userDatastore = new UserDatastore();

  protected User getUser(HttpServletRequest request, HttpServletResponse response)
    throws MeabServletException {
    String cookie = User.getCookieId(request);
    if (cookie == null) {
      throw new MeabServletException("Not logged in");
    }
    User user = userDatastore.getUser(cookie);
    if (user == null) {
      User.unsetCookie(cookie, response);
      throw new MeabServletException("Couldn't find user for " + cookie);
    }
    return user;
  }
}
