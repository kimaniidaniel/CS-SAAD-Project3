package PlanetSim;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.HashMap;

import java.lang.Exception;

import PlanetSim.ThreadModel;
import View.View;
import PlanetSim.Model;

public class Controller extends ThreadModel{
	
	// still confused about the queues but this can be changed
	BlockingQueue<Object> viewQueue = new ArrayBlockingQueue<Object>(1024); // sending
	BlockingQueue<Object> modelQueue = new ArrayBlockingQueue<Object>(1024); // receiving
	
	View ui = new View(viewQueue); //TODO view still needs ?threading? and displaying added to it
	Model model = new Model(modelQueue);

	private int precision;
	private int geographicPrecision;
	private int temporalPrecision;

	public Controller(ArrayList<Map> args){

		for (Map map : args){
			if (isKey("p",map)){ precision = (int)map.get("p"); }
			if (isKey("g",map)){ geographicPrecision = (int)map.get("g"); }
			if (isKey("t",map)){ temporalPrecision = (int)map.get("t"); }
		}

	}
	private boolean isKey(String targetKey,Map map){
		Object temp = map.get(targetKey);
		return temp != null;
	}

	@Override
	public void run(){
		new Thread(ui).start();

		while(this.isRunning()){

		}
	}
	
	public void configure(String name,int temporalPrecision,
			int geographicalPrecision, String startDate, double orbit, double tilt, 
			int gridSpacing, int timeStep, int length){
		
		//TODO we'll need to do validation on these inputs either here or in the UI
		
		model.updateConfig(name, temporalPrecision,
				geographicalPrecision, startDate, orbit, tilt, gridSpacing, 
				timeStep, length);
		
		model.run();
	}
	
	@Override
	public void pause(){
		super.pause();
		
		model.pause();
	}
	
	
	@Override
	public void resume(){
		super.resume();
		
		model.resume();
	}
	
	@Override
	public void stop(){
		super.stop();
		
		model.stop();
	}
}