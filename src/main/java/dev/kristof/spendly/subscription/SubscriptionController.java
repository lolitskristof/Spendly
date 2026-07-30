package dev.kristof.spendly.subscription;

import dev.kristof.spendly.subscription.dto.CreateSubscriptionRequest;
import dev.kristof.spendly.subscription.dto.SubscriptionResponse;
import dev.kristof.spendly.subscription.dto.UpdateSubscriptionRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    public SubscriptionResponse createSubscription(@Valid @RequestBody CreateSubscriptionRequest req) {
        Subscription subscription = subscriptionService.createSubscription(req.getCategoryId(), req.getName(), req.getAmount(), req.getBillingPeriod(), req.getNextBillingDate());

        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getUser().getId(),
                subscription.getCategory().getId(),
                subscription.getName(),
                subscription.getAmount(),
                subscription.getBillingPeriod(),
                subscription.getNextBillingDate(),
                subscription.getStatus()
        );
    }

    @GetMapping
    public List<SubscriptionResponse> getSubscriptions() {
        return subscriptionService.getSubscriptions().stream().map(subscription -> new SubscriptionResponse(
                subscription.getId(),
                subscription.getUser().getId(),
                subscription.getCategory().getId(),
                subscription.getName(),
                subscription.getAmount(),
                subscription.getBillingPeriod(),
                subscription.getNextBillingDate(),
                subscription.getStatus()
        )).toList();
    }

    @GetMapping("{subscriptionId}")
    public SubscriptionResponse getSubscription(@PathVariable Long subscriptionId) {
        Subscription subscription = subscriptionService.getSubscription(subscriptionId);

        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getUser().getId(),
                subscription.getCategory().getId(),
                subscription.getName(),
                subscription.getAmount(),
                subscription.getBillingPeriod(),
                subscription.getNextBillingDate(),
                subscription.getStatus()
        );
    }

    @PatchMapping("/{subscriptionId}")
    public SubscriptionResponse updateSubscription(@PathVariable Long subscriptionId, @Valid @RequestBody UpdateSubscriptionRequest request) {
        Subscription subscription = subscriptionService.updateSubscription(
                subscriptionId,
                request.getCategoryId(),
                request.getName(),
                request.getAmount(),
                request.getBillingPeriod(),
                request.getNextBillingDate(),
                request.getStatus()
        );

        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getUser().getId(),
                subscription.getCategory().getId(),
                subscription.getName(),
                subscription.getAmount(),
                subscription.getBillingPeriod(),
                subscription.getNextBillingDate(),
                subscription.getStatus()
        );
    }

    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable Long subscriptionId) {
        subscriptionService.deleteSubscription(subscriptionId);
        return ResponseEntity.noContent().build();
    }
}
