package com.romanmarkunas.blog.memory.example15;

import com.romanmarkunas.blog.memory.example14.PooledByteArrayMap;
import com.romanmarkunas.blog.memory.example14.checkers.ImmutableByteArray;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class OrderView {

    private final static int ID_OFFSET = 0;
    private final static int USER_OFFSET = ID_OFFSET + Long.BYTES;
    private final static int ARTICLENR_OFFSET = USER_OFFSET + Integer.BYTES;
    private final static int COUNT_OFFSET = ARTICLENR_OFFSET + Integer.BYTES;
    private final static int PRICEPENCE_OFFSET = COUNT_OFFSET + Integer.BYTES;
    private final static int ADDRESSNUMBER_OFFSET = PRICEPENCE_OFFSET + Integer.BYTES;
    private final static int ADDRESSSTREET_OFFSET = ADDRESSNUMBER_OFFSET + Integer.BYTES;
    private final static int ADDRESSCITY_OFFSET = ADDRESSSTREET_OFFSET + Integer.BYTES;
    private final static int ADDRESSREGION_OFFSET = ADDRESSCITY_OFFSET + Integer.BYTES;
    private final static int ADDRESSPOSTCODE_OFFSET = ADDRESSREGION_OFFSET + Integer.BYTES;

    final static int TOTAL_SIZE = ADDRESSPOSTCODE_OFFSET + Integer.BYTES;

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
        buffer.putInt(startPosition + USER_OFFSET, byteArrayPool.put(user));
        buffer.putInt(startPosition + ARTICLENR_OFFSET, articleNr);
        buffer.putInt(startPosition + COUNT_OFFSET, count);
        buffer.putInt(startPosition + PRICEPENCE_OFFSET, pricePence);
        buffer.putInt(startPosition + ADDRESSNUMBER_OFFSET, internString(addressNumber));
        buffer.putInt(startPosition + ADDRESSSTREET_OFFSET, internString(addressStreet));
        buffer.putInt(startPosition + ADDRESSCITY_OFFSET, internString(addressCity));
        buffer.putInt(startPosition + ADDRESSREGION_OFFSET, internString(addressRegion));
        buffer.putInt(startPosition + ADDRESSPOSTCODE_OFFSET, internString(addressPostCode));
        return this;
    }


    public long getId() {
        return buffer.getLong(startPosition + ID_OFFSET);
    }

    public byte @ImmutableByteArray [] getUser() {
        return byteArrayPool.get(getUserPoolKey());
    }

    public int getUserPoolKey() {
        return buffer.getInt(startPosition + USER_OFFSET);
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
        int addressNumber = buffer.getInt(startPosition + ADDRESSNUMBER_OFFSET);
        return restoreString(addressNumber);
    }

    public OrderView changeAddressNumber(String addressNumber) {
        int oldAddressNumber = buffer.getInt(startPosition + ADDRESSNUMBER_OFFSET);
        int newAddressNumber = replaceString(addressNumber, oldAddressNumber);
        buffer.putInt(startPosition + ADDRESSNUMBER_OFFSET, newAddressNumber);
        return this;
    }

    public String getAddressStreet() {
        int addressStreet = buffer.getInt(startPosition + ADDRESSSTREET_OFFSET);
        return restoreString(addressStreet);
    }

    public OrderView changeAddressStreet(String addressStreet) {
        int oldAddressStreet = buffer.getInt(startPosition + ADDRESSSTREET_OFFSET);
        int newAddressStreet = replaceString(addressStreet, oldAddressStreet);
        buffer.putInt(startPosition + ADDRESSSTREET_OFFSET, newAddressStreet);
        return this;
    }

    public String getAddressCity() {
        int addressCity = buffer.getInt(startPosition + ADDRESSCITY_OFFSET);
        return restoreString(addressCity);
    }

    public OrderView changeAddressCity(String addressCity) {
        int oldAddressCity = buffer.getInt(startPosition + ADDRESSCITY_OFFSET);
        int newAddressCity = replaceString(addressCity, oldAddressCity);
        buffer.putInt(startPosition + ADDRESSCITY_OFFSET, newAddressCity);
        return this;
    }

    public String getAddressRegion() {
        int addressRegion = buffer.getInt(startPosition + ADDRESSREGION_OFFSET);
        return restoreString(addressRegion);
    }

    public OrderView changeAddressRegion(String addressRegion) {
        int oldAddressRegion = buffer.getInt(startPosition + ADDRESSREGION_OFFSET);
        int newAddressRegion = replaceString(addressRegion, oldAddressRegion);
        buffer.putInt(startPosition + ADDRESSREGION_OFFSET, newAddressRegion);
        return this;
    }

    public String getAddressPostCode() {
        int addressPostCode = buffer.getInt(startPosition + ADDRESSPOSTCODE_OFFSET);
        return restoreString(addressPostCode);
    }

    public OrderView changeAddressPostCode(String addressPostCode) {
        int oldAddressPostCode = buffer.getInt(startPosition + ADDRESSPOSTCODE_OFFSET);
        int newAddressPostCode = replaceString(addressPostCode, oldAddressPostCode);
        buffer.putInt(startPosition + ADDRESSPOSTCODE_OFFSET, newAddressPostCode);
        return this;
    }

    public String getAddress() {
        return getAddressNumber() + " "
                + getAddressStreet() + ", "
                + getAddressCity() + ", "
                + getAddressRegion() + ", "
                + getAddressPostCode();
    }


    private int internString(String str) {
        return byteArrayPool.put(str.getBytes(StandardCharsets.US_ASCII));
    }

    private int replaceString(String str, int oldKey) {
        int newKey = internString(str);
        byteArrayPool.free(oldKey);
        return newKey;
    }

    @SuppressWarnings("byte.array.weakening")
    private String restoreString(int key) {
        return new String(byteArrayPool.get(key), StandardCharsets.US_ASCII);
    }
}
