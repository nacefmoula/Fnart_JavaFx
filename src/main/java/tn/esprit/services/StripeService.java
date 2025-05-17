package tn.esprit.services;

// StripeService.java
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

public class StripeService {
    static {
        // Initialize with your secret key (ideally from env file)
        Stripe.apiKey = "sk_test_51RJL8JFghCCWqLRAWBGv9HS3zqbD18L1JIEjujgDHWV2ReK1OoCCFOtc5q1ZocD2E0IEBMPxDDvMEIG6Ga0w3YbI00b9HGWMLr";
    }

    public static String createCheckoutSession(double amount, String description) throws Exception {
        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl("https://example.com/success") // Optional
                        .setCancelUrl("https://example.com/cancel")   // Optional
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("usd")
                                                        .setUnitAmount((long)(amount * 100)) // Stripe uses cents
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName(description)
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .build();

        Session session = Session.create(params);
        return session.getUrl();
    }
}
