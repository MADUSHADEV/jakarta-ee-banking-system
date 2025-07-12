package com.mdev.banking.ejb.exception;

import jakarta.ejb.ApplicationException;

/**
 * Custom exception to represent a failed withdrawal or transfer.
 * The 'rollback = true' property tells the EJB container to automatically
 * roll back the current transaction when this exception is thrown.
 */
@ApplicationException(rollback = true)
public class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
