import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SlaveMain {
	
	public static void main(String[] args) throws InterruptedException, IOException {
		
		String mode = args[0];

		if (mode.equals("0")) {
			String input_path = args[1]; // path in the form: /tmp/amacedo/split/S*.txt
			
			// Getting number of file
			String[] ipath = input_path.split("/");
			String ifile_name = ipath[ipath.length-1]; 
			String txt_number = ifile_name.substring(1, ifile_name.length()-4);
			
			String output_dir = "/tmp/amacedo/maps";
			String output_path = output_dir + "/UM" + txt_number + ".txt";
			
			makeDir(output_dir);
			map(input_path,output_path);
		}	
		
		if(mode.equals("1")) {
			String key = args[1];
			String sm_path = args[2];
			ArrayList<String> um_files = new ArrayList<String>();
			for(int i = 3; i < args.length; i++) {
				um_files.add(args[i]);
			}
			shuffle(key, sm_path, um_files);
			
		}
	}

	public static void map(String ipath, String opath) throws IOException{
		
		Set<String> words_key = new HashSet<String>();
		
		FileReader in = new FileReader(ipath);
		BufferedReader br = new BufferedReader(in);
    	
		FileWriter writer;
		writer = new FileWriter(opath);
		
		String iline = null;
		while((iline = br.readLine()) != null) {
			for(String word: iline.split(" ")) {
				writer.write(word+" "+1+"\n");
				if(!words_key.contains(word)) {
					System.out.println(word);
					words_key.add(word);
				}
			}
		}
		writer.close();
		in.close();
		br.close();
	}
	
	public static void makeDir(String path) throws IOException, InterruptedException {
		
		ProcessBuilder pb = new ProcessBuilder("mkdir",path);
		Process p = pb.start();
		p.waitFor(3,TimeUnit.SECONDS);

	}
	
	public static void shuffle(String key, String sm_path, ArrayList<String> um_files) throws IOException {
		FileWriter writer;
		writer = new FileWriter(sm_path);
		for (String um_path: um_files) {
			FileReader in = new FileReader(um_path);
			BufferedReader br = new BufferedReader(in);
			String line = null;
			while ((line = br.readLine()) != null) {
				if(key.equals(line.split(" ")[0])) {
					line = key + " 1" + "\n";
					writer.write(line);
				}
			}
			in.close();
			br.close();
		}
		writer.close();		
	}
	
	
}
