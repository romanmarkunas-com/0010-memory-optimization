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
    private static final int CAPACITY_INCREMENT = 4096;
    private static final int HASHCODE_MASK = 0x7FFF_FFFF;

    private static final byte[] REMOVED_VALUE = new byte[0];
    private static final byte[] FREE_VALUE = null; // avoid initializing arrays on creation
    //      static final int REMOVED_KEY = any negative value;
    private static final int FREE_KEY = Integer.MIN_VALUE;
    private static final int NO_REMOVED_KEY = Integer.MIN_VALUE + 1;
//    private final LongHashFunction hashFunction = LongHashFunction.murmur_3();

    private int keyGenerator = 0;
    private int[] valueIndexesByKey; // index = key, cell = 1) positive until MAX - 2: value index; 2) negative: free or removed; 2a) negative MIN: free init value; 2b) negative not min: negate - 1 gives next free key slot for reuse
    private int[] keysByValueIndex; // index = value slot, cell = key
    private byte[][] values;
    private int[] usages;
    private int size = 0; // max size should be MAX - 2
    private int probingStep;
    private int lastRemovedKey = NO_REMOVED_KEY;


    public PooledByteArrayMap(int initialCapacity) {
        probingStep = probingStepFor(initialCapacity);
        int capacity = correctCapacityToAvoidLoopingOverSameSlots(probingStep, initialCapacity);
        createInternalArrays(capacity);
    }


    public long put(byte[] value) {
        // TODO: fail on null cause it's a special value of FREE cell
        int valueHashCode = calculateHashCode(value);

        ensureCapacity(size + 1);

        int index = findSlotFor(value, valueHashCode);

        if (index < 0) {
            int indexOfExistingItem = -(index + 1);
            int key = keysByValueIndex[indexOfExistingItem];
            if (usages[key] >= Integer.MAX_VALUE) {
                throw new IllegalStateException("Too many reuses of same byte[] - " + Arrays.toString(value));
            }
            usages[key]++;
            return key;
        }
        else {
            int key = lastRemovedKey == NO_REMOVED_KEY ? getAndIncrementUniquifier() : getLastRemovedKey();

            valueIndexesByKey[key] = index;
            keysByValueIndex[index] = key;
            values[index] = value;
            usages[key] = 1;
            size++;
            return key;
        }
    }

    private int getLastRemovedKey() {
        int freeKey = -lastRemovedKey - 1;
        lastRemovedKey = valueIndexesByKey[freeKey];
        return freeKey;
    }

    public byte @ImmutableByteArray [] get(long key) {
        if (isFree((int) key) || isRemoved((int) key)) {
            return null;
        }
        else {
            int valueIndex = valueIndexesByKey[(int) key];
            return values[valueIndex];
        }
    }

    public void free(long key) {
        if (!(isFree((int) key) || isRemoved((int) key))) {
            usages[(int) key]--;
            if (usages[(int) key] <= 0) {
                int valueIndex = valueIndexesByKey[(int) key];
                values[valueIndex] = REMOVED_VALUE;
                valueIndexesByKey[(int) key] = lastRemovedKey;
                lastRemovedKey = (((int) key) * (-1)) - 1;
                size--;
            }
        }
    }

    public int size() {
        return size;
    }


    private void createInternalArrays(int capacity) {
        valueIndexesByKey = new int[capacity];
        keysByValueIndex = new int[capacity];
        values = new byte[capacity][];
        usages = new int[capacity];
        Arrays.fill(valueIndexesByKey, FREE_KEY);
        Arrays.fill(values, null);
        Arrays.fill(usages, 0);
    }

    private void ensureCapacity(int desiredCapacity) {
        int currentCapacity = valueIndexesByKey.length;
        if (desiredCapacity <= 0) {
            throw new IllegalStateException("Bad desired capacity, check overflow. Desired: " + desiredCapacity + ", current:" + currentCapacity);
        }

        if (currentCapacity >= desiredCapacity) {
            return;
        }

        int newCapacity = increasedCapacity(currentCapacity);
        probingStep = probingStepFor(newCapacity);
        newCapacity = correctCapacityToAvoidLoopingOverSameSlots(probingStep, newCapacity);

        int[] oldValueIndexesByKey = valueIndexesByKey;
        byte[][] oldValues = values;
        int[] oldUsages = usages;

        createInternalArrays(newCapacity);

        for (int key = 0; key < oldValueIndexesByKey.length; key++) {
            int valueIndex = oldValueIndexesByKey[key];
            if (valueIndex >= 0) { // free or removed
                byte[] value = oldValues[valueIndex];

                int newValueIndex = findSlotFor(value, calculateHashCode(value));

                valueIndexesByKey[key] = newValueIndex;
                keysByValueIndex[newValueIndex] = key;
                values[newValueIndex] = value;
                usages[key] = oldUsages[key];
            }
        }
    }

    private int findSlotFor(byte[] value, int valueHashCode) { // TODO: should have separate version of this for rehash without removed and full && equals stuff
        int startingIndex = valueHashCode % values.length;

        int i = startingIndex;
        do {
            if (values[i] == null || values[i] == REMOVED_VALUE) {
                return i;
            }
            if (values[i] != null && values[i] != REMOVED_VALUE && Arrays.equals(value, values[i])) {
                return -i - 1; // encode existing value as negative number, -1 is necessary because 0 is valid index
            }
            i = incrementIndex(i);
        }
        while (i != startingIndex);

        throw new IllegalStateException("linear probing should always succeed given enough capacity!");
    }

    private int incrementIndex(int index) {
        index += probingStep;
        return index >= valueIndexesByKey.length ? index - valueIndexesByKey.length : index;
    }

    private int getAndIncrementUniquifier() {
        if (keyGenerator >= Integer.MAX_VALUE - 1) {
            throw new IllegalArgumentException("Too many objects submitted for pooling - cannot guarantee operation");
        }
        return keyGenerator++;
    }

    private boolean isFree(int index) {
        return valueIndexesByKey[index] == FREE_KEY;
    }

    private boolean isRemoved(int index) {
        return valueIndexesByKey[index] < 0 && valueIndexesByKey[index] != FREE_KEY;
    }

    private static int calculateHashCode(byte[] value) {
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
        if (currentCapacity > Integer.MAX_VALUE - 1 - CAPACITY_INCREMENT) {
            return Integer.MAX_VALUE - 1;
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
