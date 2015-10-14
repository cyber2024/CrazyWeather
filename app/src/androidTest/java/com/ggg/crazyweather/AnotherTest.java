package com.ggg.crazyweather;

import android.app.Application;
import android.test.ApplicationTestCase;

/**
 * Created by Russell Elfenbein on 10/12/2015.
 */
public class AnotherTest extends ApplicationTestCase<Application> {
    public AnotherTest() {
        super(Application.class);
    }

    public void testThisIsAPass(){
        assertTrue("this has to pass",2==2);
    }

}
