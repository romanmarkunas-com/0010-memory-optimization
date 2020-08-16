package com.romanmarkunas.blog.memory.example13;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCompressedTest {

    @Test
    void shouldCompressSingleOrder() {
        Order original = new Order(
                1,
                new byte[]{1, 0, 4},
                3,
                4,
                5,
                "32",
                "Googe Street",
                "London",
                "Greater London",
                "W10 P10"
        );
        OrderCompressed compressed = OrderCompressed.compress(original);
        Order decompressed = decompress(compressed);

        assertThat(decompressed).isEqualToComparingFieldByField(original);
    }

    @Test
    void shouldCompressTwoOrdersInSequence() {
        Order original1 = new Order(
                1,
                new byte[]{1, 0, 4},
                3,
                4,
                5,
                "32",
                "Goodge Street",
                "London",
                "Greater London",
                "W10 P10"
        );
        Order original2 = new Order(
                2,
                new byte[]{1, 0, 4},
                3,
                4,
                5,
                "32",
                "Goodge Street",
                "London",
                "Greater London",
                "W10 P10"
        );
        assertThat(original1).usingRecursiveComparison().isNotEqualTo(original2);

        OrderCompressed compressed1 = OrderCompressed.compress(original1);
        OrderCompressed compressed2 = OrderCompressed.compress(original2);
        Order decompressed1 = decompress(compressed1);
        Order decompressed2 = decompress(compressed2);

        assertThat(decompressed1).isEqualToComparingFieldByField(original1);
        assertThat(decompressed2).isEqualToComparingFieldByField(original2);
    }


    private Order decompress(OrderCompressed compressedOrder) {
        try (
                ByteArrayInputStream bais = new ByteArrayInputStream(compressedOrder.getCompressedPart());
                GZIPInputStream gzipIn = new GZIPInputStream(bais);
                ObjectInputStream objectIn = new ObjectInputStream(gzipIn);
        )
        {
            Order read = (Order) objectIn.readObject();
            return new Order(
                    read.getId(),
                    read.getUser(),
                    read.getArticleNr(),
                    read.getCount(),
                    read.getPricePence(),
                    compressedOrder.getAddressNumber(),
                    compressedOrder.getAddressStreet(),
                    compressedOrder.getAddressCity(),
                    compressedOrder.getAddressRegion(),
                    compressedOrder.getAddressPostCode()
            );
        }
        catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}