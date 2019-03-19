
package edu.tamu.aser.config;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Utilities for interacting with file and solver
 * 
 * @author jeffhuang
 *
 */
public class Util {

	private final static String RV_STR = "rv";

	/**
	 * Create a file "name" under the directory "path"
	 * 
	 * @param path
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public static File newOutFile(String path, String name) throws IOException {
		
		File z3Dir = new File(path);
		//Here comes the existence check
		if(!z3Dir.exists())
			z3Dir.mkdirs();
		
		File f = new File(path, name);
		if(f.exists())
		{
		    f.delete();
		}
	
		f.createNewFile();
	
		return f;
	}
	public static PrintWriter newWriter(File file, boolean append) throws IOException {
	return new PrintWriter(new BufferedWriter(new FileWriter(file, append)));
	}
	
	public static String getTempRVDirectory() {
		String tempdir = System.getProperty("java.io.tmpdir");
		
		
		String tempRVdir = tempdir + System.getProperty("file.separator")+RV_STR+ System.getProperty("file.separator");
		
		File tempFile = new File(tempRVdir);
		if(!(tempFile.exists()))
			tempFile.mkdir();

		return tempRVdir;
	}

    static String spaces(int i) {
        return chars(i, ' ');
    }

    public static String chars(int i, char c) {
        char[] spaces = new char[i];
        Arrays.fill(spaces, c);
        return new String(spaces);
    }

    public static String center(String msg, int width, char fill) {
        int fillWidth = width - msg.length();
        return "\n" + chars(fillWidth / 2, fill) + msg + chars((fillWidth + 1) / 2, fill);
    }

    public static void redirectOutput(final InputStream outputStream, final PrintStream redirect) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner scanner = new Scanner(outputStream);
                while (scanner.hasNextLine()) {
                    String s = scanner.nextLine();
                    if (redirect != null) {
                        redirect.println(s);
                    }
                }
            }
        }).start();
    }

    public static String convertFileToString(File file) throws IOException{
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] b = new byte[fileInputStream.available()];
        fileInputStream.read(b);
        fileInputStream.close();
        String content = new String(b);
        return content;
    }

    public static String convertFileToString(String path) throws IOException{
        return convertFileToString(new File(path));
    }
}
