import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class MasterMain {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		int n_workers = 3;
		String machines_path = "/Users/andre.farias/Desktop/MSBigData_GitHub/INF727/Machines_TP.txt";
		
		ArrayList<String> machines = readMachines(machines_path);

		ArrayList<String> workers = workingMachines(machines, n_workers);
		
		makeDir(workers);
		
		// create list of files
		ArrayList<String> files = new ArrayList<String>();
		for(int i = 0; i < 3; i++) {
			files.add("S"+i+".txt");
		}
		
		HashMap<String,ArrayList<String>> workers_to_files =  machineFilesDict(workers,files);
		
		copyFiles(workers_to_files);
		
		launchSlaves(workers_to_files);

	}

	public static ArrayList<String> readMachines(String path) throws IOException{
		FileReader in = new FileReader(path);
		BufferedReader br = new BufferedReader(in);
		ArrayList<String> machines = new ArrayList<String>();
		String line = null;
		while ((line = br.readLine()) != null) {
			machines.add(line);
		}
		in.close();
		br.close();
		return machines;
	}
	
	private static ArrayList<String> outputUm(InputStream inputStream) throws IOException {
		ArrayList<String> output = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while ((line = br.readLine()) != null) {
				output.add(line);
			}
		} finally {
			br.close();
		}
		return output;
	}
	
	public static void launchSlaves(HashMap<String,ArrayList<String>> dict) throws InterruptedException, IOException {
		// HashMap to keep track of processes and machines
		HashMap<String,Process> process_um_map = new HashMap<String,Process>();
		
		// Dictionary of keywords -> list of UM*
		HashMap<String,ArrayList<String>> dict_key_words = new HashMap<String,ArrayList<String>>();
		
		String files_dir = "/tmp/amacedo/splits/";
		
		for(Entry<String,ArrayList<String>> e: dict.entrySet()) {
			String machine = e.getKey();
			ArrayList<String> machine_files = e.getValue();
			for(String infile: machine_files) {
				ProcessBuilder pb = new ProcessBuilder("ssh","amacedo@"+machine,"java","-jar",
						                              "/tmp/amacedo/SLAVE.jar","0",files_dir+infile);
				Process p = pb.start();
				String um_map = getUmName(infile);
				process_um_map.put(um_map,p);
			}
		}
	
		for(Entry<String, Process> e: process_um_map.entrySet()) {
			Process p = e.getValue();
			String um_map = e.getKey();
			p.waitFor(10,TimeUnit.SECONDS);
			InputStream is = p.getInputStream();
			ArrayList<String> key_words_list = outputUm(is);
			for(String word: key_words_list) {
				insertMap(dict_key_words,word,um_map);
			}
			
		}
		System.out.println();
		System.out.println(dict_key_words);
		System.out.println();
		for(Entry<String,ArrayList<String>> e: dict.entrySet()) {
			String machine = e.getKey();
			ArrayList<String> machine_files = e.getValue();
			for(String infile: machine_files) {
				System.out.println(getUmName(infile) + " - " + machine);
			}
		}
	}
	
	public static void insertMap(HashMap<String,ArrayList<String>> dict, String word_key, String um_map) {
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
	
	public static String getUmName(String input_file) {
		String txt_number = input_file.substring(1, input_file.length()-4);
		return "UM" + txt_number + ".txt";
	}
	
	public static void makeDir(ArrayList<String> machines) throws IOException, InterruptedException {
		
		// List to keep track of process
		ArrayList<Process> prList = new ArrayList<Process>(); 
		
		for(String machine: machines) {		
			ProcessBuilder pb = new ProcessBuilder("ssh","amacedo@"+machine,"mkdir","-p","/tmp/amacedo/splits");
			Process p = pb.start();
			prList.add(p);
		}
		for(Process p: prList) {
			p.waitFor(10,TimeUnit.SECONDS);
		}
		System.out.println("mkdir process finalized");
	}

	public static void copyFiles(HashMap<String,ArrayList<String>> dict) throws IOException, InterruptedException {
		
		String files_repo = "/Users/andre.farias/Desktop/MSBigData_GitHub/INF727/Files/";
		// List to keep track of process
		ArrayList<Process> prList = new ArrayList<Process>();
		for(Entry<String,ArrayList<String>> e: dict.entrySet()) {
			String machine = e.getKey();
			ArrayList<String> machine_files = e.getValue();
			for(String file: machine_files) {
				ProcessBuilder pb = new ProcessBuilder("scp", files_repo+file,"amacedo@"
						+machine+":/tmp/amacedo/splits");
				Process p = pb.start();
				prList.add(p);	
			}
		}
		
		for(Process p: prList) {
			p.waitFor(10,TimeUnit.SECONDS);
		}
		System.out.println("copyFiles process finalized");
	}	
	
	public static HashMap<String,ArrayList<String>> machineFilesDict(ArrayList<String> machines, ArrayList<String> files){
		
		HashMap<String,ArrayList<String>> dict = new HashMap<String,ArrayList<String>>();
		
		int i = 0;	// tracks the machine index
		for(String file: files) {
			String machine = machines.get(i);
			if(!dict.containsKey(machine)) {
				ArrayList<String> machine_files = new ArrayList<String>();
				machine_files.add(file);
				dict.put(machine, machine_files);
			}else {
				ArrayList<String> machine_files = dict.get(machine);
				machine_files.add(file);
				dict.replace(machine, machine_files);
			}
			i = (i+1) % machines.size();
		}
		return dict;
	}
	
	public static ArrayList<String> workingMachines(ArrayList<String> machines, int n_workers) throws IOException, InterruptedException {
		
		ArrayList<String> workers = new ArrayList<String>();
		
		// Launch parallelized processes  
		
		// HashMap to keep track of <machine, process>
		HashMap<String,Process> processMap = new HashMap<String,Process>(); 
		
		for(String machine : machines) {
			ProcessBuilder pb = new ProcessBuilder("ssh", "amacedo@"+machine,"hostname");
			Process p = pb.start();
			processMap.put(machine,p);
		}
		// Iterate over HashMap using wait for to check which machines can be connected
		for(Entry<String, Process> e: processMap.entrySet()) {
			Process p = e.getValue();
			String machine = e.getKey();
			
			if(p.waitFor(5,TimeUnit.SECONDS)) {
				String out_is = outputTest(p.getInputStream());
				if(out_is.equals(machine)){
					workers.add(machine);
				}
			}
	
		}
		if(workers.size() >= n_workers) {
			workers = new ArrayList<String> (workers.subList(0,n_workers));
			System.out.println("Workers: "+workers+"\n");
		}
		else {
			System.out.println("There are not enough connected machines");
		}
		return workers;
	}
	
	public static String outputTest(InputStream inputStream) throws IOException {
		BufferedReader br = null;
		ArrayList<String> outputs = new ArrayList<String>();
		String line = null;
		try {
			br = new BufferedReader(new InputStreamReader(inputStream));
			while ((line = br.readLine()) != null) {
				outputs.add(line);
			}
		} finally {
			br.close();
		}
		if(!outputs.isEmpty()){
			return outputs.get(0);
		}else {
			return "";
		}	
	}
}
