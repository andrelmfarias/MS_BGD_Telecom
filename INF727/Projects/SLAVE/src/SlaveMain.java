import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SlaveMain {
	
	public static void main(String[] args) throws InterruptedException, IOException {
		
		String mode = args[0];
		String input_path = args[1]; // path in the form: /tmp/amacedo/split/S*.txt
		
		//System.out.println("mode: "+mode);
		//System.out.println("input path: "+input_path);
		
		String[] ipath = input_path.split("/");
		String ifile_name = ipath[ipath.length-1];
		String txt_number = ifile_name.substring(1, ifile_name.length()-4);
		
		String output_dir = "/tmp/amacedo/maps";
	
		String output_path = output_dir + "/UM" + txt_number + ".txt";
		
		//System.out.println("output path: "+output_path);
		//System.out.println(mode.equals("0"));

		if (mode.equals("0")) {
			makeDir(output_dir);
			map(input_path,output_path);
		}	
	}

	public static void map(String ipath, String opath) throws IOException{
		
		FileReader in = new FileReader(ipath);
		BufferedReader br = new BufferedReader(in);
    	
		FileWriter writer;
		writer = new FileWriter(opath);
		
		String iline = null;
		while((iline = br.readLine()) != null) {
			for(String word: iline.split(" ")) {
				writer.write(word+" "+1+"\n");
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
	
}
