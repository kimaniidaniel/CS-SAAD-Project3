package PlanetSim;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import PlanetSim.util.GridCell;
//import common.Buffer;
import PlanetSim.util.Grid;
import PlanetSim.util.IGrid;
import PlanetSim.util.Tools;

public final class Simulator extends ThreadModel {

	public static final double CIRCUMFERENCE = 4.003014 * Math.pow(10, 7);
	public static final double SURFACE_AREA = 5.10072 * Math.pow(10, 14);

	public static final int MAX_TEMP = 550; // shot in the dark here...
	public static final int INITIAL_TEMP = 288;
	public static final int MIN_TEMP = 0;

	private static final int DEFAULT_DEGREES = 15;
	private static final int DEFAULT_SPEED = 1; // minutes
	private static final int MAX_DEGREES = 180;
	private static final int A_DAY = 1440;
	private static final int MAX_SPEED = 525600;
	private static final int DEFAULT_SIM_LENGTH = 12 * 30 * 1440;  // one Solar year

	private static final int[] increments = { 1,2,3,4,5,6, 9, 10, 12, 15, 18, 20, 30, 36, 45, 60, 90, 180 };

	private int currentStep;
	private int width;
	private int height;
	private int sunPositionCell;

	private GridCell prime = null;
	private int timeStep = DEFAULT_SPEED;
	private int gs = DEFAULT_DEGREES;
	private int simlen = DEFAULT_SIM_LENGTH;

	BlockingQueue<Object> queue = null;
	Map<String, Number> map = new HashMap<String, Number>();

	//P3 Heated Planet
	public static final double T = 525974.4;				//Orbital period of Earth in minutes
	public static double E = 0.0167; 					//Eccentricity of the planet earth
	//public static final double E = 0.9; 					//EXPERIMENTAL VALUE TO SEE AN ACTUAL ELLIPSE
	public static final double omega = 114;					//Argument of periapsis for the Earth:
	public static double tilt = 23.44;				//Obliquity(tilt) of the planet
	//public static double tilt = 180;				//Obliquity(tilt) of the planet
	public static int tauAN = 0;								//Time of the Equinox
	public static int currentTimeInSimulation = 0;
	private static float sunPositionDeg = 0;

	//planet around sun animation
	public static final double animationGreatestDimention = 150;
	public static final double a = 1.496 * Math.pow(10, 11);//Length of the semi-major axis of earth IN METERS
	public static final double factor = animationGreatestDimention/2*a;
	public static final double b =  a * (Math.sqrt(1-(E * E)));


	public Simulator() {
		//this.q = q;

	}

	public Simulator(BlockingQueue<Object> q) {
		this.queue = q;
	}

	public GridCell getGrid() {
		return prime;
	}

	public void configure() {
		this.configure(this.gs, this.timeStep, this.simlen);
	}
	
	public void configure(double eccentricity, double Tilt, int gs, int timeStep, int simlength) {
//		System.out.println("Ecc: " + eccentricity + ", Tilt: " + Tilt + ", Grid: " + gs + ", TStp: " + timeStep + ", SLth:" + simlength);
		E = eccentricity;
		tilt = Tilt;
		configure(gs, timeStep, simlength);
	}

	public void configure(int gs, int timeStep, int simlength) {
		// sim length is in minute

		if (gs <= 0 || gs > MAX_DEGREES)
			throw new IllegalArgumentException("Invalid grid spacing");

		if (timeStep <= 0 || timeStep > MAX_SPEED)
			throw new IllegalArgumentException("Invalid speed setting");

		this.timeStep = timeStep;
		//System.out.println("Simulator: TimeStep: "+timeStep);

		// The following could be done better - if we have time, we should do so
		if (MAX_DEGREES % gs != 0) {
			for (int i=0; i < increments.length; i++) {
				if (gs > increments[i]) {
					this.gs = increments[i];
				}
			}

//			System.out.println("gs: " + this.gs);
		} else
			this.gs = gs;

		if (simlength != -1) {
			this.simlen = this.month2Minite(simlength);
			//System.out.println("Simulator: SimLen: "+simlen);
			
		}

		this.initiate();

		this.resume();
	}

	private void initiate() {

		// reset the current time in simulation
		currentTimeInSimulation = 0;

		int x = 0, y = 0;

		width = (2 * MAX_DEGREES / this.gs); // rows
		height = (MAX_DEGREES / this.gs); // cols

		// do a reset
		sunPositionCell = (width / 2) % width;
		currentStep = 0;

		if (prime != null)			prime.setTemp(INITIAL_TEMP);
		else
			prime = new GridCell(INITIAL_TEMP, x, y, this.getLatitude(y), this.getLongitude(x), this.gs);

		prime.setTop(null);

		// South Pole
		GridCell next = null, curr = prime;
		for (x = 1; x < width; x++) {

			this.createRowCell(curr, next, null, x, y);
			curr = curr.getLeft();
		}

		// Stitch the grid row together
		prime.setRight(curr);
		curr.setLeft(prime);

		// Create each grid row, with the exception of the south pole
		GridCell bottom = prime, left = null;
		for (y = 1; y < height - 1; y++) {

			// curr should be changed, but actually have not.
			this.createNextRow(bottom, curr, y);

			curr = bottom.getTop();

			// left should be changed, but actually have not.
			this.createRow(curr, next, bottom.getLeft(), left, y);
			bottom = bottom.getTop();
		}

		this.createNextRow(bottom, curr, y);
		curr = bottom.getTop();

		// North Pole
		this.createRow(curr, next, bottom.getLeft(), left, y);

		// Calculate the average sun temperature
		float totaltemp = 0;
		float totalarea = 0;
		curr = prime;

		for (x = 0; x < height; x++) {
			GridCell rowgrid = curr.getLeft();
			for (y = 0; y < width; y++) {
				totaltemp += rowgrid.calTsun(sunPositionCell);
				totalarea += rowgrid.getSurfarea();
				rowgrid = rowgrid.getLeft();
			}
			curr = curr.getTop();
		}
		// Set initial average temperature
//		System.out.println("Initial sun temp: " + totaltemp / (width * height));
//		System.out.println("Initial total area: " + totalarea / (width * height));
		GridCell.setAvgSuntemp(totaltemp / (width * height));
		GridCell.setAverageArea(totalarea / (width * height));
	}

	public void run() {
//		System.out.println("still running");
		//System.out.println("During simulation Ecc: " + E + ", Tilt: " + tilt + ", Grid: " + this.gs + ", TStp: " + this.timeStep + ", SLth:" + this.simlen);

//		this.configure();
		this.initiate();
		while (this.isRunning()) {
			while (!this.isPaused() && !this.isComplete()) {
				try {
					this.generate();
//					System.out.println("still running");
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
		}
	}

	public boolean isComplete() {
		//if (currentTimeInSimulation < this.simlen){
		//	return false;
		//} else {
		//	this.stop();
		//	return true;
		//}
		if (currentTimeInSimulation >= this.simlen){
			System.out.println("SIM:COMPLETE:"+(currentTimeInSimulation < this.simlen )+":CURRENTTIME:"+currentTimeInSimulation+":SIMLENGTH:"+this.simlen);
			this.stop();
			return true;
		} else {
			return false;
		}
	}

	public void generate() throws InterruptedException {

//		System.out.println(currentStep);
//		System.out.println(this.prime.getSunLatitudeOnEarth());
//		this.printGrid();
//		System.out.println("Ecc: " + E + ", Tilt: " + tilt + ", Grid: " + this.gs + ", TStp: " + this.timeStep + ", SLth:" + this.simlen);

		//System.out.println("generating grid...");
		Queue<GridCell> bfs = new LinkedList<GridCell>();
		Queue<GridCell> calcd = new LinkedList<GridCell>();

		currentStep++;

		int t = timeStep * currentStep;
//		int t = currentStep;
		//System.out.println("Timestep: " + timeStep + ", currentStep: " + currentStep);
		int rotationalAngle = 360 - ((t % A_DAY) * 360 / A_DAY);
		sunPositionCell = ( (width * rotationalAngle) / 360 ) % width;

		sunPositionDeg = rotationalAngle;
		if(sunPositionDeg>180) {
			sunPositionDeg = sunPositionDeg - 360;
		}

		IGrid grid = new Grid(sunPositionCell, sunPositionDeg, t, width, height);

		float suntotal = 0;
		float calcdTemp = 0;

		calcdTemp = prime.calculateTemp(sunPositionCell);
		suntotal = suntotal + prime.calTsun(sunPositionCell);
		grid.setTemperature(prime.getX(), prime.getY(), calcdTemp);

		prime.visited(true);
		bfs.add(prime);
		
		//P3 - Heated Planet
		//Earth.currentTimeInSimulation = t * 100;  // for speeding up the simulation
		Simulator.currentTimeInSimulation = t;

		map.put("Lon", (double) prime.getLongitude());
		map.put("Lat", (double) prime.getLatitude());
		map.put("Temp", (double) prime.getTemp());
		map.put("Iter", Simulator.currentTimeInSimulation);
		map.put("Day", Tools.convertIterationToLong(simlen, Simulator.currentTimeInSimulation));
		map.put("Min", Tools.convertIterationToLong(simlen, Simulator.currentTimeInSimulation));
		// Adding data to array block queue
		queue.put(map);
		
//		System.out.println("Current time on Earth:" + Simulator.currentTimeInSimulation);
//		System.out.println("Distance from the Sun:" + prime.distanceFromPlanet(Simulator.currentTimeInSimulation));

		while (!bfs.isEmpty()) {

			GridCell point = bfs.remove();
			calcd.add(point);

			GridCell child = null;
			Iterator<GridCell> itr = point.getChildren(false);

			while (itr.hasNext()) {

				child = itr.next();
				child.visited(true);
				calcdTemp = child.calculateTemp(sunPositionCell);
				grid.setTemperature(child.getX(), child.getY(), calcdTemp);
				bfs.add(child);
				suntotal += child.calTsun(sunPositionCell);

				map.put("Lon", (double) child.getLongitude());
				map.put("Lat", (double) child.getLatitude());
				map.put("Temp", (double) child.getTemp());
				map.put("Iter", Simulator.currentTimeInSimulation);
				map.put("Day", Tools.convertIterationToLong(simlen, Simulator.currentTimeInSimulation));
				map.put("Min", Tools.convertIterationToLong(simlen, Simulator.currentTimeInSimulation));
				// Adding data to array block queue
				queue.put(map);

//                System.out.println(map + "<<<");

			}
		}

		
//		System.out.println("Current average sun temperature:");
//		System.out.println(suntotal /  (width * height));

		GridCell.setAvgSuntemp(suntotal /  (width * height));
		GridCell c = calcd.poll();
		while (c != null) {
			c.visited(false);
			c.swapTemp();
			c = calcd.poll();
		}

		//Set display values here
		grid.setSunLatitudeDeg((float) (-1 * prime.getSunLatitudeOnEarth()));
		grid.setPlanetX(prime.getPlanetX(Simulator.currentTimeInSimulation));
		grid.setPlanetY(prime.getPlanetY(Simulator.currentTimeInSimulation));

		//P3 Heated Planet: Set time of equinox
		setTimeOfEquinox();
	}

	private void createRow(GridCell curr, GridCell next, GridCell bottom,
			GridCell left, int y) {

		for (int x = 1; x < width; x++) {

			this.createRowCell(curr, next, bottom, x, y);
			bottom = bottom.getLeft();
			curr = curr.getLeft();
		}

		left = bottom.getTop(); // This should be the first cell we created

		// Stitch the grid row together
		curr.setLeft(left);
		left.setRight(curr);
	}

	private void createRowCell(GridCell curr, GridCell next, GridCell bottom,
			int x, int y) {

		if (curr.getLeft() != null) {
			GridCell l = curr.getLeft();
			l.setTemp(INITIAL_TEMP);
			l.setGridProps(x, y, this.getLatitude(y), this.getLongitude(x), this.gs);
		} else {
			next = new GridCell(null, bottom, null, curr, INITIAL_TEMP, x, y, this.getLatitude(y), this.getLongitude(x), this.gs);
			curr.setLeft(next);
			if (bottom != null) {
				bottom.setTop(next);
			}
		}
	}

	private void createNextRow(GridCell bottom, GridCell curr, int y) {

		if (bottom.getTop() != null) {
			curr = bottom.getTop();
			curr.setTemp(INITIAL_TEMP);
			curr.setGridProps(0, y, this.getLatitude(y), this.getLongitude(0), this.gs);
		} else {
			curr = new GridCell(null, bottom, null, null, INITIAL_TEMP, 0, y, this.getLatitude(y), this.getLongitude(0), this.gs);
			bottom.setTop(curr);
		}
	}

	private int getLatitude(int y) {
		return (y - (height / 2)) * this.gs;
	}

	private int getLongitude(int x) {
		return x < (width / 2) ? -(x + 1) * this.gs : (360) - (x + 1) * this.gs;
	}


	public void setTimeOfEquinox() {
		// actually two days for equinox, one is March 21, one is Sept 23
		// What I got is actually April 22nd, the beginning day is Jan. 1st.
		int t=0;
		double distClose = 1000;
		for ( ; t < Simulator.T; t++) {
			double trueAnomaly = prime.trueAnomaly(t);
			//System.out.println("\n" + "trueAnamoly " + trueAnamoly);
			double dist = Math.abs(Math.toRadians(Simulator.omega)- trueAnomaly);
			if(dist <= distClose)			//Try 10 as a limit to try first
			{
				tauAN = t;
				distClose = dist;
			}
		}
	}

	public void setcurrentStep(int step){
		this.currentStep = step;
	}

	public void setE(double newE) {
		Simulator.E = newE;
	}

	public void setTilt(double newTilt) {
		Simulator.tilt = newTilt;
	}

	public int getWidth() {
		return this.width;
	}
	
	public float getSunPositionDeg() {
		return Simulator.sunPositionDeg;
	}
	
	public int month2Minite(int month) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(Tools.getStartDate());
		cal.add(Calendar.MONTH, month);
		long startDate = Tools.getStartDate().getTime();
		long endDate = cal.getTimeInMillis();

		return (int) ((endDate - startDate) / 60000);
	}
	
	

	public void diffTemp(){
	GridCell curr = this.prime;
	//System.out.println(height);
	//System.out.println(width);
	float total = 0;
	for (int x = 0; x < height; x++) {
		GridCell rowgrid = curr.getLeft();
		for (int y = 0; y < width; y++) {
			//System.out.printf("%.2f,",rowgrid.getLongitude());
//			System.out.printf("%2d,",rowgrid.getLongitude());
//			System.out.printf("%.2f,",rowgrid.getTemp());
			rowgrid = rowgrid.getLeft();
			total += rowgrid.getTemp() - 288;
		}
//		System.out.println();
		curr = curr.getTop();
	}
	//System.out.println(total);
}
	

	public void printGrid(){
		GridCell curr = this.prime;
		//System.out.println(height);
		//System.out.println(width);
		float total = 0;
		for (int x = 0; x < height; x++) {
			GridCell rowgrid = curr.getLeft();
			for (int y = 0; y < width; y++) {
				//System.out.printf("%.2f,",rowgrid.getLongitude());
//				System.out.printf("%2d,",rowgrid.getLongitude());
				//System.out.printf("%.2f,",rowgrid.getTemp());
				rowgrid = rowgrid.getLeft();
				total += rowgrid.getTemp() - 288;
			}
			//System.out.println();
			curr = curr.getTop();
		}
		//System.out.println(total);
	}
//
//	private void printMap(Map<String, Number> map){
//		System.out.println("Lon:" + map.get("Lon"));
//		System.out.println("Lat:" + map.get("Lat"));
//		System.out.println("Temp:" + map.get("Temp"));
//		System.out.println("Iter:" + map.get("Iter"));
//	}
}