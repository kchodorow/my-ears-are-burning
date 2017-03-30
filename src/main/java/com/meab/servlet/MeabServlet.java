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
    String id = User.getIdFromCookie(request);
    if (id == null) {
      throw new MeabServletException("Not logged in");
    }
    User user = userDatastore.getUser(id);
    if (user == null) {
      User.unsetCookie(id, response);
      throw new MeabServletException("Couldn't find user for " + id);
    }
    return user;
  }
}
