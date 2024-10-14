package com.example.dreamdoodler

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * This class is a custom view that allows the user to draw on the screen.
 * It is used in the DrawingPageFragment.
 */
class DrawingCanvasView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    // The rectangle that the bitmap will be drawn in
    private val rect: Rect by lazy {Rect(0,0, if (resources.getBoolean(R.bool.isLandscape)) height else width, if (resources.getBoolean(R.bool.isLandscape)) height else width)}

    // Last x and y coordinates of the user touch for line drawing
    private var lastX: Float = 0f
    private var lastY: Float = 0f

    private var viewModelGetter: () -> DrawingViewModel? = {null}

    public fun setViewModelGetter(getter: () -> DrawingViewModel?) {
        viewModelGetter = getter
    }

    /**
     * This function is called when the user touches the screen
     * and handles drawing logic to the canvas.
     * @param event The MotionEvent that triggered this function
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val viewModel: DrawingViewModel = viewModelGetter() ?: return false

        // Convert screen coordinates to bitmap coordinates
        val bitmapToScreenScale: Float

        if(!resources.getBoolean(R.bool.isLandscape)) {
            // Convert screen coordinates to bitmap coordinates
            bitmapToScreenScale = viewModel.bitmap.value!!.width.toFloat() / width
        }
        else {
            // Convert screen coordinates to bitmap coordinates
            bitmapToScreenScale = viewModel.bitmap.value!!.width.toFloat() / height
        }
        val bitmapX = event.x * bitmapToScreenScale
        val bitmapY = event.y * bitmapToScreenScale

        // On an event action, track the users touch or drag action
        when (event.action) {
            // Initial touch
            MotionEvent.ACTION_DOWN -> {
                // Stamps are drawn when the user taps.
                viewModel.drawStamp(bitmapX, bitmapY)

                // Scribbles are drawn when the user taps as well.
                // The viewModel will ignore drawScribble when not in scribble mode.
                viewModel.drawScribble(bitmapX, bitmapY, bitmapX, bitmapY)

                lastX = bitmapX
                lastY = bitmapY
            }
            // Continue draw on dragging
            MotionEvent.ACTION_MOVE -> {
                // Scribbles are drawn when the user drags. If the user is not in
                // SCRIBBLE mode, the viewModel will do nothing
                viewModel.drawScribble(lastX, lastY, bitmapX, bitmapY)

                lastX = bitmapX
                lastY = bitmapY
            }
        }

        // Refresh canvas
        invalidate()

        return true
    }

    /**
     * Draws the bitmap to the canvas upon view invalidation
     * @param canvas View canvas
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val viewModel: DrawingViewModel = viewModelGetter() ?: return
        return canvas.drawBitmap(viewModel.bitmap.value!!, null, rect, viewModel.paint.value!!)
     }
}