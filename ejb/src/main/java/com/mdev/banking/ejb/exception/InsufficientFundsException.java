package com.mdev.banking.ejb.exception;

import jakarta.ejb.ApplicationException;

/**
 * Custom exception to represent a failed withdrawal or transfer.
 */
@ApplicationException(rollback = true)
public class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
