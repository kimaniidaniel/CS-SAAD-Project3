package Controller;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.HashMap;

import java.lang.Exception;

import PlanetSim.ThreadModel;
import View.View;
import PlanetSim.Model;

public class Controller extends ThreadModel{
	private int precision;
	private int geographicPrecision;
	private int temporalPrecision;
	// still confused about the queues but this can be changed
	BlockingQueue<Object> viewQueue = new ArrayBlockingQueue<Object>(1024); // sending
	BlockingQueue<Object> modelQueue = new ArrayBlockingQueue<Object>(1024); // receiving
	
	View ui = new View(viewQueue); //TODO view still needs ?threading? and displaying added to it
	Model model = new Model(modelQueue);
	
	Thread uiThread = new Thread(view); // Thread deprecated the stop, suspend, and pause methods
	
	@Override
	public void run(){
		uiThread.start();

		while (this.isRunning()){

			while (!uiThread.newConfigStarted()&&uiThread.isRunning()){
				Thread.sleep(1000);
			}
			if (!uiThread.isRunning()){ System.exit(0); }

			uiThread.configReset();		//reset new configuration flag
			this.model.updateConfig(uiThread.getName(),geographicPrecision,temporalPrecision,"04-Jan-2012",uiThread().getOrbit(),uiThread.getTilt,uiThread.getGSpacing(),uiThread.tStep(),uiThread.getDuration());
			new Thread (model).start();
			while(!model.isComplete()&& model.isRunning()){
				if (uiThread.isPaused()){
					model.pause();
				}else{
					if (!uiThread.isRunning()){
						model.stop();
					}else{
						if (!uiThread.isPaused()){
							model.resume();
						}
					}
				}
				Thread.sleep(500);
			}
		}
	}
	
	public void configure(String name,int  storagePrecision,int temporalPrecision,
			int geographicalPrecision, String startDate, double orbit, double tilt, 
			int gridSpacing, int timeStep, int length){
		
		//TODO we'll need to do validation on these inputs either here or in the UI
		
		model.updateConfig(name, temporalPrecision,
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