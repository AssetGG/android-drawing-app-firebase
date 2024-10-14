package com.example.dreamdoodler
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import androidx.test.runner.AndroidJUnit4
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

private const val DREAM_DOODLER_PACKAGE = "com.example.dreamdoodler"
private const val LAUNCH_TIMEOUT = 5000L
private const val STRING_TO_BE_TYPED = "UiAutomator"

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
class UIAutomatorTestDreamDoodler {
    private lateinit var device: UiDevice

    /**
     * Start the app before each test.
     */
    @Before
    fun startMainActivityFromHomeScreen() {
        // Initialize UiDevice instance
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Start from the home screen
        device.pressHome()

        // Wait for launcher
        val launcherPackage: String = device.launcherPackageName
        assertThat(launcherPackage, notNullValue())
        device.wait(
            Until.hasObject(By.pkg(launcherPackage).depth(0)),
            LAUNCH_TIMEOUT
        )

        // Launch the app
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = context.packageManager.getLaunchIntentForPackage(
            DREAM_DOODLER_PACKAGE)?.apply {
            // Clear out any previous instances
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)

        // Wait for the app to appear
        device.wait(
            Until.hasObject(By.pkg(DREAM_DOODLER_PACKAGE).depth(0)),
            LAUNCH_TIMEOUT
        )
    }
    // Citation: All code above this point is from here: https://developer.android.com/training/testing/other-components/ui-automator

    /**
     * Test that information about the user's pen shape is preserved after rotation.
     */
    @Test
    fun testRotateScreenShapeButton() {
        device.setOrientationPortrait()
        device.wait(Until.hasObject(By.res(DREAM_DOODLER_PACKAGE, "penShapeButton")), 3000)
        // Click the button that changes the pen shape
        device.findObject(By.res(DREAM_DOODLER_PACKAGE, "penShapeButton")).click()

        // Capture the text on the shape button. Should say some text besides the default text now
        val shapeButtonText = device.findObject(By.res(DREAM_DOODLER_PACKAGE, "penShapeButton")).text
        device.setOrientationLandscape()
        // Wait for the screen to rotate
        device.waitForIdle(5000)
        device.wait(Until.hasObject(By.res(DREAM_DOODLER_PACKAGE, "penShapeButton")), 3000)

        // Capture the text on the shape button again and compare it to the text that was on the shape button
        // before rotation
        val rotatedShapeButtonText = device.findObject(By.res(DREAM_DOODLER_PACKAGE, "penShapeButton")).text
        assertEquals(shapeButtonText, rotatedShapeButtonText)
    }

    /**
     * Test that the progress on the pen/stamp size slider is preserved after rotation
     */
    @Test
    fun testRotateScreenSliderPosition() {
        device.setOrientationPortrait()
        device.wait(Until.hasObject(By.res(DREAM_DOODLER_PACKAGE, "penSizeSlider")), 3000)
        // Click the button that changes the pen shape
        device.findObject(By.res(DREAM_DOODLER_PACKAGE, "penSizeSlider")).swipe(Direction.RIGHT, .2f)

        // Capture the position on the size slider
        val sliderPositionBefore = device.findObject(By.res(DREAM_DOODLER_PACKAGE, "penSizeSlider")).getText()
        device.setOrientationLandscape()
        // Wait for the screen to rotate
        device.waitForIdle(5000)
        device.wait(Until.hasObject(By.res(DREAM_DOODLER_PACKAGE, "penSizeSlider")), 3000)

        // Capture the position of the slider again and compare it to the slider position before rotation
        val sliderPositionAfter = device.findObject(By.res(DREAM_DOODLER_PACKAGE, "penSizeSlider")).getText()
        assertEquals(sliderPositionBefore, sliderPositionAfter)
    }

}