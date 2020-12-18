package com.romanmarkunas.blog.memory.example16;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.romanmarkunas.blog.memory.example15.PooledByteArrayMap;
import gnu.trove.map.hash.TLongIntHashMap;
import org.agrona.collections.Int2ObjectHashMap;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class SlabAllocatorOrderStoreMain {

    public static void main(String[] args) throws IOException {
        long startTimeMs = System.currentTimeMillis();

        ObjectMapper mapper = new ObjectMapper();
        PooledByteArrayMap pool = new PooledByteArrayMap(80_000);
        OrderSlabAllocator orderAllocator = new OrderSlabAllocator(pool);

        TLongIntHashMap ordersById = new TLongIntHashMap(8, 0.99f, -1, -1);
        Int2ObjectHashMap<int[]> ordersByUser = new Int2ObjectHashMap<>(8, 0.85f);

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                if (scanner.hasNextLine()) {
                    String received = scanner.nextLine();
                    JsonNode orderFields = mapper.readTree(received);
                    int orderRef = orderAllocator.allocate();
                    OrderView orderView = orderAllocator.get(orderRef);
                    orderView.set(
                            orderFields.get("id").asLong(),
                            orderFields.get("user").binaryValue(),
                            orderFields.get("articleNr").asInt(),
                            orderFields.get("count").asInt(),
                            orderFields.get("pricePence").asInt(),
                            orderFields.get("addressNumber").asText(),
                            orderFields.get("addressStreet").asText(),
                            orderFields.get("addressCity").asText(),
                            orderFields.get("addressRegion").asText(),
                            orderFields.get("addressPostCode").asText()
                    );

                    ordersById.put(orderView.getId(), orderRef);

                    // cannot use IntArrayList as it has min capacity of 10
                    int userPoolKey = orderView.getUserPoolKey();
                    int[] orders = ordersByUser.get(userPoolKey);
                    if (orders == null) {
                        ordersByUser.put(userPoolKey, new int[] {orderRef});
                    }
                    else {
                        int[] newOrders = new int[orders.length + 1];
                        System.arraycopy(orders, 0, newOrders, 0, orders.length);
                        newOrders[orders.length] = orderRef;
                        ordersByUser.put(userPoolKey, newOrders);
                    }
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
