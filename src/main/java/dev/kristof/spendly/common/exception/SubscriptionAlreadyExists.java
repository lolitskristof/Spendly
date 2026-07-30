package dev.kristof.spendly.common.exception;

public class SubscriptionAlreadyExists extends RuntimeException {
    public SubscriptionAlreadyExists(String message) {
        super(message);
    }
}
