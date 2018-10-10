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
	
	private static void output(InputStream inputStream) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + System.getProperty("line.separator"));
			}
		} finally {
			br.close();
		}
		String outputStr = sb.toString();
		if(outputStr.length()!=0) {
			System.out.println(outputStr.substring(0,outputStr.length()-1));
		}
	}
	
	public static void launchSlave(ArrayList<String> machines) throws InterruptedException, IOException {
		// HashMap to keep track of processes and machines
		HashMap<String,Process> processMap = new HashMap<String,Process>(); 
		
		for(String machine: machines) {		
			ProcessBuilder pb = new ProcessBuilder("ssh","amacedo@"+machine,"java","-jar",
												   "/tmp/amacedo/SLAVE.jar");
			Process p = pb.start();
			processMap.put(machine,p);
		}
		for(Entry<String, Process> e: processMap.entrySet()) {
			Process p = e.getValue();
			String machine = e.getKey();
			p.waitFor(10,TimeUnit.SECONDS);
			System.out.println("Output on machine "+machine+":");
			output(p.getInputStream());
			output(p.getErrorStream());
			System.out.println();
		}
		
		// We can also just declare as below and we will not need a 
		// ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/amacedo/SLAVE.jar").inheritIO();
		System.out.println("Slave process finalized");
		
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

	public static void copyFiles(ArrayList<String> machines, ArrayList<String> files) throws IOException, InterruptedException {
		
		String files_repo = "/Users/andre.farias/Desktop/MSBigData_GitHub/INF727/Files/";
		// List to keep track of process
		ArrayList<Process> prList = new ArrayList<Process>();
		int i = 0;	// tracks the machine index
		for(String file: files) {	
			ProcessBuilder pb = new ProcessBuilder("scp", files_repo+file,"amacedo@"
													+machines.get(i)+":/tmp/amacedo/splits");
			Process p = pb.start();
			prList.add(p);
			i = (i+1) % machines.size();
		}
		
		for(Process p: prList) {
			p.waitFor(10,TimeUnit.SECONDS);
		}
		System.out.println("copyFiles process finalized");
	}	
	
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		int n_workers = 3;
		String path = "/Users/andre.farias/Desktop/MSBigData_GitHub/INF727/Machines_TP.txt";
		
		ArrayList<String> machines = readMachines(path);

		ArrayList<String> workers = workingMachines(machines, n_workers);
		
		makeDir(workers);
		
		// create list of files
		ArrayList<String> files = new ArrayList<String>();
		for(int i = 0; i < 3; i++) {
			files.add("S"+i+".txt");
		}
		
		copyFiles(workers,files);
		
		//launchSlave(machines);

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
