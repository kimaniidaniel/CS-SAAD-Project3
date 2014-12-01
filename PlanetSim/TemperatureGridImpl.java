package PlanetSim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import cs6310.gui.widget.earth.TemperatureGrid;

/**
 * A container for the cell grid array that represents the Earth's surface.
 */
public class TemperatureGridImpl implements TemperatureGrid {

	private int[][] grid;
	private int gridRows, gridCols;

	public TemperatureGridImpl(int gridSpacing) {
		ArrayList<Integer> validGridSpacings = new ArrayList<Integer>();
		validGridSpacings.addAll( Arrays.asList(new Integer[] {1, 2, 3, 4, 5, 6, 9, 10, 12, 15, 18, 20, 30, 36, 45, 60, 90, 180}) );
		if (180 % gridSpacing != 0) {
			validGridSpacings.add(gridSpacing);
			//Sort
			Collections.sort(validGridSpacings);
			//Find the index of the value.
			int index = validGridSpacings.indexOf(gridSpacing);
			//Set the grid spacing to be the element before it
			gridSpacing = validGridSpacings.get(index - 1);
		}

		gridRows = 180 / gridSpacing;
		gridCols = 360 / gridSpacing;
		
		grid = new int[gridRows][gridCols];
	}

	/**
	 * Zero-argument (do-nothing) constructor used only by clone() method
	 */
	private TemperatureGridImpl() {
		grid = null;
		gridRows = 0;
		gridCols = 0;
	}
	
	@Override
	public void setTemperature(int lat, int lon, int temp, int gridSpacing) {
		int x = (lat + 90) / gridSpacing;
		int y = (lon + 180) / gridSpacing;
		grid[x][y] = temp;
	}

	@Override
	public double getTemperature(int x, int y) {
		int rowIndex = (gridRows - 1) - y;
		int colIndex = (gridCols + gridCols/2 - 1 - x) % gridCols;
		return celsiusToFahrenheit(kelvinToCelsius(grid[rowIndex][colIndex]));
		//return grid[rowIndex][colIndex];
	}

	private static double kelvinToCelsius(double K) {
		return K - 273.15;
	}

	private static double celsiusToFahrenheit(double C) {
		return 32.0 + 1.8 * C;
	}

	@Override
	public float getCellHeight(int x, int y) {
		return 0.0f;  // not used
	}

	public int[][] getCells() {
		return grid;
	}
}
