

package io.michaelrocks.paranoid.processor.longstring;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;


import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LongStringTest {
    @Test
    public void testLongString() {
        assertFalse(LongString.INSTANCE.getString().isEmpty());
    }
}
