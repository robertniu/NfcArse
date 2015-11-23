package com.robertniu.nfcarse;

import java.io.File;
import java.io.RandomAccessFile;
import android.os.Environment;


public class WriteXML 
{

	//private static final String SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
	//private static final String directory ="/CBDflyinglog2";

	public static void WriteGpxFile(String content,String FILE_NAME)
	{
		try
		{	
			//如果手机插入了SD卡，而且应用程序具有访问SD的权限
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			{
				//获取SD卡的目录
				File targetDir = Environment.getExternalStoragePublicDirectory(Common.HOME_DIR);
				//File path = new File(Environment.getExternalStoragePublicDirectory(Common.HOME_DIR) + "/" + Common.KEYS_DIR);
				//File targetDir = new File(sdCardDir.getCanonicalPath()+directory);
				//File targetDir = new File(sdCardDir.getCanonicalPath()+directory);
				  if (!targetDir.exists()) {
					  targetDir.mkdirs();
					  }
				File targetFile = new File(targetDir+FILE_NAME);

				//以指定文件创建	RandomAccessFile对象
				RandomAccessFile raf = new RandomAccessFile(targetFile , "rw");
				//将文件记录指针移动到最后
				raf.seek(targetFile.length());
				// 输出文件内容
				raf.write(content.getBytes());
				raf.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	
	
	

	
}

