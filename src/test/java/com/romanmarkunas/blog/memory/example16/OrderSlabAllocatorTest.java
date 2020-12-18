package com.romanmarkunas.blog.memory.example16;

import com.romanmarkunas.blog.memory.example15.PooledByteArrayMap;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderSlabAllocatorTest {

    private final OrderSlabAllocator allocatorUnderTest
            = new OrderSlabAllocator(new PooledByteArrayMap(1_000));


    @Test
    void shouldWriteAndReadSymmetrically() {
        // when
        int key = allocatorUnderTest.allocate();
        OrderView writeView = allocatorUnderTest.get(key);
        setValuesFrom(TestOrder.ORDER_1, writeView);

        // then
        OrderView readView = allocatorUnderTest.get(key);
        assertValues(TestOrder.ORDER_1, readView);
    }

    @Test
    void shouldWriteAndReadSymmetricallyInMultipleSlabs() {
        // given
        for (int i = 0; i < 2 * OrderSlabAllocator.MAX_OBJECTS_IN_SINGLE_SLAB; i++) {
            allocatorUnderTest.allocate();
        }

        // when
        int key = allocatorUnderTest.allocate();
        OrderView writeView = allocatorUnderTest.get(key);
        setValuesFrom(TestOrder.ORDER_2, writeView);

        // then
        OrderView readView = allocatorUnderTest.get(key);
        assertValues(TestOrder.ORDER_2, readView);
    }

    @Test
    void newlyAllocatedOrdersShouldNotOverwritePreviousOne() {
        // given
        int key1 = allocatorUnderTest.allocate();
        setValuesFrom(TestOrder.ORDER_1, allocatorUnderTest.get(key1));

        // when
        int key2 = allocatorUnderTest.allocate();
        setValuesFrom(TestOrder.ORDER_2, allocatorUnderTest.get(key2));

        // then
        assertThat(key2).isNotEqualTo(key1);
        OrderView readView = allocatorUnderTest.get(key1);
        assertValues(TestOrder.ORDER_1, readView);
    }

    @Test
    void shouldFreeObjectIntoNewFirstFreeSpaceAndUseFreedSlotInNextAllocation() {
        // given 12____...
        int key1 = allocatorUnderTest.allocate();
        int key2 = allocatorUnderTest.allocate();
        assertThat(key2).isNotEqualTo(key1);

        // when _2____...
        allocatorUnderTest.free(key1);

        // then 324___...
        int key3 = allocatorUnderTest.allocate();
        int key4 = allocatorUnderTest.allocate();
        assertThat(key3).isEqualTo(key1);
        assertThat(Set.of(key3, key2)).doesNotContain(key4);
    }

    @Test
    void shouldFreeObjectInFrontOfFirstFreeSpaceAndUseFreedSlotInNextAllocation() {
        // given 1_3___...
        int key1 = allocatorUnderTest.allocate();
        int key2 = allocatorUnderTest.allocate();
        int key3 = allocatorUnderTest.allocate();
        allocatorUnderTest.free(key2);

        // when __3___...
        allocatorUnderTest.free(key1);

        // then 4536__...
        int key4 = allocatorUnderTest.allocate();
        int key5 = allocatorUnderTest.allocate();
        int key6 = allocatorUnderTest.allocate();
        assertThat(key4).isEqualTo(key1);
        assertThat(key5).isEqualTo(key2);
        assertThat(Set.of(key4, key5, key3)).doesNotContain(key6);
    }

    @Test
    void shouldFreeObjectJustBehindFirstFreeSpaceAndUseFreedSlotInSubsequentAllocation() {
        // given 1_34__...
        int key1 = allocatorUnderTest.allocate();
        int key2 = allocatorUnderTest.allocate();
        int key3 = allocatorUnderTest.allocate();
        int key4 = allocatorUnderTest.allocate();
        allocatorUnderTest.free(key2);

        // when 1__4__...
        allocatorUnderTest.free(key3);

        // then 15647_...
        int key5 = allocatorUnderTest.allocate();
        int key6 = allocatorUnderTest.allocate();
        int key7 = allocatorUnderTest.allocate();
        assertThat(key5).isEqualTo(key2);
        assertThat(key6).isEqualTo(key3);
        assertThat(Set.of(key1, key5, key6, key4)).doesNotContain(key7);
    }

    @Test
    void shouldFreeObjectBehindFirstFreeSpaceAndUseFreedSlotInSubsequentAllocation() {
        // given 1_345__...
        int key1 = allocatorUnderTest.allocate();
        int key2 = allocatorUnderTest.allocate();
        int key3 = allocatorUnderTest.allocate();
        int key4 = allocatorUnderTest.allocate();
        int key5 = allocatorUnderTest.allocate();
        allocatorUnderTest.free(key2);

        // when 1_3_5__...
        allocatorUnderTest.free(key4);

        // then 163758_...
        int key6 = allocatorUnderTest.allocate();
        int key7 = allocatorUnderTest.allocate();
        int key8 = allocatorUnderTest.allocate();
        assertThat(key6).isEqualTo(key2);
        assertThat(key7).isEqualTo(key4);
        assertThat(Set.of(key1, key6, key3, key7, key5)).doesNotContain(key8);
    }

    @Test
    void shouldFreeObjectBetweenSubsequentFreeSpacesAndUseFreedSlotInSubsequentAllocation() {
        // given 12_4_678_A__...
        int key1 = allocatorUnderTest.allocate();
        int key2 = allocatorUnderTest.allocate();
        int key3 = allocatorUnderTest.allocate();
        int key4 = allocatorUnderTest.allocate();
        int key5 = allocatorUnderTest.allocate();
        int key6 = allocatorUnderTest.allocate();
        int key7 = allocatorUnderTest.allocate();
        int key8 = allocatorUnderTest.allocate();
        int key9 = allocatorUnderTest.allocate();
        int keyA = allocatorUnderTest.allocate();
        allocatorUnderTest.free(key3);
        allocatorUnderTest.free(key5);
        allocatorUnderTest.free(key9);

        // when 12_4_6_8_A__...
        allocatorUnderTest.free(key7);

        // then 12B4C6D8EAF_...
        int keyB = allocatorUnderTest.allocate();
        int keyC = allocatorUnderTest.allocate();
        int keyD = allocatorUnderTest.allocate();
        int keyE = allocatorUnderTest.allocate();
        int keyF = allocatorUnderTest.allocate();
        assertThat(keyB).isEqualTo(key3);
        assertThat(keyC).isEqualTo(key5);
        assertThat(keyD).isEqualTo(key7);
        assertThat(keyE).isEqualTo(key9);
        assertThat(Set.of(key1, key2, keyB, key4, keyC, key6, keyD, key8, keyE, keyA)).doesNotContain(keyF);
    }

    @Test
    void shouldFreeObjectInFrontOfSubsequentFreeSpaceAndUseFreedSlotInSubsequentAllocation() {
        // given 12_45_7__...
        int key1 = allocatorUnderTest.allocate();
        int key2 = allocatorUnderTest.allocate();
        int key3 = allocatorUnderTest.allocate();
        int key4 = allocatorUnderTest.allocate();
        int key5 = allocatorUnderTest.allocate();
        int key6 = allocatorUnderTest.allocate();
        int key7 = allocatorUnderTest.allocate();
        allocatorUnderTest.free(key3);
        allocatorUnderTest.free(key6);

        // when 12_4__7__...
        allocatorUnderTest.free(key5);

        // then 12849A7B_...
        int key8 = allocatorUnderTest.allocate();
        int key9 = allocatorUnderTest.allocate();
        int keyA = allocatorUnderTest.allocate();
        int keyB = allocatorUnderTest.allocate();
        assertThat(key8).isEqualTo(key3);
        assertThat(key9).isEqualTo(key5);
        assertThat(keyA).isEqualTo(key6);
        assertThat(Set.of(key1, key2, key8, key4, key9, keyA, key7)).doesNotContain(keyB);
    }

    @Test
    void shouldFreeObjectJustBehindSubsequentFreeSpaceAndUseFreedSlotInSubsequentAllocation() {
        // given 12_4_67__...
        int key1 = allocatorUnderTest.allocate();
        int key2 = allocatorUnderTest.allocate();
        int key3 = allocatorUnderTest.allocate();
        int key4 = allocatorUnderTest.allocate();
        int key5 = allocatorUnderTest.allocate();
        int key6 = allocatorUnderTest.allocate();
        int key7 = allocatorUnderTest.allocate();
        allocatorUnderTest.free(key3);
        allocatorUnderTest.free(key5);

        // when 12_4__7__...
        allocatorUnderTest.free(key6);

        // then 12849A7B_...
        int key8 = allocatorUnderTest.allocate();
        int key9 = allocatorUnderTest.allocate();
        int keyA = allocatorUnderTest.allocate();
        int keyB = allocatorUnderTest.allocate();
        assertThat(key8).isEqualTo(key3);
        assertThat(key9).isEqualTo(key5);
        assertThat(keyA).isEqualTo(key6);
        assertThat(Set.of(key1, key2, key8, key4, key9, keyA, key7)).doesNotContain(keyB);
    }

    @Test
    void shouldBeAbleToFreeVeryLastSlotInSlabAndReuseItInNextAllocation() {
        // given ...XYZ
        for (int i = 0; i < OrderSlabAllocator.MAX_OBJECTS_IN_SINGLE_SLAB - 1; i++) {
            allocatorUnderTest.allocate();
        }
        int keyZ = allocatorUnderTest.allocate();

        // when ...XY_
        allocatorUnderTest.free(keyZ);

        // then ...XY1
        int key1 = allocatorUnderTest.allocate();
        assertThat(key1).isEqualTo(keyZ);
    }

    @Test
    void shouldFailFastWhenFreeingAlreadyFreeMemoryForSingleObjectSlot() {
        // given 1_3__...
        allocatorUnderTest.allocate();
        int key2 = allocatorUnderTest.allocate();
        allocatorUnderTest.allocate();
        allocatorUnderTest.free(key2);

        assertThatThrownBy(() -> allocatorUnderTest.free(key2)).isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldFailFastWhenFreeingAlreadyFreeMemory() {
        // given 1___5__...
        allocatorUnderTest.allocate();
        int key2 = allocatorUnderTest.allocate();
        int key3 = allocatorUnderTest.allocate();
        int key4 = allocatorUnderTest.allocate();
        allocatorUnderTest.allocate();
        allocatorUnderTest.free(key2);
        allocatorUnderTest.free(key4);
        allocatorUnderTest.free(key3);

        assertThatThrownBy(() -> allocatorUnderTest.free(key2)).isExactlyInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> allocatorUnderTest.free(key3)).isExactlyInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> allocatorUnderTest.free(key4)).isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldFailFastWhenFreeingAlreadyFreeLastSlot() {
        // given ...XY_
        for (int i = 0; i < OrderSlabAllocator.MAX_OBJECTS_IN_SINGLE_SLAB - 1; i++) {
            allocatorUnderTest.allocate();
        }
        int keyZ = allocatorUnderTest.allocate();
        allocatorUnderTest.free(keyZ);

        assertThatThrownBy(() -> allocatorUnderTest.free(keyZ)).isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldFailFastWhenFreeingAlreadyFreeInSubsequentSlab() {
        // given
        for (int i = 0; i < 2 * OrderSlabAllocator.MAX_OBJECTS_IN_SINGLE_SLAB - 1; i++) {
            allocatorUnderTest.allocate();
        }
        int key = allocatorUnderTest.allocate();
        allocatorUnderTest.free(key);
        assertThat(key).isGreaterThan(OrderSlabAllocator.MAX_OBJECTS_IN_SINGLE_SLAB);

        assertThatThrownBy(() -> allocatorUnderTest.free(key)).isExactlyInstanceOf(IllegalStateException.class);
    }


    private void setValuesFrom(TestOrder order, OrderView view) {
        view.set(
                order.id,
                order.user,
                order.articleNr,
                order.count,
                order.pricePence,
                order.addressNumber,
                order.addressStreet,
                order.addressCity,
                order.addressRegion,
                order.addressPostCode
        );
    }

    private void assertValues(TestOrder order, OrderView view) {
        assertThat(view.getId()).isEqualTo(order.id);
        assertThatUserIsEqual(view, order.user);
        assertThat(view.getArticleNr()).isEqualTo(order.articleNr);
        assertThat(view.getCount()).isEqualTo(order.count);
        assertThat(view.getPricePence()).isEqualTo(order.pricePence);
        assertThat(view.getAddressNumber()).isEqualTo(order.addressNumber);
        assertThat(view.getAddressStreet()).isEqualTo(order.addressStreet);
        assertThat(view.getAddressCity()).isEqualTo(order.addressCity);
        assertThat(view.getAddressRegion()).isEqualTo(order.addressRegion);
        assertThat(view.getAddressPostCode()).isEqualTo(order.addressPostCode);
    }

    @SuppressWarnings("byte.array.weakening")
    private void assertThatUserIsEqual(OrderView readView, byte[] user) {
        assertThat(readView.getUser()).isEqualTo(user);
    }

    private enum TestOrder {
        ORDER_1(42, new byte[] {0}, Integer.MIN_VALUE, 0, Integer.MAX_VALUE, "1", "Fishery Road", "Seashoreworth", "", "SBSP42"),
        ORDER_2(43, new byte[] {-2}, 1, 0, -1, "", "Smoke House", "Seashoreworth", "Fish'n'Chips County", "SBSP43");

        long id;
        byte[] user;
        int articleNr;
        int count;
        int pricePence;
        String addressNumber;
        String addressStreet;
        String addressCity;
        String addressRegion;
        String addressPostCode;

        TestOrder(
                int id,
                byte[] user,
                int articleNr,
                int count,
                int pricePence,
                String addressNumber,
                String addressStreet,
                String addressCity,
                String addressRegion,
                String addressPostCode
        ) {
            this.id = id;
            this.user = user;
            this.articleNr = articleNr;
            this.count = count;
            this.pricePence = pricePence;
            this.addressNumber = addressNumber;
            this.addressStreet = addressStreet;
            this.addressCity = addressCity;
            this.addressRegion = addressRegion;
            this.addressPostCode = addressPostCode;
        }
    }
}