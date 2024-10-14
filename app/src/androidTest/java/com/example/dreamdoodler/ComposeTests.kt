package com.example.dreamdoodler
import android.content.Context
import android.graphics.Canvas
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertValueEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule

import com.example.dreamdoodler.SaveDrawingForm
import com.example.funfactassignment.DrawingDao
import com.example.funfactassignment.DrawingDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import java.io.File

@RunWith(AndroidJUnit4::class)
class ComposeTests {
    private lateinit var drawingViewModel: DrawingViewModel
    private lateinit var repository: DrawingRepository
    private lateinit var dao: DrawingDao
    private lateinit var database: DrawingDatabase
    private lateinit var tempTestDir: File

    var backCallbackUsed: Boolean = false
    var saveCallbackUsed: Boolean = false
    var failCallbackUsed: Boolean = false
    var itemClickCallBackUsed: Boolean = false

    /***
     * Sets up the test environment
     * Creates an in-memory database and temporary directory for testing
     * Created by Zander with reference to
     * https://developer.android.com/training/data-storage/room/testing-db
     * and some suggestions from ChatGPT
     */
    @Before
    fun setup() {
        // Setup callback flags to check if the save form is firing the right ones
        backCallbackUsed = false
        saveCallbackUsed = false
        failCallbackUsed = false
        itemClickCallBackUsed = false

        val context = ApplicationProvider.getApplicationContext<Context>()
        // Create an in-memory database
        database = Room.inMemoryDatabaseBuilder(
            context, DrawingDatabase::class.java).build()
        dao = database.drawingDao()
        // Create a temporary directory for testing
        tempTestDir = File.createTempFile("temp", "").apply {
            delete()
            mkdir()
        }
        repository = DrawingRepository(CoroutineScope(SupervisorJob()), dao, tempTestDir)
        drawingViewModel = DrawingViewModel(repository)
    }

    /**
     * Tear down the test environment.
     *
     * Clean up the temporary directory and close the in-memory database.
     *
     * Created with reference to https://developer.android.com/training/data-storage/room/testing-db
     * and some suggestions from ChatGPT.
     */
    @After
    fun tearDown() {
        tempTestDir.deleteRecursively()
        database.close()
    }

    @get:Rule
    val composeTestRule = createComposeRule()


    @Test
    fun testSaveDrawingFail() {
        // Context of the app under test.
        composeTestRule.setContent {
            SaveDrawingForm(viewModel = drawingViewModel, onBackButtonClicked = { backCallbackUsed = true }, onSuccessfulSave = { saveCallbackUsed = true }, onSaveFail = { failCallbackUsed = true})
        }
        composeTestRule.onNodeWithText("Save!").performClick()

        assertTrue(failCallbackUsed)
        assertFalse(saveCallbackUsed)
        assertFalse(backCallbackUsed)
    }

    @Test
    fun testSaveDrawingSuccess() {
        // Context of the app under test.
        composeTestRule.setContent {
            SaveDrawingForm(viewModel = drawingViewModel, onBackButtonClicked = { backCallbackUsed = true }, onSuccessfulSave = { saveCallbackUsed = true }, onSaveFail = { failCallbackUsed = true})
        }
        val drawingName = "MyDrawing"
        composeTestRule.onNodeWithText("Enter a name for your drawing:").performTextInput(drawingName)
        composeTestRule.onNodeWithText("Save!").performClick()

        assertFalse(failCallbackUsed)
        assertTrue(saveCallbackUsed)
        assertFalse(backCallbackUsed)
    }

    @Test
    fun testSaveDrawingKeepsDrawingNameInInput() {
        // Context of the app under test.
        composeTestRule.setContent {
            SaveDrawingForm(viewModel = drawingViewModel, onBackButtonClicked = { backCallbackUsed = true }, onSuccessfulSave = { saveCallbackUsed = true }, onSaveFail = { failCallbackUsed = true})
        }
        val drawingName = "MyDupeDrawing"
        // Save the drawing
        composeTestRule.onNodeWithText("Enter a name for your drawing:").performTextInput(drawingName)
        composeTestRule.onNodeWithText("Enter a name for your drawing:").assert(hasText(drawingName))
        composeTestRule.onNodeWithText("Save!").performClick()

        // Check the node
        composeTestRule.onNodeWithText("Enter a name for your drawing:").assert(hasText(drawingName))
    }

    @Test
    fun testSavedDrawingNameKeptInInputAfterBackButton() {
        // Context of the app under test.
        composeTestRule.setContent {
            SaveDrawingForm(viewModel = drawingViewModel, onBackButtonClicked = { backCallbackUsed = true }, onSuccessfulSave = { saveCallbackUsed = true }, onSaveFail = { failCallbackUsed = true})
        }
        val drawingName = "MyDupeDrawing"
        // Save the drawing
        composeTestRule.onNodeWithText("Enter a name for your drawing:").performTextInput(drawingName)
        composeTestRule.onNodeWithText("Save!").performClick()

        // Press the back button
        composeTestRule.onNodeWithText("Go back without saving").performClick()

        // Check the text input
        composeTestRule.onNodeWithText("Enter a name for your drawing:").assert(hasText(drawingName))
    }

    @Test
    fun testSavedDrawingNameNotOverwrittenByUnsavedName() {
        // Context of the app under test.
        composeTestRule.setContent {
            SaveDrawingForm(viewModel = drawingViewModel, onBackButtonClicked = { backCallbackUsed = true }, onSuccessfulSave = { saveCallbackUsed = true }, onSaveFail = { failCallbackUsed = true})
        }
        val drawingName = "MyDrawing"
        // Save the drawing
        composeTestRule.onNodeWithText("Enter a name for your drawing:").performTextInput(drawingName)
        composeTestRule.onNodeWithText("Save!").performClick()

        // Enter a different drawing name
        val tempDrawingName = "MyFunkyDrawing"
        // Press the back button without saving
        composeTestRule.onNodeWithText("Go back without saving").performClick()

        // Check the text input - should have the name of the saved drawing
        composeTestRule.onNodeWithText("Enter a name for your drawing:").assert(hasText(drawingName))
    }

    @Test
    fun testSaveDrawingTwiceSuccess() {
        // Context of the app under test.
        composeTestRule.setContent {
            SaveDrawingForm(viewModel = drawingViewModel, onBackButtonClicked = { backCallbackUsed = true }, onSuccessfulSave = { saveCallbackUsed = true }, onSaveFail = { failCallbackUsed = true})
        }
        val drawingName = "MyDupeDrawing"
        // Save the drawing
        composeTestRule.onNodeWithText("Enter a name for your drawing:").performTextInput(drawingName)
        composeTestRule.onNodeWithText("Save!").performClick()

        // Check the callback flags
        assertFalse(failCallbackUsed)
        assertTrue(saveCallbackUsed)
        assertFalse(backCallbackUsed)

        // Reset callback flags
        backCallbackUsed = false
        saveCallbackUsed = false
        failCallbackUsed = false

        // Check that the drawing name is still in the input (this is also tested in its own unit tests)
        composeTestRule.onNodeWithText("Enter a name for your drawing:").assert(hasText(drawingName))
        // Save the drawing again
        composeTestRule.onNodeWithText("Save!").performClick()

        // check the callback flags again
        assertFalse(failCallbackUsed)
        assertTrue(saveCallbackUsed)
        assertFalse(backCallbackUsed)
    }

    @Test
    fun testSaveDrawingFormGoBack() {
        // Context of the app under test.
        composeTestRule.setContent {
            SaveDrawingForm(viewModel = drawingViewModel, onBackButtonClicked = { backCallbackUsed = true }, onSuccessfulSave = { saveCallbackUsed = true }, onSaveFail = { failCallbackUsed = true})
        }
        val drawingName = "MyCoolDrawing"
        // Click the back button
        composeTestRule.onNodeWithText("Enter a name for your drawing:").performTextInput(drawingName)
        composeTestRule.onNodeWithText("Go back without saving").performClick()

        // Check the callback flags
        assertFalse(failCallbackUsed)
        assertFalse(saveCallbackUsed)
        assertTrue(backCallbackUsed)

        // Check if the drawing name that was being typed was cleared from the text input
        composeTestRule.onNodeWithText("Enter a name for your drawing:").assert(!hasText(drawingName))
    }


    @Test
    fun testLoadDrawingFormClickCallback() {
        // Context of the app under test.
        composeTestRule.setContent {
            SaveDrawingForm(viewModel = drawingViewModel, onBackButtonClicked = { backCallbackUsed = true }, onSuccessfulSave = { saveCallbackUsed = true }, onSaveFail = { failCallbackUsed = true})
            DrawingList(viewModel = drawingViewModel, onItemClick = {itemClickCallBackUsed = true})
        }

        //Save a drawing
        val drawingName = "MyCoolDrawing"
        composeTestRule.onNodeWithText("Enter a name for your drawing:").performTextInput(drawingName)
        composeTestRule.onNodeWithText("Save!").performClick()
        composeTestRule.onNodeWithText("Enter a name for your drawing:").performTextReplacement("")

        // Click the button with the drawing name
        composeTestRule.onNodeWithText(drawingName).performClick()
        assertTrue(itemClickCallBackUsed)
    }

    @Test
    fun testLoadDrawingFormShowsSavedDrawing() {
        // Context of the app under test.
        composeTestRule.setContent {
            SaveDrawingForm(viewModel = drawingViewModel, onBackButtonClicked = { backCallbackUsed = true }, onSuccessfulSave = { saveCallbackUsed = true }, onSaveFail = { failCallbackUsed = true})
            DrawingList(viewModel = drawingViewModel, onItemClick = {itemClickCallBackUsed = true})
        }

        //Save a drawing
        val drawingName = "MyCoolDrawing"
        composeTestRule.onNodeWithText("Enter a name for your drawing:").performTextInput(drawingName)
        composeTestRule.onNodeWithText("Save!").performClick()
        // Clear the text input
        composeTestRule.onNodeWithText("Enter a name for your drawing:").performTextReplacement("")

        // Check if the drawing name that was saved is a node
        composeTestRule.onNodeWithText(drawingName).assertIsDisplayed()
    }

    @Test
    fun testLoadDrawingFormDoesNotShowDuplicateNames() {
        var clickCounter = 0
        // Context of the app under test.
        composeTestRule.setContent {
            SaveDrawingForm(
                viewModel = drawingViewModel,
                onBackButtonClicked = { backCallbackUsed = true },
                onSuccessfulSave = { saveCallbackUsed = true },
                onSaveFail = { failCallbackUsed = true })
            DrawingList(viewModel = drawingViewModel, onItemClick = { clickCounter++ })
        }

        // Save a drawing
        val drawingName = "MyCoolDrawing"
        composeTestRule.onNodeWithText("Enter a name for your drawing:")
            .performTextInput(drawingName)
        composeTestRule.onNodeWithText("Save!").performClick()
        // Save the same drawing again
        composeTestRule.onNodeWithText("Enter a name for your drawing:").performTextReplacement("")
        composeTestRule.onNodeWithText("Enter a name for your drawing:")
            .performTextInput(drawingName)
        composeTestRule.onNodeWithText("Save!").performClick()
        // Clear the text input
        composeTestRule.onNodeWithText("Enter a name for your drawing:").performTextReplacement("")

        // Click the button with the given drawing name
        composeTestRule.onNodeWithText(drawingName).performClick()

        // Check that just one click occurred
        assertEquals(1, clickCounter)
    }

    @Test
    fun testSaveAndLoadWithViewModel() {
        runBlocking {
            // Context of the app under test.
            composeTestRule.setContent {
                SaveDrawingForm(
                    viewModel = drawingViewModel,
                    onBackButtonClicked = { backCallbackUsed = true },
                    onSuccessfulSave = { saveCallbackUsed = true },
                    onSaveFail = { failCallbackUsed = true })
                DrawingList(
                    viewModel = drawingViewModel,
                    onItemClick = { itemClickCallBackUsed = true })
            }

            val lifecycleOwner = TestLifecycleOwner()

            lifecycleOwner.run {
                withContext(Dispatchers.Main) {
                    drawingViewModel.drawScribble(1f, 17f, 200f, 300f)
                }
            }

            val bitmap = drawingViewModel.bitmap.value!!
            composeTestRule.onNodeWithText("Enter a name for your drawing:")
                .performTextReplacement("myScribble")
            composeTestRule.onNodeWithText("Save!").performClick()
            // Check that the bitmap is unchanged by saving
            assertTrue(bitmap.sameAs(drawingViewModel.bitmap.value!!))

            lifecycleOwner.run {
                withContext(Dispatchers.Main) {
                    // Clear the bitmap
                    drawingViewModel.newBitmap()
                }
            }

            // Check that the bitmap is cleared
            assertFalse(bitmap.sameAs(drawingViewModel.bitmap.value!!))
            //Load the drawing back in
            composeTestRule.onNodeWithText("Enter a name for your drawing:")
                .performTextReplacement("")
            composeTestRule.onNodeWithText("myScribble").performClick()

            // Check that the bitmap is back
            assertTrue(bitmap.sameAs(drawingViewModel.bitmap.value!!))
        }
    }
}