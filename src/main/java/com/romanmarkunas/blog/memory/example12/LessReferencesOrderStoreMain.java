package com.romanmarkunas.blog.memory.example12;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.romanmarkunas.blog.memory.example11.Base36String;
import org.agrona.collections.Long2ObjectHashMap;
import org.agrona.collections.Object2ObjectHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class LessReferencesOrderStoreMain {

    public static void main(String[] args) throws JsonProcessingException {
        long startTimeMs = System.currentTimeMillis();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setInjectableValues(new InjectableValues.Std().addValue(
                ByteArrayPool.class,
                new TreeBasedByteArrayPool()
        ));
        Long2ObjectHashMap<Order> ordersById = new Long2ObjectHashMap<>();
        Map<Base36String, List<Order>> ordersByUser = new Object2ObjectHashMap<>();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                if (scanner.hasNextLine()) {
                    String received = scanner.nextLine();
                    Order order = mapper.readValue(received, Order.class);
                    ordersById.put(order.getId(), order);
                    ordersByUser.computeIfAbsent(new Base36String(order.getUser()), key -> new ArrayList<>(1)).add(order);
                }
                else {
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(5));
                }
            }
        }
        catch (OutOfMemoryError oom) {
            System.out.println("Total orders: " + ordersById.size());
            System.out.println("Total users: " + ordersByUser.size());
            System.out.println("Total run time: " + (System.currentTimeMillis() - startTimeMs));
            throw oom;
        }
    }
}
