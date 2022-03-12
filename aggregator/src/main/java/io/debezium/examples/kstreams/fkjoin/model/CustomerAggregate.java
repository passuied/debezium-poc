package io.debezium.examples.kstreams.fkjoin.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomerAggregate {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerAggregate.class);

    public Customer customer;
    public List<Address> addresses = new ArrayList<>();
    public List<Order> orders = new ArrayList<>();

    public CustomerAggregate() {
    }
    public CustomerAggregate(Customer customer, List<Address> addresses, List<Order> orders) {
        this.customer = customer;
        this.addresses = addresses != null ? addresses : new ArrayList<>();
        this.orders = orders != null ? orders : new ArrayList<>();
    }


    @Override
    public String toString() {
        return "CustomerWithAddresses [customer=" + customer + ", addresses=" + addresses + ", orders=" + orders +"]";
    }
}
