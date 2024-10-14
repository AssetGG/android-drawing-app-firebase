package com.example.dreamdoodler

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.dreamdoodler.databinding.ActivityMainBinding



//Help with the viewModel provider lookup stuff
//this traces a "context" object up via its parent until it finds
//the activity
internal fun Context.findActivity(): ComponentActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Permissions should be called in the context of an Activity")
}

/**
 * The main activity of our Dream Doodler app - handles showing a splash.
 * The main functionality of this app is handled in drawingPageFragment
 */
class MainActivity : AppCompatActivity() {
    // Dynamically loads our c++ library
    companion object {
      init {
         System.loadLibrary("dreamdoodler")
      }
    }

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    // The viewModel
    private val drawingViewModel: DrawingViewModel by viewModels{
        DrawingViewModelFactory((application as DrawingApplication).repository)
    }

    /**
     * Initializes the app, splash screen and sets the content view.
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initiate the custom Splash Screen
        installSplashScreen()

        setContentView(R.layout.activity_main)
    }
}