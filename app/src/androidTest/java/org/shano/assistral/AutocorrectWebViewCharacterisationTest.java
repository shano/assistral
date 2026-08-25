/*
 * Characterisation tests for the non-password inputType/imeOptions flags set
 * by AutocorrectWebView.applyNonPasswordInputFlags().
 *
 * Purpose: pin the exact flag set that prevents the IME from treating the
 * soft-keyboard Enter key as a submit action. The regression tracked in
 * issue #7 was caused by TYPE_TEXT_FLAG_MULTI_LINE (and IME_FLAG_NO_ENTER_ACTION)
 * going missing from this set; if a future change drops either flag these
 * tests fail immediately.
 *
 * These tests document what IS, not what should be.  Do not "fix" them —
 * add a comment if behaviour looks wrong, but keep the assertion as-is.
 *
 * The flag logic was extracted into a package-private static helper so it
 * can be asserted without booting a live, focused WebView (which is what
 * onCreateInputConnection requires).  The helper is the single source of
 * truth for the flag values; onCreateInputConnection delegates to it.
 *
 * Run on a connected device or emulator:
 *   ./gradlew connectedDebugAndroidTest
 */
package org.shano.assistral;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class AutocorrectWebViewCharacterisationTest {

    private EditorInfo applyFlags() {
        EditorInfo outAttrs = new EditorInfo();
        AutocorrectWebView.applyNonPasswordInputFlags(outAttrs);
        return outAttrs;
    }

    // -------------------------------------------------------------------------
    // inputType — the exact flag set applied to non-password fields
    // -------------------------------------------------------------------------

    @Test
    public void test_input_type_is_exact_non_password_flag_set() {
        int expected = InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
                | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
        assertEquals(expected, applyFlags().inputType);
    }

    @Test
    public void test_input_type_is_multi_line() {
        // Regression guard for issue #7: a missing MULTI_LINE flag makes the
        // IME treat Enter as a submit action instead of a newline.
        int inputType = applyFlags().inputType;
        assertTrue("TYPE_TEXT_FLAG_MULTI_LINE must be set (issue #7)",
                (inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0);
    }

    @Test
    public void test_input_type_is_class_text() {
        assertTrue((applyFlags().inputType & InputType.TYPE_MASK_CLASS)
                == InputType.TYPE_CLASS_TEXT);
    }

    @Test
    public void test_input_type_has_auto_correct() {
        assertTrue((applyFlags().inputType & InputType.TYPE_TEXT_FLAG_AUTO_CORRECT) != 0);
    }

    @Test
    public void test_input_type_has_auto_complete() {
        assertTrue((applyFlags().inputType & InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0);
    }

    @Test
    public void test_input_type_has_cap_sentences() {
        assertTrue((applyFlags().inputType & InputType.TYPE_TEXT_FLAG_CAP_SENTENCES) != 0);
    }

    // -------------------------------------------------------------------------
    // imeOptions
    // -------------------------------------------------------------------------

    @Test
    public void test_ime_options_has_no_enter_action() {
        // Regression guard for issue #7: IME_FLAG_NO_ENTER_ACTION suppresses the
        // "send"/"done" action button so Enter is delivered as a key, not a submit.
        int imeOptions = applyFlags().imeOptions;
        assertTrue("IME_FLAG_NO_ENTER_ACTION must be set (issue #7)",
                (imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0);
    }

    @Test
    public void test_ime_options_preserves_existing_bits() {
        // The helper ORs its flag in rather than overwriting, so any imeOptions
        // already set by the framework before onCreateInputConnection runs are kept.
        EditorInfo outAttrs = new EditorInfo();
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NAVIGATE_NEXT;
        AutocorrectWebView.applyNonPasswordInputFlags(outAttrs);
        assertTrue((outAttrs.imeOptions & EditorInfo.IME_FLAG_NAVIGATE_NEXT) != 0);
        assertTrue((outAttrs.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0);
    }
}
