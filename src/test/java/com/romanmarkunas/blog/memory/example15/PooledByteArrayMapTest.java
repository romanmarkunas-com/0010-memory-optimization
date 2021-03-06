package com.romanmarkunas.blog.memory.example15;

import com.sun.management.ThreadMXBean;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PooledByteArrayMapTest {

    private static final int INITIAL_CAPACITY = 8;
    private final PooledByteArrayMap poolUnderTest = new PooledByteArrayMap(INITIAL_CAPACITY);


    /**
     * {@link PooledByteArrayMap#get(int)}
     */
    
    @Test
    void getOnNewMapReturnsNull() {
        assertThat(getForAssertion(1)).isNull();
        assertThat(poolUnderTest.size()).isEqualTo(0);
    }

    @Test
    void getOnEmptyMapReturnsNull() {
        int key = poolUnderTest.put(new byte[] {1, 1, 1});
        poolUnderTest.free(key);

        assertThat(getForAssertion(key)).isNull();
        assertThat(poolUnderTest.size()).isEqualTo(0);
    }

    @Test
    void getOnFullMapReturnsNull() {
        PooledByteArrayMap poolUnderTest = new PooledByteArrayMap(1);
        int key = poolUnderTest.put(new byte[] {1, 1, 1});

        assertThat(key).isNotEqualTo(1);
        assertThat(getForAssertion(1)).isNull();
    }


    /**
     * {@link PooledByteArrayMap#put(byte[])}
     */
    
    @Test
    void shouldPutValue() {
        int key = poolUnderTest.put(new byte[] {1, 1, 1});

        assertThat(getForAssertion(key)).isEqualTo(new byte[] {1, 1, 1});
        assertThat(poolUnderTest.size()).isEqualTo(1);
    }

    @Test
    void putSameValueReturnsSameKey() {
        int key1 = poolUnderTest.put(new byte[] {1, 1, 1});
        int key2 = poolUnderTest.put(new byte[] {1, 1, 1});

        assertThat(key1).isEqualTo(key2);
        assertThat(poolUnderTest.size()).isEqualTo(1);
    }

    @Test
    void putDifferentValueWithSameHashCodeReturnsDifferentKey() {
        byte[] arr1 = {1, 0}; // 31* (31 + 1) + 0
        byte[] arr2 = {0, 31}; // 31* (31 + 0) + 31
        assertThat(Arrays.hashCode(arr1)).isEqualTo(Arrays.hashCode(arr2));

        int key1 = poolUnderTest.put(arr1);
        int key2 = poolUnderTest.put(arr2);

        assertThat(key1).isNotEqualTo(key2);
        assertThat(poolUnderTest.size()).isEqualTo(2);
    }

    @Test
    void putOnNotFullMapDoesNotCauseResize() {
        // given
        byte[][] values = new byte[INITIAL_CAPACITY][];
        for (int i = 0; i < values.length; i++) {
            values[i] = new byte[] {(byte) i};
        }

        poolUnderTest.put(values[0]); // force lazy init of array page

        ThreadMXBean threadMXBean = givenConfiguredOracleThreadMXBean();
        long allocatedDuringQueryForAllocation = getAllocatedDuringQueryForAllocation(threadMXBean);
        long initialThreadMemory = threadMXBean.getThreadAllocatedBytes(Thread.currentThread().getId());

        // when
        for (int i = 1; i < values.length; i++) {
            poolUnderTest.put(values[i]);
        }

        // then
        long finalThreadMemory = threadMXBean.getThreadAllocatedBytes(Thread.currentThread().getId());

        assertThat(finalThreadMemory).isEqualTo(initialThreadMemory + allocatedDuringQueryForAllocation);
        assertThat(poolUnderTest.size()).isEqualTo(INITIAL_CAPACITY);
    }

    @Test
    void putOnFullMapCausesResize(){
        // given
        givenFullPool();

        byte[] array = {(byte) INITIAL_CAPACITY + 1};

        ThreadMXBean threadMXBean = givenConfiguredOracleThreadMXBean();
        long allocatedDuringQueryForAllocation = getAllocatedDuringQueryForAllocation(threadMXBean);
        long initialThreadMemory = threadMXBean.getThreadAllocatedBytes(Thread.currentThread().getId());

        // when
        poolUnderTest.put(array);

        // then
        long finalThreadMemory = threadMXBean.getThreadAllocatedBytes(Thread.currentThread().getId());

        assertThat(finalThreadMemory).isGreaterThan(initialThreadMemory + allocatedDuringQueryForAllocation);
        assertThat(poolUnderTest.size()).isEqualTo(INITIAL_CAPACITY + 1);
    }

    @Test
    void putShouldReuseRemovedSlots() {
        // given
        for (int i = 0; i < INITIAL_CAPACITY - 1; i++) {
            poolUnderTest.put(new byte[] {(byte) i});
        }
        int key = poolUnderTest.put(new byte[] {(byte) INITIAL_CAPACITY});
        poolUnderTest.free(key);
        byte[] array = {(byte) INITIAL_CAPACITY + 1};

        ThreadMXBean threadMXBean = givenConfiguredOracleThreadMXBean();
        long allocatedDuringQueryForAllocation = getAllocatedDuringQueryForAllocation(threadMXBean);
        long initialThreadMemory = threadMXBean.getThreadAllocatedBytes(Thread.currentThread().getId());

        // when
        poolUnderTest.put(array);

        // then
        long finalThreadMemory = threadMXBean.getThreadAllocatedBytes(Thread.currentThread().getId());

        assertThat(finalThreadMemory).isEqualTo(initialThreadMemory + allocatedDuringQueryForAllocation);
        assertThat(poolUnderTest.size()).isEqualTo(INITIAL_CAPACITY);
    }

    @Test
    void throwIfSameObjectIsPooledTooManyTimes() {
        byte[] value = {10};
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            poolUnderTest.put(value);
        }

        assertThatThrownBy(() -> poolUnderTest.put(value)).isInstanceOf(IllegalStateException.class);
    }


    /**
     * {@link PooledByteArrayMap#free(int)}
     */

    @Test
    void removeOnNewMapHasNoEffect() {
        poolUnderTest.free(1);

        assertThat(poolUnderTest.size()).isEqualTo(0);
    }

    @Test
    void removeOnEmptyMapHasNoEffect() {
        int key = poolUnderTest.put(new byte[] {2, 3, 4});
        poolUnderTest.free(key);
        assertThat(poolUnderTest.size()).isEqualTo(0);

        poolUnderTest.free(key);

        assertThat(poolUnderTest.size()).isEqualTo(0);
    }

    @Test
    void singleRemoveOfReusedEntryDoesNotRemoveIt() {
        int key1 = poolUnderTest.put(new byte[] {2, 3, 4});
        int key2 = poolUnderTest.put(new byte[] {2, 3, 4});
        assertThat(poolUnderTest.size()).isEqualTo(1);
        assertThat(key1).isEqualTo(key2);

        poolUnderTest.free(key1);

        assertThat(poolUnderTest.size()).isEqualTo(1);
    }

    @Test
    void removeRemovesEntryIfCalledSameNumberOfTimesAsPut() {
        int key1 = poolUnderTest.put(new byte[] {2, 3, 4});
        int key2 = poolUnderTest.put(new byte[] {2, 3, 4});
        assertThat(poolUnderTest.size()).isEqualTo(1);
        assertThat(key1).isEqualTo(key2);

        poolUnderTest.free(key1);
        poolUnderTest.free(key2);

        assertThat(poolUnderTest.size()).isEqualTo(0);
    }

    @Test
    void removeDoesNotRemoveEntriesWIthSameHashCode() {
        // given
        byte[] arr1 = {1, 0}; // 31* (31 + 1) + 0
        byte[] arr2 = {0, 31}; // 31* (31 + 0) + 31
        assertThat(Arrays.hashCode(arr1)).isEqualTo(Arrays.hashCode(arr2));

        int key1 = poolUnderTest.put(arr1);
        int key2 = poolUnderTest.put(arr2);
        assertThat(poolUnderTest.size()).isEqualTo(2);
        assertThat(key1).isNotEqualTo(key2);

        // when
        poolUnderTest.free(key2);

        // then
        assertThat(poolUnderTest.size()).isEqualTo(1);
        assertThat(getForAssertion(key2)).isNull();
        assertThat(getForAssertion(key1)).isNotNull();
    }


    private void givenFullPool() {
        byte[][] values = new byte[INITIAL_CAPACITY][];
        for (int i = 0; i < values.length; i++) {
            values[i] = new byte[] {(byte) i};
        }
        for (int i = 0; i < values.length; i++) {
            poolUnderTest.put(values[i]);
        }
    }

    private ThreadMXBean givenConfiguredOracleThreadMXBean() {
        java.lang.management.ThreadMXBean threadMXBeanJdk = ManagementFactory.getThreadMXBean();
        assertThat(threadMXBeanJdk)
                .withFailMessage("Must have Oracle JDK to use thread MX bean implementation below")
                .isInstanceOf(ThreadMXBean.class);

        ThreadMXBean threadMXBean = (ThreadMXBean) threadMXBeanJdk;
        assertThat(threadMXBean.isThreadAllocatedMemorySupported()).isTrue();
        assertThat(threadMXBean.isThreadAllocatedMemoryEnabled()).isTrue();
        return threadMXBean;
    }

    private long getAllocatedDuringQueryForAllocation(ThreadMXBean threadMXBean) {
        long memory1 = threadMXBean.getThreadAllocatedBytes(Thread.currentThread().getId());
        long memory2 = threadMXBean.getThreadAllocatedBytes(Thread.currentThread().getId());
        return memory2 - memory1;
    }

    @SuppressWarnings("byte.array.weakening")
    private byte [] getForAssertion(int key) {
        return poolUnderTest.get(key);
    }
}