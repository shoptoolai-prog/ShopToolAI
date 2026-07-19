package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.production.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppProductionTest {

    private lateinit var context: Context
    private lateinit var secureStorage: SecureStorage
    private lateinit var settingsManager: SettingsManager
    private lateinit var logger: AppLogger
    private lateinit var featureFlagManager: FeatureFlagManager
    private lateinit var cacheManager: ProductionCacheManager
    private lateinit var downloadManager: AppDownloadManager
    private lateinit var securityManager: SecurityManager
    private lateinit var errorReportingSystem: ErrorReportingSystem

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        secureStorage = SecureStorageImpl(context)
        settingsManager = SettingsManagerImpl(secureStorage)
        logger = AppLoggerImpl(context)
        featureFlagManager = FeatureFlagManagerImpl()
        cacheManager = ProductionCacheManagerImpl()
        downloadManager = AppDownloadManagerImpl()
        securityManager = SecurityManagerImpl()
        errorReportingSystem = ErrorReportingSystemImpl(logger)
    }

    @Test
    fun testSecureStorageReadWrite() = runBlocking {
        secureStorage.saveString("test_key", "SuperSecretData123")
        val saved = secureStorage.getString("test_key")
        assertEquals("SuperSecretData123", saved)

        // Ensure encryption actually occurred (by reading raw SharedPreferences directly)
        val rawPrefs = context.getSharedPreferences("secure_app_prefs", Context.MODE_PRIVATE)
        val rawValue = rawPrefs.getString("test_key", null)
        assertNotNull(rawValue)
        assertNotEquals("SuperSecretData123", rawValue)
    }

    @Test
    fun testSettingsManagerFlow() = runBlocking {
        // Toggle dark mode
        settingsManager.updateTheme(true)
        val settings = settingsManager.settingsState.first()
        assertTrue(settings.isDarkTheme)

        // Toggle notification preferences
        settingsManager.updateNotificationPreferences(false)
        val nextSettings = settingsManager.settingsState.first()
        assertFalse(nextSettings.isNotificationsEnabled)
    }

    @Test
    fun testFeatureFlagManagement() = runBlocking {
        // Sync flags
        val flags = featureFlagManager.syncFlags()
        assertTrue(flags.containsKey("enableGeminiAI"))
        assertTrue(featureFlagManager.isFeatureEnabled("enableGeminiAI", false))
        assertFalse(featureFlagManager.isFeatureEnabled("enableAuthLayer", true))
    }

    @Test
    fun testProductionCacheManagerTTL() = runBlocking {
        cacheManager.put("cache_key", "FastCachedObject", ttlMs = 1000L)
        val hit = cacheManager.get("cache_key", String::class.java)
        assertEquals("FastCachedObject", hit)

        // Verify eviction after expiration
        cacheManager.put("exp_key", "ExpiredObject", ttlMs = -10L)
        val miss = cacheManager.get("exp_key", String::class.java)
        assertNull(miss)
    }

    @Test
    fun testErrorReportingAndDiagnostics() = runBlocking {
        val error = AppError(
            id = "err_1",
            timestamp = System.currentTimeMillis(),
            message = "Simulated API Outage Exception",
            stackTrace = "com.example.NetworkException: Outage",
            module = "NetworkManager",
            severity = ErrorSeverity.CRITICAL
        )
        
        errorReportingSystem.reportError(error)
        val pending = errorReportingSystem.getPendingErrors()
        assertEquals(1, pending.size)
        assertEquals("Simulated API Outage Exception", pending.first().message)

        val submitted = errorReportingSystem.submitDiagnostics()
        assertTrue(submitted)
        assertEquals(0, errorReportingSystem.getPendingErrors().size)
    }

    @Test
    fun testSecurityVerification() {
        // Verify SSL hashes exist
        val hashes = securityManager.getSslPinningHashes()
        assertTrue(hashes.isNotEmpty())
        
        // Obfuscation decrypt helper
        val original = "SensitiveApiKeyBody"
        val encoded = android.util.Base64.encodeToString(original.toByteArray(), android.util.Base64.DEFAULT)
        val decrypted = securityManager.decryptSensitiveString(encoded)
        assertEquals(original, decrypted.trim())
    }

    @Test
    fun testDownloadManagerProgress() = runBlocking {
        val testFile = File(context.cacheDir, "downloaded_asset.txt")
        val downloadId = downloadManager.enqueueDownload("https://example.com/asset.png", testFile)
        
        // Wait for download completion
        delay(1500)
        
        val status = downloadManager.getDownloadStatus(downloadId)
        assertEquals(DownloadStatus.COMPLETED, status)
        assertTrue(testFile.exists())
        assertEquals("Optimized Download Content Mock", testFile.readText())
    }
}
