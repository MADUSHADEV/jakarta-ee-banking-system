package com.mdev.banking.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mdev.banking.ejb.AccountService;
import com.mdev.banking.ejb.exception.InsufficientFundsException;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/withdraw")
@ServletSecurity(
        @HttpConstraint(rolesAllowed = {"USER", "ADMIN"})
)
public class WithdrawServlet extends HttpServlet {

    @EJB
    private AccountService accountService;

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            // Read the request body
            StringBuilder requestBody = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }

            // Parse JSON to Map for simple key-value access
            @SuppressWarnings("unchecked")
            Map<String, Object> withdrawRequest = gson.fromJson(requestBody.toString(), Map.class);

            // Validate input
            if (withdrawRequest == null || !withdrawRequest.containsKey("accountNumber") ||
                    withdrawRequest.get("accountNumber") == null ||
                    withdrawRequest.get("accountNumber").toString().trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Account number is required");
                resp.getWriter().write(gson.toJson(errorResponse));
                return;
            }

            if (!withdrawRequest.containsKey("amount") || withdrawRequest.get("amount") == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Amount is required");
                resp.getWriter().write(gson.toJson(errorResponse));
                return;
            }

            String accountNumber = withdrawRequest.get("accountNumber").toString().trim();
            double amount;

            try {
                amount = Double.parseDouble(withdrawRequest.get("amount").toString());
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid amount format");
                resp.getWriter().write(gson.toJson(errorResponse));
                return;
            }

            if (amount <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Amount must be greater than 0");
                resp.getWriter().write(gson.toJson(errorResponse));
                return;
            }

            // Perform withdrawal
            accountService.withdraw(accountNumber, amount);

            resp.setStatus(HttpServletResponse.SC_OK);
            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("status", "success");
            successResponse.put("message", "Withdrawal completed successfully");
            resp.getWriter().write(gson.toJson(successResponse));

        } catch (InsufficientFundsException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            resp.getWriter().write(gson.toJson(errorResponse));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            resp.getWriter().write(gson.toJson(errorResponse));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to process withdrawal: " + e.getMessage());
            resp.getWriter().write(gson.toJson(errorResponse));
        }
    }
}