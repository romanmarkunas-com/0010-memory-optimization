package com.romanmarkunas.blog.memory.example15;

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
    private PagedArrays arrays;
    private int size = 0; // max size should be MAX - 2
    private int probingStep;
    private int lastRemovedKey = NO_REMOVED_KEY;


    public PooledByteArrayMap(int initialCapacity) {
        probingStep = probingStepFor(initialCapacity);
        int capacity = correctCapacityToAvoidLoopingOverSameSlots(probingStep, initialCapacity);
        createInternalArrays(capacity);
    }


    public int put(byte[] value) {
        if (value == FREE_VALUE) {
            throw new IllegalArgumentException("Cannot put null into pool");
        }

        int valueHashCode = calculateHashCode(value);

        ensureCapacity(size + 1);

        int index = findSlotFor(value, valueHashCode);

        if (index < 0) {
            int indexOfExistingItem = -(index + 1);
            int key = arrays.getKeyByValueIndex(indexOfExistingItem);
            if (arrays.getUsagesByKey(key) >= Integer.MAX_VALUE) {
                throw new IllegalStateException("Too many reuses of same byte[] - " + Arrays.toString(value));
            }
            arrays.incrementUsagesByKey(key);
            return key;
        }
        else {
            int key = lastRemovedKey == NO_REMOVED_KEY ? getAndIncrementUniquifier() : getLastRemovedKey();

            arrays.setValueIndexByKey(key, index);
            arrays.setKeyByValueIndex(index, key);
            arrays.setValueByValueIndex(index, value);
            arrays.setUsagesByKey(key, 1);
            size++;
            return key;
        }
    }

    private int getLastRemovedKey() {
        int freeKey = -lastRemovedKey - 1;
        lastRemovedKey = arrays.getValueIndexByKey(freeKey);
        return freeKey;
    }

    public byte @ImmutableByteArray [] get(int key) {
        if (isFree(key) || isRemoved(key)) {
            return null;
        }
        else {
            int valueIndex = arrays.getValueIndexByKey(key);
            return arrays.getValueByValueIndex(valueIndex);
        }
    }

    public void free(int key) {
        if (!(isFree(key) || isRemoved(key))) {
            arrays.decrementUsagesByKey(key);
            if (arrays.getUsagesByKey(key) <= 0) {
                int valueIndex = arrays.getValueIndexByKey(key);
                arrays.setValueByValueIndex(valueIndex, REMOVED_VALUE);
                arrays.setValueIndexByKey(key, lastRemovedKey);
                lastRemovedKey = -key - 1;
                size--;
            }
        }
    }

    public int size() {
        return size;
    }


    private void createInternalArrays(int capacity) {
        arrays = new PagedArrays(capacity);
    }

    private void ensureCapacity(int desiredCapacity) {
        int currentCapacity = arrays.capacity;
        if (desiredCapacity <= 0) {
            throw new IllegalStateException("Bad desired capacity, check overflow. Desired: " + desiredCapacity + ", current:" + currentCapacity);
        }

        if (currentCapacity >= desiredCapacity) {
            return;
        }

        int newCapacity = increasedCapacity(currentCapacity);
        probingStep = probingStepFor(newCapacity);
        newCapacity = correctCapacityToAvoidLoopingOverSameSlots(probingStep, newCapacity);

        PagedArrays oldArrays = arrays;

        createInternalArrays(newCapacity);

        for (int key = 0; key < oldArrays.capacity; key++) {
            int valueIndex = oldArrays.getValueIndexByKey(key);
            if (valueIndex >= 0) { // not free or removed
                byte[] value = oldArrays.getValueByValueIndex(valueIndex);

                int newValueIndex = findSlotFor(value, calculateHashCode(value));

                arrays.setValueIndexByKey(key, newValueIndex);
                arrays.setKeyByValueIndex(newValueIndex, key);
                arrays.setValueByValueIndex(newValueIndex, value);
                arrays.setUsagesByKey(key, oldArrays.getUsagesByKey(key));
            }
        }
    }

    private int findSlotFor(byte[] value, int valueHashCode) { // TODO: should have separate version of this for rehash without removed and full && equals stuff
        int startingIndex = valueHashCode % arrays.capacity;

        int i = startingIndex;
        do {
            byte[] existingValue = arrays.getValueByValueIndex(i);
            if (existingValue == null || existingValue == REMOVED_VALUE) {
                return i;
            }
            if (Arrays.equals(existingValue, value)) {
                return -i - 1; // encode existing value as negative number, -1 is necessary because 0 is valid index
            }
            i = incrementIndex(i);
        }
        while (i != startingIndex);

        throw new IllegalStateException("linear probing should always succeed given enough capacity!");
    }

    private int incrementIndex(int index) {
        index += probingStep;
        return index >= arrays.capacity ? index - arrays.capacity : index;
    }

    private int getAndIncrementUniquifier() {
        if (keyGenerator >= Integer.MAX_VALUE - 1) {
            throw new IllegalArgumentException("Too many objects submitted for pooling - cannot guarantee operation");
        }
        return keyGenerator++;
    }

    private boolean isFree(int index) {
        return arrays.getValueIndexByKey(index) == FREE_KEY;
    }

    private boolean isRemoved(int index) {
        return arrays.getValueIndexByKey(index) < 0 && arrays.getValueIndexByKey(index) != FREE_KEY;
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

    private static final class PagedArrays {

        private static final int PAGE_SIZE = 8192;
        private final int[] pageSizes;
        private final Page[] pages;
        private final int capacity;


        PagedArrays(int capacity) {
            this.capacity = capacity;
            int fullPages = capacity / PAGE_SIZE;
            int partialPageSize = capacity % PAGE_SIZE;
            int partialPages = partialPageSize == 0 ? 0 : 1;

            pages = new Page[fullPages + partialPages];
            pageSizes = new int[fullPages + partialPages];

            for (int i = 0; i < fullPages; i++) {
                pageSizes[i] = PAGE_SIZE;
            }
            if (partialPages == 1) {
                pageSizes[fullPages] = partialPageSize;
            }
        }


        int getValueIndexByKey(int key) {
            return pageForIndex(key).valueIndexesByKey[indexWithinPage(key)];
        }

        public void setValueIndexByKey(int key, int valueIndex) {
            pageForIndex(key).valueIndexesByKey[indexWithinPage(key)] = valueIndex;
        }

        int getKeyByValueIndex(int valueIndex) {
            return pageForIndex(valueIndex).keysByValueIndex[indexWithinPage(valueIndex)];
        }

        public void setKeyByValueIndex(int valueIndex, int key) {
            pageForIndex(valueIndex).keysByValueIndex[indexWithinPage(valueIndex)] = key;
        }

        byte[] getValueByValueIndex(int valueIndex) {
            return pageForIndex(valueIndex).values[indexWithinPage(valueIndex)];
        }

        public void setValueByValueIndex(int valueIndex, byte[] value) {
            pageForIndex(valueIndex).values[indexWithinPage(valueIndex)] = value;
        }

        public int getUsagesByKey(int key) {
            return pageForIndex(key).usages[indexWithinPage(key)];
        }

        public void setUsagesByKey(int key, int usages) {
            pageForIndex(key).usages[indexWithinPage(key)] = usages;
        }

        public void incrementUsagesByKey(int key) {
            pageForIndex(key).usages[indexWithinPage(key)]++;
        }

        public void decrementUsagesByKey(int key) {
            pageForIndex(key).usages[indexWithinPage(key)]--;
        }


        private Page pageForIndex(int index) {
            int pageIndex = index / PAGE_SIZE;
            if (pages[pageIndex] == null) {
                pages[pageIndex] = new Page(pageSizes[pageIndex]);
            }
            return pages[pageIndex];
        }

        private int indexWithinPage(int index) {
            return index % PAGE_SIZE;
        }


        private static final class Page {
            // index = key, cell =
            //              1) positive until MAX - 2: value index;
            //              2) negative: free or removed;
            //              2a) negative MIN: free init value;
            //              2b) negative not min: negate - 1 gives next free key slot for reuse (negative MIN + 1 means its a last free slot)
            private final int[] valueIndexesByKey;
            // index = value slot, cell = key
            private final int[] keysByValueIndex;
            private final byte[][] values;
            private final int[] usages;

            Page(int capacity) {
                valueIndexesByKey = new int[capacity];
                keysByValueIndex = new int[capacity];
                values = new byte[capacity][];
                usages = new int[capacity];
                Arrays.fill(valueIndexesByKey, FREE_KEY);
            }
        }
    }
}
