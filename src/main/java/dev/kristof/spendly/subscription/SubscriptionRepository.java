package dev.kristof.spendly.subscription;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserId(Long userId);

    Optional<Subscription> findByIdAndUserId(Long subscriptionId, Long userId);

    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);

    boolean existsByUserIdAndNameIgnoreCaseAndIdNot(
            Long userId,
            String name,
            Long subscriptionId
    );

    List<Subscription> findByUserIdAndStatusAndNextBillingDateBetweenOrderByNextBillingDateAsc(
            Long userId,
            BillingStatus status,
            LocalDate from,
            LocalDate to
    );
}