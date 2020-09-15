package com.romanmarkunas.blog.memory.example14;

import java.util.Arrays;

public class PooledByteArrayMap {

    private static final byte[] REMOVED = new byte[0];
    private static final long FREE_OR_REMOVED = 0;

    private int keyUniquefier = 1;
    private long[] keys;
    private byte[][] values;
    private int[] usages;
    private int size = 0;


    public PooledByteArrayMap(int initialCapacity) {
        createInternalArrays(initialCapacity);
    }


    public long put(byte[] value) {
        int valueHashCode = Arrays.hashCode(value) & 0x7fffffff;

        ensureCapacity(size + 1);

        int index = findSlotFor(value, valueHashCode);

        if (index < 0) {
            int indexOfExistingItem = -(index + 1);
            usages[indexOfExistingItem]++;
            return keys[indexOfExistingItem];
        }
        else {
            long uniquePrefix = (getAndIncrementUniquifier() << Integer.SIZE) & 0xFFFF0000;
            long key = uniquePrefix + valueHashCode;

            keys[index] = key;
            values[index] = value;
            usages[index] = 1;
            size++;
            return key;
        }
    }

    public byte[] get(long key) {
        int index = findSlotOf(key);
        return index < 0 ? null : values[index];
    }

    public void free(long key) {
        int index = findSlotOf(key);

        if (index >= 0) {
            usages[index]--;
            if (usages[index] <= 0) {
                keys[index] = FREE_OR_REMOVED;
                values[index] = REMOVED;
                size--;
            }
        }
    }

    public int size() {
        return size;
    }


    private void createInternalArrays(int capacity) {
        keys = new long[capacity];
        values = new byte[capacity][];
        usages = new int[capacity];
        Arrays.fill(keys, FREE_OR_REMOVED);
        Arrays.fill(values, null);
        Arrays.fill(usages, 0);
    }

    private void ensureCapacity(int desiredCapacity) {
        int currentCapacity = keys.length;
        if (desiredCapacity <= 0) {
            throw new IllegalStateException("Bad desired capacity, check overflow. Desired: " + desiredCapacity + ", current:" + currentCapacity);
        }

        if (currentCapacity >= desiredCapacity) {
            return;
        }

        // + resizing algorithm that is more gentle to bigger sizes, e.g increase by 1 MB after specific size
        int newCapacity = currentCapacity > ((Integer.MAX_VALUE) / 2) ? Integer.MAX_VALUE : currentCapacity * 2;

        long[] oldKeys = keys;
        byte[][] oldValues = values;
        int[] oldUsages = usages;

        createInternalArrays(newCapacity);

        for (int i = 0; i < oldKeys.length; i++) {
            long oldKey = oldKeys[i];
            if (oldKey != FREE_OR_REMOVED) {
                byte[] oldValue = oldValues[i];

                int index = findSlotFor(oldValue, hashCode(oldKey));
                if (index < 0) {
                    throw new IllegalStateException("byte[] was mutated, leaving pool in inconsistent state");
                }

                keys[index] = oldKey;
                values[index] = oldValue;
                usages[index] = oldUsages[i];
            }
        }
    }

    private int findSlotFor(byte[] value, int valueHashCode) {
        int startingIndex = valueHashCode % values.length;
        int firstRemoved = -1;

        int i = startingIndex;
        do
        {
            if (isFree(i)) {
                return i;
            }
            if (isFull(i) && Arrays.equals(value, values[i])) {
                return -i - 1; // encode existing value as negative number, -1 is necessary because 0 is valid index
            }
            if (firstRemoved == -1 && isRemoved(i)) {
                firstRemoved = i;
            }
            i = incrementIndex(i);
        }
        while (i != startingIndex);

        if (firstRemoved != -1) {
            return firstRemoved;
        }

        throw new IllegalStateException("linear probing should always succeed given enough capacity!");
    }

    private int incrementIndex(int index) {
        return index >= keys.length - 1 ? 0 : index + 1;
    }

    private long getAndIncrementUniquifier() {
        if (keyUniquefier >= Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Too many objects submitted for pooling - cannot guarantee operation");
        }
        return keyUniquefier++;
    }

    private boolean isFree(int index) {
        return keys[index] == FREE_OR_REMOVED && values[index] != REMOVED;
    }

    private boolean isRemoved(int index) {
        return keys[index] == FREE_OR_REMOVED && values[index] == REMOVED;
    }

    private boolean isFull(int index) {
        return keys[index] != FREE_OR_REMOVED;
    }

    private int findSlotOf(long key) {
        int valueHashCode = hashCode(key);

        int startingIndex = valueHashCode % values.length;

        int i = startingIndex;
        do
        {
            if (isFree(i)) {
                return -1;
            }
            if (isFull(i) && key == keys[i]) {
                return i;
            }
            i = incrementIndex(i);
        }
        while (i != startingIndex);

        return -1;
    }

    private int hashCode(long key) {
        return (int) (key & 0x0000FFFF);
    }
}
