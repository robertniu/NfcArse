package com.robertniu.nfcarse;

import android.app.Activity;
import android.content.Intent;


/**
 * An Activity implementing the NFC foreground dispatch system overwriting
 * onResume() and onPause(). New Intents will be treated as new Tags.
 * @see Common#enableNfcForegroundDispatch(Activity)
 * @see Common#disableNfcForegroundDispatch(Activity)
 * @see Common#treatAsNewTag(Intent, android.content.Context)
 * @author Gerhard Klostermeier
 *
 */
public abstract class BasicActivity extends Activity {

    /**
     * Enable NFC foreground dispatch system.
     * @see Common#disableNfcForegroundDispatch(Activity)
     */
    @Override
    public void onResume() {
        super.onResume();
        Common.enableNfcForegroundDispatch(this);
    }

    /**
     * Disable NFC foreground dispatch system.
     * @see Common#disableNfcForegroundDispatch(Activity)
     */
    @Override
    public void onPause() {
        super.onPause();
        Common.disableNfcForegroundDispatch(this);
    }

    /**
     * Handle new Intent as a new tag Intent and if the tag/device does not
     * support Mifare Classic, then run {@link TagInfoTool}.
     * @see Common#treatAsNewTag(Intent, android.content.Context)
     * @see TagInfoTool
     */
    @Override
    public void onNewIntent(Intent intent) {
        int typeCheck = Common.treatAsNewTag(intent, this);
        if (typeCheck == -1 || typeCheck == -2) {
            // Device or tag does not support Mifare Classic.
            // Run the only thing that is possible: The tag info tool.
            Intent i = new Intent(this, TagInfoTool.class);
            startActivity(i);
        }
    }
}
