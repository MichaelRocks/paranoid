

package io.michaelrocks.paranoid.sample;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {
    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void textViewsHaveProperText() {
        onView(withId(R.id.questionTextView))
                .check(matches(withText("Q:\r\nHow does it work?")));
        onView(withId(R.id.answerTextView))
                .check(matches(withText("A:\r\nIt's magic! ¯\\_(ツ)_/¯")));
        onView(withId(R.id.showDialogButton))
                .check(matches(withText("Show dialog")));
    }
}
