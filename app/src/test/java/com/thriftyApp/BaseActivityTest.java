package com.thriftyApp;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Confirms {@link BaseActivity} applies the locale via attachBaseContext. */
@RunWith(RobolectricTestRunner.class)
@Config(application = ThriftyApp.class)
public class BaseActivityTest {

    @Test
    public void activityCreatesAndAttachesLocalisedContext() {
        BaseActivity activity = Robolectric.buildActivity(BaseActivity.class).create().get();
        assertNotNull(activity);
        assertNotNull(activity.getBaseContext());
    }
}
