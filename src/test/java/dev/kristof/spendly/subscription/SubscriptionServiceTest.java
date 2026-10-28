package dev.kristof.spendly.subscription;

import dev.kristof.spendly.auth.CurrentUserService;
import dev.kristof.spendly.category.Category;
import dev.kristof.spendly.category.CategoryService;
import dev.kristof.spendly.category.CategoryType;
import dev.kristof.spendly.common.exception.InvalidSubscriptionCategoryException;
import dev.kristof.spendly.common.exception.SubscriptionAlreadyExists;
import dev.kristof.spendly.common.exception.SubscriptionNotFound;
import dev.kristof.spendly.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    private static final Long USER_ID = 42L;
    private static final Long SUBSCRIPTION_ID = 10L;
    private static final Long CATEGORY_ID = 3L;
    private static final Long NEW_CATEGORY_ID = 4L;

    private static final LocalDate BILLING_DATE =
            LocalDate.of(2026, 11, 15);

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User mockLoggedInUser() {
        User user = mock(User.class);

        when(currentUserService.getLoggedInUser())
                .thenReturn(user);

        return user;
    }

    private User mockLoggedInUserWithId() {
        User user = mockLoggedInUser();

        when(user.getId()).thenReturn(USER_ID);

        return user;
    }

    private Subscription mockOwnedSubscription() {
        mockLoggedInUserWithId();

        Subscription subscription = mock(Subscription.class);

        when(subscriptionRepository.findByIdAndUserId(
                SUBSCRIPTION_ID, USER_ID
        )).thenReturn(Optional.of(subscription));

        return subscription;
    }

    // CREATE

    @Test
    void createSubscription_shouldSaveValidSubscription() {
        User user = mockLoggedInUserWithId();
        Category category = mock(Category.class);

        BigDecimal amount = new BigDecimal("4990.00");

        when(categoryService.getAccessibleCategoryById(CATEGORY_ID))
                .thenReturn(category);

        when(category.getCategoryType())
                .thenReturn(CategoryType.EXPENSE);

        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Subscription result = subscriptionService.createSubscription(
                CATEGORY_ID,
                "Netflix",
                amount,
                BillingPeriod.MONTHLY,
                BILLING_DATE
        );

        ArgumentCaptor<Subscription> captor =
                ArgumentCaptor.forClass(Subscription.class);

        verify(subscriptionRepository).save(captor.capture());

        Subscription savedSubscription = captor.getValue();

        assertSame(savedSubscription, result);
        assertSame(category, result.getCategory());
        assertEquals("Netflix", result.getName());
        assertEquals(amount, result.getAmount());
        assertEquals(BillingPeriod.MONTHLY, result.getBillingPeriod());
        assertEquals(BILLING_DATE, result.getNextBillingDate());
        assertEquals(BillingStatus.ACTIVE, result.getStatus());

        assertSame(
                user,
                ReflectionTestUtils.getField(result, "user")
        );

        verify(subscriptionRepository)
                .existsByUserIdAndNameIgnoreCase(USER_ID, "Netflix");
    }

    @Test
    void createSubscription_shouldRejectDuplicateName() {
        mockLoggedInUserWithId();

        when(categoryService.getAccessibleCategoryById(CATEGORY_ID))
                .thenReturn(mock(Category.class));

        when(subscriptionRepository.existsByUserIdAndNameIgnoreCase(
                USER_ID, "Netflix"
        )).thenReturn(true);

        assertThrows(
                SubscriptionAlreadyExists.class,
                () -> subscriptionService.createSubscription(
                        CATEGORY_ID,
                        "Netflix",
                        BigDecimal.TEN,
                        BillingPeriod.MONTHLY,
                        BILLING_DATE
                )
        );

        verify(subscriptionRepository, never())
                .save(any(Subscription.class));
    }

    @Test
    void createSubscription_shouldRejectIncomeCategory() {
        mockLoggedInUserWithId();

        Category category = mock(Category.class);

        when(categoryService.getAccessibleCategoryById(CATEGORY_ID))
                .thenReturn(category);

        when(category.getCategoryType())
                .thenReturn(CategoryType.INCOME);

        assertThrows(
                InvalidSubscriptionCategoryException.class,
                () -> subscriptionService.createSubscription(
                        CATEGORY_ID,
                        "Netflix",
                        BigDecimal.TEN,
                        BillingPeriod.MONTHLY,
                        BILLING_DATE
                )
        );

        verify(subscriptionRepository, never())
                .save(any(Subscription.class));
    }

    @Test
    void createSubscription_shouldRejectInaccessibleCategory() {
        mockLoggedInUser();

        when(categoryService.getAccessibleCategoryById(CATEGORY_ID))
                .thenThrow(new IllegalArgumentException(
                        "Category not accessible"
                ));

        assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionService.createSubscription(
                        CATEGORY_ID,
                        "Netflix",
                        BigDecimal.TEN,
                        BillingPeriod.MONTHLY,
                        BILLING_DATE
                )
        );

        verifyNoInteractions(subscriptionRepository);
    }

    // LIST

    @Test
    void getSubscriptions_shouldReturnOnlyOwnedSubscriptions() {
        mockLoggedInUserWithId();

        List<Subscription> expected =
                List.of(mock(Subscription.class));

        when(subscriptionRepository.findByUserId(USER_ID))
                .thenReturn(expected);

        List<Subscription> result =
                subscriptionService.getSubscriptions();

        assertSame(expected, result);

        verify(subscriptionRepository).findByUserId(USER_ID);
    }

    @Test
    void getSubscriptions_shouldReturnEmptyList() {
        mockLoggedInUserWithId();

        when(subscriptionRepository.findByUserId(USER_ID))
                .thenReturn(List.of());

        List<Subscription> result =
                subscriptionService.getSubscriptions();

        assertTrue(result.isEmpty());
    }

    // GET BY ID / OWNERSHIP

    @Test
    void getSubscription_shouldReturnOwnedSubscription() {
        Subscription subscription = mockOwnedSubscription();

        Subscription result =
                subscriptionService.getSubscription(SUBSCRIPTION_ID);

        assertSame(subscription, result);

        verify(subscriptionRepository).findByIdAndUserId(
                SUBSCRIPTION_ID, USER_ID
        );
    }

    @Test
    void getSubscription_shouldRejectSubscriptionNotOwnedByUser() {
        mockLoggedInUserWithId();

        when(subscriptionRepository.findByIdAndUserId(
                SUBSCRIPTION_ID, USER_ID
        )).thenReturn(Optional.empty());

        assertThrows(
                SubscriptionNotFound.class,
                () -> subscriptionService.getSubscription(SUBSCRIPTION_ID)
        );

        verify(subscriptionRepository).findByIdAndUserId(
                SUBSCRIPTION_ID, USER_ID
        );
    }

    // UPDATE

    @Test
    void updateSubscription_shouldUpdateAllFields() {
        Subscription subscription = mockOwnedSubscription();
        Category newCategory = mock(Category.class);

        BigDecimal newAmount = new BigDecimal("8990.00");
        LocalDate newDate = LocalDate.of(2026, 12, 15);

        when(categoryService.getAccessibleCategoryById(NEW_CATEGORY_ID))
                .thenReturn(newCategory);

        when(newCategory.getCategoryType())
                .thenReturn(CategoryType.EXPENSE);

        when(subscriptionRepository.save(subscription))
                .thenReturn(subscription);

        Subscription result = subscriptionService.updateSubscription(
                SUBSCRIPTION_ID,
                NEW_CATEGORY_ID,
                "Spotify",
                newAmount,
                BillingPeriod.YEARLY,
                newDate,
                BillingStatus.ACTIVE
        );

        assertSame(subscription, result);

        verify(subscription).setCategory(newCategory);
        verify(subscription).setName("Spotify");
        verify(subscription).setAmount(newAmount);
        verify(subscription).setBillingPeriod(BillingPeriod.YEARLY);
        verify(subscription).setNextBillingDate(newDate);
        verify(subscription).setStatus(BillingStatus.ACTIVE);

        verify(subscriptionRepository)
                .existsByUserIdAndNameIgnoreCaseAndIdNot(
                        USER_ID,
                        "Spotify",
                        SUBSCRIPTION_ID
                );

        verify(subscriptionRepository).save(subscription);
    }

    @Test
    void updateSubscription_shouldKeepExistingValuesWhenNull() {
        Subscription subscription = mockOwnedSubscription();
        Category originalCategory = mock(Category.class);

        BigDecimal originalAmount =
                new BigDecimal("4990.00");

        when(subscription.getCategory())
                .thenReturn(originalCategory);

        when(subscription.getName())
                .thenReturn("Netflix");

        when(subscription.getAmount())
                .thenReturn(originalAmount);

        when(subscription.getBillingPeriod())
                .thenReturn(BillingPeriod.MONTHLY);

        when(subscription.getNextBillingDate())
                .thenReturn(BILLING_DATE);

        when(subscription.getStatus())
                .thenReturn(BillingStatus.ACTIVE);

        when(subscriptionRepository.save(subscription))
                .thenReturn(subscription);

        Subscription result = subscriptionService.updateSubscription(
                SUBSCRIPTION_ID,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertSame(subscription, result);

        verify(subscription).setCategory(originalCategory);
        verify(subscription).setName("Netflix");
        verify(subscription).setAmount(originalAmount);
        verify(subscription).setBillingPeriod(BillingPeriod.MONTHLY);
        verify(subscription).setNextBillingDate(BILLING_DATE);
        verify(subscription).setStatus(BillingStatus.ACTIVE);

        verifyNoInteractions(categoryService);

        verify(subscriptionRepository)
                .existsByUserIdAndNameIgnoreCaseAndIdNot(
                        USER_ID,
                        "Netflix",
                        SUBSCRIPTION_ID
                );

        verify(subscriptionRepository).save(subscription);
    }

    @Test
    void updateSubscription_shouldRejectDuplicateName() {
        mockOwnedSubscription();

        when(subscriptionRepository
                .existsByUserIdAndNameIgnoreCaseAndIdNot(
                        USER_ID,
                        "Spotify",
                        SUBSCRIPTION_ID
                )).thenReturn(true);

        assertThrows(
                SubscriptionAlreadyExists.class,
                () -> subscriptionService.updateSubscription(
                        SUBSCRIPTION_ID,
                        null,
                        "Spotify",
                        null,
                        null,
                        null,
                        null
                )
        );

        verify(subscriptionRepository, never())
                .save(any(Subscription.class));
    }

    @Test
    void updateSubscription_shouldRejectIncomeCategory() {
        mockOwnedSubscription();

        Category newCategory = mock(Category.class);

        when(categoryService.getAccessibleCategoryById(NEW_CATEGORY_ID))
                .thenReturn(newCategory);

        when(newCategory.getCategoryType())
                .thenReturn(CategoryType.INCOME);

        assertThrows(
                InvalidSubscriptionCategoryException.class,
                () -> subscriptionService.updateSubscription(
                        SUBSCRIPTION_ID,
                        NEW_CATEGORY_ID,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );

        verify(subscriptionRepository, never())
                .save(any(Subscription.class));
    }

    @Test
    void updateSubscription_shouldRejectInaccessibleCategory() {
        mockOwnedSubscription();

        when(categoryService.getAccessibleCategoryById(NEW_CATEGORY_ID))
                .thenThrow(new IllegalArgumentException(
                        "Category not accessible"
                ));

        assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionService.updateSubscription(
                        SUBSCRIPTION_ID,
                        NEW_CATEGORY_ID,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );

        verify(subscriptionRepository, never())
                .save(any(Subscription.class));
    }

    @Test
    void updateSubscription_shouldRejectSubscriptionNotOwnedByUser() {
        mockLoggedInUserWithId();

        when(subscriptionRepository.findByIdAndUserId(
                SUBSCRIPTION_ID, USER_ID
        )).thenReturn(Optional.empty());

        assertThrows(
                SubscriptionNotFound.class,
                () -> subscriptionService.updateSubscription(
                        SUBSCRIPTION_ID,
                        NEW_CATEGORY_ID,
                        "Spotify",
                        BigDecimal.TEN,
                        BillingPeriod.MONTHLY,
                        BILLING_DATE,
                        BillingStatus.ACTIVE
                )
        );

        verifyNoInteractions(categoryService);

        verify(subscriptionRepository, never())
                .save(any(Subscription.class));
    }

    @Test
    void updateSubscription_shouldAllowChangingOnlyAmount() {
        Subscription subscription = mockOwnedSubscription();
        Category category = mock(Category.class);

        BigDecimal newAmount = new BigDecimal("6990.00");

        when(subscription.getCategory()).thenReturn(category);
        when(subscription.getName()).thenReturn("Netflix");

        when(subscriptionRepository.save(subscription))
                .thenReturn(subscription);

        subscriptionService.updateSubscription(
                SUBSCRIPTION_ID,
                null,
                null,
                newAmount,
                null,
                null,
                null
        );

        verify(subscription).setAmount(newAmount);

        verify(subscriptionRepository)
                .existsByUserIdAndNameIgnoreCaseAndIdNot(
                        USER_ID,
                        "Netflix",
                        SUBSCRIPTION_ID
                );

        verify(subscriptionRepository).save(subscription);
    }

    // DELETE

    @Test
    void deleteSubscription_shouldDeleteOwnedSubscription() {
        Subscription subscription = mockOwnedSubscription();

        subscriptionService.deleteSubscription(SUBSCRIPTION_ID);

        verify(subscriptionRepository).delete(subscription);
    }

    @Test
    void deleteSubscription_shouldRejectSubscriptionNotOwnedByUser() {
        mockLoggedInUserWithId();

        when(subscriptionRepository.findByIdAndUserId(
                SUBSCRIPTION_ID, USER_ID
        )).thenReturn(Optional.empty());

        assertThrows(
                SubscriptionNotFound.class,
                () -> subscriptionService.deleteSubscription(SUBSCRIPTION_ID)
        );

        verify(subscriptionRepository, never())
                .delete(any(Subscription.class));
    }
}
