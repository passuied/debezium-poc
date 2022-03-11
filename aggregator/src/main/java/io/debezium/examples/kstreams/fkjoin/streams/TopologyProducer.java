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
import io.debezium.examples.kstreams.fkjoin.model.CustomerWithAddresses;
import io.debezium.serde.DebeziumSerdes;
import io.quarkus.kafka.client.serialization.JsonbSerde;
import static java.util.Map.entry;

@ApplicationScoped
public class TopologyProducer {

    @ConfigProperty(name = "customers.topic")
    String customersTopic;

    @ConfigProperty(name = "addresses.topic")
    String addressesTopic;

    @ConfigProperty(name = "customers.with.addresses.topic")
    String customersWithAddressesTopic;

    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        Serde<Long> adressKeySerde = DebeziumSerdes.payloadJson(Long.class);
        adressKeySerde.configure(Collections.emptyMap(), true);
        Serde<Address> addressSerde = DebeziumSerdes.payloadJson(Address.class);
        var cfgSerde = Map.ofEntries(
            entry("from.field", "after")
            //, entry("unknown.properties.ignored", "true")
            );
        addressSerde.configure(cfgSerde, false);

        Serde<Integer> customersKeySerde = DebeziumSerdes.payloadJson(Integer.class);
        customersKeySerde.configure(Collections.emptyMap(), true);
        Serde<Customer> customersSerde = DebeziumSerdes.payloadJson(Customer.class);
        customersSerde.configure(cfgSerde, false);

        JsonbSerde<AddressListByCustomerId> addressListByCustomerIdSerde = new JsonbSerde<>(AddressListByCustomerId.class);

        JsonbSerde<CustomerWithAddresses> customerWithAddressesSerde = new JsonbSerde<>(CustomerWithAddresses.class);

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

        // TODO: remove - just for validation
        // addressesByCustomer.toStream()
        //     .to(
        //         "addresses-by-customer-id",
        //         Produced.with(Serdes.Integer(), addressListByCustomerIdSerde)
        //     );
                

        KTable<Integer, Customer> customers = builder.table(
                customersTopic,
                Consumed.with(customersKeySerde, customersSerde)
        );
       

        KTable<Integer, CustomerWithAddresses> customersWithAddresses = customers.leftJoin(
                addressesByCustomer,
                (cust, apc) -> new CustomerWithAddresses(cust, apc != null? apc.getAddresses(): null),
                Materialized.with(Serdes.Integer(), customerWithAddressesSerde)
            );

        customersWithAddresses.toStream()
        .to(
                customersWithAddressesTopic,
                Produced.with(Serdes.Integer(), customerWithAddressesSerde)
        );

        return builder.build();
    }
}
