package com.example.dreamdoodler

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import com.example.dreamdoodler.databinding.FragmentSaveDrawingBinding


/**
 * A fragment that allows the user to save a drawing at a file path of their choosing
 */
class SaveDrawingFragment : Fragment() {

    private val viewModel: DrawingViewModel by activityViewModels {
        DrawingViewModelFactory((requireActivity().application as DrawingApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentSaveDrawingBinding.inflate(layoutInflater)

        // Add our composable functions to the composeView for this fragment
        binding.saveDrawingComposeView.setContent {
            SaveDrawingForm(
                Modifier.padding(16.dp),
                onBackButtonClicked = { findNavController().popBackStack() },
                onSuccessfulSave = { findNavController().popBackStack() },
                onSaveFail = {
                    Toast.makeText(
                        context,
                        "Something went wrong. Please make sure to enter a valid file path",
                        Toast.LENGTH_SHORT
                    ).show()
                },
            )
        }

        return binding.root
    }

}

@Composable
public fun SaveDrawingForm(
    modifier: Modifier = Modifier,
    viewModel: DrawingViewModel = viewModel(
        viewModelStoreOwner = LocalContext.current.findActivity()
    ),
    onBackButtonClicked: () -> Unit,
    onSuccessfulSave: () -> Unit,
    onSaveFail: () -> Unit
) {
    Column(modifier = modifier.padding(32.dp)) {

        //val drawingInput by viewModel.drawingNameInput.collectAsState()
        var drawingInput by remember {mutableStateOf(viewModel.prevDrawingName.value)}

        TextField(
            value = drawingInput,
            onValueChange = { drawingInput = it },
            label = {Text("Enter a name for your drawing:")}
        )
        Row() {
            OutlinedButton(
                // Use callbacks to determine what to do if there was a problem saving the drawing
                onClick = {
                    drawingInput = viewModel.prevDrawingName.value
                    onBackButtonClicked() }
            ) {
                Text("Go Back Without Saving")
            }
            Button(
                // Use callbacks to determine what to do if there was a problem saving the drawing
                onClick = { if (viewModel.saveDrawing(drawingInput)) onSuccessfulSave() else onSaveFail() }
            ) {
                Text("Save!")
            }
        }

    }
}

