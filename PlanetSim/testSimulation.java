package PlanetSim;

import static org.junit.Assert.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.Test;

import PlanetSim.util.GridCell;

public class testSimulation {

	@Test
	public void testEquinoxes() throws InterruptedException {
		BlockingQueue<Object> simQueue = new ArrayBlockingQueue<Object>(500000);
		Simulator newearth = new Simulator(simQueue);
		newearth.configure(1, 1, 1);
		// configuration for Earth
		newearth.setE(0.0167); 
		newearth.setTilt(23.4);
		newearth.setTimeOfEquinox();
		new Thread(newearth).start();
		while (newearth.isRunning()){};
		newearth.setcurrentStep(Simulator.tauAN);
		System.out.println("The current time for Equinoxes is:" + Simulator.tauAN);
		
		newearth.generate();
		GridCell newcell = new GridCell(180, 0, 0, 0, 0, 1);
		double latitude  = newcell.getSunLatitudeOnEarth();
		// The Earth latitude should be 0 at Earth.tauAN.
		assertTrue("Sun's latitude should about 0 at Equinoxes", Math.round(latitude * 10) == 0);
	}

	@Test
	public void testBasicSimulation() throws InterruptedException {
		// Test simulation setting up
		BlockingQueue<Object> simQueue = new ArrayBlockingQueue<Object>(5000);
		Simulator newearth = new Simulator(simQueue);
		newearth.configure(60, 1, 10);
//		newearth.printGrid();
		new Thread(newearth).start();
		while (newearth.isRunning()){};
		System.out.println(Simulator.currentTimeInSimulation);
		
		// The Earth latitude should be 0 at Earth.tauAN.
		assertTrue("The current time of simulation should be 10: ", Simulator.currentTimeInSimulation == 10);
	}
	
	@Test
	public void testIsDone() throws InterruptedException {
		// Test isDone function
		BlockingQueue<Object> simQueue = new ArrayBlockingQueue<Object>(1024);
		Simulator newearth = new Simulator(simQueue);
		newearth.configure(60, 1, 1);
		new Thread(newearth).start();
		while (newearth.isRunning()){};
		assertTrue("The current simulation should be done: ", newearth.isRunning() == false);
	}
	
	@Test
	public void testInitialSunLatitude() throws InterruptedException {
		BlockingQueue<Object> simQueue = new ArrayBlockingQueue<Object>(5000);
		Simulator newearth = new Simulator(simQueue);
		newearth.configure(60, 1, 1);
		newearth.generate();
		GridCell prime = newearth.getGrid();
		double latitude = prime.getSunLatitudeOnEarth();
		double longitude = newearth.getSunPositionDeg();
//		System.out.println("latitude:" + latitude);
//		System.out.println("longitude:" + longitude);
		assertTrue("The initial longitude should be 0: ", longitude == 0);
		assertTrue("In the beginning of the year, the latitude of the Sun should be at southern hemisphere: ", latitude <= 0);
	}
	
	@Test
	public void testMonth2Minutes() throws InterruptedException {
		BlockingQueue<Object> simQueue = new ArrayBlockingQueue<Object>(5000);
		Simulator newearth = new Simulator(simQueue);
		newearth.configure(60, 1, 1);
		int minutes = newearth.month2Miniute(2, 1);
		System.out.println("miniutes in two month:" + minutes);
	}
}
