[1mdiff --git a/src/main/java/com/employeeFinderByDataForSEO/Entity/Subscription.java b/src/main/java/com/employeeFinderByDataForSEO/Entity/Subscription.java[m
[1mindex 5cb85b4..4985333 100644[m
[1m--- a/src/main/java/com/employeeFinderByDataForSEO/Entity/Subscription.java[m
[1m+++ b/src/main/java/com/employeeFinderByDataForSEO/Entity/Subscription.java[m
[36m@@ -1,4 +1,53 @@[m
 package com.employeeFinderByDataForSEO.Entity;[m
 [m
[32m+[m[32mimport jakarta.persistence.*;[m
[32m+[m[32mimport lombok.Getter;[m
[32m+[m[32mimport lombok.Setter;[m
[32m+[m
[32m+[m[32mimport java.time.LocalDateTime;[m
[32m+[m
[32m+[m[32m@Entity[m
[32m+[m[32m@Table(name = "subscriptions")[m
[32m+[m[32m@Getter[m
[32m+[m[32m@Setter[m
 public class Subscription {[m
[31m-}[m
[32m+[m
[32m+[m[32m    @Id[m
[32m+[m[32m    @GeneratedValue(strategy = GenerationType.IDENTITY)[m
[32m+[m[32m    private Long id;[m
[32m+[m
[32m+[m[32m    @OneToOne[m
[32m+[m[32m    @JoinColumn(name = "account_id", nullable = false, unique = true)[m
[32m+[m[32m    private Account account;[m
[32m+[m
[32m+[m[32m    @Enumerated(EnumType.STRING)[m
[32m+[m[32m    @Column(nullable = false)[m
[32m+[m[32m    private Plan plan;[m
[32m+[m
[32m+[m[32m    @Enumerated(EnumType.STRING)[m
[32m+[m[32m    @Column(nullable = false)[m
[32m+[m[32m    private SubscriptionStatus status;[m
[32m+[m
[32m+[m[32m    @Column(nullable = false)[m
[32m+[m[32m    private LocalDateTime startDate;[m
[32m+[m
[32m+[m[32m    @Column(nullable = false)[m
[32m+[m[32m    private LocalDateTime endDate;[m
[32m+[m
[32m+[m[32m    @Column(nullable = false)[m
[32m+[m[32m    private LocalDateTime createdAt;[m
[32m+[m
[32m+[m[32m    @PrePersist[m
[32m+[m[32m    protected void onCreate() {[m
[32m+[m[32m        createdAt = LocalDateTime.now();[m
[32m+[m[32m    }[m
[32m+[m
[32m+[m[32m    public enum Plan {[m
[32m+[m[32m        STANDARD[m
[32m+[m[32m    }[m
[32m+[m
[32m+[m[32m    public enum SubscriptionStatus {[m
[32m+[m[32m        ACTIVE,[m
[32m+[m[32m        EXPIRED[m
[32m+[m[32m    }[m
[32m+[m[32m}[m
\ No newline at end of file[m
[1mdiff --git a/src/main/java/com/employeeFinderByDataForSEO/controller/EmployeeController.java b/src/main/java/com/employeeFinderByDataForSEO/controller/EmployeeController.java[m
[1mindex bb21d0c..12601ea 100644[m
[1m--- a/src/main/java/com/employeeFinderByDataForSEO/controller/EmployeeController.java[m
[1m+++ b/src/main/java/com/employeeFinderByDataForSEO/controller/EmployeeController.java[m
[36m@@ -5,6 +5,7 @@[m [mimport com.employeeFinderByDataForSEO.dto.EmployeeResponse;[m
 import com.employeeFinderByDataForSEO.repository.AccountRepository;[m
 import com.employeeFinderByDataForSEO.service.EmployeeSearchService;[m
 import com.employeeFinderByDataForSEO.service.QuotaService;[m
[32m+[m[32mimport com.employeeFinderByDataForSEO.service.SubscriptionService;[m
 import org.springframework.security.core.Authentication;[m
 import org.springframework.web.bind.annotation.GetMapping;[m
 import org.springframework.web.bind.annotation.RequestMapping;[m
[36m@@ -21,11 +22,13 @@[m [mpublic class EmployeeController {[m
     private final EmployeeSearchService employeeSearchService;[m
     private final AccountRepository accountRepository;[m
     private final QuotaService quotaService;[m
[32m+[m[32m    private final SubscriptionService subscriptionService;[m
 [m
[31m-    public EmployeeController(EmployeeSearchService employeeSearchService, AccountRepository accountRepository, QuotaService quotaService) {[m
[32m+[m[32m    public EmployeeController(EmployeeSearchService employeeSearchService, AccountRepository accountRepository, QuotaService quotaService, SubscriptionService subscriptionService) {[m
         this.employeeSearchService = employeeSearchService;[m
         this.accountRepository = accountRepository;[m
         this.quotaService = quotaService;[m
[32m+[m[32m        this.subscriptionService = subscriptionService;[m
     }[m
 [m
 [m
[36m@@ -38,6 +41,8 @@[m [mpublic class EmployeeController {[m
                 .orElseThrow(() ->[m
                         new RuntimeException("Account not found"));[m
 [m
[32m+[m[32m        subscriptionService.checkActiveSubscription(account);[m
[32m+[m
         quotaService.checkQuota(account);[m
 [m
         long remainingQuota =[m
[1mdiff --git a/src/main/java/com/employeeFinderByDataForSEO/exception/GlobalExceptionHandler.java b/src/main/java/com/employeeFinderByDataForSEO/exception/GlobalExceptionHandler.java[m
[1mindex 8bb62df..ead3d9a 100644[m
[1m--- a/src/main/java/com/employeeFinderByDataForSEO/exception/GlobalExceptionHandler.java[m
[1m+++ b/src/main/java/com/employeeFinderByDataForSEO/exception/GlobalExceptionHandler.java[m
[36m@@ -6,6 +6,7 @@[m [mimport org.springframework.web.bind.annotation.ExceptionHandler;[m
 import org.springframework.web.bind.annotation.RestControllerAdvice;[m
 [m
 import java.time.LocalDateTime;[m
[32m+[m[32mimport java.util.HashMap;[m
 import java.util.Map;[m
 [m
 @RestControllerAdvice[m
[36m@@ -27,4 +28,18 @@[m [mpublic class GlobalExceptionHandler {[m
                 .body(response);[m
     }[m
 [m
[32m+[m[32m    @ExceptionHandler(SubscriptionException.class)[m
[32m+[m[32m    public ResponseEntity<Map<String, Object>> handleSubscriptionException([m
[32m+[m[32m            SubscriptionException ex) {[m
[32m+[m
[32m+[m[32m        Map<String, Object> response = new HashMap<>();[m
[32m+[m
[32m+[m[32m        response.put("status", HttpStatus.FORBIDDEN.value());[m
[32m+[m[32m        response.put("message", ex.getMessage());[m
[32m+[m
[32m+[m[32m        return ResponseEntity[m
[32m+[m[32m                .status(HttpStatus.FORBIDDEN)[m
[32m+[m[32m                .body(response);[m
[32m+[m[32m    }[m
[32m+[m
 }[m
[1mdiff --git a/src/main/java/com/employeeFinderByDataForSEO/exception/SubscriptionException.java b/src/main/java/com/employeeFinderByDataForSEO/exception/SubscriptionException.java[m
[1mindex 5b3b598..8cc8582 100644[m
[1m--- a/src/main/java/com/employeeFinderByDataForSEO/exception/SubscriptionException.java[m
[1m+++ b/src/main/java/com/employeeFinderByDataForSEO/exception/SubscriptionException.java[m
[36m@@ -1,4 +1,8 @@[m
 package com.employeeFinderByDataForSEO.exception;[m
 [m
[31m-public class SubscriptionException {[m
[31m-}[m
[32m+[m[32mpublic class SubscriptionException extends RuntimeException {[m
[32m+[m
[32m+[m[32m    public SubscriptionException(String message) {[m
[32m+[m[32m        super(message);[m
[32m+[m[32m    }[m
[32m+[m[32m}[m
\ No newline at end of file[m
[1mdiff --git a/src/main/java/com/employeeFinderByDataForSEO/repository/SubscriptionRepository.java b/src/main/java/com/employeeFinderByDataForSEO/repository/SubscriptionRepository.java[m
[1mindex d0803d6..643034a 100644[m
[1m--- a/src/main/java/com/employeeFinderByDataForSEO/repository/SubscriptionRepository.java[m
[1m+++ b/src/main/java/com/employeeFinderByDataForSEO/repository/SubscriptionRepository.java[m
[36m@@ -1,4 +1,18 @@[m
 package com.employeeFinderByDataForSEO.repository;[m
 [m
[31m-public interface SubscriptionRepository {[m
[31m-}[m
[32m+[m[32mimport com.employeeFinderByDataForSEO.Entity.Account;[m
[32m+[m[32mimport com.employeeFinderByDataForSEO.Entity.Subscription;[m
[32m+[m[32mimport org.springframework.data.jpa.repository.JpaRepository;[m
[32m+[m
[32m+[m[32mimport java.util.Optional;[m
[32m+[m
[32m+[m[32mpublic interface SubscriptionRepository[m
[32m+[m[32m        extends JpaRepository<Subscription, Long> {[m
[32m+[m
[32m+[m[32m    Optional<Subscription> findByAccount(Account account);[m
[32m+[m
[32m+[m[32m    Optional<Subscription> findByAccountAndStatus([m
[32m+[m[32m            Account account,[m
[32m+[m[32m            Subscription.SubscriptionStatus status[m
[32m+[m[32m    );[m
[32m+[m[32m}[m
\ No newline at end of file[m
[1mdiff --git a/src/main/java/com/employeeFinderByDataForSEO/service/RazorpayService.java b/src/main/java/com/employeeFinderByDataForSEO/service/RazorpayService.java[m
[1mindex 8d9ed62..8f2581c 100644[m
[1m--- a/src/main/java/com/employeeFinderByDataForSEO/service/RazorpayService.java[m
[1m+++ b/src/main/java/com/employeeFinderByDataForSEO/service/RazorpayService.java[m
[36m@@ -19,11 +19,13 @@[m [mpublic class RazorpayService {[m
     private final RazorpayClient razorpayClient;[m
     private final RazorpayConfig razorpayConfig;[m
     private final PaymentRepository paymentRepository;[m
[32m+[m[32m    private final SubscriptionService subscriptionService;[m
 [m
[31m-    public RazorpayService(RazorpayClient razorpayClient, RazorpayConfig razorpayConfig, PaymentRepository paymentRepository) {[m
[32m+[m[32m    public RazorpayService(RazorpayClient razorpayClient, RazorpayConfig razorpayConfig, PaymentRepository paymentRepository, SubscriptionService subscriptionService) {[m
         this.razorpayClient = razorpayClient;[m
         this.razorpayConfig = razorpayConfig;[m
         this.paymentRepository = paymentRepository;[m
[32m+[m[32m        this.subscriptionService = subscriptionService;[m
     }[m
 [m
 [m
[36m@@ -96,6 +98,8 @@[m [mpublic class RazorpayService {[m
 [m
             paymentRepository.save(payment);[m
 [m
[32m+[m[32m            subscriptionService.createSubscription(payment.getAccount());[m
[32m+[m
             return true;[m
 [m
         } catch (Exception e) {[m
[1mdiff --git a/src/main/java/com/employeeFinderByDataForSEO/service/SubscriptionService.java b/src/main/java/com/employeeFinderByDataForSEO/service/SubscriptionService.java[m
[1mindex 876d51f..5a2c0ef 100644[m
[1m--- a/src/main/java/com/employeeFinderByDataForSEO/service/SubscriptionService.java[m
[1m+++ b/src/main/java/com/employeeFinderByDataForSEO/service/SubscriptionService.java[m
[36m@@ -1,4 +1,88 @@[m
 package com.employeeFinderByDataForSEO.service;[m
 [m
[32m+[m[32mimport com.employeeFinderByDataForSEO.Entity.Account;[m
[32m+[m[32mimport com.employeeFinderByDataForSEO.Entity.Subscription;[m
[32m+[m[32mimport com.employeeFinderByDataForSEO.exception.SubscriptionException;[m
[32m+[m[32mimport com.employeeFinderByDataForSEO.repository.SubscriptionRepository;[m
[32m+[m[32mimport org.springframework.stereotype.Service;[m
[32m+[m
[32m+[m[32mimport java.time.LocalDateTime;[m
[32m+[m
[32m+[m[32m@Service[m
 public class SubscriptionService {[m
[31m-}[m
[32m+[m
[32m+[m[32m    private final SubscriptionRepository subscriptionRepository;[m
[32m+[m
[32m+[m[32m    public SubscriptionService([m
[32m+[m[32m            SubscriptionRepository subscriptionRepository) {[m
[32m+[m
[32m+[m[32m        this.subscriptionRepository = subscriptionRepository;[m
[32m+[m[32m    }[m
[32m+[m
[32m+[m[32m    public Subscription createSubscription(Account account) {[m
[32m+[m
[32m+[m[32m        if (subscriptionRepository.findByAccount(account).isPresent()) {[m
[32m+[m[32m            throw new SubscriptionException([m
[32m+[m[32m                    "Account already has a subscription"[m
[32m+[m[32m            );[m
[32m+[m[32m        }[m
[32m+[m
[32m+[m[32m        Subscription subscription = new Subscription();[m
[32m+[m
[32m+[m[32m        subscription.setAccount(account);[m
[32m+[m[32m        subscription.setPlan(Subscription.Plan.STANDARD);[m
[32m+[m[32m        subscription.setStatus([m
[32m+[m[32m                Subscription.SubscriptionStatus.ACTIVE[m
[32m+[m[32m        );[m
[32m+[m
[32m+[m[32m        LocalDateTime startDate = LocalDateTime.now();[m
[32m+[m[32m        LocalDateTime endDate = startDate.plusMonths(1);[m
[32m+[m
[32m+[m[32m        subscription.setStartDate(startDate);[m
[32m+[m[32m        subscription.setEndDate(endDate);[m
[32m+[m
[32m+[m[32m        return subscriptionRepository.save(subscription);[m
[32m+[m[32m    }[m
[32m+[m
[32m+[m[32m    public void checkActiveSubscription(Account account) {[m
[32m+[m
[32m+[m[32m        Subscription subscription = subscriptionRepository[m
[32m+[m[32m                .findByAccount(account)[m
[32m+[m[32m                .orElseThrow(() ->[m
[32m+[m[32m                        new SubscriptionException([m
[32m+[m[32m                                "No active subscription found"[m
[32m+[m[32m                        ));[m
[32m+[m
[32m+[m[32m        if (subscription.getStatus() !=[m
[32m+[m[32m                Subscription.SubscriptionStatus.ACTIVE) {[m
[32m+[m
[32m+[m[32m            throw new SubscriptionException([m
[32m+[m[32m                    "Subscription is not active"[m
[32m+[m[32m            );[m
[32m+[m[32m        }[m
[32m+[m
[32m+[m[32m        if (subscription.getEndDate()[m
[32m+[m[32m                .isBefore(LocalDateTime.now())) {[m
[32m+[m
[32m+[m[32m            subscription.setStatus([m
[32m+[m[32m                    Subscription.SubscriptionStatus.EXPIRED[m
[32m+[m[32m            );[m
[32m+[m
[32m+[m[32m            subscriptionRepository.save(subscription);[m
[32m+[m
[32m+[m[32m            throw new SubscriptionException([m
[32m+[m[32m                    "Subscription has expired"[m
[32m+[m[32m            );[m
[32m+[m[32m        }[m
[32m+[m[32m    }[m
[32m+[m
[32m+[m[32m    public boolean hasActiveSubscription(Account account) {[m
[32m+[m
[32m+[m[32m        try {[m
[32m+[m[32m            checkActiveSubscription(account);[m
[32m+[m[32m            return true;[m
[32m+[m[32m        } catch (SubscriptionException e) {[m
[32m+[m[32m            return false;[m
[32m+[m[32m        }[m
[32m+[m[32m    }[m
[32m+[m[32m}[m
\ No newline at end of file[m
[1mdiff --git a/src/test/java/com/employeeFinderByDataForSEO/controller/EmployeeControllerTest.java b/src/test/java/com/employeeFinderByDataForSEO/controller/EmployeeControllerTest.java[m
[1mindex 977be7a..ea2be2c 100644[m
[1m--- a/src/test/java/com/employeeFinderByDataForSEO/controller/EmployeeControllerTest.java[m
[1m+++ b/src/test/java/com/employeeFinderByDataForSEO/controller/EmployeeControllerTest.java[m
[36m@@ -1,65 +1,130 @@[m
 package com.employeeFinderByDataForSEO.controller;[m
 [m
 import com.employeeFinderByDataForSEO.Entity.Account;[m
[31m-import com.employeeFinderByDataForSEO.exception.DailyQuotaExceededException;[m
[32m+[m[32mimport com.employeeFinderByDataForSEO.dto.EmployeeResponse;[m
 import com.employeeFinderByDataForSEO.repository.AccountRepository;[m
 import com.employeeFinderByDataForSEO.service.EmployeeSearchService;[m
 import com.employeeFinderByDataForSEO.service.QuotaService;[m
[32m+[m[32mimport com.employeeFinderByDataForSEO.service.SubscriptionService;[m
[32m+[m[32mimport com.employeeFinderByDataForSEO.exception.DailyQuotaExceededException;[m
 import org.junit.jupiter.api.BeforeEach;[m
 import org.junit.jupiter.api.Test;[m
[32m+[m[32mimport org.junit.jupiter.api.extension.ExtendWith;[m
[32m+[m[32mimport org.mockito.Mock;[m
[32m+[m[32mimport org.mockito.junit.jupiter.MockitoExtension;[m
 import org.springframework.security.core.Authentication;[m
 [m
[31m-import static org.junit.jupiter.api.Assertions.assertThrows;[m
[32m+[m[32mimport java.util.List;[m
[32m+[m[32mimport java.util.Optional;[m
[32m+[m
[32m+[m[32mimport static org.junit.jupiter.api.Assertions.*;[m
[32m+[m[32mimport static org.mockito.ArgumentMatchers.*;[m
 import static org.mockito.Mockito.*;[m
 [m
[32m+[m[32m@ExtendWith(MockitoExtension.class)[m
 class EmployeeControllerTest {[m
 [m
[32m+[m[32m    @Mock[m
     private EmployeeSearchService employeeSearchService;[m
[32m+[m
[32m+[m[32m    @Mock[m
     private AccountRepository accountRepository;[m
[32m+[m
[32m+[m[32m    @Mock[m
     private QuotaService quotaService;[m
[32m+[m
[32m+[m[32m    @Mock[m
[32m+[m[32m    private SubscriptionService subscriptionService;[m
[32m+[m
[32m+[m[32m    @Mock[m
     private Authentication authentication;[m
 [m
     private EmployeeController employeeController;[m
 [m
[32m+[m[32m    private Account account;[m
[32m+[m
     @BeforeEach[m
     void setUp() {[m
 [m
[31m-        employeeSearchService =[m
[31m-                mock(EmployeeSearchService.class);[m
[32m+[m[32m        employeeController = new EmployeeController([m
[32m+[m[32m                employeeSearchService,[m
[32m+[m[32m                accountRepository,[m
[32m+[m[32m                quotaService,[m
[32m+[m[32m                subscriptionService[m
[32m+[m[32m        );[m
[32m+[m
[32m+[m[32m        account = new Account([m
[32m+[m[32m                1L,[m
[32m+[m[32m                "testuser",[m
[32m+[m[32m                "password",[m
[32m+[m[32m                null,[m
[32m+[m[32m                "test@example.com"[m
[32m+[m[32m        );[m
[32m+[m[32m    }[m
 [m
[31m-        accountRepository =[m
[31m-                mock(AccountRepository.class);[m
[32m+[m[32m    @Test[m
[32m+[m[32m    void shouldReturnEmployeesSuccessfully() {[m
[32m+[m
[32m+[m[32m        when(authentication.getName())[m
[32m+[m[32m                .thenReturn("test@example.com");[m
 [m
[31m-        quotaService =[m
[31m-                mock(QuotaService.class);[m
[32m+[m[32m        when(accountRepository.findByEmail("test@example.com"))[m
[32m+[m[32m                .thenReturn(Optional.of(account));[m
 [m
[31m-        authentication =[m
[31m-                mock(Authentication.class);[m
[32m+[m[32m        doNothing()[m
[32m+[m[32m                .when(subscriptionService)[m
[32m+[m[32m                .checkActiveSubscription(account);[m
 [m
[31m-        employeeController =[m
[31m-                new EmployeeController([m
[31m-                        employeeSearchService,[m
[31m-                        accountRepository,[m
[31m-                        quotaService[m
[32m+[m[32m        when(quotaService.getRemainingQuota(account))[m
[32m+[m[32m                .thenReturn(100L);[m
[32m+[m
[32m+[m[32m        List<EmployeeResponse> employees = List.of([m
[32m+[m[32m                new EmployeeResponse()[m
[32m+[m[32m        );[m
[32m+[m
[32m+[m[32m        when(employeeSearchService.search("Wipro", 10))[m
[32m+[m[32m                .thenReturn(employees);[m
[32m+[m
[32m+[m[32m        List<EmployeeResponse> result =[m
[32m+[m[32m                employeeController.search([m
[32m+[m[32m                        "Wipro",[m
[32m+[m[32m                        authentication[m
                 );[m
[32m+[m
[32m+[m[32m        assertNotNull(result);[m
[32m+[m[32m        assertEquals(1, result.size());[m
[32m+[m
[32m+[m[32m        verify(subscriptionService)[m
[32m+[m[32m                .checkActiveSubscription(account);[m
[32m+[m
[32m+[m[32m        verify(quotaService)[m
[32m+[m[32m                .checkQuota(account);[m
[32m+[m
[32m+[m[32m        verify(employeeSearchService)[m
[32m+[m[32m                .search("Wipro", 10);[m
[32m+[m
[32m+[m[32m        verify(quotaService)[m
[32m+[m[32m                .consumeProfiles(account, 1);[m
     }[m
 [m
     @Test[m
[31m-    void shouldRejectRequestWhenDailyQuotaIsExceeded() {[m
[32m+[m[32m    void shouldRejectWhenDailyQuotaExceeded() {[m
 [m
         when(authentication.getName())[m
[31m-                .thenReturn("abishek@example.com");[m
[32m+[m[32m                .thenReturn("test@example.com");[m
 [m
[31m-        Account account = new Account();[m
[32m+[m[32m        when(accountRepository.findByEmail("test@example.com"))[m
[32m+[m[32m                .thenReturn(Optional.of(account));[m
 [m
[31m-        when(accountRepository.findByEmail("abishek@example.com"))[m
[31m-                .thenReturn(java.util.Optional.of(account));[m
[32m+[m[32m        doNothing()[m
[32m+[m[32m                .when(subscriptionService)[m
[32m+[m[32m                .checkActiveSubscription(account);[m
 [m
[31m-        doThrow([m
[31m-                new DailyQuotaExceededException([m
[31m-                        "Daily employee profile quota exceeded"[m
[31m-                )[m
[31m-        ).when(quotaService).checkQuota(account);[m
[32m+[m[32m        doThrow(new DailyQuotaExceededException([m
[32m+[m[32m                "Daily quota exceeded"[m
[32m+[m[32m        ))[m
[32m+[m[32m                .when(quotaService)[m
[32m+[m[32m                .checkQuota(account);[m
 [m
         assertThrows([m
                 DailyQuotaExceededException.class,[m
[36m@@ -69,6 +134,12 @@[m [mclass EmployeeControllerTest {[m
                 )[m
         );[m
 [m
[32m+[m[32m        verify(subscriptionService)[m
[32m+[m[32m                .checkActiveSubscription(account);[m
[32m+[m
[32m+[m[32m        verify(quotaService)[m
[32m+[m[32m                .checkQuota(account);[m
[32m+[m
         verify(employeeSearchService, never())[m
                 .search(anyString(), anyInt());[m
     }[m
