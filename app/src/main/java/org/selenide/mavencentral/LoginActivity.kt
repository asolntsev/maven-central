@file:OptIn(ExperimentalMaterial3Api::class)

package org.selenide.mavencentral

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.nexus.InvalidCredentials
import org.nexus.NexusClient
import org.selenide.mavencentral.ui.theme.MavencentralTheme

class LoginActivity : ComponentActivity() {
  private val repository by lazy { CredentialsRepository(applicationContext) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MavencentralTheme {
        Surface(
          color = MaterialTheme.colorScheme.background
        ) {
          LoginForm(this::onSubmit)
        }
      }
    }
  }

  private fun onSubmit(credentials: Credentials) {
    lifecycleScope.launch {
      val loggedIn = withContext(Dispatchers.IO) {
        checkCredentials(credentials)
      }
      withContext(Dispatchers.Main) {
        if (loggedIn) {
          repository.save(credentials)
          finish()
        }
      }
    }
  }

  private fun checkCredentials(credentials: Credentials): Boolean {
    Log.i("LoginActivity", "Checking $credentials in ${Thread.currentThread().name}...")
    val client = NexusClient(credentials.username, credentials.password)
    try {
      val sessionId = client.login()
      Log.i("LoginActivity", "Credentials are valid, sessionId=${sessionId}")
      return true
    } catch (e: InvalidCredentials) {
      Log.i("LoginActivity", "Login failed: $e")
      return false
    } catch (e: Exception) {
      Log.e("LoginActivity", "Login error", e)
      return false
    }
  }
}

@Composable
private fun LoginForm(onSubmit: (credentials: Credentials) -> Unit) {
  var username by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }

  Column {
    Text(text = "Login to Maven central!")
    OutlinedTextField(
      value = username,
      onValueChange = { username = it },
      label = { Text(text = "Username") }
    )
    OutlinedTextField(
      value = password,
      onValueChange = { password = it },
      label = { Text(text = "Password") }
    )
    Button(onClick = {
      val credentials = Credentials(username, password)
      onSubmit(credentials)
    }) {
      Text(text = "Login")
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
  MavencentralTheme {
    LoginForm { run {} }
  }
}