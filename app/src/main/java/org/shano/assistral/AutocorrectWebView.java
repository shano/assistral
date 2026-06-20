package org.shano.assistral;

import android.content.Context;
import android.os.SystemClock;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.webkit.WebView;

public class AutocorrectWebView extends WebView {

    // Track whether the active field is a password so dispatchKeyEvent can skip it.
    private boolean activeFieldIsPassword = false;

    public AutocorrectWebView(Context context) {
        super(context);
    }

    public AutocorrectWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d("assistral", "[ACV] instantiated");
    }

    public AutocorrectWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        Log.d("assistral", "[ACV] onCreateInputConnection called");
        InputConnection ic = super.onCreateInputConnection(outAttrs);
        if (ic == null) return null;
        int variation = outAttrs.inputType & InputType.TYPE_MASK_VARIATION;
        activeFieldIsPassword = variation == InputType.TYPE_TEXT_VARIATION_PASSWORD
                || variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                || variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD;
        if (!activeFieldIsPassword) {
            outAttrs.inputType = InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
                    | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE
                    | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                    | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
            outAttrs.imeOptions |= EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        }
        if (activeFieldIsPassword) return ic;

        // Wrap the InputConnection so soft-keyboard Enter (both sendKeyEvent and
        // performEditorAction paths) becomes Shift+Enter, which chat apps treat as newline.
        return new InputConnectionWrapper(ic, true) {
            @Override
            public boolean sendKeyEvent(KeyEvent event) {
                Log.d("assistral", "[ACV] sendKeyEvent keyCode=" + event.getKeyCode() + " action=" + event.getAction());
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && !event.isShiftPressed()) {
                    Log.d("assistral", "[ACV] converting Enter -> Shift+Enter via sendKeyEvent");
                    return super.sendKeyEvent(new KeyEvent(
                        event.getDownTime(), event.getEventTime(),
                        event.getAction(), event.getKeyCode(),
                        event.getRepeatCount(),
                        event.getMetaState() | KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON
                    ));
                }
                return super.sendKeyEvent(event);
            }

            @Override
            public boolean performEditorAction(int actionCode) {
                Log.d("assistral", "[ACV] performEditorAction actionCode=" + actionCode);
                // IME "send" action → Shift+Enter newline instead of submitting.
                long now = SystemClock.uptimeMillis();
                int shiftMeta = KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON;
                super.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_ENTER, 0, shiftMeta));
                super.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_ENTER, 0, shiftMeta));
                return true;
            }
        };
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
            Log.d("assistral", "[ACV] dispatchKeyEvent ENTER action=" + event.getAction() + " shift=" + event.isShiftPressed() + " pwd=" + activeFieldIsPassword);
        // Hardware keyboard: convert plain Enter → Shift+Enter for non-password fields.
        if (!activeFieldIsPassword
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                && !event.isShiftPressed()) {
            return super.dispatchKeyEvent(new KeyEvent(
                event.getDownTime(), event.getEventTime(),
                event.getAction(), event.getKeyCode(),
                event.getRepeatCount(),
                event.getMetaState() | KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON
            ));
        }
        return super.dispatchKeyEvent(event);
    }
}
