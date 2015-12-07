package com.sphero.ovalfreefall;

import com.orbotix.macro.cmd.MacroCommandCreationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileManager {

    //Take an input stream representing an Oval program file and return the content as a String
    public static String getOvalProgram( InputStream inputStream ) throws IOException, MacroCommandCreationException {

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
        return new String(bytes);
    }
}