import java.util.ArrayList;
import java.util.HashMap;

public class Test {

	public static void main(String[] args) {
		
		HashMap<String,String> myMap = new HashMap<String,String>();
		myMap.put("a", "1");
		myMap.put("b", "1");
		long n = myMap.values().stream().distinct().count();
		System.out.println(n);
		

	}

}
