import java.util.ArrayList;
import java.util.HashMap;

public class Test {

	public static void main(String[] args) {
		
		HashMap<String,ArrayList<String>> dict = new HashMap<String,ArrayList<String>>();
		insertElement(dict,"car","UM0.txt");
		
		insertElement(dict,"car","UM1.txt");
		
		insertElement(dict,"beer","UM1.txt");
		
		System.out.println(dict);
		

	}
	
	public static void insertElement(HashMap<String,ArrayList<String>> dict, String word_key, String um_map) {
		if(dict.containsKey(word_key)) {
			ArrayList<String> umList = dict.get(word_key);
			umList.add(um_map);
			dict.replace(word_key, umList);
		}else {
			ArrayList<String> umList = new ArrayList<String>();
			umList.add(um_map);
			dict.put(word_key, umList);
		}
	}

}
