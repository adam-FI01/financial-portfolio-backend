package com.corebank.financialportfolio.config;

import com.corebank.financialportfolio.entity.Account;
import com.corebank.financialportfolio.entity.AccountType;
import com.corebank.financialportfolio.entity.Holding;
import com.corebank.financialportfolio.entity.Institution;
import com.corebank.financialportfolio.entity.Transaction;
import com.corebank.financialportfolio.entity.User;
import com.corebank.financialportfolio.repository.AccountRepository;
import com.corebank.financialportfolio.repository.HoldingRepository;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

/**
 * Seeds a realistic, varied dataset (5 institutions, 10 accounts, 25 holdings
 * across 2 investment accounts, ~75 transactions spanning the last 60 days)
 * for a fixed dev-only test user, so the read endpoints have enough variety
 * to build real features against.
 *
 * Gated to the "dev" profile — {@code @Profile("dev")} means this bean isn't
 * even created, let alone run, unless spring.profiles.active includes "dev".
 *
 * Deterministic and idempotent by way of always resetting: on every startup
 * it wipes any existing seed data for the test user and recreates it fresh
 * from the definitions below (using a fixed random seed for the varied
 * transactions), so the dataset shape stays in sync with this file rather
 * than whatever was seeded by an older version of it.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements ApplicationRunner {

    private static final String TEST_USER_EMAIL = "dev@example.com";
    private static final String TEST_USER_PASSWORD = "DevPassword123!";
    private static final long RANDOM_SEED = 20260101L;
    private static final int HISTORY_DAYS = 60;

    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final HoldingRepository holdingRepository;
    private final PasswordEncoder passwordEncoder;

    private record MerchantTemplate(String description, String category, double minAmount, double maxAmount,
                                     boolean credit) {
    }

    private static final List<MerchantTemplate> EVERYDAY_SPENDING = List.of(
            new MerchantTemplate("Whole Foods Market", "Groceries", 40, 140, false),
            new MerchantTemplate("Trader Joe's", "Groceries", 30, 95, false),
            new MerchantTemplate("Kroger", "Groceries", 25, 110, false),
            new MerchantTemplate("Blue Bottle Coffee", "Dining", 5, 18, false),
            new MerchantTemplate("Chipotle", "Dining", 10, 25, false),
            new MerchantTemplate("Local Bistro", "Dining", 25, 85, false),
            new MerchantTemplate("Shell Gas Station", "Transportation", 30, 65, false),
            new MerchantTemplate("MTA Transit", "Transportation", 8, 35, false),
            new MerchantTemplate("Uber", "Transportation", 12, 40, false),
            new MerchantTemplate("PG&E Utility", "Utilities", 80, 180, false),
            new MerchantTemplate("Comcast Internet", "Utilities", 60, 90, false),
            new MerchantTemplate("Netflix", "Subscriptions", 15.49, 15.49, false),
            new MerchantTemplate("Spotify", "Subscriptions", 11.99, 11.99, false),
            new MerchantTemplate("Amazon.com", "Shopping", 20, 150, false),
            new MerchantTemplate("Target", "Shopping", 25, 120, false),
            new MerchantTemplate("AMC Theatres", "Entertainment", 15, 45, false),
            new MerchantTemplate("Transfer to Savings", "Transfers", 200, 600, false)
    );

    private static final List<MerchantTemplate> CHASE_CHECKING_ACTIVITY = concat(EVERYDAY_SPENDING, List.of(
            new MerchantTemplate("Freelance Payment", "Income", 300, 900, true),
            new MerchantTemplate("Transfer from Savings", "Transfers", 150, 500, true)
    ));

    private static final List<MerchantTemplate> CREDIT_CARD_SPENDING = List.of(
            new MerchantTemplate("Amazon.com", "Shopping", 20, 220, false),
            new MerchantTemplate("Target", "Shopping", 25, 140, false),
            new MerchantTemplate("Best Buy", "Shopping", 40, 350, false),
            new MerchantTemplate("Chipotle", "Dining", 10, 28, false),
            new MerchantTemplate("Olive Garden", "Dining", 35, 95, false),
            new MerchantTemplate("Delta Air Lines", "Travel", 140, 480, false),
            new MerchantTemplate("Marriott Hotels", "Travel", 120, 340, false),
            new MerchantTemplate("Shell Gas Station", "Transportation", 30, 70, false),
            new MerchantTemplate("Spotify", "Subscriptions", 11.99, 11.99, false),
            new MerchantTemplate("Netflix", "Subscriptions", 15.49, 15.49, false),
            new MerchantTemplate("Whole Foods Market", "Groceries", 40, 120, false),
            new MerchantTemplate("AMC Theatres", "Entertainment", 15, 45, false),
            new MerchantTemplate("Payment Received - Thank You", "Payment", 100, 400, true)
    );

    private static final List<MerchantTemplate> SAVINGS_ACTIVITY = List.of(
            new MerchantTemplate("Transfer from Checking", "Transfers", 200, 1000, true),
            new MerchantTemplate("Interest Payment", "Income", 4, 22, true)
    );

    private static final List<MerchantTemplate> BROKERAGE_ACTIVITY = List.of(
            new MerchantTemplate("Transfer from Checking", "Transfers", 300, 1200, true)
    );

    private static List<MerchantTemplate> concat(List<MerchantTemplate> a, List<MerchantTemplate> b) {
        return Stream.concat(a.stream(), b.stream()).toList();
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User user = userRepository.findByEmail(TEST_USER_EMAIL).orElse(null);

        if (user == null) {
            user = userRepository.save(new User(TEST_USER_EMAIL, passwordEncoder.encode(TEST_USER_PASSWORD)));
            log.info("Seeded dev test user: {} / {}", TEST_USER_EMAIL, TEST_USER_PASSWORD);
        }

        resetExistingSeedData(user);
        seedFreshData(user);
    }

    private void resetExistingSeedData(User user) {
        List<Institution> institutions = institutionRepository.findByUserId(user.getId());
        if (institutions.isEmpty()) {
            return;
        }

        List<Account> accounts = accountRepository.findByInstitutionUserId(user.getId());
        for (Account account : accounts) {
            transactionRepository.deleteByAccountId(account.getId());
            holdingRepository.deleteByAccountId(account.getId());
        }
        accountRepository.deleteAll(accounts);
        institutionRepository.deleteAll(institutions);

        log.info("Cleared existing dev seed data for {} before reseeding", TEST_USER_EMAIL);
    }

    private void seedFreshData(User user) {
        Institution firstHorizon = institutionRepository.save(new Institution(user, "First Horizon Bank"));
        Institution chase = institutionRepository.save(new Institution(user, "Chase"));
        Institution fidelity = institutionRepository.save(new Institution(user, "Fidelity"));
        Institution sofi = institutionRepository.save(new Institution(user, "SoFi"));
        Institution robinhood = institutionRepository.save(new Institution(user, "Robinhood"));

        Account firstHorizonChecking = accountRepository.save(withAvailable(
                new Account(firstHorizon, "Everyday Checking", AccountType.CHECKING, new BigDecimal("8543.18")),
                new BigDecimal("8493.18")));

        Account chaseChecking = accountRepository.save(withAvailable(
                new Account(chase, "Total Checking", AccountType.CHECKING, new BigDecimal("14821.44")),
                new BigDecimal("14821.44")));

        Account sofiSavings = accountRepository.save(withAvailable(
                new Account(sofi, "Savings", AccountType.SAVINGS, new BigDecimal("52420.77")),
                new BigDecimal("52420.77")));

        Account firstHorizonCard = accountRepository.save(withCreditLimit(withAvailable(
                new Account(firstHorizon, "Rewards Credit Card", AccountType.CREDIT_CARD, new BigDecimal("-812.44")),
                new BigDecimal("4187.56")), new BigDecimal("5000.00")));

        Account chaseCard = accountRepository.save(withCreditLimit(withAvailable(
                new Account(chase, "Sapphire Preferred", AccountType.CREDIT_CARD, new BigDecimal("-2140.65")),
                new BigDecimal("7859.35")), new BigDecimal("10000.00")));

        Account sofiCard = accountRepository.save(withCreditLimit(withAvailable(
                new Account(sofi, "SoFi Credit Card", AccountType.CREDIT_CARD, new BigDecimal("-365.20")),
                new BigDecimal("4634.80")), new BigDecimal("5000.00")));

        Account fidelityBrokerage = accountRepository.save(withAvailable(
                new Account(fidelity, "Brokerage Account", AccountType.INVESTMENT, new BigDecimal("470900.00")),
                new BigDecimal("2500.00")));

        Account robinhoodBrokerage = accountRepository.save(withAvailable(
                new Account(robinhood, "Individual Brokerage", AccountType.INVESTMENT, new BigDecimal("112500.00")),
                new BigDecimal("1200.00")));

        Account chaseAutoLoan = accountRepository.save(
                new Account(chase, "Auto Loan", AccountType.LOAN, new BigDecimal("-18450.32")));

        Account sofiStudentLoan = accountRepository.save(
                new Account(sofi, "Student Loan", AccountType.LOAN, new BigDecimal("-24680.00")));

        seedHoldings(fidelityBrokerage, FIDELITY_HOLDINGS);
        seedHoldings(robinhoodBrokerage, ROBINHOOD_HOLDINGS);

        Random random = new Random(RANDOM_SEED);
        LocalDate today = LocalDate.now();

        int transactionCount = 0;
        transactionCount += seedRecurringTransactions(firstHorizonChecking, "Payroll Deposit", "Income",
                new BigDecimal("2500.00"), 14, 5, today);
        transactionCount += seedTransactionsForAccount(firstHorizonChecking, EVERYDAY_SPENDING, 12, random, today);

        transactionCount += seedTransactionsForAccount(chaseChecking, CHASE_CHECKING_ACTIVITY, 12, random, today);

        transactionCount += seedTransactionsForAccount(sofiSavings, SAVINGS_ACTIVITY, 6, random, today);

        transactionCount += seedTransactionsForAccount(firstHorizonCard, CREDIT_CARD_SPENDING, 10, random, today);
        transactionCount += seedTransactionsForAccount(chaseCard, CREDIT_CARD_SPENDING, 12, random, today);
        transactionCount += seedTransactionsForAccount(sofiCard, CREDIT_CARD_SPENDING, 8, random, today);

        transactionCount += seedTransactionsForAccount(fidelityBrokerage, BROKERAGE_ACTIVITY, 4, random, today);

        transactionCount += seedRecurringTransactions(chaseAutoLoan, "Chase Auto Loan Payment", "Loan Payment",
                new BigDecimal("-450.00"), 30, 3, today);
        transactionCount += seedRecurringTransactions(sofiStudentLoan, "SoFi Student Loan Payment", "Loan Payment",
                new BigDecimal("-280.00"), 30, 3, today);

        log.info("Seeded dev data for {}: 5 institutions, 10 accounts, {} holdings, {} transactions",
                TEST_USER_EMAIL, FIDELITY_HOLDINGS.size() + ROBINHOOD_HOLDINGS.size(), transactionCount);
    }

    private record HoldingSeed(String symbol, String name, BigDecimal shares, BigDecimal costBasis,
                                BigDecimal currentValue) {
    }

    private static final List<HoldingSeed> FIDELITY_HOLDINGS = List.of(
            new HoldingSeed("VTSAX", "Vanguard Total Stock Market Index Fund",
                    new BigDecimal("950.123456"), new BigDecimal("115000.00"), new BigDecimal("140000.00")),
            new HoldingSeed("FXAIX", "Fidelity 500 Index Fund",
                    new BigDecimal("480.500000"), new BigDecimal("78000.00"), new BigDecimal("90000.00")),
            new HoldingSeed("VTIAX", "Vanguard Total International Stock Index Fund",
                    new BigDecimal("1200.789000"), new BigDecimal("36000.00"), new BigDecimal("40000.00")),
            new HoldingSeed("VBTLX", "Vanguard Total Bond Market Index Fund",
                    new BigDecimal("3200.250000"), new BigDecimal("34500.00"), new BigDecimal("35000.00")),
            new HoldingSeed("SWPPX", "Schwab S&P 500 Index Fund",
                    new BigDecimal("250.333000"), new BigDecimal("12800.00"), new BigDecimal("15000.00")),
            new HoldingSeed("VYM", "Vanguard High Dividend Yield ETF",
                    new BigDecimal("210.500000"), new BigDecimal("19000.00"), new BigDecimal("22000.00")),
            new HoldingSeed("VUG", "Vanguard Growth ETF",
                    new BigDecimal("145.250000"), new BigDecimal("15800.00"), new BigDecimal("18500.00")),
            new HoldingSeed("VNQ", "Vanguard Real Estate ETF",
                    new BigDecimal("110.750000"), new BigDecimal("10500.00"), new BigDecimal("9200.00")),
            new HoldingSeed("QQQ", "Invesco QQQ Trust",
                    new BigDecimal("68.300000"), new BigDecimal("21000.00"), new BigDecimal("27000.00")),
            new HoldingSeed("VXUS", "Vanguard Total International Stock ETF",
                    new BigDecimal("195.600000"), new BigDecimal("12000.00"), new BigDecimal("11300.00")),
            new HoldingSeed("BND", "Vanguard Total Bond Market ETF",
                    new BigDecimal("190.400000"), new BigDecimal("15200.00"), new BigDecimal("14700.00")),
            new HoldingSeed("VIG", "Vanguard Dividend Appreciation ETF",
                    new BigDecimal("47.800000"), new BigDecimal("7600.00"), new BigDecimal("8900.00")),
            new HoldingSeed("SCHD", "Schwab US Dividend Equity ETF",
                    new BigDecimal("172.900000"), new BigDecimal("11900.00"), new BigDecimal("13400.00")),
            new HoldingSeed("VGT", "Vanguard Information Technology ETF",
                    new BigDecimal("32.600000"), new BigDecimal("14500.00"), new BigDecimal("19800.00")),
            new HoldingSeed("GLD", "SPDR Gold Shares",
                    new BigDecimal("26.100000"), new BigDecimal("5800.00"), new BigDecimal("6100.00"))
    );

    private static final List<HoldingSeed> ROBINHOOD_HOLDINGS = List.of(
            new HoldingSeed("SPY", "SPDR S&P 500 ETF Trust",
                    new BigDecimal("55.400000"), new BigDecimal("24500.00"), new BigDecimal("31000.00")),
            new HoldingSeed("IWM", "iShares Russell 2000 ETF",
                    new BigDecimal("41.200000"), new BigDecimal("9400.00"), new BigDecimal("8700.00")),
            new HoldingSeed("EFA", "iShares MSCI EAFE ETF",
                    new BigDecimal("84.900000"), new BigDecimal("6000.00"), new BigDecimal("6300.00")),
            new HoldingSeed("AGG", "iShares Core US Aggregate Bond ETF",
                    new BigDecimal("102.300000"), new BigDecimal("10600.00"), new BigDecimal("10200.00")),
            new HoldingSeed("ARKK", "ARK Innovation ETF",
                    new BigDecimal("98.700000"), new BigDecimal("7200.00"), new BigDecimal("4100.00")),
            new HoldingSeed("VTV", "Vanguard Value ETF",
                    new BigDecimal("76.500000"), new BigDecimal("10900.00"), new BigDecimal("12800.00")),
            new HoldingSeed("XLK", "Technology Select Sector SPDR Fund",
                    new BigDecimal("68.100000"), new BigDecimal("12100.00"), new BigDecimal("16500.00")),
            new HoldingSeed("XLF", "Financial Select Sector SPDR Fund",
                    new BigDecimal("165.400000"), new BigDecimal("6800.00"), new BigDecimal("7400.00")),
            new HoldingSeed("VEA", "Vanguard FTSE Developed Markets ETF",
                    new BigDecimal("205.800000"), new BigDecimal("9900.00"), new BigDecimal("9600.00")),
            new HoldingSeed("SCHB", "Schwab US Broad Market ETF",
                    new BigDecimal("92.300000"), new BigDecimal("4900.00"), new BigDecimal("5900.00"))
    );

    private void seedHoldings(Account account, List<HoldingSeed> seeds) {
        for (HoldingSeed seed : seeds) {
            holdingRepository.save(new Holding(
                    account, seed.symbol(), seed.name(), seed.shares(), seed.costBasis(), seed.currentValue()));
        }
    }

    private Account withAvailable(Account account, BigDecimal availableBalance) {
        account.setAvailableBalance(availableBalance);
        return account;
    }

    private Account withCreditLimit(Account account, BigDecimal creditLimit) {
        account.setCreditLimit(creditLimit);
        return account;
    }

    private int seedTransactionsForAccount(Account account, List<MerchantTemplate> templates, int count,
                                            Random random, LocalDate today) {
        for (int i = 0; i < count; i++) {
            MerchantTemplate template = templates.get(random.nextInt(templates.size()));
            int daysAgo = random.nextInt(HISTORY_DAYS);
            double rawAmount = template.minAmount() + random.nextDouble() * (template.maxAmount() - template.minAmount());
            BigDecimal amount = BigDecimal.valueOf(rawAmount).setScale(2, RoundingMode.HALF_UP);
            if (!template.credit()) {
                amount = amount.negate();
            }

            Transaction transaction = new Transaction(account, amount, template.description(), today.minusDays(daysAgo));
            transaction.setCategory(template.category());
            transaction.setPending(daysAgo <= 2 && random.nextBoolean());
            transactionRepository.save(transaction);
        }
        return count;
    }

    private int seedRecurringTransactions(Account account, String description, String category, BigDecimal amount,
                                           int intervalDays, int occurrences, LocalDate today) {
        int created = 0;
        for (int i = 0; i < occurrences; i++) {
            int daysAgo = i * intervalDays;
            if (daysAgo > HISTORY_DAYS) {
                break;
            }
            Transaction transaction = new Transaction(account, amount, description, today.minusDays(daysAgo));
            transaction.setCategory(category);
            transactionRepository.save(transaction);
            created++;
        }
        return created;
    }

}
