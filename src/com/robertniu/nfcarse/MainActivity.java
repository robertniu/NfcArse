package com.robertniu.nfcarse;

import java.io.IOException;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private int port = 8008;
	private WebServer server;
	private AlertDialog mEnableNfc;
	private boolean mResume = true;
	 private Intent mOldIntent = null;
	 LinearLayout mLayout;
		private String GpxFileNameHead = "/MifareUL_";
		private String GpxFileNameEnd = ".xml";
		private String GpxFileName = "";
		private String [] getData=new String[]{"","","",""};
		TextView mErrorMessage;
	    int mMFCSupport;
	    private final Handler mHandler = new Handler();
	private static final String LOG_TAG =
            MainActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		TextView tv = (TextView)findViewById(R.id.tvInfo);
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
		final String formatedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
		tv.setText("http://" + formatedIpAddress + ":" + port);
		
		 /////////////////////////
		
        // Check if there is an NFC hardware component.
        Common.setNfcAdapter(NfcAdapter.getDefaultAdapter(this));
        if (Common.getNfcAdapter() == null) {
            new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_no_nfc_title)
                .setMessage(R.string.dialog_no_nfc)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.action_exit_app,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                 })
                 .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                 })
                 .show();
            mResume = false;
            return;
        }

        if (Common.isExternalStorageWritableErrorToast(this)) {
            // Create keys directory.
            File path = new File(Environment.getExternalStoragePublicDirectory(
                    Common.HOME_DIR) + "/" + Common.KEYS_DIR);
            if (path.exists() == false && !path.mkdirs()) {
                // Could not create directory.
                Log.e(LOG_TAG, "Error while crating '" + Common.HOME_DIR
                        + "/" + Common.KEYS_DIR + "' directory.");
                return;
            }

            // Create dumps directory.
            path = new File(Environment.getExternalStoragePublicDirectory(
                    Common.HOME_DIR) + "/" + Common.DUMPS_DIR);
            if (path.exists() == false && !path.mkdirs()) {
                // Could not create directory.
                Log.e(LOG_TAG, "Error while crating '" + Common.HOME_DIR
                        + "/" + Common.DUMPS_DIR + "' directory.");
                return;
            }

            // Create tmp directory.
            path = new File(Environment.getExternalStoragePublicDirectory(
                    Common.HOME_DIR) + "/" + Common.TMP_DIR);
            if (path.exists() == false && !path.mkdirs()) {
                // Could not create directory.
                Log.e(LOG_TAG, "Error while crating '" + Common.HOME_DIR
                        + Common.TMP_DIR + "' directory.");
                return;
            }
            // Clean up tmp directory.
            for (File file : path.listFiles()) {
                file.delete();
            }

            // Create std. key file if there is none.
           // copyStdKeysFilesIfNecessary();
        }

        
        // Create a dialog that send user to NFC settings if NFC is off.
        // (Or let the user use the App in editor only mode / exit the App.)
        mEnableNfc = new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_nfc_not_enabled_title)
            .setMessage(R.string.dialog_nfc_not_enabled)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton(R.string.action_nfc,new DialogInterface.OnClickListener() 
            {
                @Override
                @SuppressLint("InlinedApi")
                public void onClick(DialogInterface dialog, int which) {
                    // Goto NFC Settings.
                    if (Build.VERSION.SDK_INT >= 16) {
                        startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                    } else {
                        startActivity(new Intent(
                                Settings.ACTION_WIRELESS_SETTINGS));
                    }
                }
             })
             
             .setNeutralButton(R.string.action_editor_only,new DialogInterface.OnClickListener() 
             {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Only use Editor. Do nothing.
                }
             })

             .setNegativeButton(R.string.action_exit_app,new DialogInterface.OnClickListener() 
             {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // Exit the App.
                    finish();
                }
             })
             .create();

       
    
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			server = new WebServer(port);
	        if (mResume) {
	            checkNfc();
	        }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (server != null)			
			server.stop();
		Common.disableNfcForegroundDispatch(this);
	}
	
    public void on_MUL_Read(View view) {
        Intent intent = new Intent(this, MUL_Read.class);
        startActivity(intent);
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
        	//当app界面已经打开，检测到新的卡片时直接运行读取操作
           // Intent i = new Intent(this, TagInfoTool.class);
        	Intent i = new Intent(this, MUL_Read.class);  
            
            startActivity(i);
        }
    }
    
	//被onResume调用
    private void checkNfc() {
        // Check if the NFC hardware is enabled.
        if (Common.getNfcAdapter() != null
                && !Common.getNfcAdapter().isEnabled()) {
            // NFC is disabled. Show dialog.
            mEnableNfc.show();
            // Disable read/write tag options.
           //  mReadTag.setEnabled(false);  //未启用nfc时运行错误，需要注释掉这两行，编译器也不会报警
           // mWriteTag.setEnabled(false);
            return;
        } else {
            // NFC is enabled. Hide dialog and enable NFC
            // foreground dispatch.
            if (mOldIntent != getIntent()) {
                int typeCheck = Common.treatAsNewTag(getIntent(), this);
                if (typeCheck == -1 || typeCheck == -2) {
                    // Device or tag does not support Mifare Classic.
                    // Run the only thing that is possible: The tag info tool.
                	//如果检测到不是classic卡，直接显示taginfotool，再进入主界面
                   // Intent i = new Intent(this, TagInfoTool.class);
                   // startActivity(i);
                }
                mOldIntent = getIntent();
            }
            Common.enableNfcForegroundDispatch(this);
            
            mEnableNfc.hide();
         //   mReadTag.setEnabled(true);
         //  mWriteTag.setEnabled(true);
           
                  
            
        }
    }
    



   
}
