package com.romanmarkunas.blog.memory.example13;

import com.fasterxml.jackson.core.JsonProcessingException;
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

public class CompressingOrderStoreMain {

    public static void main(String[] args) throws JsonProcessingException {
        long startTimeMs = System.currentTimeMillis();
        ObjectMapper mapper = new ObjectMapper();
        Long2ObjectHashMap<OrderCompressed> ordersById = new Long2ObjectHashMap<>();
        Map<Base36String, List<OrderCompressed>> ordersByUser = new Object2ObjectHashMap<>();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                if (scanner.hasNextLine()) {
                    String received = scanner.nextLine();
                    Order order = mapper.readValue(received, Order.class);
                    OrderCompressed compressedOrder = OrderCompressed.compress(order);

                    ordersById.put(order.getId(), compressedOrder);
                    ordersByUser.computeIfAbsent(new Base36String(order.getUser()), key -> new ArrayList<>(1)).add(compressedOrder);
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
