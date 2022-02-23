package io.github.lightman314.lightmanscurrency.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileUtil {
	
	public static void writeStringToFile(File file, String string) throws IOException {
		
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter writer = new PrintWriter(bw);
		
		writer.print(string);
		
		writer.close();
		
	}
	
}
