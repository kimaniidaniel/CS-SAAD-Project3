package PlanetSim;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class FauxViewer extends ThreadModel {

	BlockingQueue<Object> queue = null;
	private boolean running =true,
					paused  =false;
	FauxViewer (BlockingQueue<Object> queue){
		this.queue = queue;
	}
	public void run(){
		System.out.println("STARTING VIEWER");
		Map map = null;
		while ( this.running &&!this.paused){
//			System.out.println("WHILE LOOP VIEWER:Counter"+counter++);
				try {
					 map = (Map) queue.take();
//					{System.out.println(map+"-->");}
					
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
			
		}

	public void pause(){
		this.paused = true;
	}
	public void resume(){
		this.paused = false;
	}
	public void stop(){
		this.running = false;
	}
}
