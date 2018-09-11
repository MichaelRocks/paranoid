

package io.michaelrocks.paranoid.processor.allcharsstring;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AllCharsStringTest {
    @Test
    public void testAllCharsString() {
        final String string = AllCharString.INSTANCE.getString();
        assertTrue(string.length() == 0x10000);

        for (int i = 0; i < string.length(); ++i) {
            assertEquals(i, string.charAt(i));
        }
    }
}
