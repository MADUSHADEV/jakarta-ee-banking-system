package com.mdev.banking.ejb;

import com.mdev.banking.core.entity.Customer;
import com.mdev.banking.ejb.interceptor.LoggingInterceptor;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

/**
 * EJB for managing Customer entities.
 */
@Stateless
@Interceptors(LoggingInterceptor.class)
public class CustomerService {
    @PersistenceContext(unitName = "BankPU")
    private EntityManager em;

    public void createCustomer(Customer customer) {
        em.persist(customer);
    }

    public Customer findCustomer(Long id) {
        return em.find(Customer.class, id);
    }

    public List<Customer> getAllCustomers() {
        return em.createQuery("SELECT c FROM Customer c", Customer.class).getResultList();
    }
}
