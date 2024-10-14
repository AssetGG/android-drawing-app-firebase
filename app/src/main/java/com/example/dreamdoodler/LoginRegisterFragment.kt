package com.example.dreamdoodler

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.dreamdoodler.databinding.FragmentDrawingSelectBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream
import java.util.Date

/**
 * Fragment to handle user login and registration
 */
class LoginRegisterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDrawingSelectBinding.inflate(inflater)
        val activity = activity as MainActivity

        binding.drawingSelectComposeView.setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                LoginPage(activity, findNavController())
            }
        }

        return binding.root
    }
}

/**
 * Composable function to display the login and registration page
 */
@Composable
fun LoginPage (
    activity: MainActivity,
    navController: NavController
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf("") }
        var buttonsEnabled by remember { mutableStateOf(false) }

        val buttonWidth = 200.dp

        Spacer(modifier = Modifier.height(100.dp))

        // Display error message if there is one
        Text(
            text = errorMessage,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFFCC0000)
            )
        )
        if (errorMessage.isNotEmpty())
            Spacer(modifier = Modifier.height(10.dp))

        // Text fields for email and password
        OutlinedTextField(
            value = email,
            onValueChange =
            {
                email = it
                buttonsEnabled = email.isNotEmpty() && password.isNotEmpty()
            },
            label = { Text("Email") })
        OutlinedTextField(
            value = password,
            onValueChange =
            {
                password = it
                buttonsEnabled = email.isNotEmpty() && password.isNotEmpty()
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        // Buttons to log in and sign up
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )  {
            Button(onClick = {
                Firebase.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(activity) { task ->
                        if (task.isSuccessful) {
                            navController.popBackStack()
                        } else {
                            errorMessage = getErrorMessage(task.exception)
                            Log.e("Login error", "${task.exception}")
                        }
                    }
                },
                enabled = buttonsEnabled,
                modifier = Modifier.width(buttonWidth)
            ) {
                Text("Log In")
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Not registered? Sign up with the information above:",
                fontStyle = FontStyle.Italic,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                Firebase.auth.createUserWithEmailAndPassword(
                    email,
                    password
                )
                    .addOnCompleteListener(activity) { task ->
                        if (task.isSuccessful) {
                            navController.popBackStack()
                        } else {
                            errorMessage = getErrorMessage(task.exception)
                            Log.e("Create user error", "${task.exception}")
                        }
                    }
                },
                enabled = buttonsEnabled,
                modifier = Modifier.width(buttonWidth)
            ) {
                Text("Sign Up")
            }
        }
    }
}

/**
 * Get a human-readable error message from a Firebase exception
 *
 * Coded with limited help from ChatGPT to figure out how to
 * get the right exception on which to base the error message.
 *
 * @param exception The exception to get the error message from
 */
fun getErrorMessage(exception: Exception?): String {
    return when (exception) {
        is FirebaseAuthInvalidUserException -> "User not found. Please check your email."
        is FirebaseAuthInvalidCredentialsException -> {
            when {
                exception.message?.contains("The email address is badly formatted.") == true -> "Invalid email address format."
                exception.message?.contains("password") == true -> "Password must be at least 6 characters long."
                else -> "Username and/or password supplied is incorrect."
            }
        }
        is FirebaseAuthUserCollisionException -> "Email already in use. Please log in."
        else -> "An error occurred. Please try again later."
    }
}