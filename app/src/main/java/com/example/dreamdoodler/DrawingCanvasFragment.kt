package com.example.dreamdoodler

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.dreamdoodler.databinding.FragmentDrawingCanvasBinding

/**
 * Fragment to hold the drawing canvas custom view.
 */
class DrawingCanvasFragment : Fragment() {
    private lateinit var binding: FragmentDrawingCanvasBinding

    // Last x and y coordinates of the user touch for line drawing
    private var lastX: Float = 0f
    private var lastY: Float = 0f

    val viewModel: DrawingViewModel by activityViewModels{
        DrawingViewModelFactory((requireActivity().application as DrawingApplication).repository)}

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDrawingCanvasBinding.inflate(inflater)
        binding.customView.setViewModelGetter { viewModelGetter() }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bitmap.observe(viewLifecycleOwner) { bitmap ->
            // Invalidate the custom view so it will redraw with the new bitmap
            binding.customView.invalidate()
        }
    }

    private fun viewModelGetter() : DrawingViewModel {
        return this.viewModel
    }

    private fun getBitmap(): Bitmap {
        return viewModel.bitmap.value!!
    }
}