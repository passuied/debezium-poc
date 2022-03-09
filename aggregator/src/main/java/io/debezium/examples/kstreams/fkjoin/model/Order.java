package io.debezium.examples.kstreams.fkjoin.model;

import io.debezium.time.Date;

public class Order {
    public int order_number;
    public Date order_date;
    public int purchaser;
    public int quantity;
    public int product_id;

    @Override
    public String toString(){
        return "Order [number=" +order_number +", date=" +order_date +", purchaser=" +purchaser +", quantity=" +quantity +" product_id=" +product_id +"]";
    }
    
}
