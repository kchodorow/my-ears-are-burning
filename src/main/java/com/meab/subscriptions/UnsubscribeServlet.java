package com.meab.subscriptions;

import com.google.common.collect.ImmutableMap;
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

public class UnsubscribeServlet extends SubscriptionServlet {
  private static final Logger log = Logger.getLogger(UnsubscribeServlet.class.getName());

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    User user;
    try {
      user = getUser(request, response);
    } catch (MeabServletException e) {
      log.warning(e.getMessage());
      return;
    }
    try {
      Subscription.retrieve(user.subscriptionInfo().getString("id"))
        .cancel(ImmutableMap.<String, Object>of());
    } catch (AuthenticationException | InvalidRequestException | APIConnectionException |
      CardException | APIException e) {
      log.warning(e.getMessage());
    }
  }
}
