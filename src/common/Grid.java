package common;

import java.util.Map;
import java.util.TreeMap;

public class Grid implements IGrid {

	// Used to transport the temps in the buffer
	private final int sunPosition, time, width, height;
	private final float sunPositionDeg;
	private float sunLatitudeDeg=0;
	private float planetX, planetY;

	// We use a TreeMap to not consume a contiguous amount of memory. It's
	// backed by a Red/Black Tree, so we get pretty decent access times
	private final Map<Integer, Float> grid;

	public Grid(int sunPosition, float sunPositionDeg, int time, int width, int height) {

		this.sunPosition = sunPosition;
		this.sunPositionDeg = sunPositionDeg;
		this.time = time;
		this.width = width;
		this.height = height;

		grid = new TreeMap<Integer, Float>();
	}
	
	public Grid(Grid toCopy) {
		
		this.sunPosition = toCopy.sunPosition;
		this.sunPositionDeg = toCopy.sunPositionDeg;
		this.time = toCopy.time;
		this.width = toCopy.width;
		this.height = toCopy.height;
		
		this.grid = new TreeMap<Integer, Float>(toCopy.grid);
	}

	@Override
	public void setTemperature(int x, int y, float temp) {
		if (y > height || x > width || x < 0 || y < 0)
			throw new IllegalArgumentException("index (" + x + ", " + y + ") out of bounds");
		
		grid.put(y * width + x, temp);
	}

	@Override
	public float getTemperature(int x, int y) {
		if (y >= height || x >= width || x < 0 || y < 0)
			throw new IllegalArgumentException("index (" + x + ", " + y + ") out of bounds");
		
		return grid.get(y * width + x);
	}

	@Override
	public float getSunPositionDeg() {
		return this.sunPositionDeg;
	}
	
	public float getSunLatitudeDeg() {
		return this.sunLatitudeDeg;
	}
	
	public void setSunLatitudeDeg(float lat) {
		this.sunLatitudeDeg = (float) lat;
	}
	
	@Override
	public int getCurrentTime() {
		return this.time;
	}

	@Override
	public int getGridWidth() {
		return this.width;
	}

	@Override
	public int getGridHeight() {
		return this.height;
	}

	@Override
	public void setPlanetX(float x) {
		this.planetX = x;
	}

	@Override
	public void setPlanetY(float y) {
		this.planetY = y;
	}

	@Override
	public float getPlanetX() {
		return this.planetX;
	}

	@Override
	public float getPlanetY() {
		return this.planetY;
	}
}
