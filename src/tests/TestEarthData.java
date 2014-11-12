package tests;

import common.IGrid;

// Provides generated data for testing visualization

public class TestEarthData implements IGrid {
	
	private int rows;
	private int cols;
	private Boolean invert;
	private int rowOffset; // used to circularly shift pattern along rows
	private int colOffset; // used to circularly shift pattern along cols

	public TestEarthData() {
		this(10);
	}

	public TestEarthData(int rows) {
		this(rows, rows);
	}

	public TestEarthData(int rows, int cols) {
		this(rows, cols, false);
	}

	public TestEarthData(int rows, int cols, Boolean invert) {
		this(rows, cols, invert, 0, 0);
	}

	public TestEarthData(int rows, int cols, Boolean invert, int rowOffset, int colOffset) {
		this.rows = rows;
		this.cols = cols;
		this.invert = invert;
		this.rowOffset = rowOffset;
		this.colOffset = colOffset;
	}
	@Override
	public float getTemperature(int x, int y) {
		
		// Generate a test pattern for display
		// for now we'll just checker board getting fainter as you advance to
		// right (except that first row is completely on and last completely off

		// Apply offset values
		y = (y + rowOffset) % getGridHeight();
		x = (x + colOffset) % getGridWidth();

		double initialVal = (y + x) % 2;
		if (y == 0) {
			initialVal = 1.0;
		} else if (y == getGridHeight() - 1) {
			initialVal = 0.0;
		}
		
		if (invert) {
			initialVal = (initialVal + 1) % 2;
		}
		
		double rightGradientScale = 1.0 - ((double) x / getGridWidth());
		
		// System.out.printf("row/col/val %d/%d/%f\n", row, col,
		// (float)(initialVal*rightGradientScale));
		return (float) (initialVal * rightGradientScale);
	}


	@Override
	public void setTemperature(int x, int y, float temp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getSunPositionDeg() {
		// TODO Auto-generated method stub
		return rowOffset;
	}

	@Override
	public int getGridWidth() {
		return cols;
	}

	@Override
	public int getGridHeight() {
		return rows;
	}

	@Override
	public int getCurrentTime() {
		// TODO Auto-generated method stub
		return colOffset;
	}

	@Override
	public void setSunLatitudeDeg(float lat) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getSunLatitudeDeg() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPlanetX(float x) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPlanetY(float y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getPlanetX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getPlanetY() {
		// TODO Auto-generated method stub
		return 0;
	}
}
