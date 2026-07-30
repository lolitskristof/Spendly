package dev.kristof.spendly.subscription;

import dev.kristof.spendly.auth.CurrentUserService;
import dev.kristof.spendly.category.Category;
import dev.kristof.spendly.category.CategoryService;
import dev.kristof.spendly.category.CategoryType;
import dev.kristof.spendly.common.exception.InvalidSubscriptionCategoryException;
import dev.kristof.spendly.common.exception.SubscriptionAlreadyExists;
import dev.kristof.spendly.common.exception.SubscriptionNotFound;
import dev.kristof.spendly.user.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final CurrentUserService currentUserService;
    private final CategoryService categoryService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, CurrentUserService currentUserService, CategoryService categoryService) {
        this.subscriptionRepository = subscriptionRepository;
        this.currentUserService = currentUserService;
        this.categoryService = categoryService;
    }

    public Subscription createSubscription(Long categoryId, String name, BigDecimal amount, BillingPeriod billingPeriod, LocalDate nextBillingDate) {
        User user = currentUserService.getLoggedInUser();
        Category category = categoryService.getAccessibleCategoryById(categoryId);
        boolean alreadyExists = subscriptionRepository.existsByUserIdAndNameIgnoreCase(user.getId(), name);

        if (alreadyExists) {
            throw new SubscriptionAlreadyExists("Subscription with name " + name + " already exists");
        }

        if (category.getCategoryType() != CategoryType.EXPENSE) {
            throw new InvalidSubscriptionCategoryException(
                    "Subscription can only use expense categories"
            );
        }

        Subscription subscription = new Subscription(user, category, name, amount, billingPeriod, nextBillingDate, BillingStatus.ACTIVE);

        return subscriptionRepository.save(subscription);
    }

    public List<Subscription> getSubscriptions() {
        User user = currentUserService.getLoggedInUser();
        return subscriptionRepository.findByUserId(user.getId());
    }

    public Subscription getSubscription(Long subscriptionId) {
        Long userId = currentUserService.getLoggedInUser().getId();

        return subscriptionRepository.findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(() ->
                        new SubscriptionNotFound(
                                "Subscription not found with id " + subscriptionId
                        )
                );
    }

    public Subscription updateSubscription(
            Long subscriptionId,
            Long categoryId,
            String name,
            BigDecimal amount,
            BillingPeriod billingPeriod,
            LocalDate nextBillingDate,
            BillingStatus status
    ) {
        User user = currentUserService.getLoggedInUser();

        Subscription subscription = subscriptionRepository
                .findByIdAndUserId(subscriptionId, user.getId())
                .orElseThrow(() ->
                        new SubscriptionNotFound(
                                "Subscription not found with id " + subscriptionId
                        )
                );

        Category updatedCategory = subscription.getCategory();

        if (categoryId != null) {
            updatedCategory = categoryService.getAccessibleCategoryById(categoryId);

            if (updatedCategory.getCategoryType() != CategoryType.EXPENSE) {
                throw new InvalidSubscriptionCategoryException(
                        "Subscription can only use expense categories"
                );
            }
        }

        String updatedName = name != null
                ? name
                : subscription.getName();

        boolean alreadyExists =
                subscriptionRepository.existsByUserIdAndNameIgnoreCaseAndIdNot(
                        user.getId(),
                        updatedName,
                        subscriptionId
                );

        if (alreadyExists) {
            throw new SubscriptionAlreadyExists(
                    "Subscription with name " + updatedName + " already exists"
            );
        }

        subscription.setCategory(updatedCategory);
        subscription.setName(updatedName);
        subscription.setAmount(
                amount != null ? amount : subscription.getAmount()
        );
        subscription.setBillingPeriod(
                billingPeriod != null
                        ? billingPeriod
                        : subscription.getBillingPeriod()
        );
        subscription.setNextBillingDate(
                nextBillingDate != null
                        ? nextBillingDate
                        : subscription.getNextBillingDate()
        );
        subscription.setStatus(
                status != null ? status : subscription.getStatus()
        );

        return subscriptionRepository.save(subscription);
    }

    public void deleteSubscription(Long subscriptionId) {
        User user = currentUserService.getLoggedInUser();
        Subscription subscription = subscriptionRepository.findByIdAndUserId(subscriptionId, user.getId()).orElseThrow(() ->
                new SubscriptionNotFound(
                        "Subscription not found with id " + subscriptionId
                ));
        subscriptionRepository.delete(subscription);
    }
}
