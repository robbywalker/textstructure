package main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import difflib.Chunk;
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

		String maindir = args[0] + args[1];
		System.out.println(maindir);
		List<List<String>> filelines = processDirectory(maindir);
		File f = new File(maindir);
		String[] files = f.list();

		Map<String, List<Patch>> m = new HashMap<String, List<Patch>>();

		for (int i = 0; i < filelines.size(); i++) {

			m.put(files[i], new ArrayList<Patch>());
			for (int j = 0; j < filelines.size(); j++) {

				if (j != i) {

					Patch patch = DiffUtils.diff(filelines.get(i), filelines.get(j));
					m.get(files[i]).add(patch);

				}
			}
		}

		for (int i = 0; i < files.length; i++) {

			List<Patch> patches = m.get(files[i]);
			List<Pair<String, String>> relations = processFile(patches, files[i]);
			System.out.println("about to print out relations!!");
			
			for (Pair<String, String> p : relations) {
				System.out.println(p); 
			}
			System.out.println("*************************************************************************************************************************");
		}

	}


	
	public static List<Pair<String, String>> processFile(List<Patch> patches, String fname) {

		List<Pair<String, String>> relations = new ArrayList<Pair<String, String>>();
		Map<Set<String>, Integer> dups = new HashMap<Set<String>, Integer>();
		System.out.println(fname);
		for (Patch p : patches) {
			//System.out.println("NEW PATCH");

			for (Delta d : p.getDeltas()) {


				if (d instanceof difflib.ChangeDelta) {

					Chunk orig = d.getOriginal();
					Chunk revis = d.getRevised();

					//System.out.println(orig);

					//System.out.println(revis);

					if (orig.getLines().size() == revis.getLines().size()) {

						StringBuffer s1 = new StringBuffer();
						StringBuffer s2 = new StringBuffer();
						
						for (int i = 0; i < orig.getLines().size(); i++) {
							
							s1.append((String)orig.getLines().get(i) + " ");
							s2.append((String)revis.getLines().get(i) + " ");
						}
						
					
						for (int i = 0; i < orig.getLines().size(); i++) {

							Pair<String, String> pair = findRelation(s1.toString(), s2.toString());
							
							if (pair != null && !dups.containsKey(new HashSet<String>(Arrays.asList(pair.toString().split("\\s+"))))) {

								relations.add(pair);
								dups.put(new HashSet<String>(Arrays.asList(pair.toString().split("\\s+"))), 1);
							}
						}
					}
				}

			}
		}


		return relations;

	}

	public static Pair<String, String> findRelation(String s1, String s2) {

		String[] s1arr = s1.split("\\s+");
		String[] s2arr = s2.split("\\s+");
		StringBuffer relation = new StringBuffer();
		StringBuffer arg = new StringBuffer();
		int split = 0; 
		for (int i = 0; i < Math.min(s1arr.length, s2arr.length); i++) {


			if (s1arr[i].equals(s2arr[i])) {

				relation.append(s1arr[i]);
				relation.append(" ");

			} else {

				arg.append(s1arr[i]);
				arg.append(" ");
			}

		}


		Pair<String, String> p = new Pair(relation.toString().trim(), arg.toString().trim()); 
		return p;
	}


	public static List<List<String>> processDirectory(String dir) {

		List<List<String>> toret = new ArrayList<List<String>>();

		File f = new File(dir);
		String[] c = f.list();
		for (String s : c) {

			List<String> lines = fileToLines(dir + "/" + s);
			toret.add(lines);
		}

		return toret;
	}

}
