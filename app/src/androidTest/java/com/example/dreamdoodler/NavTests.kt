package com.example.dreamdoodler

import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import com.example.dreamdoodler.DrawingPageFragment
import com.example.dreamdoodler.ColorPickerDialogFragment
import com.example.dreamdoodler.DrawingSelectFragment
import com.example.dreamdoodler.SaveDrawingFragment
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Navigation tests for Dream Doodler app.
 *
 * Ensure that the all functions in fragments
 * properly navigate to the correct destinations.
 *
 * Created with reference to https://developer.android.com/guide/fragments/test
 * and https://developer.android.com/guide/navigation/testing
 */
@RunWith(AndroidJUnit4::class)
class NavUnitTest {

    /**
     * Test that clicking the color picker button in the DrawingPageFragment
     * navigates to the ColorPickerDialogFragment.
     */
    @Test
    fun testNavigationFromDrawingPageToColorPickerDialog() {
        val navController = TestNavHostController(getApplicationContext())

        val scenario = launchFragmentInContainer<DrawingPageFragment>()

        scenario.onFragment { fragment ->
            // Set the graph on the TestNavHostController
            navController.setGraph(R.navigation.nav_graph)

            // Make the NavController available via the findNavController() APIs
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(ViewMatchers.withId(R.id.colorPickButton)).perform(ViewActions.click())
        assertEquals(navController.currentDestination?.id, R.id.colorPickerDialog)
    }

    @Test
    fun testNavigationFromDrawingPageToSaveFragment() {
        val navController = TestNavHostController(getApplicationContext())

        val scenario = launchFragmentInContainer<DrawingPageFragment>()

        scenario.onFragment { fragment ->
            // Set the graph on the TestNavHostController
            navController.setGraph(R.navigation.nav_graph)

            // Make the NavController available via the findNavController() APIs
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(ViewMatchers.withId(R.id.saveButton)).perform(ViewActions.click())
        assertEquals(navController.currentDestination?.id, R.id.saveDrawingFragment)
    }

    @Test
    fun testNavigationFromDrawingPageToDrawingSelectFragment() {
        val navController = TestNavHostController(getApplicationContext())

        val scenario = launchFragmentInContainer<DrawingPageFragment>()

        scenario.onFragment { fragment ->
            // Set the graph on the TestNavHostController
            navController.setGraph(R.navigation.nav_graph)

            // Make the NavController available via the findNavController() APIs
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(ViewMatchers.withId(R.id.loadButton)).perform(ViewActions.click())
        assertEquals(navController.currentDestination?.id, R.id.drawingSelectFragment)
    }
}
