package com.mdev.banking.web.security;

import com.mdev.banking.core.entity.User;
import com.mdev.banking.ejb.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;

import java.util.Set;
import java.util.logging.Logger;

@ApplicationScoped
public class DatabaseIdentityStore implements IdentityStore {

    private static final Logger logger = Logger.getLogger(DatabaseIdentityStore.class.getName());

    @Inject
    private UserService userService;

    @Override
    public CredentialValidationResult validate(Credential credential) {
        if (credential instanceof UsernamePasswordCredential) {
            UsernamePasswordCredential upc = (UsernamePasswordCredential) credential;

            if (userService.validate(upc.getCaller(), upc.getPasswordAsString())) {
                User user = userService.findUserByUsername(upc.getCaller())
                        .orElseThrow(() -> new IllegalStateException("User not found: " + upc.getCaller()));


                // Log successful authentication
                return new CredentialValidationResult(user.getUsername(), Set.of(user.getRoles().toArray(String[]::new))); // Convert roles to uppercase for consistency);

            }
            logger.warning("Invalid credentials for user: " + upc.getCaller());

        }

        return CredentialValidationResult.INVALID_RESULT;
    }
}