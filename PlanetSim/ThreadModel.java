package PlanetSim;

import java.util.concurrent.BlockingQueue;

public class ThreadModel implements Runnable {
	
	private final int BUFFER_SIZE = 32;
	private final boolean debug = true;
	private String thread_name;
	
	@Override
	public void run() {
		//default and needed in the creation of the class
	}
	
	public void setThreadName( String name){
		this.thread_name = name;
	}
	public String getThreadName(){
		return thread_name;
	}
	public boolean isDebug(){
		return this.debug;
	}

}
