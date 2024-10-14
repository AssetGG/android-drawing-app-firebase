package com.example.dreamdoodler

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar

/**
 * A simple fragment that uses the skydoves color picker API to let the user select a color.
 * The selected color will be passed to the viewModel and this dialog will close.
 */
class ColorPickerDialogFragment : DialogFragment() {
    private val viewModel: DrawingViewModel by activityViewModels{
        DrawingViewModelFactory((requireActivity().application as DrawingApplication).repository)}

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.color_picker_dialog, null)
        val colorPickerView = view.findViewById<ColorPickerView>(R.id.colorPickerView)
        val brightnessSlideBar = view.findViewById<BrightnessSlideBar>(R.id.brightnessSlide)

        colorPickerView.attachBrightnessSlider(brightnessSlideBar)

        // Set up the Dialog
        return AlertDialog.Builder(requireContext()).apply {
            setTitle("Choose a color")
            setView(view)
            setPositiveButton("OK") { dialog, which ->
                val selectedColor = Color.valueOf(colorPickerView.color)
                viewModel.updatePaintColor(selectedColor)
            }
            setNegativeButton("Cancel", null)
        }.create()
    }
}