package com.meab.subscriptions;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.meab.DatastoreConstants;
import com.meab.notifications.Notification;
import com.meab.notifications.NotificationDatastore;
import com.meab.servlet.MeabServletException;
import com.meab.user.User;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Customer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Handle subscriptions with Stripe.
 */
public class SubscribeServlet extends SubscriptionServlet {
  private static final Logger log = Logger.getLogger(SubscribeServlet.class.getName());

  private static final String STRIPE_TOKEN = "stripeToken";
  private static final String STRIPE_EMAIL = "stripeEmail";
  private static final String SUBSCRIPTION_ID = "meab";
  private final NotificationDatastore notificationDatastore = new NotificationDatastore();

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    User user;
    try {
      user = getUser(request, response);
    } catch (MeabServletException e) {
      log.warning("Auth error during subscribe: " + e.getMessage());
      response.sendRedirect("/user?msg=auth-error");
      return;
    }

    String stripeToken = request.getParameter(STRIPE_TOKEN);
    String stripeEmail = request.getParameter(STRIPE_EMAIL);

    Map<String,Object> customerParams = ImmutableMap.<String, Object>builder()
      .put("source", stripeToken)
      .put("email", stripeEmail)
      .put("plan", SUBSCRIPTION_ID)
      .build();
    Customer customer;
    try {
      customer = Customer.create(customerParams);
    } catch (AuthenticationException | InvalidRequestException | APIConnectionException
      | CardException | APIException e) {
      log.warning(e.getClass().getName() + ": " + e.getMessage() + " for " + stripeEmail
        + "(" + stripeToken + ")");
      response.sendRedirect("/user?msg=subscribe-error");
      return;
    }

    Entity userEntity = user.getEntity();
    Set<String> trackedRepositories = user.trackedRepositories();
    for (Entity entity : notificationDatastore.getNotifications(user.id())) {
      Notification notification = Notification.fromEntity(entity);
      if (notification == null) {
        continue;
      }
      trackedRepositories.add(notification.getRepository());
    }

    userEntity.setProperty(
      DatastoreConstants.User.TRACKED_REPOSITORIES,
      Lists.newArrayList(trackedRepositories.iterator()));
    userEntity.setProperty(DatastoreConstants.User.SUBSCRIPTION_INFO, new Text(customer.toJson()));
    userEntity.setProperty(DatastoreConstants.User.MAX_REPOS, Integer.MAX_VALUE);
    userDatastore.update(userEntity);

    response.sendRedirect("/user?msg=subscribe");
  }
}
