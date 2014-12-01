package PlanetSim;

import java.util.concurrent.BlockingQueue;

public class ThreadModel implements Runnable {
	
	private final int BUFFER_SIZE = 32;
	private final boolean debug = false;
	private volatile boolean running = true;
	private volatile boolean pause = true;
	private String thread_name;
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
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
	
	public boolean isRunning(){
		return this.running;
	}
	public boolean isPaused(){
		return this.pause;
	}

	public void pause(){
		this.pause = true;
	}
	public void resume(){
		this.pause = false;
	}
	public void stop(){
		this.running = false;
	}

}
