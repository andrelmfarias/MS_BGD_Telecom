import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class CleanMain {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		String path = "/Users/andre.farias/Desktop/MSBigData_GitHub/INF727/Projects/Machines_TP.txt";
		
		ArrayList<String> machines = readMachines(path);
		ArrayList<String> workers = workingMachines(machines);
		clean(workers);

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
			if(p.waitFor(10,TimeUnit.SECONDS)) {
				String out_is = outputTest(p.getInputStream());
				if(out_is.equals(machine)){
					workers.add(machine);
				}
			}
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
	
	public static void clean(ArrayList<String> machines) throws IOException, InterruptedException {
		
		// List to keep track of process
		ArrayList<Process> prList = new ArrayList<Process>(); 
		
		for(String machine: machines) {		
			ProcessBuilder pb = new ProcessBuilder("ssh","amacedo@"+machine,"rm","-Rf","/tmp/amacedo/splits");
			Process p = pb.start();
			prList.add(p);
		}
		
		for(String machine: machines) {		
			ProcessBuilder pb = new ProcessBuilder("ssh","amacedo@"+machine,"rm","-Rf","/tmp/amacedo/maps");
			Process p = pb.start();
			prList.add(p);
		}
		
		for(String machine: machines) {		
			ProcessBuilder pb = new ProcessBuilder("ssh","amacedo@"+machine,"rm","-Rf","/tmp/amacedo/reduces");
			Process p = pb.start();
			prList.add(p);
		}
		
		for(String machine: machines) {		
			ProcessBuilder pb = new ProcessBuilder("ssh","amacedo@"+machine,"rm","/tmp/amacedo/SLAVE.jar");
			Process p = pb.start();
			prList.add(p);
		}
		
		for(Process p: prList) {
			p.waitFor(10,TimeUnit.SECONDS);
		}
		System.out.println("Clean process finalized");
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
	
}
