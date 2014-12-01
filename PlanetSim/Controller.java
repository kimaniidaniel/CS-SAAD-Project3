package PlanetSim;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by amounib on 11/20/2014.
 */
public class Controller extends ThreadModel{
	private int precision;
	private int geographicPrecision;
	private int temporalPrecision;
	static final String DEFAULT_DATE = "04-Jan-2014";

	// still confused about the queues but this can be changed
	BlockingQueue<Object> viewQueue = new ArrayBlockingQueue<Object>(1024); // sending
	// BlockingQueue<Object> modelQueue = new ArrayBlockingQueue<Object>(1024); // receiving

	View ui = new View(viewQueue); //TODO view still needs ?threading? and displaying added to it
	Model model = new Model(viewQueue);
	Thread uiThread = new Thread(ui); // Thread deprecated the stop, suspend, and pause methods

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
		uiThread.start();
		System.out.println("STARTING CONTOLLER");
		while (this.isRunning()){
			System.out.println("Controlller Loop : Main");
			while (!ui.newConfigStarted() && ui.isRunning()){
				System.out.println("Controller Waiting for start");
				ui.stop(); // hack to get UI reset if model is completed...
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (!ui.isRunning()){ System.exit(0); }
			System.out.println("new config started");
			ui.configReset();		//reset new configuration flag
			this.model.updateConfig(ui.getSimName(),temporalPrecision,geographicPrecision,DEFAULT_DATE,
					ui.getOrbit(),ui.getTilt(),ui.getGSpacing(),ui.getStep(),ui.getDuration());
			new Thread (model).start();
			
			while(!model.isComplete() && model.isRunning() && !ui.newConfigStarted()){
				if (ui.isPaused()){
					System.out.println("Controller:PAUSE");
					model.pause();
				}else{
					if (!ui.isRunning()){
						System.out.println("Controller:STOP");
						model.stop();
					}else{
						if (!ui.isPaused() && model.isPaused()){
							System.out.println("Controller:RESUME");
							model.resume();
						}
						else
						{
							System.out.println("Controller running");	
						}
					}
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			
			if(ui.newConfigStarted()) {
				System.out.println("Model Stop");
				model.stop();
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
	
	@Override
	public void pause(){
		super.pause();
		
		model.pause();
		//uiThread.pause();
	}
	
	
	@Override
	public void resume(){
		super.resume();
		
		model.resume();
		//uiThread.resume();
	}
	
	@Override
	public void stop(){
		super.stop();
		
		model.stop();
		//uiThread.stop();
	}
}