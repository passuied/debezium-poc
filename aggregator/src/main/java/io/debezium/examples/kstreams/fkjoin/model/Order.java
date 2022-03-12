package io.debezium.examples.kstreams.fkjoin.model;

import java.util.Date;

public class Order {
    public int order_number;
    public Date order_date;
    public int purchaser;
    public int quantity;
    public int product_id;

    public Order()
    {
    }
    
    public Order(OrderDbz order) {
        
        if (order != null) {
            this.order_number = order.order_number;
            this.order_date = org.apache.kafka.connect.data.Date.toLogical(org.apache.kafka.connect.data.Date.SCHEMA, order.order_date);
            this.purchaser = order.purchaser;
            this.quantity = order.quantity;
            this.product_id = order.product_id;
        }
    }

    @Override
    public String toString(){
        return "Order [number=" +order_number +", date=" +order_date +", purchaser=" +purchaser +", quantity=" +quantity +" product_id=" +product_id +"]";
    }
}
