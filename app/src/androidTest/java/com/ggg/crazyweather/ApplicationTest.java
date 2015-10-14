package com.ggg.crazyweather;

import android.app.Application;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testThatDemonstratesFail() throws Exception {
        int a = 0,
                b = 1,
                c = 2,
                d = 3;
        assertEquals("X should be equal", a, 0);
        assertTrue("b should be 1", b == 1);
        assertFalse("c should be 2", c == 3);
        if(d < 3) {
            fail("D should be == 3");
        }

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}