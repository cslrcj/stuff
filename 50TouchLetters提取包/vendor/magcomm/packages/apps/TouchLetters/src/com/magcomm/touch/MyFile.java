package com.magcomm.touch;

import java.io.FileInputStream;
import java.io.FileOutputStream;

//import org.apache.http.util.EncodingUtils;

import android.content.Context;
// added by bruce for glove mode begin
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
// added by bruce for glove mode end

public class MyFile {
	private static final String FILENAME = "TouchLetter_OnOff.txt";
	/*begin: edit by hmq 20140715 Modify for 手势翻页*/
	private static final String PSFILENAME = "TouchPS_OnOff.txt";
	/*end: edit by hmq 20140715 Modify for 手势翻页*/
	// added by bruce for write file
	private static final String TOUCHLETTER_FILENAME = "toucherletter_bitflag.txt";
	private static final String GLOVEMODE_FILENAME = "glove_flag.txt"; // added by bruce for glove mode
	
	private static final String ENCODING = "UTF-8";
	private static final String OPEN = "ON";
	private static final String CLOSE = "OFF";
	// 读文件方法
	// ON:open, OFF:close
	public static String read(Context c) {
		try {
			FileInputStream inputStream = c.openFileInput(FILENAME);
			byte[] b = new byte[inputStream.available()];
			inputStream.read(b);
			//String result = EncodingUtils.getString(b, ENCODING);
            String result = new String(b, ENCODING);
			return result;
		} catch (Exception e) {
			return OPEN;
		}
	}

	// 写文件
	public static void write(Context c, String content) {
		try {
			FileOutputStream fos = c.openFileOutput(FILENAME,
					Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
			fos.write(content.getBytes(ENCODING));
			fos.close();
		} catch (Exception e) {
		}
	}

   public static void write(String path, Context c, String content) {
        try {
            FileOutputStream fos = c.openFileOutput(path,
                    Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
            fos.write(content.getBytes(ENCODING));
            fos.close();
        } catch (Exception e) {
        }
    }
	
	public static void OpenTouchletter(Context c) {
		write(c, OPEN);
	}

	public static void CloseTouchletter(Context c) {
		write(c, CLOSE);
	}
	
	/*begin: edit by hmq 20140715 Modify for 手势翻页*/
    public static void SwitchTouchPs(Context c, boolean bool) {
        if (bool)
            write(PSFILENAME, c, OPEN);
        else
            write(PSFILENAME, c, CLOSE);
    }
	/*end: edit by hmq 20140715 Modify for 手势翻页*/

    // added by bruce for write file begin
	public static void switchTouchLetter(Context c, String data){
	    if (data != null){
	        write(TOUCHLETTER_FILENAME, c, data);
	    }else {
	        write(TOUCHLETTER_FILENAME, c, "0");
	    }
	}
	// added by bruce for write file end
	
	public static boolean getTouchletterOnOFF(Context c)
	{
		if (read(c).equals(CLOSE))
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	// added by bruce for glove mode begin
    private static final String GLOVE_FILENAME = "/sys/bus/i2c/devices/0-005d/glove_flag";
	public static void switchGloveMode(Context c, String data){
	    if (data != null){
	        write(GLOVEMODE_FILENAME, c, data);
	    }else {
	        write(GLOVEMODE_FILENAME, c, "0");
	    }
	}

	public static void writeGloveFile(String value){
	    Log.i("bruce_nan", "MyFile_writeGloveFile: value = " + value);
	    File awakeTimeFile = new File(GLOVE_FILENAME);
        FileWriter fr;
        try {
            fr = new FileWriter(awakeTimeFile);
            fr.write(value);
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	public static String readGloveFile(Context c) {
		try {
			FileInputStream inputStream = c.openFileInput(GLOVEMODE_FILENAME);
			byte[] b = new byte[inputStream.available()];
			inputStream.read(b);
			//String result = EncodingUtils.getString(b, ENCODING);
            String result = new String(b, ENCODING);
			return result;
		} catch (Exception e) {
			return "0";
		}
	}
	// added by bruce for glove mode end
}
