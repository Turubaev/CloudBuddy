package dev.catandbunny.cloudbuddy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.catandbunny.cloudbuddy.app.CloudBuddyApp
import dev.catandbunny.cloudbuddy.ui.theme.CloudBuddyTheme
import ru.rustore.sdk.pay.RuStorePayClient
import ru.rustore.sdk.pay.model.SdkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        proceedRuStoreIntent(intent)
        setContent {
            CloudBuddyTheme {
                CloudBuddyApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        proceedRuStoreIntent(intent)
    }

    private fun proceedRuStoreIntent(intent: Intent?) {
        if (BuildConfig.RUSTORE_CONSOLE_APP_ID.isNotBlank()) {
            runCatching { RuStorePayClient.instance.getIntentInteractor().proceedIntent(intent, SdkTheme.LIGHT) }
        }
    }
}
