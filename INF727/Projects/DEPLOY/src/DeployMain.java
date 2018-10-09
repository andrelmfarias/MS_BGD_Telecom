import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class DeployMain {
	
	public static String output(InputStream inputStream) throws IOException {
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
	
	
	public static ArrayList<String> readMachines(String path) throws IOException{
		FileReader in = new FileReader(path);
		BufferedReader br = new BufferedReader(in);
		ArrayList<String> machines = new ArrayList<String>();
		String line = null;
		while ((line = br.readLine()) != null) {
			machines.add(line);
		}
		in.close();
		return machines;
	}
	
	public static ArrayList<String> workingMachines(ArrayList<String> machines) throws IOException, InterruptedException {
		
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
				String out_is = output(p.getInputStream());
				String out_es = output(p.getErrorStream());
				if(out_is.equals(machine)){
					System.out.println(machine+": OK");
					workers.add(machine);
				} else{
					System.err.println("Cannot connect to "+machine);
					if(!out_es.isEmpty()) {
						System.err.println("Error message: "+out_es);
					}
				}
			}else { //if we wait too much
				System.err.println("Timeout: Cannot connect to "+machine);
			}
		}
		if(!workers.isEmpty()) {
			System.out.println("Workers: "+workers+"\n");
		}
		else {
			System.out.println("There are not connected machines");
		}
		return workers;
	}
	
	public static void makeDir(ArrayList<String> machines) throws IOException, InterruptedException {
		
		// List to keep track of process
		ArrayList<Process> prList = new ArrayList<Process>(); 
		
		for(String machine: machines) {		
			ProcessBuilder pb = new ProcessBuilder("ssh","amacedo@"+machine,"mkdir","-p","/tmp/amacedo");
			Process p = pb.start();
			prList.add(p);
		}
		for(Process p: prList) {
			p.waitFor(3,TimeUnit.SECONDS);
		}
		System.out.println("mkdir process finalized");
	}
	
	public static void copyFile(ArrayList<String> machines) throws IOException, InterruptedException {
		
		// List to keep track of process
		ArrayList<Process> prList = new ArrayList<Process>(); 
		
		String slave_path = "/Users/andre.farias/Desktop/MSBigData_GitHub/INF727/SLAVE.jar";
	
		for(String machine: machines) {	
			ProcessBuilder pb = new ProcessBuilder("scp", slave_path,"amacedo@"
													+machine+":/tmp/amacedo");
			Process p = pb.start();
			prList.add(p);
		}
		
		for(Process p: prList) {
			p.waitFor(3,TimeUnit.SECONDS);
		}
		System.out.println("scp process finalized");
	}


	public static void main(String[] args) throws IOException, InterruptedException {
		
		String path = "/Users/andre.farias/Desktop/MSBigData_GitHub/INF727/Machines_TP.txt";
		int n_workers = 3;
		
		ArrayList<String> machines = readMachines(path);
		ArrayList<String> workers = workingMachines(machines);
		if(!workers.isEmpty() && workers.size() >= n_workers) {
			// taking a sublist of n_workers
			workers = new ArrayList<String> (workers.subList(0,n_workers)); 
			makeDir(workers);
			copyFile(workers);
		}
	}
}
