package com.orbotix.sample.helloworld;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import orbotix.macro.MacroCommandCreationException;
import orbotix.macro.MacroObject;

public class FileManager {

	public MacroObject getMacro(Context context, String filename) throws IOException, MacroCommandCreationException{
		
		InputStream is = context.getAssets().open(filename);
		
		ByteArrayOutputStream bos= new ByteArrayOutputStream();
		
		int next = is.read();
		
		while (next > -1){
			bos.write(next);
			next= is.read();
		}
		
		byte[] bytes = bos.toByteArray();
		
		return new MacroObject(bytes);
	}
}
