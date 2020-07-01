package com.romanmarkunas.blog.memory.example1;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderGeneratorTest {

    @Test
    void shouldProduceDeterministicResults() {
        OrderGenerator og1 = new OrderGenerator();
        OrderGenerator og2 = new OrderGenerator();
        OrderGenerator og3 = new OrderGenerator();

        for (int i = 0; i < 1_000_000; i++) {
            Order order1 = og1.next();
            Order order2 = og2.next();
            Order order3 = og3.next();

            assertThat(order1).isEqualToComparingFieldByField(order2);
            assertThat(order2).isEqualToComparingFieldByField(order3);
            assertThat(order1).isEqualToComparingFieldByField(order3);
        }
    }
}