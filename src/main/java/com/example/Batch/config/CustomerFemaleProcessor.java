package com.example.Batch.config;

import com.example.Batch.entity.Customer;
import org.springframework.batch.item.ItemProcessor;

public class CustomerFemaleProcessor implements ItemProcessor<Customer, Customer> {

    public Customer process(Customer customer) throws Exception {
        if (customer.getGender().equals("Female")) {
            return customer;
        } else {
            return null;
        }
    }
}
