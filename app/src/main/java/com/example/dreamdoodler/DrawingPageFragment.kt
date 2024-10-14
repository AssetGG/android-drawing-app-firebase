package com.example.dreamdoodler

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.dreamdoodler.databinding.FragmentDrawingPageBinding

/**
 * Fragment to hold the drawing canvas
 * and all drawing tools controls
 */
class DrawingPageFragment : Fragment(), OnSeekBarChangeListener {

    private val viewModel: DrawingViewModel by activityViewModels{
        DrawingViewModelFactory((requireActivity().application as DrawingApplication).repository)}
    private lateinit var binding: FragmentDrawingPageBinding

    /**
     * Called at time of fragment creation.
     * Initializes tool controls and sets up listeners.
     * @param inflater
     * @param container
     * @param savedInstanceState
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDrawingPageBinding.inflate(inflater)

        // Set up the penSizeSlider.
        binding.penSizeSlider.min = 1
        binding.penSizeSlider.max = 100

        // Set this fragment as a listener for the pen size slider
        binding.penSizeSlider.setOnSeekBarChangeListener(this)

        // Update all button text and labels to match what's in the view model
        matchUIToViewModel()

        // Set up the shape changing button
        binding.penShapeButton.setOnClickListener{
            viewModel.cycleDrawingMode()
            if(viewModel.getDrawingMode() == DrawingMode.SCRIBBLE) {
                binding.penShapeButton.text = getString(R.string.button_scribble_mode)
                binding.penSizeLabel.text = getString(R.string.size_slider_scribble)
            }
            else if(viewModel.getDrawingMode() == DrawingMode.CIRCLE) {
                binding.penShapeButton.text = getString(R.string.button_circle_mode)
                binding.penSizeLabel.text = getString(R.string.size_slider_circle)
            }
            else if(viewModel.getDrawingMode() == DrawingMode.SQUARE) {
                binding.penShapeButton.text = getString(R.string.button_square_mode)
                binding.penSizeLabel.text = getString(R.string.size_slider_square)
            }
        }

        // Show the Color Wheel Dialog on click
        binding.colorPickButton.setOnClickListener {
            findNavController().navigate(R.id.launchColorPickerDialog)
        }

        // Change the color of the "Pick Color" button to represent current selection
        viewModel.paint.observe(viewLifecycleOwner) { paint ->
            binding.colorPickButton.setBackgroundColor(paint.color)
        }

        binding.loadButton?.setOnClickListener {
            findNavController().navigate(R.id.toSelectDrawing)
        }

        binding.saveButton?.setOnClickListener{
            findNavController().navigate(R.id.openSaveFragment)
        }

        binding.newButton?.setOnClickListener {
            val isUnsaved = viewModel.isCanvasModified.value

            if (isUnsaved) {
                AlertDialog.Builder(context)
                    .setTitle("Unsaved Changes")
                    .setMessage("You have unsaved changes. Are you sure you want to create a new drawing?")
                    .setPositiveButton("Yes") { dialog, which ->
                        viewModel.newBitmap()
                    }
                    .setNegativeButton("No", null)
                    .show()
            } else {
                viewModel.newBitmap()
            }
        }

        binding.uploadButton?.setOnClickListener {
            findNavController().navigate(R.id.openGalleryFragment)
        }

        binding.adjustButton?.setOnClickListener {
            findNavController().navigate(R.id.openAdjustments)
        }

        binding.settingButton?.setOnClickListener {
            findNavController().navigate(R.id.toSettings)
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    /**
     * Updates UI Components to match what is stored in the view model
     */
    private fun matchUIToViewModel() {
        // Set the position of the slider to match the drawSize in the viewModel
        binding.penSizeSlider.progress = viewModel.getDrawSize()

        // Set the slider and pen shape labels to match the current pen shape
        if(viewModel.getDrawingMode() == DrawingMode.SCRIBBLE) {
            binding.penShapeButton.text = getString(R.string.button_scribble_mode)
            binding.penSizeLabel.text = getString(R.string.size_slider_scribble)
        }
        else if(viewModel.getDrawingMode() == DrawingMode.CIRCLE) {
            binding.penShapeButton.text = getString(R.string.button_circle_mode)
            binding.penSizeLabel.text = getString(R.string.size_slider_circle)
        }
        else if(viewModel.getDrawingMode() == DrawingMode.SQUARE) {
            binding.penShapeButton.text = getString(R.string.button_square_mode)
            binding.penSizeLabel.text = getString(R.string.size_slider_square)
        }
    }

    // The below methods come from the OnSeekBarChangeListener interface. They are
    // used to track updates to the pen size seek bar (slider)

    /**
     * Notification that the progress level has changed. Clients can use the fromUser parameter
     * to distinguish user-initiated changes from those that occurred programmatically.
     *
     * @param seekBar The SeekBar whose progress has changed
     * @param progress The current progress level. This will be in the range min..max where min
     * and max were set by [ProgressBar.setMin] and
     * [ProgressBar.setMax], respectively. (The default values for
     * min is 0 and max is 100.)
     * @param fromUser True if the progress change was initiated by the user.
     */
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        viewModel.updateDrawSize(progress)
    }

    /**
     * Notification that the user has started a touch gesture. Clients may want to use this
     * to disable advancing the seekbar.
     * @param seekBar The SeekBar in which the touch gesture began
     */
    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        // This method is intentionally left empty
    }

    /**
     * Notification that the user has finished a touch gesture. Clients may want to use this
     * to re-enable advancing the seekbar.
     * @param seekBar The SeekBar in which the touch gesture began
     */
    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        // This method is intentionally left empty
    }
}