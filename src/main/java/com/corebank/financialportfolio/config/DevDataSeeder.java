package com.corebank.financialportfolio.config;

import com.corebank.financialportfolio.entity.Account;
import com.corebank.financialportfolio.entity.AccountType;
import com.corebank.financialportfolio.entity.Institution;
import com.corebank.financialportfolio.entity.Transaction;
import com.corebank.financialportfolio.entity.User;
import com.corebank.financialportfolio.repository.AccountRepository;
import com.corebank.financialportfolio.repository.InstitutionRepository;
import com.corebank.financialportfolio.repository.TransactionRepository;
import com.corebank.financialportfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Seeds one institution, two accounts, and ~15 transactions for a fixed
 * dev-only test user so the read endpoints return realistic data before
 * Plaid is wired up.
 *
 * Gated to the "dev" profile — {@code @Profile("dev")} means this bean isn't
 * even created, let alone run, unless spring.profiles.active includes "dev".
 * Idempotent: skips seeding if the test user already has institutions.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements ApplicationRunner {

    private static final String TEST_USER_EMAIL = "dev@example.com";
    private static final String TEST_USER_PASSWORD = "DevPassword123!";

    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User user = userRepository.findByEmail(TEST_USER_EMAIL).orElse(null);

        if (user == null) {
            user = userRepository.save(new User(TEST_USER_EMAIL, passwordEncoder.encode(TEST_USER_PASSWORD)));
            log.info("Seeded dev test user: {} / {}", TEST_USER_EMAIL, TEST_USER_PASSWORD);
        }

        if (!institutionRepository.findByUserId(user.getId()).isEmpty()) {
            log.info("Dev seed data already present for {}, skipping.", TEST_USER_EMAIL);
            return;
        }

        Institution institution = institutionRepository.save(new Institution(user, "First Horizon Bank"));

        Account checking = new Account(institution, "Everyday Checking", AccountType.CHECKING, new BigDecimal("2543.18"));
        checking.setAvailableBalance(new BigDecimal("2493.18"));
        checking = accountRepository.save(checking);

        Account credit = new Account(institution, "Rewards Credit Card", AccountType.CREDIT_CARD, new BigDecimal("-812.44"));
        credit.setAvailableBalance(new BigDecimal("4187.56"));
        credit = accountRepository.save(credit);

        seedTransactions(checking, credit);

        log.info("Seeded 1 institution, 2 accounts, and 15 transactions for {}", TEST_USER_EMAIL);
    }

    private record SeedTransaction(
            Account account, BigDecimal amount, String description, String category, int daysAgo, boolean pending) {
    }

    private void seedTransactions(Account checking, Account credit) {
        LocalDate today = LocalDate.now();

        List<SeedTransaction> seeds = List.of(
                new SeedTransaction(checking, new BigDecimal("-64.12"), "Whole Foods Market", "Groceries", 1, false),
                new SeedTransaction(checking, new BigDecimal("2500.00"), "Payroll Deposit", "Income", 2, false),
                new SeedTransaction(checking, new BigDecimal("-15.40"), "Blue Bottle Coffee", "Dining", 2, false),
                new SeedTransaction(checking, new BigDecimal("-120.00"), "PG&E Utility", "Utilities", 3, false),
                new SeedTransaction(checking, new BigDecimal("-45.99"), "Netflix", "Entertainment", 4, false),
                new SeedTransaction(checking, new BigDecimal("-9.50"), "MTA Transit", "Transportation", 5, true),
                new SeedTransaction(checking, new BigDecimal("-88.23"), "Trader Joe's", "Groceries", 6, false),
                new SeedTransaction(checking, new BigDecimal("-32.00"), "Shell Gas Station", "Transportation", 7, false),
                new SeedTransaction(checking, new BigDecimal("-1800.00"), "Rent Payment", "Housing", 8, false),
                new SeedTransaction(credit, new BigDecimal("-54.30"), "Amazon.com", "Shopping", 1, false),
                new SeedTransaction(credit, new BigDecimal("-22.10"), "Chipotle", "Dining", 3, false),
                new SeedTransaction(credit, new BigDecimal("-140.00"), "Delta Air Lines", "Travel", 5, false),
                new SeedTransaction(credit, new BigDecimal("-18.75"), "Spotify", "Entertainment", 9, false),
                new SeedTransaction(credit, new BigDecimal("-63.44"), "Target", "Shopping", 10, false),
                new SeedTransaction(credit, new BigDecimal("120.00"), "Payment Received - Thank You", "Payment", 12, false)
        );

        for (SeedTransaction seed : seeds) {
            Transaction transaction = new Transaction(
                    seed.account(), seed.amount(), seed.description(), today.minusDays(seed.daysAgo()));
            transaction.setCategory(seed.category());
            transaction.setPending(seed.pending());
            transactionRepository.save(transaction);
        }
    }

}
