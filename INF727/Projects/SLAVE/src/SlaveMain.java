import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class SlaveMain {

	@SuppressWarnings("resource")
	public static HashMap<String,Integer> map(String file) throws IOException{
		
		FileReader in = new FileReader(file);
		BufferedReader br = new BufferedReader(in);
		HashMap<String,Integer> words_map = new HashMap<String,Integer>();
		String line = null;
		while((line = br.readLine()) != null) {
			for(String word: line.split(" ")) {
				if(words_map.containsKey(word)) {
					words_map.replace(word, words_map.get(word)+1);
				}else {
					words_map.put(word, 1);
				}
			}
		}
		return words_map;
	}
	
	public static void makeDir(String path) throws IOException, InterruptedException {
		
		ProcessBuilder pb = new ProcessBuilder("mkdir",path);
		Process p = pb.start();
		p.waitFor(3,TimeUnit.SECONDS);

	}	
	
    public static void writeOutput(Map<String,Integer> map, String path) throws IOException {
    	FileWriter writer;
		writer = new FileWriter(path);
		for(Entry<String,Integer> e : map.entrySet()) {
			String word = e.getKey();
			String count = e.getValue().toString();
			String line = word + " " + count + "\n";
			writer.write(line);
		}
		writer.close();
    }
	
	public static void main(String[] args) throws InterruptedException, IOException {
		
		String mode = args[0];
		String input_path = args[1];
		
		System.out.println("mode: "+mode);
		System.out.println("input path: "+input_path);
		
		String[] ipath = input_path.split("/");
		String ifile_name = ipath[ipath.length-1];
		String txt_number = ifile_name.substring(1, ifile_name.length()-4);
		
		String output_dir = "/tmp/amacedo/maps";
	
		String output_path = output_dir + "/UM" + txt_number + ".txt";
		
		System.out.println("output path: "+output_path);
		System.out.println(mode.equals("0"));

		if (mode.equals("0")) {
			HashMap<String,Integer> words_map = map(input_path);
			makeDir(output_dir);
			writeOutput(words_map,output_path);
		}	
	}
}
