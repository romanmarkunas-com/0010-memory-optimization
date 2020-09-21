package com.romanmarkunas.blog.memory.example14;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import org.agrona.collections.Long2ObjectHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class CustomStringPoolOrderStoreMain {

    public static void main(String[] args) throws JsonProcessingException {
        long startTimeMs = System.currentTimeMillis();

        ObjectMapper mapper = new ObjectMapper();
        mapper.setInjectableValues(new InjectableValues.Std().addValue(
                PooledByteArrayMap.class,
                new PooledByteArrayMap(1024)
        ));

        Long2ObjectHashMap<Order> ordersById = new Long2ObjectHashMap<>();
        Long2ObjectHashMap<List<Order>> ordersByUser = new Long2ObjectHashMap<>();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                if (scanner.hasNextLine()) {
                    String received = scanner.nextLine();
                    Order order = mapper.readValue(received, Order.class);
                    ordersById.put(order.getId(), order);
                    ordersByUser.computeIfAbsent(order.getUser(), key -> new ArrayList<>(1)).add(order);
                }
                else {
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(5));
                }
            }
        }
        catch (OutOfMemoryError | ValueInstantiationException oom) {
            System.out.println("Total orders: " + ordersById.size());
            System.out.println("Total users: " + ordersByUser.size());
            System.out.println("Total run time: " + (System.currentTimeMillis() - startTimeMs));
            throw oom;
        }
    }
}
