package com.orbotix.macroloader;

import com.orbotix.macro.MacroObject;
import com.orbotix.macro.cmd.MacroCommandCreationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileManager {

    //Take an input stream representing a macro file and return the content as a MacroObject
    public static MacroObject getMacro( InputStream inputStream ) throws IOException, MacroCommandCreationException {

        if( inputStream == null ) {
            return null;
        }

        ByteArrayOutputStream bos= new ByteArrayOutputStream();

        int next = inputStream.read();

        while (next > -1){
            bos.write(next);
            next= inputStream.read();
        }

        byte[] bytes = bos.toByteArray();
        return new MacroObject(bytes);
    }
}