package com.mdev.banking.ejb;


import com.mdev.banking.core.entity.Account;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;

import java.util.List;
import java.util.logging.Logger;

/**
 * EJB Timer Service for performing scheduled, automated tasks.
 */
@Stateless
@RunAs("ADMIN")
public class TimerServiceBean {
    private static final Logger logger = Logger.getLogger(TimerServiceBean.class.getName());

    @Inject
    private AccountService accountService;

    @Schedule(hour = "0", minute = "1", second = "0", persistent = false)
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void applyDailyInterest() {
        logger.info("TIMER: Starting daily interest calculation task.");
        List<Account> accounts = accountService.getAllAccounts();

        for (Account account : accounts) {
            // Daily interest = balance * (annual_rate / 365)
            double dailyInterest = account.getBalance() * (account.getInterestRate() / 365.0);
            accountService.deposit(account.getAccountNumber(), dailyInterest);
            logger.info("TIMER: Applied interest of " + String.format("%.4f", dailyInterest) + " to account " + account.getAccountNumber());
        }
        logger.info("TIMER: Finished daily interest calculation task.");
    }

    @Schedule(hour = "2", minute = "0", second = "0", persistent = false)
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void performScheduledTransfer() {
        logger.info("TIMER: Starting scheduled fund transfer task.");
        try {

            String fromAccount = "ACC001";
            String toAccount = "ACC002";
            double amount = 50.00;

            accountService.transfer(fromAccount, toAccount, amount);

            logger.info("TIMER: Successfully transferred " + amount + " from " + fromAccount + " to " + toAccount);

        } catch (Exception e) {

            logger.severe("TIMER: Scheduled fund transfer failed: " + e.getMessage());
        }
        logger.info("TIMER: Finished scheduled fund transfer task.");
    }
}
