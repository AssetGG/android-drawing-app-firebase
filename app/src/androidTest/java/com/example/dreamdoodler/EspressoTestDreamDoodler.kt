package com.example.dreamdoodler

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.screenshot.captureToBitmap
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class EspressoTestDreamDoodler {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    /**
     * Test that tapping the screen changes the bitmap in scribble mode
     */
    @Test
    fun testDrawDotScribbleMode() {
        // Capture the bitmap before tapping
        val initialBitmap = onView(withId(R.id.customView)).captureToBitmap()
        // Tap
        onView(withId(R.id.customView)).perform(click())
        // Capture the bitmap after tapping
        val currentBitmap = onView(withId(R.id.customView)).captureToBitmap()

        // Assert that clicking has made some change to the bitmap
        assertFalse(initialBitmap.sameAs(currentBitmap))
    }

    /**
     * Test that tapping the screen changes the bitmap in circular stamp mode
     */
    @Test
    fun testDrawDotCircleStampMode() {
        // Switch to one of the stamp modes
        onView(withId(R.id.penShapeButton)).perform(click())

        // Capture the bitmap before tapping
        val initialBitmap = onView(withId(R.id.customView)).captureToBitmap()
        // Tap
        onView(withId(R.id.customView)).perform(click())
        // Capture the bitmap after tapping
        val currentBitmap = onView(withId(R.id.customView)).captureToBitmap()

        // Assert that clicking has made some change to the bitmap
        assertFalse(initialBitmap.sameAs(currentBitmap))
    }

    /**
     * Test that tapping the screen changes the bitmap in square stamp mode
     */
    @Test
    fun testDrawDotSquareStampMode() {
        // Switch to the next stamp mode
        onView(withId(R.id.penShapeButton)).perform(click())
        onView(withId(R.id.penShapeButton)).perform(click())
        // Capture the bitmap before tapping
        val initialBitmap = onView(withId(R.id.customView)).captureToBitmap()
        // Tap
        onView(withId(R.id.customView)).perform(click())
        // Capture the bitmap after tapping
        val currentBitmap = onView(withId(R.id.customView)).captureToBitmap()

        // Assert that clicking has made some change to the bitmap
        assertFalse(initialBitmap.sameAs(currentBitmap))
    }

    @Test
    fun testNewDrawingResetsCanvas() {
        onView(withId(R.id.customView)).perform(click())

        // Bitmap after making some drawings
        val modifiedBitmap = onView(withId(R.id.customView)).captureToBitmap()

        // Press the "New" button to reset the canvas for a new drawing
        onView(withId(R.id.newButton)).perform(click())

        // Dismiss the Alert warning about unsaved changes
        onView(withText("Yes")).perform(click())

        // Bitmap after pressing "New"
        val newBitmap = onView(withId(R.id.customView)).captureToBitmap()

        // Verify that the new canvas (bitmap) is different from the modified one
        assertFalse(modifiedBitmap.sameAs(newBitmap))
    }

    @Test
    fun testNewDrawingCanceledDoesNotResetCanvas() {
        onView(withId(R.id.customView)).perform(click())

        // Bitmap before pressing "New"
        val modifiedBitmap = onView(withId(R.id.customView)).captureToBitmap()

        // Press the "New" button to reset the canvas for a new drawing
        onView(withId(R.id.newButton)).perform(click())

        // Cancel the "New" action by pressing "No" on the Dialog Box
        onView(withText("No")).perform(click())

        // Bitmap after pressing "New"
        val newBitmap = onView(withId(R.id.customView)).captureToBitmap()

        // Verify that the new canvas (bitmap) is different from the modified one
        assertTrue(modifiedBitmap.sameAs(newBitmap))
    }
}