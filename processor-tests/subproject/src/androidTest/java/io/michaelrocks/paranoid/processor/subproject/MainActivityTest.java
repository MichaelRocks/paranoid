

package io.michaelrocks.paranoid.processor.subproject;

import android.support.annotation.NonNull;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;


import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {
    @Test
    public void testMainActivityIsProcessed() {
        assertDeobfuscatorContainsString("Subprojects: ");
    }

    @Test
    public void testAndroidConstantsIsProcessed() {
        assertDeobfuscatorContainsString("android");
    }

    @Test
    public void testJavaConstantsIsProcessed() {
        assertDeobfuscatorContainsString("java");
    }

    private void assertDeobfuscatorContainsString(@NonNull final String string) {
        try {
            final String suffix = BuildConfig.DEBUG ? "Debug" : "Release";
            final Class<?> deobfuscatorClass =
                    Class.forName("io.michaelrocks.paranoid.Deobfuscator$processortests$subproject$" + suffix);
            final Method deobfuscationMethod = deobfuscatorClass.getDeclaredMethod("getString", Integer.TYPE);
            for (int i = 0; ; ++i) {
                final String deobfuscatedString = (String) deobfuscationMethod.invoke(null, i);
                if (string.equals(deobfuscatedString)) {
                    return;
                }
            }
        } catch (final Throwable exception) {
            fail("expected:<" + string + "> but was exception: <" + exception + ">");
        }
    }
}
