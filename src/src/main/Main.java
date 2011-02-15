package main;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

public class Main {

	private static List<String> fileToLines(String filename) {
		
		List<String> lines = new LinkedList<String>();
		String line = "";
		try {
			
			BufferedReader in = new BufferedReader(new FileReader(filename));
			while ((line = in.readLine()) != null) {
				lines.add(line);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return lines;
	}

	public static void main(String[] args) {
		
		List<String> s1 = fileToLines("/Users/jliu/greplin/documents/amazon/amazon-order1.txt");
		List<String> s2 = fileToLines("/Users/jliu/greplin/documents/amazon/amazon-order2.txt");
		
		Patch patch = DiffUtils.diff(s1, s2);
		
		for (Delta delta : patch.getDeltas()) {
			
			System.out.println(delta);
			if (delta instanceof difflib.ChangeDelta) {
				
				System.out.println(delta.getOriginal());
				System.out.println(delta.getRevised());
			}	

		}
	}
	
	public static void processFile() {
		
		
	}

}
