package com.mdev.banking.ejb.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * An interceptor for logging EJB method calls.
 * This demonstrates a cross-cutting concern.
 */
public class LoggingInterceptor implements Serializable {
    private static final Logger logger = Logger.getLogger(LoggingInterceptor.class.getName());

    @AroundInvoke
    public Object logMethodCall(InvocationContext ctx) throws Exception {
        String methodName = ctx.getTarget().getClass().getSimpleName() + "." + ctx.getMethod().getName();
        logger.info("===> Entering method: " + methodName);
        try {
            return ctx.proceed();
        } finally {
            logger.info("<=== Exiting method: " + methodName);
        }
    }
}
