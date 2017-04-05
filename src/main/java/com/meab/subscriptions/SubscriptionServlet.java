package com.meab.subscriptions;

import com.google.common.base.Preconditions;
import com.meab.SecretDatastore;
import com.meab.servlet.MeabServlet;
import com.stripe.Stripe;

public class SubscriptionServlet extends MeabServlet {
  private static final String STRIPE_SK_KEY = "stripe";

  public SubscriptionServlet() {
    SecretDatastore secretDatastore = new SecretDatastore();
    String apiKey = secretDatastore.get(STRIPE_SK_KEY);
    Preconditions.checkNotNull(apiKey, "Stripe API key not found in DB with key " + STRIPE_SK_KEY);
    Stripe.apiKey = apiKey;
  }
}
