package io.debezium.examples.kstreams.fkjoin.streams;

import java.util.Collections;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.debezium.examples.kstreams.fkjoin.model.Address;
import io.debezium.examples.kstreams.fkjoin.model.AddressListByCustomerId;
import io.debezium.examples.kstreams.fkjoin.model.Customer;
import io.debezium.examples.kstreams.fkjoin.model.CustomerAggregate;
import io.debezium.examples.kstreams.fkjoin.model.Order;
import io.debezium.examples.kstreams.fkjoin.model.OrderDbz;
import io.debezium.examples.kstreams.fkjoin.model.OrderListByCustomerId;
import io.debezium.serde.DebeziumSerdes;
import io.quarkus.kafka.client.serialization.JsonbSerde;
import static java.util.Map.entry;

@ApplicationScoped
public class TopologyProducer {

    @ConfigProperty(name = "customers.topic")
    String customersTopic;

    @ConfigProperty(name = "addresses.topic")
    String addressesTopic;

    @ConfigProperty(name = "orders.topic")
    String ordersTopic;

    @ConfigProperty(name = "customers.aggregate.topic")
    String customersAggregateTopic;

    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        var cfgSerde = Map.ofEntries(
            entry("from.field", "after")
            //, entry("unknown.properties.ignored", "true")
            );

        // Addresses
        Serde<Long> adressKeySerde = DebeziumSerdes.payloadJson(Long.class);
        adressKeySerde.configure(Collections.emptyMap(), true);
        Serde<Address> addressSerde = DebeziumSerdes.payloadJson(Address.class);
        addressSerde.configure(cfgSerde, false);
        JsonbSerde<AddressListByCustomerId> addressListByCustomerIdSerde = new JsonbSerde<>(AddressListByCustomerId.class);

        KTable<Long, Address> addresses = builder.table(
            addressesTopic, 
            Consumed.with(adressKeySerde, addressSerde)
        );

        KTable<Integer, AddressListByCustomerId> addressesByCustomer = addresses
                //.selectKey((addId, address) -> address.customer_id)
                .groupBy(
                (addressId, address) -> KeyValue.pair(address.customer_id, address),    
                Grouped.with(Serdes.Integer(), addressSerde))
                .aggregate( 
                    AddressListByCustomerId::new,
                    (customerId, address, list) -> list.addAddress(customerId, address),
                    (customerId, address, list) -> list.removeAddress(address),
                    Materialized.with(Serdes.Integer(), addressListByCustomerIdSerde)
                );

        // Orders
        Serde<Integer> orderKeySerde = DebeziumSerdes.payloadJson(Integer.class);
        orderKeySerde.configure(Collections.emptyMap(), true);
        Serde<OrderDbz> orderDbzSerde = DebeziumSerdes.payloadJson(OrderDbz.class);
        orderDbzSerde.configure(cfgSerde, false);

        JsonbSerde<OrderListByCustomerId> orderListByCustomerIdSerde = new JsonbSerde<>(OrderListByCustomerId.class);

        KTable<Integer, OrderDbz> orders = builder.table(
            ordersTopic, 
            Consumed.with(orderKeySerde, orderDbzSerde)
        );

        KTable<Integer, OrderListByCustomerId> ordersByCustomer = orders
                .groupBy(
                    (orderId, order) -> KeyValue.pair(order.purchaser, order),    
                    Grouped.with(Serdes.Integer(), orderDbzSerde))
                .aggregate( 
                    OrderListByCustomerId::new,
                    (customerId, order, list) -> list.addOrder(customerId, new Order(order)),
                    (customerId, order, list) -> list.removeOrder(new Order(order)),
                    Materialized.with(Serdes.Integer(), orderListByCustomerIdSerde)
                );

        // Customers
        Serde<Integer> customersKeySerde = DebeziumSerdes.payloadJson(Integer.class);
        customersKeySerde.configure(Collections.emptyMap(), true);
        Serde<Customer> customersSerde = DebeziumSerdes.payloadJson(Customer.class);
        customersSerde.configure(cfgSerde, false);
        JsonbSerde<CustomerAggregate> customerAggregateSerde = new JsonbSerde<>(CustomerAggregate.class);

        KTable<Integer, Customer> customers = builder.table(
                customersTopic,
                Consumed.with(customersKeySerde, customersSerde)
        );
       

        KTable<Integer, CustomerAggregate> customersWithAddresses = customers
            .leftJoin(
                addressesByCustomer,
                (cust, apc) -> new CustomerAggregate(cust, apc != null? apc.getAddresses(): null, null),
                Materialized.with(Serdes.Integer(), customerAggregateSerde)
            )
            .leftJoin(
                ordersByCustomer,
                (aggr, opc) -> new CustomerAggregate(aggr.customer, aggr.addresses, opc != null? opc.getOrders(): null),
                Materialized.with(Serdes.Integer(), customerAggregateSerde)
                );

        // save result to topic
        customersWithAddresses.toStream()
        .to(
                customersAggregateTopic,
                Produced.with(Serdes.Integer(), customerAggregateSerde)
        );

        return builder.build();
    }
}
