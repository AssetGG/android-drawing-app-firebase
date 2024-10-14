package com.example.dreamdoodler

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import com.example.dreamdoodler.databinding.FragmentAdjustmentsBinding

class AdjustmentsFragment : Fragment() {
    private val viewModel: DrawingViewModel by activityViewModels {
        DrawingViewModelFactory((requireActivity().application as DrawingApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentAdjustmentsBinding.inflate(layoutInflater);

        // Reset the previewed bitmap each time this fragment is opened
        viewModel.resetPreviewAdjustments();

        binding.saveDrawingComposeView.setContent {
            AdjustImageComponent(
                onBackButtonClicked = { findNavController().popBackStack() },
            )
        }


        return binding.root;
    }

}

@Composable
public fun AdjustImageComponent(
    modifier: Modifier = Modifier,
    viewModel: DrawingViewModel = viewModel(
        viewModelStoreOwner = LocalContext.current.findActivity()
    ),
    onBackButtonClicked: () -> Unit,
) {
    Column(modifier = modifier.padding(32.dp)) {

        Text(text ="Image Adjustments:",
            fontSize = 20.sp
            )

        val previewBitmap by viewModel.previewBitmap.collectAsState()
        var brightnessSliderPosition by remember { mutableFloatStateOf(1f) }
        var noiseSliderPosition by remember { mutableFloatStateOf(0f) }


        Image(
            bitmap = previewBitmap.asImageBitmap(),
            contentDescription = "Preview of adjusted drawing"
        );

        BasicText(
            text = "Change Brightness:"
        )
        Slider(
            value = brightnessSliderPosition,
            valueRange = 0f.. 2f,
            onValueChange = {
                brightnessSliderPosition = it
                viewModel.adjustPreviewBrightness(it);
            }
        )

        BasicText(
            text = "Add noise:"
        )
        Slider(
            value = noiseSliderPosition,
            valueRange = 0f.. 250f,
            steps = 10,
            onValueChange = {
                noiseSliderPosition = it
                viewModel.previewNoise((it).toInt());
            }
        )

        // Colors are set up this way so that the button will flip back and forth when the user clicks the button
        val buttonRGB = intArrayOf(75, 41, 135)
        var buttonColor by remember{mutableStateOf(Color(buttonRGB[0], buttonRGB[1], buttonRGB[2], 255))}
        var invertedButtonColor by remember{mutableStateOf(Color(255 - buttonRGB[0], 255 - buttonRGB[1], 255 - buttonRGB[2], 255))}
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight()
        ) {
            ElevatedButton(
                onClick = {
                    // invert the bitmap
                    viewModel.invertPreviewColors()
                    // Swap the preview colors
                    val temp = buttonColor
                    buttonColor = invertedButtonColor
                    invertedButtonColor = temp
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = invertedButtonColor
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { Text("Invert Colors", fontWeight = FontWeight.Bold) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                OutlinedButton(
                    onClick = {
                        // Don't save changes when the user clicks "Go Back"
                        viewModel.resetPreviewAdjustments()
                        onBackButtonClicked()
                    }
                )
                {
                    Text("Go Back")
                }

                FilledTonalButton(
                    onClick = {
                        // Save changes when the user clicks "confirm", then go back
                        // to the drawing screen
                        viewModel.commitPreviewedAdjustments()
                        onBackButtonClicked()
                    }
                )
                {
                    Text("Confirm Choices")
                }
            }
        }

    }
}
