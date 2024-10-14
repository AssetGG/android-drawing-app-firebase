package com.example.dreamdoodler

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking

import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.funfactassignment.DrawingDao
import com.example.funfactassignment.DrawingDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import java.io.File


/**
 * Instrumented tests for Dream Doodler app.
 */
@RunWith(AndroidJUnit4::class)
class DrawingViewModelInstrumentedTest {

    private lateinit var drawingViewModel: DrawingViewModel
    private lateinit var repository: DrawingRepository
    private lateinit var dao: DrawingDao
    private lateinit var database: DrawingDatabase
    private lateinit var tempTestDir: File

    /**
     * Set up the test environment.
     *
     * Create an in-memory database and a temporary directory for testing.
     *
     * Created with reference to https://developer.android.com/training/data-storage/room/testing-db
     * and some suggestions from ChatGPT.
     */
    @Before
    fun setup() {
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

    /***
     * Test to make sure color is changed in the
     * view model when updatePaintColor is called
     */
    @Test
    fun changeColor() {
        runBlocking {
            val lifecycleOwner = TestLifecycleOwner()
            val before = drawingViewModel.paint.value!!.color
            var callBackFired = false;

            lifecycleOwner.run {
                withContext(Dispatchers.Main) {
                    // Make sure observer is notified when pain object is changed
                    drawingViewModel.paint.observe(lifecycleOwner) {
                        callBackFired = true
                    }
                    drawingViewModel.updatePaintColor(Color.valueOf(Color.RED))

                    // Make sure observer is notified when pain object is changed
                    assertTrue(callBackFired)
                    // Make sure color is changed
                    assertNotEquals(before, drawingViewModel.paint.value!!.color)
                    // Make sure color is correct
                    assertEquals(Color.RED, drawingViewModel.paint.value!!.color)
                }
            }
        }
    }

    /***
     * Test to make sure stroke width is changed in the
     * view model when updateDrawSize is called
     */
    @Test
    fun changeDrawSize() {
        runBlocking {
            val lifecycleOwner = TestLifecycleOwner()
            val before = drawingViewModel.paint.value!!.strokeWidth.toInt()
            var callBackFired = false;

            lifecycleOwner.run {
                withContext(Dispatchers.Main) {
                    // Make sure observer is notified when pain object is changed
                    drawingViewModel.paint.observe(lifecycleOwner) {
                        callBackFired = true
                    }
                    drawingViewModel.updateDrawSize(5)

                    // Make sure observer is notified when pain object is changed
                    assertTrue(callBackFired)
                    // Make sure draw size is changed
                    assertNotEquals(before, drawingViewModel.paint.value!!.strokeWidth.toInt())
                    // Make sure draw size is correct
                    assertEquals(5 * before, drawingViewModel.paint.value!!.strokeWidth.toInt())
                }
            }
        }
    }

    /***
     * Test to make sure drawing mode is changed in the
     * view model when cycleDrawingMode is called
     */
    @Test
    fun changeDrawMode() {
        runBlocking {
            val lifecycleOwner = TestLifecycleOwner()

            lifecycleOwner.run {
                withContext(Dispatchers.Main) {
                    // Toggle to circle
                    drawingViewModel.cycleDrawingMode()
                    assertEquals(DrawingMode.CIRCLE.toString(), drawingViewModel.getDrawingMode().toString())

                    // Toggle to square
                    drawingViewModel.cycleDrawingMode()
                    assertEquals(DrawingMode.SQUARE.toString(), drawingViewModel.getDrawingMode().toString())

                    // Toggle to scribble
                    drawingViewModel.cycleDrawingMode()
                    assertEquals(DrawingMode.SCRIBBLE.toString(), drawingViewModel.getDrawingMode().toString())
                }
            }
        }
    }

    /**
     * Test to make sure the drawing is saved correctly
     */
    @Test
    fun testSave() {
        runBlocking {
            val lifecycleOwner = TestLifecycleOwner()

            val testPath = "test"

            lifecycleOwner.run {
                withContext(Dispatchers.Main) {
                    drawingViewModel.saveDrawing(testPath)

                    // Make sure database has right amount of filePaths
                    val drawingPathsList = drawingViewModel.allDrawingPaths.firstOrNull()
                    assertEquals(1, drawingPathsList?.size)

                    // Make sure file path was added correctly
                    val actualPath = drawingPathsList?.firstOrNull()?.filePath
                    assertEquals(testPath, actualPath)

                    // Make sure file is saved to the file directory
                    assertTrue(File(tempTestDir, testPath).exists())
                }
            }
        }
    }

    /**
     * Test to make sure multiple drawings are saved correctly
     */
    @Test
    fun testSaveMultiple() {
        runBlocking {
            val lifecycleOwner = TestLifecycleOwner()

            val pathList : List<String> = listOf("test1", "test2", "test3", "test4", "test5")

            lifecycleOwner.run {
                withContext(Dispatchers.Main) {
                    // Save each file to a path
                    for (path in pathList) {
                        drawingViewModel.saveDrawing(path)
                    }

                    // Make sure database has right amount of filePaths
                    val drawingPathsList = drawingViewModel.allDrawingPaths.firstOrNull()
                    assertEquals(5, drawingPathsList?.size)

                    for (i in pathList.indices) {
                        // Make sure file path was added correctly
                        val actualPath = drawingPathsList?.get(i)?.filePath
                        assertEquals(pathList[i], actualPath)

                        // Make sure file is saved to the file directory
                        assertTrue(File(tempTestDir, pathList[i]).exists())
                    }
                }
            }
        }
    }

    /**
     * Test to make sure the drawing is loaded correctly
     */
    @Test
    fun testLoad() {
        runBlocking {
            val lifecycleOwner = TestLifecycleOwner()

            // Set up a test bitmap to compare saved/loaded bitmap to
            val bitmap = drawingViewModel.bitmap.value!!
            val comparisonBitmap = bitmap.copy(bitmap.config, bitmap.isMutable)
            val testCanvas = Canvas(comparisonBitmap)
            val testPaint = drawingViewModel.paint.value!!
            testCanvas.drawLine(0F, 0F, 10F, 10F, testPaint)

            val testPath = "test"

            lifecycleOwner.run {
                withContext(Dispatchers.Main) {

                    drawingViewModel.drawScribble(0F, 0F, 10F, 10F)

                    drawingViewModel.saveDrawing(testPath)

                    // Make sure the file is loaded correctly
                    drawingViewModel.loadBitmap(testPath)

                    // Make sure bitmap loaded is what is expected
                    assertTrue(bitmap.sameAs(repository.loadDrawing(testPath)))
                }
            }
        }
    }
}