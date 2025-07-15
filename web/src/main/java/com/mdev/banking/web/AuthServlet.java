package com.mdev.banking.web;

import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/auth/login")
public class AuthServlet extends HttpServlet {

    @Inject
    private SecurityContext securityContext;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userName = req.getParameter("username");
        String password = req.getParameter("password");

        AuthenticationParameters parameters = AuthenticationParameters.withParams()
                .credential(new UsernamePasswordCredential(userName, password));

        AuthenticationStatus status = securityContext.authenticate(req, resp, parameters);

        System.out.println("Authentication Status: " + status);

        if (status == AuthenticationStatus.SUCCESS) {
            resp.sendRedirect(req.getContextPath() + "/customers.html");
        } else {
            resp.sendRedirect(req.getContextPath() + "/signin.html");
        }
    }

}


