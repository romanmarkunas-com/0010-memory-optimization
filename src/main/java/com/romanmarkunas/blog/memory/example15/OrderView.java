package com.romanmarkunas.blog.memory.example15;

import com.romanmarkunas.blog.memory.example14.PooledByteArrayMap;
import com.romanmarkunas.blog.memory.example14.checkers.ImmutableByteArray;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class OrderView {

    private final static int ID_OFFSET = 0;
    private final static int USER_OFFSET = ID_OFFSET + Long.BYTES;
    private final static int ARTICLENR_OFFSET = USER_OFFSET + Long.BYTES;
    private final static int COUNT_OFFSET = ARTICLENR_OFFSET + Integer.BYTES;
    private final static int PRICEPENCE_OFFSET = COUNT_OFFSET + Integer.BYTES;
    private final static int ADDRESSNUMBER_OFFSET = PRICEPENCE_OFFSET + Integer.BYTES;
    private final static int ADDRESSSTREET_OFFSET = ADDRESSNUMBER_OFFSET + Long.BYTES;
    private final static int ADDRESSCITY_OFFSET = ADDRESSSTREET_OFFSET + Long.BYTES;
    private final static int ADDRESSREGION_OFFSET = ADDRESSCITY_OFFSET + Long.BYTES;
    private final static int ADDRESSPOSTCODE_OFFSET = ADDRESSREGION_OFFSET + Long.BYTES;

    final static int TOTAL_SIZE = ADDRESSPOSTCODE_OFFSET + Long.BYTES;

    private final PooledByteArrayMap byteArrayPool;

    private ByteBuffer buffer;
    private int startPosition;


    OrderView(PooledByteArrayMap byteArrayPool) {
        this.byteArrayPool = byteArrayPool;
    }

    OrderView wrap(ByteBuffer buffer, int startPosition) {
        this.buffer = buffer;
        this.startPosition = startPosition;
        return this;
    }

    public OrderView set(
            long id,
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
        buffer.putLong(startPosition + ID_OFFSET, id);
        buffer.putLong(startPosition + USER_OFFSET, byteArrayPool.put(user));
        buffer.putInt(startPosition + ARTICLENR_OFFSET, articleNr);
        buffer.putInt(startPosition + COUNT_OFFSET, count);
        buffer.putInt(startPosition + PRICEPENCE_OFFSET, pricePence);
        buffer.putLong(startPosition + ADDRESSNUMBER_OFFSET, internString(addressNumber));
        buffer.putLong(startPosition + ADDRESSSTREET_OFFSET, internString(addressStreet));
        buffer.putLong(startPosition + ADDRESSCITY_OFFSET, internString(addressCity));
        buffer.putLong(startPosition + ADDRESSREGION_OFFSET, internString(addressRegion));
        buffer.putLong(startPosition + ADDRESSPOSTCODE_OFFSET, internString(addressPostCode));
        return this;
    }


    public long getId() {
        return buffer.getLong(startPosition + ID_OFFSET);
    }

    public byte @ImmutableByteArray [] getUser() {
        return byteArrayPool.get(getUserPoolKey());
    }

    public long getUserPoolKey() {
        return buffer.getLong(startPosition + USER_OFFSET);
    }

    public int getArticleNr() {
        return buffer.getInt(startPosition + ARTICLENR_OFFSET);
    }

    public int getCount() {
        return buffer.getInt(startPosition + COUNT_OFFSET);
    }

    public int getPricePence() {
        return buffer.getInt(startPosition + PRICEPENCE_OFFSET);
    }

    public String getAddressNumber() {
        long addressNumber = buffer.getLong(startPosition + ADDRESSNUMBER_OFFSET);
        return restoreString(addressNumber);
    }

    public OrderView changeAddressNumber(String addressNumber) {
        long oldAddressNumber = buffer.getLong(startPosition + ADDRESSNUMBER_OFFSET);
        long newAddressNumber = replaceString(addressNumber, oldAddressNumber);
        buffer.putLong(startPosition + ADDRESSNUMBER_OFFSET, newAddressNumber);
        return this;
    }

    public String getAddressStreet() {
        long addressStreet = buffer.getLong(startPosition + ADDRESSSTREET_OFFSET);
        return restoreString(addressStreet);
    }

    public OrderView changeAddressStreet(String addressStreet) {
        long oldAddressStreet = buffer.getLong(startPosition + ADDRESSSTREET_OFFSET);
        long newAddressStreet = replaceString(addressStreet, oldAddressStreet);
        buffer.putLong(startPosition + ADDRESSSTREET_OFFSET, newAddressStreet);
        return this;
    }

    public String getAddressCity() {
        long addressCity = buffer.getLong(startPosition + ADDRESSCITY_OFFSET);
        return restoreString(addressCity);
    }

    public OrderView changeAddressCity(String addressCity) {
        long oldAddressCity = buffer.getLong(startPosition + ADDRESSCITY_OFFSET);
        long newAddressCity = replaceString(addressCity, oldAddressCity);
        buffer.putLong(startPosition + ADDRESSCITY_OFFSET, newAddressCity);
        return this;
    }

    public String getAddressRegion() {
        long addressRegion = buffer.getLong(startPosition + ADDRESSREGION_OFFSET);
        return restoreString(addressRegion);
    }

    public OrderView changeAddressRegion(String addressRegion) {
        long oldAddressRegion = buffer.getLong(startPosition + ADDRESSREGION_OFFSET);
        long newAddressRegion = replaceString(addressRegion, oldAddressRegion);
        buffer.putLong(startPosition + ADDRESSREGION_OFFSET, newAddressRegion);
        return this;
    }

    public String getAddressPostCode() {
        long addressPostCode = buffer.getLong(startPosition + ADDRESSPOSTCODE_OFFSET);
        return restoreString(addressPostCode);
    }

    public OrderView changeAddressPostCode(String addressPostCode) {
        long oldAddressPostCode = buffer.getLong(startPosition + ADDRESSPOSTCODE_OFFSET);
        long newAddressPostCode = replaceString(addressPostCode, oldAddressPostCode);
        buffer.putLong(startPosition + ADDRESSPOSTCODE_OFFSET, newAddressPostCode);
        return this;
    }

    public String getAddress() {
        return getAddressNumber() + " "
                + getAddressStreet() + ", "
                + getAddressCity() + ", "
                + getAddressRegion() + ", "
                + getAddressPostCode();
    }


    private long internString(String str) {
        return byteArrayPool.put(str.getBytes(StandardCharsets.US_ASCII));
    }

    private long replaceString(String str, long oldKey) {
        long newKey = internString(str);
        byteArrayPool.free(oldKey);
        return newKey;
    }

    @SuppressWarnings("byte.array.weakening")
    private String restoreString(long key) {
        return new String(byteArrayPool.get(key), StandardCharsets.US_ASCII);
    }

}
