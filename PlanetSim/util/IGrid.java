package PlanetSim.util;

public interface IGrid {
	
	public void setTemperature(int x, int y, float temp);
	
	public float getTemperature(int x, int y);
	
	public float getTemp();
	
	public float getSunPositionDeg();
	
	public void setSunLatitudeDeg(float lat);
	
	public float getSunLatitudeDeg();
	
	public void setPlanetX(float x);
	public void setPlanetY(float y);
	public float getPlanetX();
	public float getPlanetY();	
	
	public int getCurrentTime();
	
	public int getGridWidth();
	
	public int getGridHeight();
	
	public int getLatitude();
	
	public int getLongitude();

}
