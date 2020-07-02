package com.romanmarkunas.blog.memory.example1;

import com.romanmarkunas.blog.memory.address.Address;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class OrderGeneratorTest {

    @Test
    void shouldProduceDeterministicResults() {
        List<Address> addresses = asList(address("1"), address("2"), address("3"));
        OrderGenerator og1 = new OrderGenerator(addresses);
        OrderGenerator og2 = new OrderGenerator(addresses);
        OrderGenerator og3 = new OrderGenerator(addresses);

        for (int i = 0; i < 1_000_000; i++) {
            Order order1 = og1.next();
            Order order2 = og2.next();
            Order order3 = og3.next();

            assertThat(order1).isEqualToComparingFieldByField(order2);
            assertThat(order2).isEqualToComparingFieldByField(order3);
            assertThat(order1).isEqualToComparingFieldByField(order3);
        }
    }


    private Address address(String differentiator) {
        return new Address(
                "number" + differentiator,
                "street" + differentiator,
                "city" + differentiator,
                "region" + differentiator,
                "postCode" + differentiator
        );
    }
}