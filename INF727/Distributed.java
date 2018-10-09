package test;

import java.nio.file.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.*;




public class Distributed {


	private static String readFile(String path) {
		Charset charset = Charset.forName("US-ASCII");
    	String fullText = "";
    	try (BufferedReader reader = Files.newBufferedReader(Paths.get(path), charset)) {
    		String line = null;
    		while ((line = reader.readLine()) != null) {
    			fullText = fullText + line;
    		}
    	} catch (IOException x) {
    		System.err.format("IOException: %s%n", x);
    	}
    	return fullText;
	}
	
	private static Hashtable countOfWords(String fileText) {
		Hashtable wordsDict = new Hashtable();
		String[] splitFileText = fileText.replace("\n", " ").split(" ");
		
		for (String word : splitFileText) {
			if (wordsDict.containsKey(word)) {
				wordsDict.put(word, 1);
			} else {
				wordsDict.put(word, (int)wordsDict.get(word)+1);
			}
		}
		return wordsDict;
	}
	
	public static void count(String path) {
		String textFile = readFile(path);
		Hashtable wordsDict = countOfWords(textFile);
		Enumeration keys = wordsDict.keys();
		String tmp = "";
		while (keys.hasMoreElements()) {
			tmp = (String) keys.nextElement();
			System.out.println(tmp + wordsDict.get(tmp));
		}
	}
	
	public static void main () {
		Distributed.count("input.txt");
	}
}
