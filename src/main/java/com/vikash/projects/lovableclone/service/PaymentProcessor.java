package com.vikash.projects.lovableclone.service;

import com.vikash.projects.lovableclone.dto.subscription.CheckoutRequest;
import com.vikash.projects.lovableclone.dto.subscription.CheckoutResponse;
import com.vikash.projects.lovableclone.dto.subscription.PortalResponse;
import com.stripe.model.StripeObject;

import java.util.Map;

public interface PaymentProcessor {

    CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request);

    PortalResponse openCustomerPortal();

    void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata);
}
