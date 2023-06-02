package org.selenide.mavencentral

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.tooling.preview.Preview
import org.nexus.Timeline
import org.selenide.mavencentral.ui.theme.MavencentralTheme

class MainActivity : ComponentActivity() {
  private val model: MainActivityViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      MavencentralTheme {
        // A surface container using the 'background' color from the theme
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          MainView()
        }
      }
    }

    model.init()
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  private fun MainView() {
    val showLoginButton by model.showLoginButton.collectAsState()
    val showRefreshButton by model.showRefreshButton.collectAsState()
    val showLoadingInProgress by model.showLoadingInProgress.collectAsState()
    val downloads by model.downloads.collectAsState()
    val uniqueIPs by model.uniqueIPs.collectAsState()
    val loadingError by model.loadingError.collectAsState()

    Column {
      Greeting()
      if (showLoadingInProgress) {
        Text(text = "...", color = Blue)
        Text(text = "\uD83D\uDD04", color = Blue)
        Text(text = "...", color = Blue)
      }
      else {
        downloads?.let { TimelineView("Downloads", it) }
        uniqueIPs?.let { TimelineView("Unique IPs", it) }
        loadingError?.let {
          Text(text = "Failed to check downloads statistics: $loadingError", color = Red)
        }
        if (showRefreshButton) {
          Button(onClick = { model.refresh() }) {
            Text(text = "Refresh")
          }
        }
      }
      if (showLoginButton) {
        Button(onClick = {
          startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        }) {
          Text(text = "Login")
        }
      }
    }
  }
}

@Composable
private fun Greeting() {
  Column {
    Text(text = "Welcome to Nexus!")
  }
}

@Composable
private fun TimelineView(title: String, timeline: Timeline) {
  Column {
    Text(text = title)
    Text(text = "Last month: ${timeline.data.lastMonth()}")
    Text(text = "Prev month: ${timeline.data.previousMonth()}")
  }
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
  MavencentralTheme {
    Greeting()
  }
}