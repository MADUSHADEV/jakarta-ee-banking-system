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

    /**
     * This method runs automatically every day at 1 minute past midnight.
     * It calculates and deposits daily interest for all accounts.
     * persistent = false means the timer won't survive a server restart.
     */
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
}
