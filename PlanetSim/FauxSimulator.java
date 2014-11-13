package PlanetSim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class FauxSimulator extends ThreadModel  {
	
	BlockingQueue queue = null;
	Map<String, Number> map = new HashMap<String, Number>();
	ArrayList<Map> list = new ArrayList();
	boolean paused = false;
	boolean done = false;
	FauxSimulator(BlockingQueue queue){
		this.queue = queue;
	}
	int tempurature = 100;
	public void run(){
		int counter = 0;
		while ( counter++ < 10000 && !paused){
			for (int i = 359; i > -1 ; i--){
				for (int j = 179; j > -1; j--){
					map.put("Lon", (double)i);
					map.put("Lat", (double)j);
					map.put("Temp", (double)tempurature);
					map.put("Iter", counter);
				}
				list.add(map);
				map = new HashMap();
				tempurature = ((tempurature > -1) ? --tempurature : 100);
			}
			try {
				queue.put(list);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(0);
			}
			list = new ArrayList();
		}
		this.done =true;
	}
	
	public boolean isDone(){
		return done;
	}

}
