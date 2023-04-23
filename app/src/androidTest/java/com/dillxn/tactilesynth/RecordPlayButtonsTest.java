package com.dillxn.tactilesynth;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class RecordPlayButtonsTest {

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void recordPlayButtonsDisplayed() {
        Espresso.onView(withId(R.id.armRecording)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.playRecording)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.metronomeToggle)).check(matches(isDisplayed()));
    }

    @Test
    public void testArmRecordingToggle() {
        // Assuming default state is not armed
        Espresso.onView(withId(R.id.armRecording)).perform(ViewActions.click());
        // Verify armRecordingBtn is updated to the armed state
        // You may need to add a custom matcher to verify the drawable if required

        Espresso.onView(withId(R.id.armRecording)).perform(ViewActions.click());
        // Verify armRecordingBtn is updated back to the not armed state
        // You may need to add a custom matcher to verify the drawable if required
    }

    @Test
    public void testMetronomeToggle() {
        // Assuming default state is not playing
        Espresso.onView(withId(R.id.metronomeToggle)).perform(ViewActions.click());
        // Verify metronomeToggle is updated to the playing state
        // You may need to add a custom matcher to verify the drawable if required

        Espresso.onView(withId(R.id.metronomeToggle)).perform(ViewActions.click());
        // Verify metronomeToggle is updated back to the not playing state
        // You may need to add a custom matcher to verify the drawable if required
    }
}