package dev.kristof.spendly.common.exception;

public class SubscriptionNotFound extends RuntimeException {
    public SubscriptionNotFound(String message) {
        super(message);
    }
}
