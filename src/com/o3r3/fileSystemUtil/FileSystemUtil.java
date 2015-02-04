package com.o3r3.fileSystemUtil;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.o3r3.browserandroidbridge.PCBridgeActivity;
import com.o3r3.browserandroidbridge.Constantes;



public class FileSystemUtil {
	
	private Context context;
	private File logFile;
	
	public FileSystemUtil(Context context) {
		super();
		this.context = context;
		getOutputMediaFile();
	}
	
	/** Create a File for saving logs 
	 * No estaria mal la cosa de crear una carpeta para
	 * almacenar las cosas pero tampoco importa demasiado 
	 * gracias al prefijo*/
	
	private File getOutputMediaFile() {
		File mediaStorageDir = null;
		// comprobar si tenemos sdcar
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			mediaStorageDir = new File(Environment.getExternalStorageDirectory()+File.separator+Constantes.directory);
		} else {
			mediaStorageDir = context.getFilesDir();
		}		
		// Create a media file name
		logFile = new File(mediaStorageDir.getPath() + File.separator + "data" + Constantes.extensionTXT); 
		return logFile;
	}
	
	public boolean createDirectory() {
		boolean b = false;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			try {
				File directory = new File(Environment.getExternalStorageDirectory()+File.separator+Constantes.directory);
				b = directory.mkdirs();
			} catch (Exception ex) {
				Log.e(PCBridgeActivity.tag, "Error a la hora de crear el directorio");
			}
		} else {
			
		}	
		
		return b;
	}



	public File getLogFile() {
		return logFile;
	}

	public void setContext(Context context) {
		this.context = context;
	}

   
}
