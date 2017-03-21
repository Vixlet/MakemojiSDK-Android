package com.makemoji.sbaar.mojilist;

import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by s_baa on 3/21/2017.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class InputTest {
    @Rule
    public ActivityTestRule<InputActivity> mActivityRule = new ActivityTestRule<>(
            InputActivity.class);

}
