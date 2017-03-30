package com.meab.subscriptions;

import com.google.appengine.api.datastore.Entity;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.meab.DatastoreConstants;
import com.meab.SecretDatastore;
import com.meab.servlet.MeabServlet;
import com.meab.servlet.MeabServletException;
import com.meab.user.User;
import com.stripe.Stripe;
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

/**
 * Handle subscriptions with Stripe.
 */
public class SubscribeServlet extends MeabServlet {

  private static final String STRIPE_SK_KEY = "stripe";
  private static final String STRIPE_TOKEN = "stripeToken";
  private static final String STRIPE_EMAIL = "stripeEmail";
  private static final String SUBSCRIPTION_ID = "meab";

  public SubscribeServlet() {
    SecretDatastore secretDatastore = new SecretDatastore();
    String apiKey = secretDatastore.get(STRIPE_SK_KEY);
    Preconditions.checkNotNull(apiKey, "Stripe API key not found in DB with key " + STRIPE_SK_KEY);
    Stripe.apiKey = apiKey;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    User user;
    try {
      user = getUser(request, response);
    } catch (MeabServletException e) {
      response.sendRedirect("/signup-auth-error.html");
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
    } catch (AuthenticationException e) {
      e.printStackTrace();
      return;
    } catch (InvalidRequestException e) {
      e.printStackTrace();
      return;
    } catch (APIConnectionException e) {
      e.printStackTrace();
      return;
    } catch (CardException e) {
      e.printStackTrace();
      return;
    } catch (APIException e) {
      e.printStackTrace();
      return;
    }

    Entity userEntity = user.getEntity();
    userEntity.setProperty(DatastoreConstants.User.CUSTOMER_ID, customer.getId());
    userDatastore.update(userEntity);

    response.sendRedirect("/thank-you.html");
  }
}
