/*
 * Copyright 2026 https://www.github.com/jackutil
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.jackutil;

import java.util.Arrays;

/**
 * An optimized utility class for encoding and decoding base-10 integers
 * to and from Base62 strings.
 */
public final class Base62 {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = 62;
    private static final int BUFFER_SIZE = 11;
    private static final int[] DECODE_TABLE = new int[128];

    // Lookup-Table initialization for ASCII characters
    static {
        Arrays.fill(DECODE_TABLE, -1);

        for (int i = 0; i < ALPHABET.length(); i++) {
            DECODE_TABLE[ALPHABET.charAt(i)] = i;
        }
    }

    /**
     * Encodes a non-negative base-10 long integer into a Base62 string.
     * @param number The non-negative long integer to encode.
     * @return A Base62 encoded string representation of the given number.
     * @throws IllegalArgumentException If the provided number is negative.
     */
    public static String encode(final long number) {
        if (number < 0) {
            throw new IllegalArgumentException("Number can not be negative");
        }

        if (number == 0) {
            return "0";
        }

        long num = number;

        char[] buf = new char[BUFFER_SIZE];

        int index = BUFFER_SIZE;

        while (num > 0) {
            int remainder = (int) (num % BASE);

            buf[--index] = ALPHABET.charAt(remainder);

            num /= BASE;
        }

        return new String(buf, index, BUFFER_SIZE - index);
    }

    /**
     * Decodes a Base62 string back into a base-10 long integer.
     * @param input The Base62 encoded string to decode.
     * @return The decoded base-10 long integer.
     * @throws IllegalArgumentException If the input string is null, empty, or
     * contains invalid Base62 characters.
     */
    public static long decode(final String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Input can not be null or empty");
        }

        long result = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c >= 128) {
                throw new IllegalArgumentException("Invalid Base62 Character: " + c);
            }

            int index = DECODE_TABLE[c];

            if (index == -1) {
                throw new IllegalArgumentException("Invalid Base62 Character: " + c);
            }

            result = (BASE * result) + index;
        }

        return result;
    }
}