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
import dev.jackutil.Base62;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Base62Test {
    // ==========================================
    // ENCODING TESTS
    // ==========================================

    @Test
    void testEncode_Zero() {
        assertEquals("0", Base62.encode(0L));
    }

    @Test
    void testEncode_SmallNumbers() {
        assertEquals("1", Base62.encode(1L));
        assertEquals("9", Base62.encode(9L));
        assertEquals("a", Base62.encode(10L));
        assertEquals("Z", Base62.encode(61L));
        assertEquals("10", Base62.encode(62L));
    }

    @Test
    void testEncode_LargeNumbers() {
        assertEquals("15FTGf", Base62.encode(999_999_999L));
    }

    @Test
    void testEncode_MaxLongValue() {
        // 9,223,372,036,854,775,807 is the absolute maximum value for a Java primitive long
        assertEquals("aZl8N0y58M7", Base62.encode(Long.MAX_VALUE));
    }

    @Test
    void testEncode_NegativeNumberThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Base62.encode(-1L)
        );
        assertEquals("Number can not be negative", exception.getMessage());
    }

    // ==========================================
    // DECODING TESTS
    // ==========================================

    @Test
    void testDecode_Zero() {
        assertEquals(0L, Base62.decode("0"));
    }

    @Test
    void testDecode_SmallStrings() {
        assertEquals(1L, Base62.decode("1"));
        assertEquals(10L, Base62.decode("a"));
        assertEquals(61L, Base62.decode("Z"));
        assertEquals(62L, Base62.decode("10"));
    }

    @Test
    void testDecode_LargeStrings() {
        assertEquals(999_999_999L, Base62.decode("15FTGf"));
    }

    @Test
    void testDecode_MaxLongString() {
        assertEquals(Long.MAX_VALUE, Base62.decode("aZl8N0y58M7"));
    }

    @Test
    void testDecode_NullOrEmptyThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> Base62.decode(null));
        assertThrows(IllegalArgumentException.class, () -> Base62.decode(""));
    }

    @Test
    void testDecode_InvalidAsciiCharacterThrowsException() {
        // Contains a hyphen '-' which is valid ASCII but invalid Base62
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Base62.decode("abc-123")
        );
        assertTrue(exception.getMessage().contains("Invalid Base62 Character: -"));
    }

    @Test
    void testDecode_ExtendedUnicodeCharacterThrowsException() {
        // Contains the 'é' character which has an integer value > 127
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Base62.decode("abé123")
        );
        assertTrue(exception.getMessage().contains("Invalid Base62 Character: é"));
    }

    // ==========================================
    // SYMMETRY TEST (Encode -> Decode)
    // ==========================================

    @Test
    void testSymmetry_EncodeThenDecode() {
        long[] testValues = {
                0L, 1L, 61L, 62L, 100L, 123456789L, 999999999999L, Long.MAX_VALUE
        };

        for (long originalValue : testValues) {
            String encoded = Base62.encode(originalValue);
            long decoded = Base62.decode(encoded);
            assertEquals(originalValue, decoded, "Failed on value: " + originalValue);
        }
    }
}
