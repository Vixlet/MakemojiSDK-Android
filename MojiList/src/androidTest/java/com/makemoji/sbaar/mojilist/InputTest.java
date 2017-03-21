package com.makemoji.sbaar.mojilist;
import static android.support.test.espresso.Espresso.*;

import android.content.Context;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;

import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.inputmethod.InputMethodManager;

import com.makemoji.mojilib.MojiInputLayout;

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

    @Test//test ! appears
    public void testSearchClick(){
        onView(withId(R.id._mm_edit_text)).perform(ViewActions.click(),ViewActions.typeTextIntoFocusedView("ab"),ViewActions.clearText());//open kb
        onView(withId(R.id._mm_flashtag_button)).perform(ViewActions.click());
        onView(withId(R.id._mm_edit_text)).check(ViewAssertions.matches(ViewMatchers.withText("!")));
    }

    @Test public void testOpenLeft(){
        onView(withId(R.id._mm_edit_text)).perform(ViewActions.click(),ViewActions.typeTextIntoFocusedView("ab"),ViewActions.clearText());//open kb
        onView(withId(R.id._mm_horizontal_top_scroller)).perform(ViewActions.swipeRight());
        onView(withId(R.id._mm_categories_button)).perform(ViewActions.click());
        onView(withId(R.id._mm_page_container)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }
}
