package org.selenide.mavencentral

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys


class CredentialsRepository(
  private val applicationContext: Context
) {
  private val sharedPreferences by lazy {
    EncryptedSharedPreferences.create(
      "credentials",
      MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
      applicationContext,
      EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
      EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
  }

  fun read(): Credentials? {
    val username = sharedPreferences.getString("username", "")!!
    val password = sharedPreferences.getString("password", "")!!
    return if (username.isBlank() || password.isBlank()) null
    else Credentials(username, password)
  }

  fun save(credentials: Credentials) {
    sharedPreferences.edit()
      .putString("username", credentials.username)
      .putString("password", credentials.password)
      .apply()
  }
}