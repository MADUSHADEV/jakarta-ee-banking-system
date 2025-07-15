package com.mdev.banking.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mdev.banking.core.entity.Account;
import com.mdev.banking.core.entity.Customer;
import com.mdev.banking.ejb.AccountService;
import com.mdev.banking.ejb.CustomerService;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.BufferedReader;

@WebServlet("/api/accounts")
@ServletSecurity(
        @HttpConstraint(rolesAllowed = {"USER", "ADMIN"})
)
public class AccountServlet extends HttpServlet {
    @EJB
    private AccountService accountService;
    @EJB
    private CustomerService customerService;

    private final Gson gson = new GsonBuilder().create();

    /**
     * Handles GET requests to /api/accounts?customerId=...
     * Returns a JSON object containing the customer and their accounts.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String customerIdStr = req.getParameter("customerId");
        if (customerIdStr == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"customerId parameter is required\"}");
            return;
        }

        long customerId = Long.parseLong(customerIdStr);
        Customer customer = customerService.findCustomer(customerId);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(customer)); // Using Gson instead of JSON-B
    }

    /**
     * Handles POST requests to /api/accounts.
     * The request body should be a JSON object representing the new account.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // Read the request body
            BufferedReader reader = req.getReader();
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            // Parse JSON using Gson
            Account newAccount = gson.fromJson(jsonBuilder.toString(), Account.class);

            if (newAccount.getCustomer() == null || newAccount.getCustomer().getId() == null) {
                throw new IllegalArgumentException("JSON payload must include a customer object with an id.");
            }

            // Fetch the managed customer entity to ensure a valid relationship
            Customer customer = customerService.findCustomer(newAccount.getCustomer().getId());
            if (customer == null) {
                throw new IllegalArgumentException("Customer not found for the given ID.");
            }

            // Associate the managed customer with the new account
            newAccount.setCustomer(customer);
            newAccount.setId(null); // Ensure JPA generates a new ID

            accountService.createAccount(newAccount);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"status\":\"success\", \"message\":\"Account created\"}");

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
        }
    }
}