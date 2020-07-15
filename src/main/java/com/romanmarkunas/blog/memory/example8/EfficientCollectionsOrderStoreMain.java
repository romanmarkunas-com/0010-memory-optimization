package com.romanmarkunas.blog.memory.example8;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.romanmarkunas.blog.memory.example1.Order;
import org.agrona.collections.Object2ObjectHashMap;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class EfficientCollectionsOrderStoreMain {

    public static void main(String[] args) throws JsonProcessingException {
        long startTimeMs = System.currentTimeMillis();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Order> ordersById = new Object2ObjectHashMap<>();
        Map<String, List<Order>> ordersByUser = new Object2ObjectHashMap<>();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                if (scanner.hasNextLine()) {
                    String received = scanner.nextLine();
                    Order order = mapper.readValue(received, Order.class);
                    ordersById.put(order.getId(), order);
                    ordersByUser.computeIfAbsent(order.getUser(), key -> new ArrayList<>(1)).add(order);
                } else {
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
