package dev.catandbunny.cloudbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.catandbunny.cloudbuddy.app.CloudBuddyApp
import dev.catandbunny.cloudbuddy.ui.theme.CloudBuddyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CloudBuddyTheme {
                CloudBuddyApp()
            }
        }
    }
}
