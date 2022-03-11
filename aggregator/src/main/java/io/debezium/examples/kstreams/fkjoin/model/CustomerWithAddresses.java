package io.debezium.examples.kstreams.fkjoin.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomerWithAddresses {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerWithAddresses.class);

    public Customer customer;
    public List<Address> addresses = new ArrayList<>();

    public CustomerWithAddresses() {
    }
    public CustomerWithAddresses(Customer customer, ArrayList<Address> addresses) {
        this.customer = customer;
        this.addresses = addresses != null ? addresses : new ArrayList<>();
    }


    @Override
    public String toString() {
        return "CustomerWithAddresses [customer=" + customer + ", addresses=" + addresses + "]";
    }
}
