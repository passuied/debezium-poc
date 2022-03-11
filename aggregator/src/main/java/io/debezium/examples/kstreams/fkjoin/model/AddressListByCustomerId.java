package io.debezium.examples.kstreams.fkjoin.model;

import java.util.ArrayList;
import java.util.Iterator;

public class AddressListByCustomerId {

    public ArrayList<Address> addresses = new ArrayList<>();
    public int customerId = 0;

    public AddressListByCustomerId() {

    }
    public AddressListByCustomerId(int customerId, ArrayList<Address> addresses) {
        this.addresses = addresses != null? addresses : new ArrayList<>();
    }

    public AddressListByCustomerId addAddress(int customerId, Address address) {
        this.customerId = customerId;
        addresses.add(address);
        return this;
    }
    
    public AddressListByCustomerId removeAddress(Address address) {
        
        Iterator<Address> it = addresses.iterator();
        while (it.hasNext()) {
            Address a = it.next();
            if (a.id == address.id) {
                it.remove();
                break;
            }
        }

        return this;
    }

    public ArrayList<Address> getAddresses(){
        return addresses;
    }
}
