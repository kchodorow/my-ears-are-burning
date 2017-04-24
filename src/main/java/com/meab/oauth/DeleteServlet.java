package com.meab.oauth;

import com.google.common.collect.ImmutableMap;
import com.meab.notifications.NotificationDatastore;
import com.meab.servlet.MeabServlet;
import com.meab.servlet.MeabServletException;
import com.meab.user.User;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Subscription;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class DeleteServlet extends MeabServlet {
  private static final Logger log = Logger.getLogger(DeleteServlet.class.getName());

  NotificationDatastore notificationDatastore = new NotificationDatastore();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    User user;
    try {
      user = getUser(request, response);
    } catch (MeabServletException e) {
      log.warning(e.getMessage());
      return;
    }
    User.unsetCookie(user.cookieId(), response);
    try {
      if (user.subscriptionInfo().has("id")) {
        Subscription.retrieve(user.subscriptionInfo().getString("id"))
          .cancel(ImmutableMap.<String, Object>of());
      }
    } catch (AuthenticationException | InvalidRequestException | APIConnectionException |
      CardException | APIException e) {
      log.warning(e.getMessage());
    }
    userDatastore.delete(user);
    notificationDatastore.delete(user);
  }
}
