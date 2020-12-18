package com.romanmarkunas.blog.memory.example16;

import com.romanmarkunas.blog.memory.example15.PooledByteArrayMap;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Similar to Linux SLOB implementation
 */
public class OrderSlabAllocator {

    static final int MAX_OBJECTS_IN_SINGLE_SLAB = Slab.MAX_OBJECTS_IN_SLAB;

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

        private static final int SLAB_SIZE_BYTES = 16 * 1024;
        private static final int OBJECT_SIZE_BYTES = OrderView.TOTAL_SIZE;
        private static final int MAX_OBJECTS_IN_SLAB = SLAB_SIZE_BYTES / OBJECT_SIZE_BYTES;
        private static final int LEFT_OUTSIDE_SLAB = -1;
        private static final int RIGHT_OUTSIDE_SLAB = MAX_OBJECTS_IN_SLAB;

        private final OrderView view; // TODO: pass in view to not store it on each slab
        private final ByteBuffer buffer;

        private int firstFreeSpacePosition;


        private Slab(OrderView view) {
            this.view = view;
            buffer = ByteBuffer.allocate(SLAB_SIZE_BYTES);
            firstFreeSpacePosition = 0;
            setSizeOfFreeSpace(firstFreeSpacePosition, MAX_OBJECTS_IN_SLAB);
            setNextFreeSpacePosition(firstFreeSpacePosition, RIGHT_OUTSIDE_SLAB);
        }


        public OrderView get(int index) {
            return view.wrap(buffer, byteOffsetOf(index));
        }

        public int putIntoFirstFree() {
            int allocatedPosition = this.firstFreeSpacePosition;

            int freeSpaceSize = geSizeOfFreeSpaceInObjects(firstFreeSpacePosition);
            int nextFreeSpacePosition = getNextFreeSpacePosition(firstFreeSpacePosition);

            if (freeSpaceSize == 1) {
                this.firstFreeSpacePosition = nextFreeSpacePosition;
            }
            else {
                this.firstFreeSpacePosition++;
                setSizeOfFreeSpace(firstFreeSpacePosition, freeSpaceSize - 1);
                setNextFreeSpacePosition(firstFreeSpacePosition, nextFreeSpacePosition);
            }

            return allocatedPosition;
        }

        public void free(int index) {
            // find between which pair of free spaces new free slot will appear
            // for full slab, firstFreeSpacePosition == RIGHT_OUTSIDE_SLAB
            int left = LEFT_OUTSIDE_SLAB;
            int right = firstFreeSpacePosition;

            while (!(index > left && index < right)) {
                if (index == right || (right + geSizeOfFreeSpaceInObjects(right) > index)) {
                    throw new IllegalStateException("Freeing again @ index [" + index + "]");
                }
                left = right;
                right = getNextFreeSpacePosition(right);
            }

            // for simplicity do not handle merging free spaces into one, which is &&'ed 2 clauses below
            if (index == right - 1) { // freeing prepends to right free space
                if (left != LEFT_OUTSIDE_SLAB) {
                    setNextFreeSpacePosition(left, index);
                }
                setSizeOfFreeSpace(index, geSizeOfFreeSpaceInObjects(right) + 1);
                setNextFreeSpacePosition(index, getNextFreeSpacePosition(right));
            }
            else if (left != LEFT_OUTSIDE_SLAB && index == left + geSizeOfFreeSpaceInObjects(left)) { // freeing appends to left free space
                setSizeOfFreeSpace(left, geSizeOfFreeSpaceInObjects(left) + 1);
            }
            else { // freeing is somewhere between left and right without touching them
                if (left != LEFT_OUTSIDE_SLAB) {
                    setNextFreeSpacePosition(left, index);
                }
                setSizeOfFreeSpace(index, 1);
                setNextFreeSpacePosition(index, right);
            }

            // set first free space to leftmost free space
            if (index < firstFreeSpacePosition) {
                firstFreeSpacePosition = index;
            }
        }

        public boolean hasSpace() {
            return firstFreeSpacePosition < MAX_OBJECTS_IN_SLAB;
        }


        private int byteOffsetOf(int position) {
            return position * OBJECT_SIZE_BYTES;
        }

        private int geSizeOfFreeSpaceInObjects(int positionOfFreeSpace) {
            return buffer.getInt(byteOffsetOf(positionOfFreeSpace));
        }

        private void setSizeOfFreeSpace(int positionOfFreeSpace, int sizeOfFreeSpaceInObjects) {
            buffer.putInt(byteOffsetOf(positionOfFreeSpace), sizeOfFreeSpaceInObjects);
        }

        private int getNextFreeSpacePosition(int positionOfFreeSpace) {
            return buffer.getInt(byteOffsetOf(positionOfFreeSpace) + Integer.BYTES);
        }

        private void setNextFreeSpacePosition(int positionOfFreeSpace, int nextFreeSpacePosition) {
            buffer.putInt(byteOffsetOf(positionOfFreeSpace) + Integer.BYTES, nextFreeSpacePosition);
        }
    }
}
