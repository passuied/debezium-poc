package io.debezium.examples.kstreams.fkjoin.model;

import java.util.ArrayList;
import java.util.Iterator;

public class OrderListByCustomerId {
    public ArrayList<Order> orders = new ArrayList<>();
    public int customerId = 0;

    public OrderListByCustomerId() {

    }
    public OrderListByCustomerId(int customerId, ArrayList<Order> orders) {
        this.orders = orders != null? orders : new ArrayList<>();
    }

    public OrderListByCustomerId addOrder(int customerId, Order order) {
        this.customerId = customerId;
        orders.add(order);
        return this;
    }
    
    public OrderListByCustomerId removeOrder(Order order) {
        Iterator<Order> it = orders.iterator();
        while (it.hasNext()) {
            Order o = it.next();
            if (o.order_number == order.order_number) {
                it.remove();
                break;
            }
        }
        return this;
    }

    public ArrayList<Order> getOrders(){
        return orders;
    }
}
