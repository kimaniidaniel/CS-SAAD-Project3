package Controller;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.HashMap;

import java.lang.Exception;

import PlanetSim.ThreadModel;
import View.View;
import PlantSim.Model;

public class Controller extends ThreadModel{
	
	// still confused about the queues but this can be changed
	BlockingQueue<Object> viewQueue = new ArrayBlockingQueue<Object>(1024); // sending
	BlockingQueue<Object> modelQueue = new ArrayBlockingQueue<Object>(1024); // receiving
	
	View ui = new View(viewQueue); //TODO view still needs ?threading? and displaying added to it
	Model model = new Model(modelQueue);
	
	Thread uiThread = new Thread(view); // Thread deprecated the stop, suspend, and pause methods
	
	@override
	public void run(){
		uiThread.start();
	}
	
	public void configure(String name,int  storagePrecision,int temporalPrecision,
			int geographicalPrecision, String startDate, double orbit, double tilt, 
			int gridSpacing, int timeStep, int length){
		
		//TODO we'll need to do validation on these inputs either here or in the UI
		
		model.updateConfig(name, storagePrecision, temporalPrecision, 
				geographicalPrecision, startDate, orbit, tilt, gridSpacing, 
				timeStep, length);
		
		model.run();
	}
	
	@override
	public void pause(){
		super.pause();
		
		model.pause();
		//uiThread.pause();
	}
	
	
	@override
	public void resume(){
		super.resume();
		
		model.resume();
		//uiThread.resume();
	}
	
	@override
	public void stop(){
		super.stop();
		
		model.stop();
		//uiThread.stop();
	}
}