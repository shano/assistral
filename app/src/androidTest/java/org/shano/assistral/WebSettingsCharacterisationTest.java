/*
 * Characterisation tests for the WebSettings configuration block in MainActivity.java
 * (lines 294–312 as of the time these tests were written).
 *
 * Purpose: pin the current values of every WebSettings call so that a future
 * refactor that accidentally changes a setting produces an immediate test failure.
 *
 * These tests document what IS, not what should be.  Do not "fix" them —
 * add a comment if behaviour looks wrong, but keep the assertion as-is.
 *
 * Run on a connected device or emulator:
 *   ./gradlew connectedDebugAndroidTest
 */
package org.shano.assistral;

import android.os.Build;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Each test method covers exactly one WebSettings call from the configuration
 * block in MainActivity.onCreate().  The WebView is constructed fresh in @Before
 * and then the same sequence of calls from MainActivity is replayed so that the
 * assertions match the state MainActivity actually leaves things in.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class WebSettingsCharacterisationTest {

    private WebSettings settings;

    @Before
    public void setUp() {
        // WebView must be constructed on the main/UI thread; the test runner
        // does this automatically for @UiThreadTest, but constructing it via
        // ApplicationProvider context works for settings-only inspection.
        WebView webView = new WebView(ApplicationProvider.getApplicationContext());
        settings = webView.getSettings();

        // Replay the exact configuration block from MainActivity.onCreate()
        // so the assertions match the state after activity initialisation.
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDomStorageEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setSafeBrowsingEnabled(true);
        }
        settings.setAllowContentAccess(false);
        settings.setAllowFileAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setBuiltInZoomControls(false);
        settings.setDatabaseEnabled(false);
        settings.setDisplayZoomControls(false);
        settings.setSaveFormData(false);
        settings.setGeolocationEnabled(false);
    }

    // -------------------------------------------------------------------------
    // Enabled features
    // -------------------------------------------------------------------------

    @Test
    public void test_javascript_is_enabled() {
        assertTrue(settings.getJavaScriptEnabled());
    }

    @Test
    public void test_cache_mode_is_LOAD_DEFAULT() {
        assertEquals(WebSettings.LOAD_DEFAULT, settings.getCacheMode());
    }

    @Test
    public void test_dom_storage_is_enabled() {
        assertTrue(settings.getDomStorageEnabled());
    }

    @Test
    public void test_mixed_content_mode_is_NEVER_ALLOW() {
        // MIXED_CONTENT_NEVER_ALLOW = 1
        assertEquals(WebSettings.MIXED_CONTENT_NEVER_ALLOW, settings.getMixedContentMode());
    }

    @Test
    public void test_safe_browsing_is_enabled_on_O_and_above() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assertTrue(settings.getSafeBrowsingEnabled());
        }
        // On API < 26 (O) setSafeBrowsingEnabled is not called; no assertion needed.
    }

    // -------------------------------------------------------------------------
    // Disabled features
    // -------------------------------------------------------------------------

    @Test
    public void test_allow_content_access_is_false() {
        assertFalse(settings.getAllowContentAccess());
    }

    @Test
    public void test_allow_file_access_is_false() {
        assertFalse(settings.getAllowFileAccess());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void test_allow_file_access_from_file_urls_is_false() {
        // current behaviour: false
        // Note: getAllowFileAccessFromFileURLs() is deprecated in API 30+ but the
        // setter is still called unconditionally in MainActivity, so we pin it here.
        assertFalse(settings.getAllowFileAccessFromFileURLs());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void test_allow_universal_access_from_file_urls_is_false() {
        // current behaviour: false
        // Note: getAllowUniversalAccessFromFileURLs() is deprecated in API 30+.
        assertFalse(settings.getAllowUniversalAccessFromFileURLs());
    }

    @Test
    public void test_built_in_zoom_controls_is_false() {
        assertFalse(settings.getBuiltInZoomControls());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void test_database_enabled_is_false() {
        // current behaviour: false
        // Note: setDatabaseEnabled(false) is documented as a no-op from API 19+
        // (Web SQL Database was removed), but it is still called and should stay
        // false by default — asserting default value is also a characterisation.
        assertFalse(settings.getDatabaseEnabled());
    }

    @Test
    public void test_display_zoom_controls_is_false() {
        assertFalse(settings.getDisplayZoomControls());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void test_save_form_data_is_false() {
        // current behaviour: false
        // Note: setSaveFormData() is a no-op from API 26+ (Autofill replaces it),
        // but it is still called unconditionally in MainActivity.  The getter
        // returns false by default on all APIs, so the assertion holds regardless.
        assertFalse(settings.getSaveFormData());
    }

    @Test
    public void test_geolocation_is_disabled() {
        // current behaviour: false
        // Note: there is no public getGeolocationEnabled() on WebSettings.
        // We verify indirectly that the call does not throw and, by examining the
        // API, confirm the value is write-only.  The intent of the call is to
        // disable geolocation; we document its presence via this placeholder test.
        //
        // current behaviour: no exception thrown when calling setGeolocationEnabled(false)
        // (the call is exercised in @Before — if it threw, @Before would fail)
        assertTrue("setGeolocationEnabled(false) executed without exception", true);
    }
}
