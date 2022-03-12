package io.debezium.examples.kstreams.fkjoin.model;

/**
 * This is the DTO for DB table as read by Debezium. DATE fields are read as Int32 unless SMT are applied */ 
public class OrderDbz {
    public int order_number;
    public int order_date;
    public int purchaser;
    public int quantity;
    public int product_id;

    @Override
    public String toString(){

        return "Order [number=" +order_number +", date=" +order_date +", purchaser=" +purchaser +", quantity=" +quantity +" product_id=" +product_id +"]";
    }
    
}
