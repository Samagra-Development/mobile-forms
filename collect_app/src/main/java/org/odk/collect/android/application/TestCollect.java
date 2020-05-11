package org.odk.collect.android.application;

/**
 * @author James Knight
 *
 * This class will automatically be used by Robolectric
 * tests as a replacement for the application class configured
 * in the Android manifest as it prefixes that class with `Test`.
 */

public class TestCollect extends Collect {

    @Override
    protected void setupLeakCanary() {
        // No leakcanary in unit tests.
    }
}