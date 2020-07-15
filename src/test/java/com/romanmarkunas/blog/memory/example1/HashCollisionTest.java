package com.romanmarkunas.blog.memory.example1;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.fail;

class HashCollisionTest {

    @Test
    void userCollisionRate() {
        HashSet<String> users = generateAllPossibleUsers();
        System.out.println("Total user count: " + users.size());

        for (int i = 24; i < 27; i++) {
            int mapCapacity = 1 << i;

            int collisions = 0;
            HashSet<Integer> javaMapSlots = new HashSet<>();

            for (String user : users) {
                if (!javaMapSlots.add(doWhateverJavaHashMapDoesToFigureOutSlot(user, mapCapacity))) {
                    collisions++;
                }
            }
            System.out.println(
                    "capacity: " + mapCapacity
                  + ", collisions: " + collisions
                  + ", simple int would be: " + Math.max(users.size() - mapCapacity, 0)
            );
        }
    }

    @Test
    void integerIdCollisionRate() {
        int totalIds = 17_576_000;
        for (int i = 24; i < 27; i++) {
            int hashMapCapacity = 1 << i;
            int collisions = 0;
            HashSet<Integer> slots = new HashSet<>();
            for (int j = 0; j < totalIds; j++) {
                if (!slots.add(doWhateverJavaHashMapDoesToFigureOutSlot(String.valueOf(j), hashMapCapacity))) {
                    collisions++;
                }
            }
            System.out.println(
                    "capacity: " + hashMapCapacity
                  + ", collisions: " + collisions
                  + ", simple int would be: " + Math.max(totalIds - hashMapCapacity, 0)
            );
        }
    }


    private HashSet<String> generateAllPossibleUsers() {
        HashSet<Integer> userHashes = new HashSet<>();
        HashSet<String> users = new HashSet<>();
        int charCount = 26;

        for (int i = 0; i < charCount; i++) {
            for (int k = 0; k < charCount; k++) {
                for (int m = 0; m < charCount; m++) {
                    for (int p = 0; p < 1000; p++) {
                        String user = charFrom(i) + charFrom(k) + charFrom(m) + String.format("%03d", p);
                        boolean added = userHashes.add(user.hashCode());
                        if (!added) {
                            fail("Duplicate hashcode caused by: " + user);
                        }
                        users.add(user);
                    }
                }
            }
        }
        return users;
    }

    private String charFrom(int i) {
        return "" + (char) ('A' + i);
    }

    private int doWhateverJavaHashMapDoesToFigureOutSlot(Object o, int capacity) {
        int hash = o.hashCode();
        return (capacity - 1) & (hash ^ (hash >>> 16));
    }
}