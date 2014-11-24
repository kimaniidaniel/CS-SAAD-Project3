package PlanetSim.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import PlanetSim.Simulator;

public final class GridCell implements EarthCell<GridCell> {

	// gs: grid spacing
	public int x, y, latitude, longitude, gs;
	
	// average temperature
	private static float avgsuntemp;
	private static float avgArea;

	private boolean visited;
	private float currTemp, newTemp;

	private GridCell top = null, bottom = null, left = null, right = null;

	// Cell properties: surface area, perimeter
	private float lv, lb, lt, surfarea, pm;
	
	//private static final int suntemp = 278;
	private static final int suntemp = 50;

	public GridCell(float temp, int x, int y, int latitude, int longitude, int gs) {

		if (temp > Float.MAX_VALUE) throw new IllegalArgumentException("Invalid temp provided");
		if (x > Integer.MAX_VALUE || x < Integer.MIN_VALUE) throw new IllegalArgumentException("Invalid 'x' provided");
		if (y > Integer.MAX_VALUE || y < Integer.MIN_VALUE) throw new IllegalArgumentException("Invalid 'y' provided");

		this.setGridProps(x, y, latitude, longitude, gs);

		this.setTemp(temp);
		this.visited = false;
	}

	public GridCell(GridCell top, GridCell bottom, GridCell left, GridCell right, float temp, int x, int y, int latitude, int longitude, int gs) {
		
		this(temp, x, y, latitude, longitude, gs);

		this.setTop(top);
		this.setBottom(bottom);
		this.setLeft(left);
		this.setRight(right);
	}

	@Override
	public void setTop(GridCell top) {

		if (top == null) return;
		this.top = top;
	}

	@Override
	public GridCell getTop() {
		return this.top;
	}

	@Override
	public void setBottom(GridCell bottom) {

		if (bottom == null) return;
		this.bottom = bottom;
	}

	@Override
	public GridCell getBottom() {
		return this.bottom;
	}

	@Override
	public void setRight(GridCell right) {

		if (right == null) return;
		this.right = right;
	}

	@Override
	public GridCell getRight() {
		return this.right;
	}

	@Override
	public void setLeft(GridCell left) {

		if (left == null) return;
		this.left = left;
	}

	@Override
	public GridCell getLeft() {
		return this.left;
	}

	@Override
	public float getTemp() {
		return this.currTemp;
	}

	@Override
	public void setTemp(float temp) {

		if (temp > Float.MAX_VALUE) throw new IllegalArgumentException("Invalid temp provided");
		this.currTemp = temp;
	}

	@Override
	public void setGridProps(int x, int y, int latitude, int longitude, int gs) {

		this.setX(x);
		this.setY(y);
		this.setLatitude(latitude);
		this.setLongitude(longitude);
		this.setGridSpacing(gs);

		// calc lengths, area, etc.
		this.calSurfaceArea(latitude, gs);
	}

	@Override
	public void setLatitude(int latitude) {
		this.latitude = latitude;
	}

	@Override
	public void setLongitude(int longitude) {
		this.longitude = longitude;
	}

	@Override
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public void setY(int y) {
		this. y = y;
	}
	
	@Override
	public float calculateTemp(int sunPosition) {
		float temp   = this.currTemp + (calTneighbors() - this.currTemp) / 5 + ( calTsun(sunPosition) + calTcool() ) / 10;
		this.newTemp = (temp > 0) ? temp : 0;    // avoid negative temperature
		return this.newTemp; // new temp
	}

	@Override
	public void swapTemp() {
		this.currTemp = this.newTemp;
		this.newTemp = 0;
	}

	@Override
	public void visited(boolean visited) {
		this.visited = visited;
	}

	@Override
	public Iterator<GridCell> getChildren(boolean visited) {
		List<GridCell> ret = new ArrayList<GridCell>();

		if (this.top != null 	&& this.top.visited == visited) 	ret.add(this.top);
		if (this.bottom != null && this.bottom.visited == visited) 	ret.add(this.bottom);
		if (this.left != null 	&& this.left.visited == visited) 	ret.add(this.left);
		if (this.right != null 	&& this.right.visited == visited) 	ret.add(this.right);

		return ret.iterator();
	}

	@Override
	public int getX() {
		return this.x;
	}

	@Override
	public int getY() {
		return this.y;
	}

	@Override
	public void setGridSpacing(int gs) {
		this.gs = gs;
	}

	@Override
	public int getGridSpacing() {
		return this.gs;
	}
	
	public float calTsun(int sunPosition) {
		
		// so the Equator will be hotter
		float beta = (float) (this.surfarea / avgArea);  // actual grid area / average cell area
		
		int   sunLongitude      = getSunLocationOnEarth(sunPosition);
		//float attenuation_lat   = (float) Math.cos(Math.toRadians(this.latitude  + 1.0 * this.gs / 2));
		//P3 - Heated Planet : Find correct attenuation depending on the sun latitude
		int   sunLatitude      = (int) getSunLatitudeOnEarth();
		//System.out.println("\n" + "Sun Latitude is " + sunLatitude + " for Earth.currentTimeInSimulation " + Earth.currentTimeInSimulation);
		float attenuation_lat   = (float) Math.cos(Math.toRadians(Math.abs((sunLatitude - this.latitude  - 1.0 * this.gs / 2))));
		//float attenuation_longi = (float) (( (Math.abs(sunLongitude - this.longitude) % 360 ) < 90 ) ? Math.cos(Math.toRadians(sunLongitude - this.longitude)) : 0);
		float attenuation_longi = (float) Math.cos(Math.toRadians(sunLongitude - this.longitude));
		attenuation_longi = attenuation_longi > 0 ? attenuation_longi : 0;
		
		//return 278 * attenuation_lat * attenuation_longi;
		//P3 - Heated Planet : Sun's distance from planet, inverse square law
		double ratio = Math.pow((Simulator.a + Simulator.b)/2, 2) / Math.pow(distanceFromPlanet(Simulator.currentTimeInSimulation),2);
		return (float) (suntemp * beta * ratio * attenuation_lat * attenuation_longi); 
		//============ Math.pow(distanceFromPlanet(Earth.currentTimeInSimulation),2));
	}
	
	private void calSurfaceArea(int latitude, int gs) {
		
		double p  = 1.0 * gs / 360;
		this.lv   = (float) (Simulator.CIRCUMFERENCE * p);
		this.lb   = (float) (Math.cos(Math.toRadians(latitude)) * this.lv);
		this.lb   = this.lb > 0 ? this.lb: 0;
		this.lt   = (float) (Math.cos(Math.toRadians(latitude + gs)) * this.lv);
		this.lt   = this.lt > 0 ? this.lt: 0;
		double h  = Math.sqrt(Math.pow(this.lv,2) - 1/4 * Math.pow((this.lb - this.lt), 2));

		this.pm = (float) (this.lt + this.lb + 2 * this.lv);
		this.surfarea =  (float) (1.0/2 * (this.lt + this.lb) * h);
	}

	// A help function for get the Sun's corresponding location on longitude.
	public int getSunLocationOnEarth(int sunPosition) {
		
		// Grid column under the Sun at sunPosition
		int cols = 360 / this.gs;
		int j    = sunPosition;
		return j < (cols / 2) ? -(j + 1) * this.gs : (360) - (j + 1) * this.gs;
	}

	public float calTcool() {
		//return -1 * beta * avgsuntemp;
		return -1 * this.currTemp / 288 * avgsuntemp;
	}
	
	public static void setAvgSuntemp(float avg){
		avgsuntemp = avg;
	}
	
	public static float getAvgSuntemp(){
		return avgsuntemp;
	}
	
	public float getSurfarea() {
		return this.surfarea;
	}
	
	public static void setAverageArea(float avgarea) {
		avgArea = avgarea;
	}
	
	public static float getAverageArea() {
		return avgArea;
	}
	
	public float calTneighbors() {

		float top_temp = 0, bottom_temp = 0;

		if (this.top != null) 	top_temp = this.lt / this.pm * this.top.getTemp();
		if (this.bottom != null) 	bottom_temp = this.lb / this.pm * this.bottom.getTemp();

		//System.out.println(this.lt / this.pm + this.lb / this.pm + this.lv / this.pm * 2);
		return  top_temp + bottom_temp + this.lv / this.pm * (this.left.getTemp() + this.right.getTemp());
	}
	
	//=================================================
	//P3 Heated Planet
	public double getMeanAnomaly(int currentTime) {
		return (2 * Math.PI * currentTime / Simulator.T);
	}
	
	public double getEccentricAnomaly(int currentTime) {
		return approximationInversion(getMeanAnomaly(currentTime));
	}
	
	public double approximationInversion(double meanAnamoly) {
		// approximation inversion for Kepler's Equation
		double tol = 1e-9;
		boolean breakflag = false;
		double E1 = meanAnamoly, E = 0;

		while (breakflag == false) {
		    //Fixed-point iterative version of Kepler's Equation
		    E = meanAnamoly + Simulator.E * Math.sin(Math.toRadians(E1));
		    //Break loop if tolerance is achieved
		    if (Math.abs(E - E1) < tol) {
		    	breakflag = true;
		    }
		    E1 = E;
		}
		
		//Format the answer so that it is between 0 and 2*pi
		while (E > (2 * Math.PI)) {
			E = E - 2 * Math.PI;
		}
		while (E < 0) {
			E = E + 2 * Math.PI;
		}
		
		return E;
	}
	
	public double trueAnomaly(int currentTime) {
		double eccentricAnamoly = getEccentricAnomaly(currentTime);
		double numerator = Math.cos((eccentricAnamoly)) - Simulator.E;
		double denominator = 1 - (Simulator.E * Math.cos((eccentricAnamoly)));
		return (Math.acos((numerator/denominator)));
	}
	
	public double distanceFromPlanet(int currentTime) {
		double numerator = 1 - (Simulator.E * Simulator.E);
		double denominator = 1 + (Simulator.E * Math.cos((trueAnomaly(currentTime))));
		return (Simulator.a * numerator / denominator);
	}
	
	public float getPlanetX(int currentTime) {
		return (float) ((Simulator.a * Simulator.E)  + (Simulator.a * Math.cos((getEccentricAnomaly(currentTime)))));
	}

	public float getPlanetY(int currentTime) {
		double b = Simulator.a * (Math.sqrt(1-(Simulator.E * Simulator.E)));
		return (float) (b * Math.sin((getEccentricAnomaly(currentTime))));
	}
	
	public double getRotationalAngle(int currentTime)
	{
		double mod = (currentTime - Simulator.tauAN) % Simulator.T;
		return (mod * 2 * Math.PI / Simulator.T);
	}
		
	public double getSunLatitudeOnEarth() {
		//return (Earth.tilt * Math.sin((getRotationalAngle(currentTime))));
		return (Simulator.tilt * Math.sin((getRotationalAngle(Simulator.currentTimeInSimulation))));
	}

	@Override
	public int getLatitude() {
		return this.latitude;
	}

	@Override
	public int getLongitude() {
		return this.longitude;
	}
}


