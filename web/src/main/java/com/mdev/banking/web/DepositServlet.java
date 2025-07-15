package com.mdev.banking.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mdev.banking.ejb.AccountService;
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

@WebServlet("/api/deposit")
@ServletSecurity(
        @HttpConstraint(rolesAllowed = {"USER", "ADMIN"})
)
public class DepositServlet extends HttpServlet {

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
            Map<String, Object> depositRequest = gson.fromJson(requestBody.toString(), Map.class);

            // Validate input
            if (depositRequest == null || !depositRequest.containsKey("accountNumber") ||
                    depositRequest.get("accountNumber") == null ||
                    depositRequest.get("accountNumber").toString().trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Account number is required");
                resp.getWriter().write(gson.toJson(errorResponse));
                return;
            }

            if (!depositRequest.containsKey("amount") || depositRequest.get("amount") == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Amount is required");
                resp.getWriter().write(gson.toJson(errorResponse));
                return;
            }

            String accountNumber = depositRequest.get("accountNumber").toString().trim();
            double amount;

            try {
                amount = Double.parseDouble(depositRequest.get("amount").toString());
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

            // Perform deposit
            accountService.deposit(accountNumber, amount);

            resp.setStatus(HttpServletResponse.SC_OK);
            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("status", "success");
            successResponse.put("message", "Deposit completed successfully");
            resp.getWriter().write(gson.toJson(successResponse));

        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            resp.getWriter().write(gson.toJson(errorResponse));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to process deposit: " + e.getMessage());
            resp.getWriter().write(gson.toJson(errorResponse));
        }
    }
}