package com.robertniu.nfcarse;

import java.io.File;
import java.io.IOException;

import android.os.Environment;

public class WebServer extends NanoHTTPD
{
	public WebServer(int port) throws IOException {
		//super(port, new File(Environment.getExternalStorageDirectory().getAbsolutePath()));
		super(port, new File("/"));
	}
}
