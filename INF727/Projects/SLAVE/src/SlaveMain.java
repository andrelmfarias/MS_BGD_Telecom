
public class SlaveMain {

	public static void main(String[] args) throws InterruptedException {
		int a = (3+5);
		long startTime = System.currentTimeMillis();
		Thread.sleep(3000); 
		long endTime = System.currentTimeMillis();	
		long secs = (endTime - startTime)/1000;
		System.out.format("The time of execution is %ds\n",secs);
		System.out.println("3+5 = "+a);
	}

}
