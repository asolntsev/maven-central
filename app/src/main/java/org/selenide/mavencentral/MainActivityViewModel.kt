package org.selenide.mavencentral

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.nexus.InvalidCredentials
import org.nexus.NexusClient
import org.nexus.Timeline

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
  private companion object {
    private const val PROJECT_SELENIDE = "186c4c63cde8c"
    private const val GROUP_ID = "com.codeborne"
    private const val ARTIFACT_ID = "selenide"
  }

  private val _showLoginButton: MutableStateFlow<Boolean> = MutableStateFlow(false)
  private val _showRefreshButton: MutableStateFlow<Boolean> = MutableStateFlow(false)
  private val _showLoadingInProgress: MutableStateFlow<Boolean> = MutableStateFlow(false)
  private val _downloads: MutableStateFlow<Timeline?> = MutableStateFlow(null)
  private val _uniqueIPs: MutableStateFlow<Timeline?> = MutableStateFlow(null)
  private val _loadingError: MutableStateFlow<Exception?> = MutableStateFlow(null)
  private val repository by lazy { CredentialsRepository(application.applicationContext) }

  val showLoginButton: StateFlow<Boolean> = _showLoginButton
  val showRefreshButton: StateFlow<Boolean> = _showRefreshButton
  val showLoadingInProgress: StateFlow<Boolean> = _showLoadingInProgress
  val downloads: StateFlow<Timeline?> = _downloads
  val uniqueIPs: StateFlow<Timeline?> = _uniqueIPs
  val loadingError: StateFlow<Exception?> = _loadingError

  fun init() {
    refresh()
  }
  fun refresh() {
    _loadingError.value = null
    _showLoginButton.value = false
    _showRefreshButton.value = false
    _showLoadingInProgress.value = true

    viewModelScope.launch {
      val credentials = readCredentials()
      if (credentials == null) {
        Log.i("MainActivity", "No credentials :(")
        _showLoginButton.value = true
        _showLoadingInProgress.value = false
      } else {
        Log.i(
          "MainActivity",
          "Check statistics for '" + credentials.username + "' in thread '" + Thread.currentThread().name + "'"
        )

        try {
          val (downloads, uniqueIPs) = checkStatistics(credentials)
          _downloads.value = downloads
          _uniqueIPs.value = uniqueIPs
          _showRefreshButton.value = true
        } catch (e: InvalidCredentials) {
          Log.e("MainActivity", "Failed to check statistics: $e")
          _loadingError.value = e
          _showLoginButton.value = true
        } catch (e: Exception) {
          Log.e("MainActivity", "Failed to check statistics", e)
          _loadingError.value = e
          _showRefreshButton.value = true
        }
        finally {
          _showLoadingInProgress.value = false
        }
      }
    }
  }

  private suspend fun readCredentials() = withContext(IO) {
    repository.read()
  }

  private suspend fun checkStatistics(credentials: Credentials): Pair<Timeline, Timeline> =
    withContext(IO) {

      val client = NexusClient(credentials.username, credentials.password)
      Log.i("MainActivity", "$credentials")
      val downloads = client.downloads(PROJECT_SELENIDE, GROUP_ID, ARTIFACT_ID)
      val uniqueIPs = client.uniqueIPs(PROJECT_SELENIDE, GROUP_ID, ARTIFACT_ID)

      Log.i(
        "MainActivity",
        "Downloads last month: ${downloads.data.lastMonth()} (previous: ${downloads.data.previousMonth()})"
      )
      Log.i(
        "MainActivity",
        "Unique IPs last month: ${uniqueIPs.data.lastMonth()} (previous: ${uniqueIPs.data.previousMonth()})"
      )


      Pair(downloads, uniqueIPs)
    }
}