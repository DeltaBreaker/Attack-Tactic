package io.itch.deltabreaker.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileManager {

	public static final String HEADER_EXTENSION = ".hdr";
	public static final String MAIN_FILE_EXTENSION = ".dat";

	public static List<File> getFiles(String directoryName) {
		File directory = new File(directoryName);

		List<File> resultList = new ArrayList<File>();

		File[] fList = directory.listFiles();
		resultList.addAll(Arrays.asList(fList));
		for (File file : fList) {
			if (file.isDirectory()) {
				resultList.addAll(getFiles(file.getAbsolutePath()));
			}
		}

		return resultList;
	}

	public static String readAsString(String file) {
		StringBuilder string = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(new File(file)))) {
			String line;
			while ((line = br.readLine()) != null) {
				string.append(line);
				string.append("\n");
			}
			br.close();
		} catch (Exception e) {
			System.err.println("[FileHandler]: There was an issue reading the file");
			e.printStackTrace();
		}
		return string.toString();
	}

}
