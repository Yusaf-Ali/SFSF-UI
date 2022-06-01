package utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class FileManager {
	public static void writeToFile(String content, String filepath) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(filepath);
		pw.print(content);
		pw.flush();
		pw.close();
	}

	public static String readFromFile(String filepath) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filepath));
		byte[] b = bis.readAllBytes();
		bis.close();
		return new String(b);
	}
}
