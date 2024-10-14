package com.example.dreamdoodler

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.dreamdoodler.databinding.FragmentDrawingSelectBinding
import com.example.dreamdoodler.databinding.FragmentSettingsAccountBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

/**
 * Fragment to display account information
 * and allow the user to log in or out
 */
class SettingsAccountFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSettingsAccountBinding.inflate(inflater)

        binding.settingsAccountComposeView.setContent {
            AccountInfo(findNavController())
        }

        return binding.root
    }
}

/**
 * Composable function to display account information
 * and allow the user to log in or out
 */
@Composable
fun AccountInfo (
    navController: NavController
) {
    var user by remember { mutableStateOf(Firebase.auth.currentUser) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            "My Account",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(20.dp))

        // If the user is logged in, display their email and a log out button
        if (user != null) {
            Text("Logged in as ${user!!.email}")
            Spacer(modifier = Modifier.height(15.dp))
            Button(onClick = {
                Firebase.auth.signOut()
                user = null
            }) {
                Text("Log out")
            }
        }

        // If the user is not logged in, display a message and a log in button
        else {
            Text("Not logged in.")
            Spacer(modifier = Modifier.height(15.dp))
            Button(onClick = {
                navController.navigate(R.id.toLoginRegister)
            }) {
                Text("Log in")
            }
        }

        // Back button
        Spacer(modifier = Modifier.height(5.dp))
        Button(onClick = {
            navController.popBackStack()
        }) {
            Text("Back")
        }
    }
}