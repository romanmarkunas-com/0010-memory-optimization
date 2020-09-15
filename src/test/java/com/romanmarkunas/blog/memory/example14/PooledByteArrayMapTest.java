package com.romanmarkunas.blog.memory.example14;

import com.sun.management.ThreadMXBean;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class PooledByteArrayMapTest {

    private static final int INITIAL_CAPACITY = 8;
    private final PooledByteArrayMap poolUnderTest = new PooledByteArrayMap(INITIAL_CAPACITY);


    @Test
    void getOnNewMapReturnsNull() {
        assertThat(poolUnderTest.get(1)).isNull();
        assertThat(poolUnderTest.size()).isEqualTo(0);
    }

    @Test
    void getOnEmptyMapReturnsNull() {
        long key = poolUnderTest.put(new byte[] {1, 1, 1});
        poolUnderTest.free(key);

        assertThat(poolUnderTest.get(key)).isNull();
        assertThat(poolUnderTest.size()).isEqualTo(0);
    }

    @Test
    void getOnFullMapReturnsNull() {
        PooledByteArrayMap poolUnderTest = new PooledByteArrayMap(1);
        long key = poolUnderTest.put(new byte[] {1, 1, 1});

        assertThat(key).isNotEqualTo(1);
        assertThat(poolUnderTest.get(1)).isNull();
    }


    @Test
    void shouldPutValue() {
        long key = poolUnderTest.put(new byte[] {1, 1, 1});

        assertThat(poolUnderTest.get(key)).isEqualTo(new byte[] {1, 1, 1});
        assertThat(poolUnderTest.size()).isEqualTo(1);
    }

    @Test
    void putSameValueReturnsSameKey() {
        long key1 = poolUnderTest.put(new byte[] {1, 1, 1});
        long key2 = poolUnderTest.put(new byte[] {1, 1, 1});

        assertThat(key1).isEqualTo(key2);
        assertThat(poolUnderTest.size()).isEqualTo(1);
    }

    @Test
    void putDifferentValueWithSameHashCodeReturnsDifferentKey() {
        byte[] arr1 = {1, 0}; // 31* (31 + 1) + 0
        byte[] arr2 = {0, 31}; // 31* (31 + 0) + 31
        assertThat(Arrays.hashCode(arr1)).isEqualTo(Arrays.hashCode(arr2));

        long key1 = poolUnderTest.put(arr1);
        long key2 = poolUnderTest.put(arr2);

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

        ThreadMXBean threadMXBean = givenConfiguredOracleThreadMXBean();
        long allocatedDuringQueryForAllocation = getAllocatedDuringQueryForAllocation(threadMXBean);
        long initialThreadMemory = threadMXBean.getThreadAllocatedBytes(Thread.currentThread().getId());

        // when
        for (int i = 0; i < values.length; i++) {
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
        byte[][] values = new byte[INITIAL_CAPACITY][];
        for (int i = 0; i < values.length; i++) {
            values[i] = new byte[] {(byte) i};
        }
        for (int i = 0; i < values.length; i++) {
            poolUnderTest.put(values[i]);
        }

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
}