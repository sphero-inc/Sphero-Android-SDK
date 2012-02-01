package orbotix.twophones;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class orbotix.twophones.ConnectionActivityTest \
 * orbotix.twophones.tests/android.test.InstrumentationTestRunner
 */
public class ConnectionActivityTest extends ActivityInstrumentationTestCase2<ConnectionActivity> {

    public ConnectionActivityTest() {
        super("orbotix.twophones", ConnectionActivity.class);
    }

}
