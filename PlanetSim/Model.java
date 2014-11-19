package PlanetSim;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Model extends ThreadModel{

	BlockingQueue<Object> simQueue = new ArrayBlockingQueue<Object>(1024);		//queue to simulator
	BlockingQueue<Object> viewQueue = new ArrayBlockingQueue<Object>(1024);		//queue to contoller
	DBModel db = null;

	Simulator sim = new Simulator(simQueue);	//place holder for simulation object
	//used by the controller for creation - passes the queue for consumption
	public Model (BlockingQueue<Object> viewQueue){
		this.viewQueue = viewQueue;
	}
	@SuppressWarnings("unused")
	public Model (){
	}
	@SuppressWarnings("rawtypes")
        @Override
	public void run(){
		Map map = null;
		new Thread(sim).start();
		while (this.isRunning()){							//add && (!sim.complete())
			System.out.println("STARTING MODELTHREAD");
			while (!this.isPaused()){
				try {
					map = dequeue(simQueue);	//retrieves data from simulator
					this.db.storeMap(map);		//presents to the DBModel for processing
					enqueue(viewQueue,map);		//returns the values to the controller
				} catch (InterruptedException e) {
					e.printStackTrace();
					//sim.stop();
					this.db.closeDBSession();
					this.stop();
					System.exit(0);
				}
			}
		}
	}

	//used by the controller to update the configuration
	public void updateConfig(String Name,int  StoragePrecision,int TemporalPrecision,int GeographicalPrecision, String StartDate, double Orbit, double Tilt, int GridSpacing, int TimeStep, int Length){
		if (this.isDebug()) { System.out.println("UDPATING SIM CONFIGURATION"); }
		this.db = new DBModel( Name, StoragePrecision, TemporalPrecision, GeographicalPrecision, StartDate, Orbit, Tilt, GridSpacing, TimeStep, Length);
		this.sim.configure(Orbit, Tilt, GridSpacing, TimeStep, Length);
	}
	@SuppressWarnings({ "rawtypes" })
	private Map dequeue(BlockingQueue<Object> queue) throws InterruptedException{
			return (Map) queue.take();
	}
	private void enqueue(BlockingQueue<Object> queue, Object item) throws InterruptedException{
			queue.put(item);
}
	public void stopModel(){
		if (this.isDebug()) { System.out.println("MODEL STOPPING"); }
		//this.sim.stop();
		this.stop();
	}
        @Override
	public void pause(){
		if (this.isDebug()) { System.out.println("MODEL PAUSED"); }
		//this.sim.pause();
		try { this.db.manualCommit();
		} catch (SQLException e) {
			e.printStackTrace();
			this.stop();
		}
		this.pause();
	}
	public void unpause(){
		if (this.isDebug()) { System.out.println("MODEL RESUMING"); }
		//this.sim.resume();
		this.resume();
	}
	// used for debugging and sim and performance testing
	@SuppressWarnings({ "rawtypes"})
	public void runFauxSimAndSim() throws InterruptedException{
	     int 			storagePrecision = 0;
	     int 			temporalPrecision = 100;
	     int 			geographicaPrecision = 100;
	     String 		startDate= "WHATEVER";
		 double 		orbit=1D;
		 double 		tilt=1D;
		 int 			gridSpacing = 10;
		 int 			timeStep = 10;
		 int 			length = 100;
	     String 		name = "";
		Map map = null;
		this.db = new  DBModel(name, storagePrecision, temporalPrecision, geographicaPrecision, startDate, orbit, tilt, gridSpacing, timeStep, length);
		Simulator sim = new Simulator(simQueue);
		sim.configure(orbit, tilt, gridSpacing, timeStep, length);
		FauxViewer view = new FauxViewer(viewQueue);
		new Thread(sim).start();
		new Thread(view).start();
		while (sim.isRunning()){
				map = dequeue(simQueue);
				this.db.storeMap(map);
				enqueue(viewQueue,map);
		}

		view.stop();			//stops viewer
		this.db.closeDBSession();	//cleans up the db
		this.stop();

	}

}