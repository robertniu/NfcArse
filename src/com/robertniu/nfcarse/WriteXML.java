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
			//����ֻ�������SD��������Ӧ�ó�����з���SD��Ȩ��
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			{
				//��ȡSD����Ŀ¼
				File targetDir = Environment.getExternalStoragePublicDirectory(Common.HOME_DIR);
				//File path = new File(Environment.getExternalStoragePublicDirectory(Common.HOME_DIR) + "/" + Common.KEYS_DIR);
				//File targetDir = new File(sdCardDir.getCanonicalPath()+directory);
				//File targetDir = new File(sdCardDir.getCanonicalPath()+directory);
				  if (!targetDir.exists()) {
					  targetDir.mkdirs();
					  }
				File targetFile = new File(targetDir+FILE_NAME);

				//��ָ���ļ�����	RandomAccessFile����
				RandomAccessFile raf = new RandomAccessFile(targetFile , "rw");
				//���ļ���¼ָ���ƶ������
				raf.seek(targetFile.length());
				// ����ļ�����
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

