package com.example.dreamdoodler

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * This enum represents the various modes a user can be drawing in.
 *
 * SCRIBBLE means the user is drawing organic scribbles as they drag their finger across the
 *        screen
 * CIRCLE means the user is creating a circle
 * SQUARE means the user is creating a rectangle
 */
enum class DrawingMode {
    SCRIBBLE, CIRCLE, SQUARE
}

/**
 * This class is a ViewModel that holds the data for the drawing canvas.
 * It is used in the DrawingCanvasFragment.
 */
class DrawingViewModel(
    private val repository: DrawingRepository
) : ViewModel() {

    // The bitmap and canvas that the user will draw on
    private val _bitmap : MutableLiveData<Bitmap> = MutableLiveData(Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888))
    private val _canvas : MutableLiveData<Canvas> = MutableLiveData(Canvas(_bitmap.value!!))

    // Brush size and mode data
    private var drawSizeMultiplier: Int = 1
    private var drawSize: Float = 0.005f*_bitmap.value!!.width * drawSizeMultiplier
    private var drawingMode: DrawingMode = DrawingMode.SCRIBBLE

    // Paint object for drawing
    private val _paint : MutableLiveData<Paint> = MutableLiveData(Paint().apply {
        color = Color.BLACK
        strokeWidth = drawSize
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    })

    // Saving and loading
    // The name of the last drawing that was saved or loaded
    private var _prevDrawingName : MutableStateFlow<String> = MutableStateFlow("")
    val prevDrawingName : StateFlow<String> = _prevDrawingName.asStateFlow()

    val allDrawingPaths = repository.allDrawingPaths

    // Part of ViewModel that is exposed to the view
    val bitmap = _bitmap as LiveData<Bitmap>
    val paint = _paint as LiveData<Paint>

    // For image adjustments
    private val _previewBitmap : MutableStateFlow<Bitmap> = MutableStateFlow(_bitmap.value!!)
    val previewBitmap = _previewBitmap as StateFlow<Bitmap>
    private var colorsInverted = false;
    private var brightnessLevel = 1f;
    private var noiseLevel = 0;


    // Track if the canvas has been modified
    private var _isCanvasModified : MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isCanvasModified : StateFlow<Boolean> = _isCanvasModified.asStateFlow()

    // c++ functionality wrapper
    private val wrapper = NativeWrappers();

    /**
     * Initializes the bitmap with a white background
     */
    init {
        _canvas.value?.drawRect(
            0f,
            0f,
            _bitmap.value!!.width.toFloat(),
            _bitmap.value!!.height.toFloat(),
            Paint().apply {
                color = Color.WHITE
            }
        )
    }

    /**
     * Updates the color of the paint object
     * @param color The new color to set the paint object to
     */
    @RequiresApi(Build.VERSION_CODES.O)
    public fun updatePaintColor(color: Color) {
        _paint.value?.color = color.toArgb()
        _paint.value = _paint.value
    }

    /**
     * Updates the size of the brush
     * @param width The new width to set the paint object to
     */
    public fun updateDrawSize(width: Int) {
        drawSizeMultiplier = width
        drawSize = 0.005f*_bitmap.value!!.width * drawSizeMultiplier
        _paint.value?.strokeWidth = drawSize
        _paint.value = _paint.value
    }

    /**
     * Draws a line on the canvas when the user is in SCRIBBLE mode
     *
     * @param x1 The x coordinate of the start of the line
     * @param y1 The y coordinate of the start of the line
     * @param x2 The x coordinate of the end of the line
     * @param y2 The y coordinate of the end of the line
     */
    public fun drawScribble(x1: Float, y1: Float, x2: Float, y2: Float) {
        // Does nothing if the user is not in scribble mode
        if(drawingMode == DrawingMode.SCRIBBLE) {
            _paint.value?.strokeWidth = drawSize
            _canvas.value?.drawLine(x1, y1, x2, y2, _paint.value!!)
            _canvas.value = _canvas.value
            _isCanvasModified.value = true
        }
    }

    /**
     * Draws a stamp on the canvas according to the selected shape when the
     * user is in CIRCLE or SQUARE mode
     *
     * @param centerX The x coordinate of the center of the stamp
     * @param centerY The y coordinate of the center of the stamp
     */
    public fun drawStamp(centerX: Float, centerY: Float) {
        if(drawingMode == DrawingMode.SCRIBBLE) {
            // Does nothing if the user is in scribble mode
            return;
        }

        if(drawingMode == DrawingMode.CIRCLE) {
            _canvas.value?.drawCircle(centerX, centerY, drawSize/2, _paint.value!!)
        }
        else if(drawingMode == DrawingMode.SQUARE) {
            val topLeftX: Float = centerX - drawSize / 2
            val topLeftY: Float = centerY - drawSize / 2
            val bottomRightX: Float = topLeftX + drawSize
            val bottomRightY: Float = topLeftY + drawSize
            _canvas.value?.drawRect(topLeftX, topLeftY, bottomRightX, bottomRightY, _paint.value!!)
        }
        _canvas.value = _canvas.value
        _isCanvasModified.value = true
    }

    /**
     * Cycles the drawing mode to the next mode in the enum
     */
    public fun cycleDrawingMode() {
        drawingMode = when (drawingMode) {
            DrawingMode.SCRIBBLE -> {
                DrawingMode.CIRCLE
            }

            DrawingMode.CIRCLE -> {
                DrawingMode.SQUARE
            }

            DrawingMode.SQUARE -> {
                DrawingMode.SCRIBBLE
            }
        }
    }

    /**
     * Returns the current drawing mode - i.e. what shape the pen is
     * @return The current drawing mode
     */
    public fun getDrawingMode() : DrawingMode {
        return drawingMode
    }

    /**
     * Get the current pen size. Note: this is a relative unit, the true
     * size of the pen will take the size of the bitmap into account.
     *
     * @return the size of the pen, from 0 - 100
     */
    public fun getDrawSize() : Int {
        return drawSizeMultiplier
    }

    /**
     * Saves the current bitmap to the path set by setDrawingNameInput, reflected in
     * the variable drawingInput.
     * The path where the bitmap was saved to will be reflected in prevDrawingName
     * @return True if the save was successful, false otherwise
     */
    public fun saveDrawing(newDrawingName: String) : Boolean {
        _prevDrawingName.value = newDrawingName
        _isCanvasModified.value = false
        return repository.saveDrawing(_prevDrawingName.value, _bitmap.value!!)
    }

    /**
     * Loads a copy of the bitmap stored at the given path and sets as the current drawing.
     * The name of the loaded drawing will be stored so that the user can easily save to the
     * same place.
     */
    public fun loadBitmap(loadPath: String) {
        _prevDrawingName.value = loadPath
        _bitmap.value = repository.loadDrawing(loadPath)?.copy(Bitmap.Config.ARGB_8888, true)
        _canvas.value = Canvas(_bitmap.value!!)
    }

    /**
     * Creates a new empty bitmap (i.e. a blank drawing). This will overwrite any existing bitmap
     */
    public fun newBitmap() {
        // Create a new bitmap
        _bitmap.value = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
        _canvas.value = Canvas(_bitmap.value!!)

        // Clear with a white background
        _canvas.value?.drawColor(Color.WHITE)

        // Clear the name of the previous drawing since we don't want to save to its path
        _prevDrawingName.value = ""

        _isCanvasModified.value = false
    }

    /**
     * Resets any previewed adjustments to the bitmap. For example, this
     * method might be called when the user hits the back button on the
     * adjustments fragment to cancel changes to brightness or colors
     */
    public fun resetPreviewAdjustments() {
        colorsInverted = false
        brightnessLevel = 1f
        noiseLevel = 0
        _previewBitmap.value = _bitmap.value!!;
    }

    /**
     * Adjusts the brightness of the bitmap that the user is previewing changes
     * on. These changes won't affect the original bitmap until
     * commitPreviewChanges is called
     *
     * @param brightnessChange the factor to increase brightness by. Should range from 0 to 2
     *                         where values below 1 are dimmer and values above 1 are brighter
     *                         than the original
     */
    public fun adjustPreviewBrightness(brightnessChange: Float) {
        brightnessLevel = brightnessChange
        previewAllAdjustments()
    }

    /**
     * Inverts the colors of the bitmap that the user is previewing changes
     * on. These changes won't affect the original bitmap until
     * commitPreviewChanges is called
     */
    public fun invertPreviewColors() {
        colorsInverted = !colorsInverted;
        previewAllAdjustments()
    }

    /**
     * Adds noise to the preview bitmap by randomly shifting the pixels according to the
     * requested noise level
     *
     * @param brightnessChange the factor to increase noise by. Should range from 0 to 255
     *                         where values near 255 will make the bitmap unrecognizable
     */
    public fun previewNoise(noiseLevel: Int) {
        this.noiseLevel = noiseLevel
        previewAllAdjustments()
    }

    /**
     * Applies all previewable adjustments to make sure they are in sync with one another
     */
    private fun previewAllAdjustments() {
        // Copy the original bitmap to add a new noise level
        val tempBitmap = _bitmap.value!!.copy(Bitmap.Config.ARGB_8888, true);

        // Add noise, if any
        if(noiseLevel > 0) {
            wrapper.noise(tempBitmap, noiseLevel)
        }

        // Add brightness changes, if any
        if(brightnessLevel != 1f) {
            wrapper.setBrightness(tempBitmap, brightnessLevel);
        }

        // Invert colors if needed
        if(colorsInverted) {
            wrapper.invertColors(tempBitmap);
        }

        // Show the changes on the preview bitmap
        _previewBitmap.value = tempBitmap;
    }

    /**
     * Commits any previewed adjustments (such as inverted colors or brightness
     * changes) to the original bitmap
     */
    public fun commitPreviewedAdjustments() {
        _bitmap.value = _previewBitmap.value;
        _canvas.value = Canvas(_bitmap.value!!)

        // put all instance variables tracking adjustments back to their
        // default values
        resetPreviewAdjustments()
    }

    fun setBitmap(bitmap: Bitmap) {
        _bitmap.value = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        _canvas.value = Canvas(_bitmap.value!!)
    }

}

class DrawingViewModelFactory(
    private val repository: DrawingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DrawingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DrawingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}