package com.mdev.banking.web;

import com.mdev.banking.core.entity.Customer;
import com.mdev.banking.ejb.CustomerService;
import jakarta.ejb.EJB;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/customers")
public class CustomerServlet extends HttpServlet {
    @EJB(name = "com.mdev.banking.web.CustomerServlet/customerService")
    private CustomerService customerService;

    // A single, reusable JSON-B instance
    private final Jsonb jsonb = JsonbBuilder.create();

    /**
     * Handles GET requests to /api/customers.
     * Returns a JSON array of all customers.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. Get data from EJB
        List<Customer> customers = customerService.getAllCustomers();

        // 2. Set response headers for JSON
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // 3. Serialize the list of customers to JSON and write to the response
        resp.getWriter().write(jsonb.toJson(customers));
    }

    /**
     * Handles POST requests to /api/customers.
     * Consumes a JSON object from the request body to create a new customer.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // 1. Deserialize the JSON from the request body into a Customer object
            Customer newCustomer = jsonb.fromJson(req.getReader(), Customer.class);

            // 2. Use the EJB to persist the new customer
            customerService.createCustomer(newCustomer);

            // 3. Send a success response
            resp.setStatus(HttpServletResponse.SC_CREATED); // 201 Created status
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write("{\"status\":\"success\", \"message\":\"Customer created\"}");

        } catch (Exception e) {
            // Handle potential errors (e.g., malformed JSON)
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
        }
    }
}
