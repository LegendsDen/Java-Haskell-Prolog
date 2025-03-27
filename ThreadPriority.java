import java.lang.Thread;

class PriThrd extends Thread{
	static int x = 0; 
	String name; PriThrd (String n) { name = n; } 
	public void increment() { 
		x = x+1; System.out.println(x + " " + name); 
	} 
	public void run() { while(true) this.increment();  }
} 
public class ThreadPriority { 
	public static void main(String args[]) {
		PriThrd a = new PriThrd("a"); 
		PriThrd b = new PriThrd("b"); 
		a.setPriority(10); b.setPriority(1);
		a.start(); b.start(); 

		try {
	  	    Thread.sleep(1000);
		} catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}
		//stop(); b.stop();
		//Use Ctrl-C to stop
	} 
}










