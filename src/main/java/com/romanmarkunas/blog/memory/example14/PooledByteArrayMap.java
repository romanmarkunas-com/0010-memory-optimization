package com.romanmarkunas.blog.memory.example14;

import com.romanmarkunas.blog.memory.example14.checkers.ImmutableByteArray;

import java.util.Arrays;

public class PooledByteArrayMap {

    private static final int[] PROBING_STEPS = new int[] {
            99991,
            9973,
            997,
            97,
            7,
            1
    };
    private static final byte[] REMOVED = new byte[0];
    private static final long FREE_OR_REMOVED = 0;
    private static final int CAPACITY_INCREMENT = 4096;
    private static final int UNIQUIFIER_MASK = 0xFFFF0000;
    private static final int HASHCODE_MASK = 0x00007FFF;
//    private final LongHashFunction hashFunction = LongHashFunction.murmur_3();

    private int keyUniquefier = 1;
    private long[] keys;
    private byte[][] values;
    private int[] usages;
    private int size = 0;
    private int probingStep;


    public PooledByteArrayMap(int initialCapacity) {
        probingStep = probingStepFor(initialCapacity);
        int capacity = correctCapacityToAvoidLoopingOverSameSlots(probingStep, initialCapacity);
        createInternalArrays(capacity);
    }


    public long put(byte[] value) {
        int valueHashCode = calculateHashCode(value);

        ensureCapacity(size + 1);

        int index = findSlotFor(value, valueHashCode);

        if (index < 0) {
            int indexOfExistingItem = -(index + 1);
            if (usages[indexOfExistingItem] >= Integer.MAX_VALUE) {
                throw new IllegalStateException("Too many reuses of same byte[] - " + Arrays.toString(value));
            }
            usages[indexOfExistingItem]++;
            return keys[indexOfExistingItem];
        }
        else {
            long uniquePrefix = (getAndIncrementUniquifier() << Integer.SIZE) & UNIQUIFIER_MASK;
            long key = uniquePrefix | valueHashCode;

            keys[index] = key;
            values[index] = value;
            usages[index] = 1;
            size++;
            return key;
        }
    }

    public byte @ImmutableByteArray [] get(long key) {
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

        int newCapacity = increasedCapacity(currentCapacity);
        probingStep = probingStepFor(newCapacity);
        newCapacity = correctCapacityToAvoidLoopingOverSameSlots(probingStep, newCapacity);

        long[] oldKeys = keys;
        byte[][] oldValues = values;
        int[] oldUsages = usages;

        createInternalArrays(newCapacity);

        for (int i = 0; i < oldKeys.length; i++) {
            long oldKey = oldKeys[i];
            if (oldKey != FREE_OR_REMOVED) {
                byte[] oldValue = oldValues[i];

                int index = findSlotFor(oldValue, hashCodeFromKey(oldKey));

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
        do {
            if (isFree(i)) {
                return firstRemoved != -1 ? firstRemoved : i;
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
        index += probingStep;
        return index >= keys.length ? index - keys.length : index;
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
        int valueHashCode = hashCodeFromKey(key);
        int startingIndex = valueHashCode % values.length;

        int i = startingIndex;
        do {
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

    private static int hashCodeFromKey(long key) {
        return (int) (key & HASHCODE_MASK);
    }

    private static int calculateHashCode(byte[] value) {
//        return ((int) hashFunction.hashBytes(value)) & HASHCODE_MASK;

        int hashCode = Arrays.hashCode(value);
        hashCode = hashCode ^ (hashCode >>> 16);
        return hashCode & HASHCODE_MASK;
    }

    private static int probingStepFor(int capacity) {
        for (int i = 0; i < PROBING_STEPS.length; i++) {
            int step = PROBING_STEPS[i];
            if (step <= capacity) {
                return step;
            }
        }
        return 1; // never executed as last of PROBING_STEPS is 1 and capacity must be at least 1
    }

    private static int increasedCapacity(int currentCapacity) {
        // double until chunk (4096 ¬ 16K) worth of references
        if (currentCapacity < CAPACITY_INCREMENT) {
            return currentCapacity * 2;
        }
        // cap at max value
        if (currentCapacity > Integer.MAX_VALUE - CAPACITY_INCREMENT) {
            return Integer.MAX_VALUE;
        }
        // round up until whole increment of a chunk
        int mod = currentCapacity % CAPACITY_INCREMENT;
        if (mod != 0) {
            return currentCapacity + CAPACITY_INCREMENT - mod;
        }
        // add another chunk on top
        return currentCapacity + CAPACITY_INCREMENT;
    }

    private static int correctCapacityToAvoidLoopingOverSameSlots(final int probingStep, final int capacity) {
        if (probingStep > 1 && capacity % probingStep == 0) {
            return capacity + 1;
        }
        return capacity;
    }
}
