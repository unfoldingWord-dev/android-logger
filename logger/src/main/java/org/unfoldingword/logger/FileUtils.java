package org.unfoldingword.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * Created by joel on 2/26/2016.
 */
public class FileUtils {


    public static String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String readFileToString(File file) throws IOException {
        FileInputStream fin = new FileInputStream(file);
        String content = convertStreamToString(fin);
        fin.close();
        return content;
    }

    public static void writeStringToFile(File file, String content) throws IOException {
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
        outputStreamWriter.write(content);
        outputStreamWriter.close();
    }
}
