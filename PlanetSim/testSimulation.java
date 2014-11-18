package PlanetSim;

import static org.junit.Assert.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.Test;

import PlanetSim.util.GridCell;

public class testSimulation {

	@Test
	public void testEquinoxes() throws InterruptedException {
		BlockingQueue<Object> simQueue = new ArrayBlockingQueue<Object>(1024);
		Simulator newearth = new Simulator(simQueue);
		newearth.configure(1, 1, 4000);
		// configuration for Earth
		newearth.setE(0.0167); 
		newearth.setTilt(23.4);
		newearth.setTimeOfEquinox();
		newearth.setcurrentStep(Simulator.tauAN);
		new Thread(newearth).start();
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
		Simulator newearth = new Simulator();
		newearth.configure(1, 1, 10);
		new Thread(newearth).start();
		while (newearth.isRunning()){};
		System.out.println(Simulator.currentTimeInSimulation);
		
		// The Earth latitude should be 0 at Earth.tauAN.
		assertTrue("The current time of simulation should be 10: ", Simulator.currentTimeInSimulation == 10);
	}
	
	@Test
	public void testIsDone() throws InterruptedException {
		// Test isDone function
		Simulator newearth = new Simulator();
		newearth.configure(1, 1, 10);
		new Thread(newearth).start();
		
		assertTrue("The current simulation should be done: ", newearth.isRunning() == false);
	}
}
