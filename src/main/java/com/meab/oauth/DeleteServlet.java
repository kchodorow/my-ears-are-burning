package com.meab.oauth;

import com.meab.servlet.MeabServlet;
import com.meab.servlet.MeabServletException;
import com.meab.user.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class DeleteServlet extends MeabServlet {
  private static final Logger log = Logger.getLogger(DeleteServlet.class.getName());

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    User user = null;
    try {
      user = getUser(request, response);
    } catch (MeabServletException e) {
      log.warning(e.getMessage());
      return;
    }
    userDatastore.delete(user);
  }
}
