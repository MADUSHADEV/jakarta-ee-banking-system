package com.mdev.banking.ejb;


import com.mdev.banking.core.entity.Account;
import com.mdev.banking.ejb.exception.InsufficientFundsException;
import com.mdev.banking.ejb.interceptor.LoggingInterceptor;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

/**
 * EJB for managing banking operations like deposit, withdraw, and transfer.
 * Demonstrates transactions, security, and exception handling.
 */
@Stateless
@Interceptors(LoggingInterceptor.class)
@DenyAll // By default, deny all access unless a method specifies otherwise.
public class AccountService {
    @PersistenceContext(unitName = "BankPU")
    private EntityManager em;

    @PermitAll // Anyone can create an account.
    public void createAccount(Account account) {
        em.persist(account);
    }

    @RolesAllowed({"USER", "ADMIN"}) // Only users and admins can find accounts.
    public Optional<Account> findAccountByNumber(String accountNumber) {
        try {
            Account account = em.createQuery("SELECT a FROM Account a WHERE a.accountNumber = :accNum", Account.class)
                    .setParameter("accNum", accountNumber)
                    .getSingleResult();
            return Optional.of(account);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @RolesAllowed({"USER", "ADMIN"})
    public List<Account> getAllAccounts() {
        return em.createQuery("SELECT a FROM Account a", Account.class).getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @RolesAllowed({"USER", "ADMIN"})
    public void deposit(String accountNumber, double amount) {
        findAccountByNumber(accountNumber).ifPresent(account -> {
            account.setBalance(account.getBalance() + amount);
            em.merge(account);
        });
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @RolesAllowed({"USER", "ADMIN"})
    public void withdraw(String accountNumber, double amount) throws InsufficientFundsException {
        Account account = findAccountByNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (account.getBalance() < amount) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal. Balance: " + account.getBalance());
        }
        account.setBalance(account.getBalance() - amount);
        em.merge(account);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @RolesAllowed("ADMIN") // Only admins can perform transfers.
    public void transfer(String fromAccountNumber, String toAccountNumber, double amount) throws InsufficientFundsException {
        // This method combines two operations in a single transaction.
        // If either withdraw or deposit fails, the whole operation is rolled back.
        withdraw(fromAccountNumber, amount);
        deposit(toAccountNumber, amount);
    }
}
