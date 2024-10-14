package com.example.dreamdoodler

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import com.example.dreamdoodler.databinding.FragmentDrawingSelectBinding

/**
 * A simple [Fragment] subclass.
 * Use the [DrawingSelectFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DrawingSelectFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDrawingSelectBinding.inflate(inflater)

        binding.drawingSelectComposeView.setContent {
            DrawingSelectScreenContainer(
                onBackClicked = {
                    findNavController().popBackStack()
                },
                onDrawingPathItemClicked = {
                    findNavController().popBackStack()
                }
            )
        }

        return binding.root
    }

}

@Composable
fun DrawingSelectScreenContainer(
    onBackClicked: ()->Unit,
    onDrawingPathItemClicked: ()->Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        BackButton(onClick = onBackClicked)
        DrawingList(onItemClick = onDrawingPathItemClicked)
    }
}

@Composable
fun BackButton(
    onClick: ()->Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = "Back", modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun DrawingList(modifier: Modifier = Modifier,
                viewModel: DrawingViewModel = viewModel(
                 viewModelStoreOwner = LocalContext.current.findActivity()
                ),
                onItemClick: ()->Unit) {

    val list by viewModel.allDrawingPaths.collectAsState(listOf())
    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (drawingPath in list) {
            item {
                DrawingPathItem(drawingPath = drawingPath.filePath) {
                    viewModel.loadBitmap(drawingPath.filePath)
                    onItemClick()
                }
            }
        }
    }
}

@Composable
fun DrawingPathItem(drawingPath: String, onItemClick: ()->Unit) {
    Button(
        onClick = onItemClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = drawingPath, modifier = Modifier.padding(16.dp))
    }
}