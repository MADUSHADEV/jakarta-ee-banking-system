package com.mdev.banking.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mdev.banking.core.entity.Customer;
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
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/customers")
@ServletSecurity(
        @HttpConstraint(rolesAllowed = {"USER", "ADMIN"})
)
public class CustomerServlet extends HttpServlet {

    @EJB
    private CustomerService customerService;

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<Customer> customers = customerService.getAllCustomers();

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            resp.setHeader("Pragma", "no-cache");
            resp.setHeader("Expires", "0");

            resp.getWriter().write(gson.toJson(customers));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve customers: " + e.getMessage());
            resp.getWriter().write(gson.toJson(errorResponse));
        }
    }

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

            // Parse JSON to Customer object using Gson
            Customer newCustomer = gson.fromJson(requestBody.toString(), Customer.class);

            // Validate input
            if (newCustomer == null || newCustomer.getName() == null || newCustomer.getName().trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Customer name is required");
                resp.getWriter().write(gson.toJson(errorResponse));
                return;
            }

            if (newCustomer.getContactInfo() == null || newCustomer.getContactInfo().trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Customer contact info is required");
                resp.getWriter().write(gson.toJson(errorResponse));
                return;
            }

            // Create the customer
            customerService.createCustomer(newCustomer);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("status", "success");
            successResponse.put("message", "Customer created successfully");
            resp.getWriter().write(gson.toJson(successResponse));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create customer: " + e.getMessage());
            resp.getWriter().write(gson.toJson(errorResponse));
        }
    }
}