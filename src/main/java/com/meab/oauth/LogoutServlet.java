package com.meab.oauth;

import com.google.appengine.api.datastore.Entity;
import com.meab.DatastoreConstants;
import com.meab.servlet.MeabServlet;
import com.meab.servlet.MeabServletException;
import com.meab.user.User;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class LogoutServlet extends MeabServlet {
  private static final Logger log = Logger.getLogger(LogoutServlet.class.getName());

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    User user = null;
    try {
      user = getUser(request, response);
    } catch (MeabServletException e) {
      // That's fine, we're logging out anyway.
      log.warning(e.getMessage());
    }
    if (user != null) {
      Entity userEntity = user.getEntity();
      userEntity.setProperty(DatastoreConstants.User.COOKIE, null);
      userDatastore.update(userEntity);
    }
    for (Cookie cookie : request.getCookies()) {
      if (cookie.getName().equals(DatastoreConstants.User.USER_ID)) {
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        break;
      }
    }
    response.sendRedirect("/");
  }
}
