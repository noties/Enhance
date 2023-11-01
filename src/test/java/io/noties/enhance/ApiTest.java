package io.noties.enhance;

import org.checkerframework.checker.units.qual.A;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class ApiTest {

    @Test
    public void testOf() {
        final int length = Api.values().length;
        // index starts at 0, but values start at 1
        for (int i = 1, max = length + 1; i < max; i++) {
            final Api api = Api.of(i);
            assertNotNull(String.valueOf(i), api);
            assertEquals(String.valueOf(i), i, api.sdkInt);
        }
    }

    @Test
    public void testUnknown() {
        final Api[] values = Api.values();
        final int max = values[values.length - 1].sdkInt;

        final int[] inputs = {
                Integer.MIN_VALUE,
                -1,
                0,
                max + 1,
                max + 2,
                Integer.MAX_VALUE
        };

        for (int input: inputs) {
            assertNull(String.valueOf(input), Api.of(input));
        }
    }

    @Test
    public void testSequential() {
        final Api[] values = Api.values();

        // verify all go without interruption from 1 to max
        assertEquals(1, values[0].sdkInt);

        for (int i = 1, length = values.length; i < length; i++) {
            final Api api = values[i];
            assertEquals(
                    "i:" + i + ", api:" + api,
                    values[i - 1].sdkInt + 1,
                    api.sdkInt
            );
        }
    }
}