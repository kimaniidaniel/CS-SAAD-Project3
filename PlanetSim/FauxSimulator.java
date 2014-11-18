package PlanetSim;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class FauxSimulator extends ThreadModel  {
	
	BlockingQueue<Object> queue = null;
	Map<String, Number> map = new HashMap<String, Number>();
	@SuppressWarnings("rawtypes")
	boolean paused = false;
	boolean done = false;
	FauxSimulator(BlockingQueue<Object> simQueue){
		this.queue = simQueue;
	}
	int tempurature = 100;
	public void run(){
		int counter = 0;
		int dayCtr = 0;
		while ( counter++ < 10000 && !paused){
			System.out.println("WHILE LOOP:Counter"+counter);
			for (int i = 359; i > -1 ; i--){
				for (int j = 179; j > -1; j--){
					map.put("Lon", (double)j);
					map.put("Lat", (double)i);
					map.put("Temp", (double)tempurature);
					map.put("Iter", counter);
					map.put("Day", dayCtr);
					map.put("Min", counter);
					try { queue.put(map); } catch (InterruptedException e) { e.printStackTrace();System.exit(0); }
					map = new HashMap<String, Number>();
				}
				tempurature = ((tempurature > -1) ? --tempurature : 100);	
			}
			if (counter==1440){ dayCtr++; }
		}
		this.done =true;
		System.out.println("DONE");
	}
	
	public boolean isDone(){
		return done;
	}

}
