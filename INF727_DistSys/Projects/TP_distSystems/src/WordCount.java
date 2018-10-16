import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Collections;
import java.util.Comparator;
import java.io.*;

public class WordCount {
	
	// Function that sort by count
	public static LinkedHashMap <String,Integer> sortByCount(HashMap <String,Integer> unsortMap) {
		
		// Creating a list of entries of the unsortMap to be sorted using Collections
		List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());
		
		
		// Defining Comparator and sorting
		Collections.sort(list, new Comparator<Entry<String,Integer>>(){
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {	
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		
		// Storing ordered results in a LinkedHashMap
		LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
	    for (Entry<String, Integer> entry : list) {
	    	sortedMap.put(entry.getKey(), entry.getValue());
	    } 
	    return sortedMap;	
	}
	
	
	// Function that sort by count, than by alphabetic order for words with the same count 
    public static LinkedHashMap <String,Integer> sortByCountAndAlpha(HashMap <String,Integer> unsortMap) {
		
		// Creating a list of entries of the unsortMap to be sorted using Collections
		List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());
		
		
		// Defining Comparator and sorting
		Collections.sort(list, new Comparator<Entry<String,Integer>>(){
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				int compByValue = o2.getValue().compareTo(o1.getValue());	
				if(compByValue == 0) { // If both counts are the same, use alphabetic order as comparator
					return o1.getKey().compareTo(o2.getKey());
				}else {
					return compByValue;
				}
			}
		});
		
		// Storing ordered results in a LinkedHashMap
		LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
	    for (Entry<String, Integer> entry : list) {
	    	sortedMap.put(entry.getKey(), entry.getValue());
	    } 
	    return sortedMap;	
	}
	
	// Function to write on a output file
    public static void writeOutput(Map<String,Integer> map, String path) {
    	FileWriter writer;
		try {
			writer = new FileWriter(path);
			for(Entry<String,Integer> entry : map.entrySet()) {
				String line = entry.getKey().concat(" ").concat(entry.getValue().toString()).concat("\n");
				writer.write(line);
			}
			writer.close();
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		}	
    }
    
    // Function that print results on console
    
    public static void printResults(Map<String,Integer> map) {
    	for(Entry<String,Integer> entry : map.entrySet()) {
    		System.out.println(entry.getKey().concat(" ").concat(entry.getValue().toString()));
    	}
    }
    
    public static void printFirst50(Map<String,Integer> map) {
    	int i = 0;
    	for(Entry<String,Integer> entry : map.entrySet()) {
    		if(i==50) {
    			break;
    		}
    		System.out.println(entry.getKey().concat(" ").concat(entry.getValue().toString()));
    		i++;
    	}
    }

	public static void main(String[] args) throws IOException {
		
		long startTime = System.currentTimeMillis();
		
		//Read text file
		File file = new File("sante_publique.txt");
		Scanner input = new Scanner(file);
		
		//Storing words in a HashMap and counting it at the same time
		HashMap <String,Integer> map = new HashMap <String,Integer>();
		while (input.hasNext()) {
			String word = input.next().toLowerCase();
			if(!map.containsKey(word)) {
				map.put(word, 1);
			}else{
				map.replace(word, map.get(word)+1);
			}
		}
		
		input.close();
		
		LinkedHashMap<String,Integer> sortedMap = sortByCountAndAlpha(map);
		
		long endTime = System.currentTimeMillis();	
		long totalTime = endTime - startTime;
		System.out.format("The time of execution is %dms",totalTime);

		
		// Writing the results in a text file
		writeOutput(sortedMap,"output.txt");
		
		// Display results
		//printFirst50(sortedMap);
		
		

	}

}