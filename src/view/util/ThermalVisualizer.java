package view.util;

import java.awt.Color;

public class ThermalVisualizer implements ColorGenerator {
	
	private ColorMap colorMap;
	private final float opacity;
	private final int min, max;

    public ThermalVisualizer(String cpName, int minTemp, int maxTemp, float opacity) {
    	
    	if (opacity < 0 || opacity > 1)
    		throw new IllegalArgumentException("Invalid opacity provided");
    	
    	if (minTemp >= Integer.MAX_VALUE )
    		throw new IllegalArgumentException("Invalid minimum temp provided");
    	
    	if (maxTemp >= Integer.MAX_VALUE )
    		throw new IllegalArgumentException("Invalid maximum temp provided");
    	
    	if (minTemp > maxTemp)
    		throw new IllegalArgumentException("Minimum and maximum temps cannot be equal");
    	
    	colorMap = ColorMap.getMap(cpName);
    	this.min = minTemp;
    	this.max = maxTemp;
    	this.opacity = opacity;
    }

	@Override
	public Color calculateColor(double temp) {
		
		// convert temp to a 0 .. 1.0 scale
		if (temp > max) temp = max;
		if (temp < min) temp = min;
		double scaled = (((1 - 0) * (temp - min)) / (max - min)) + 0;
		return this.colorMap.getColor(scaled, this.opacity);
	}
}