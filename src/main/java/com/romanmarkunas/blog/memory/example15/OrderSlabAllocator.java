package com.romanmarkunas.blog.memory.example15;

import com.romanmarkunas.blog.memory.example14.PooledByteArrayMap;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Similar to Linux SLOB implementation
 */
public class OrderSlabAllocator {

    private final List<Slab> slabs = new ArrayList<>();
    private final OrderView view;


    public OrderSlabAllocator(PooledByteArrayMap pool) {
        view = new OrderView(pool);
    }


    public int allocate() {
        int vacantSlabIndex = findOrCreateVacantSlab();
        int indexWithinSlab = slabs.get(vacantSlabIndex).putIntoFirstFree();
        return vacantSlabIndex * Slab.MAX_OBJECTS_IN_SLAB + indexWithinSlab;
    }

    public OrderView get(int key) {
        return slabs.get(slabIndex(key)).get(indexWithinSlab(key));
    }

    public void free(int key) {
        slabs.get(slabIndex(key)).free(indexWithinSlab(key));
    }


    private int slabIndex(int key) {
        return key / Slab.MAX_OBJECTS_IN_SLAB;
    }

    private int indexWithinSlab(int key) {
        return key % Slab.MAX_OBJECTS_IN_SLAB;
    }

    private int findOrCreateVacantSlab() {
        for (int i = 0, size = slabs.size(); i < size; i++) {
            Slab slab = slabs.get(i);
            if (slab.hasSpace()) {
                return i;
            }
        }

        slabs.add(new Slab(view));

        return slabs.size() - 1;
    }


    private static class Slab {

        private static final int SLAB_SIZE_BYTES = 1024 * 1024;
        private static final int OBJECT_SIZE_BYTES = OrderView.TOTAL_SIZE;
        private static final int MAX_OBJECTS_IN_SLAB = SLAB_SIZE_BYTES / OBJECT_SIZE_BYTES;

        private final OrderView view;
        private final ByteBuffer buffer;

        private int firstFreeSpacePosition;


        private Slab(OrderView view) {
            this.view = view;
            buffer = ByteBuffer.allocate(SLAB_SIZE_BYTES);
            firstFreeSpacePosition = 0;
            putFreeListInfo(firstFreeSpacePosition, MAX_OBJECTS_IN_SLAB, MAX_OBJECTS_IN_SLAB);
        }


        public OrderView get(int index) {
            return view.wrap(buffer, byteOffsetOf(index));
        }

        public int putIntoFirstFree() {
            int allocatedPosition = this.firstFreeSpacePosition;

            int freeSpaceSize = getFreeSpaceSizeInObjects(firstFreeSpacePosition);
            int nextFreeSpacePosition = getNextFreeSpacePosition(firstFreeSpacePosition);

            if (freeSpaceSize == 1) {
                this.firstFreeSpacePosition = nextFreeSpacePosition;
            }
            else {
                this.firstFreeSpacePosition++;
                putFreeListInfo(firstFreeSpacePosition, freeSpaceSize - 1, nextFreeSpacePosition);
            }

            return allocatedPosition;
        }

        public void free(int index) {
            if (index < firstFreeSpacePosition - 1) { // freeing creates new first free space
                int previousFirstFreePosition = this.firstFreeSpacePosition;
                this.firstFreeSpacePosition = index;
                putFreeListInfo(index, 1, previousFirstFreePosition);
                return;
            }

            int freeSpaceSize = getFreeSpaceSizeInObjects(firstFreeSpacePosition);
            int nextFreeSpacePosition = getNextFreeSpacePosition(firstFreeSpacePosition);

            if (index == firstFreeSpacePosition - 1) { // freeing prepends to existing first free space
                this.firstFreeSpacePosition--;
                putFreeListInfo(firstFreeSpacePosition, freeSpaceSize + 1, nextFreeSpacePosition);
                return;
            }

            if (index == firstFreeSpacePosition + freeSpaceSize) { // freeing appends to existing first free space
                putFreeListInfo(firstFreeSpacePosition, freeSpaceSize + 1, nextFreeSpacePosition);
                return;
            }

            // freeing happens after first free space
            int left = firstFreeSpacePosition;
            int right = nextFreeSpacePosition;

            while (!(index > left && index < right)) {
                left = right;
                right = getNextFreeSpacePosition(right);
            }

            putFreeListInfo(left, getFreeSpaceSizeInObjects(left), index);
            putFreeListInfo(index, 1, right);
        }

        public boolean hasSpace() {
            return firstFreeSpacePosition < MAX_OBJECTS_IN_SLAB;
        }


        private int byteOffsetOf(int position) {
            return position * OBJECT_SIZE_BYTES;
        }

        private void putFreeListInfo(
                int positionOfFreeSpace,
                int sizeOfFreeSpaceInObjects,
                int nextFreeSpacePosition
        ) {
            buffer.putInt(byteOffsetOf(positionOfFreeSpace), sizeOfFreeSpaceInObjects);
            buffer.putInt(byteOffsetOf(positionOfFreeSpace) + Integer.BYTES, nextFreeSpacePosition);
        }

        private int getFreeSpaceSizeInObjects(int positionOfFreeSpace) {
            return buffer.getInt(byteOffsetOf(positionOfFreeSpace));
        }

        private int getNextFreeSpacePosition(int positionOfFreeSpace) {
            return buffer.getInt(byteOffsetOf(positionOfFreeSpace) + Integer.BYTES);
        }
    }
}
