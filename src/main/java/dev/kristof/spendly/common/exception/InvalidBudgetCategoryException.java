package dev.kristof.spendly.common.exception;

public class InvalidBudgetCategoryException extends RuntimeException {
    public InvalidBudgetCategoryException(String message) {
        super(message);
    }
}
