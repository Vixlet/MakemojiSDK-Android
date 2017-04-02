package com.makemoji.sbaar.mojilist;
import static android.support.test.espresso.Espresso.*;

import android.content.Context;
import android.content.Intent;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.contrib.RecyclerViewActions.*;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.espresso.matcher.*;

import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.makemoji.mojilib.Moji;
import com.makemoji.mojilib.MojiInputLayout;
import com.makemoji.mojilib.wall.MojiWallActivity;

import android.support.test.InstrumentationRegistry;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by s_baa on 3/21/2017.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class InputTest {
    final String SPAN_STRING = " \uFFFC ";
    @Rule
    public ActivityTestRule<InputActivity> mActivityRule = new ActivityTestRule<>(
            InputActivity.class);

    public void openKb(){
        onView(withId(R.id._mm_edit_text)).perform(ViewActions.click(),ViewActions.typeTextIntoFocusedView("ab"),ViewActions.clearText());
    }
    @Test//test ! appears
    public void testSearchClick(){
        openKb();
        onView(withId(R.id._mm_flashtag_button)).perform(ViewActions.click());
        onView(withId(R.id._mm_edit_text)).check(ViewAssertions.matches(ViewMatchers.withText("!")));
    }

    @Test public void testOpenLeft(){
        openKb();
        onView(withId(R.id._mm_horizontal_top_scroller)).perform(ViewActions.swipeRight());
        onView(withId(R.id._mm_categories_button)).perform(ViewActions.click());
        onView(withId(R.id._mm_page_container)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }
    //tests handling the wall result intent
    @Test public void testHandleIntent(){
        final Intent intent = new Intent();
        intent.putExtra(Moji.EXTRA_PACKAGE_ORIGIN,InstrumentationRegistry.getTargetContext().getPackageName());
        intent.putExtra(Moji.EXTRA_JSON,"{\"native\":0,\"emoji\":[],\"flashtag\":\"SWAG\",\"gif\":0,\"id\":1011327," +
                "\"image_url\":\"https:\\/\\/d1tvcfe0bfyi6u.cloudfront.net\\/emoji\\/1011327-large@2x.png\"," +
                "\"locked\":false,\"name\":\"SWAG\",\"phrase\":0,\"video\":0,\"video_url\":\"\"}");
        onView(withId(R.id.mojiInput)).perform(new CustomActionMojiInput(){
            @Override
            public void perform(UiController uiController, View view) {
                ((MojiInputLayout)view).handleIntent(intent);
            }
        });
        onView(withId(R.id._mm_edit_text)).check(ViewAssertions.matches(ViewMatchers.withText(" \uFFFC ")));
    }
    //test visibility and focus when attatch/detaching an outside edit text
    @Test public void testAttatchDetatch(){
        onView(withId(R.id._mm_edit_text)).perform(ViewActions.click(),ViewActions.typeTextIntoFocusedView("a"));
        onView(withId(R.id._mm_recylcer_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0,ViewActions.click()));
        onView(withId(R.id._mm_edit_text)).check(ViewAssertions.matches(ViewMatchers.withText("a" +SPAN_STRING)));
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText("Attatch EditText")).perform(ViewActions.click());

        onView(withId(R.id.outside_met)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id._mm_edit_text)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.outside_met)).perform(ViewActions.click(),ViewActions.typeTextIntoFocusedView("b"));
        onView(withId(R.id._mm_recylcer_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0,ViewActions.click()));
        onView(withId(R.id.outside_met)).check(ViewAssertions.matches(ViewMatchers.withText("b" + SPAN_STRING)));

        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText("Detach EditText")).perform(ViewActions.click());

        onView(withId(R.id._mm_edit_text)).perform(ViewActions.click(),ViewActions.typeTextIntoFocusedView("b"));
        onView(withId(R.id._mm_recylcer_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0,ViewActions.click()));
        onView(withId(R.id._mm_edit_text)).check(ViewAssertions.matches(ViewMatchers.withText("a"+SPAN_STRING+"b"+ SPAN_STRING)));
        onView(withId(R.id.outside_met)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id._mm_edit_text)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

    }
}
