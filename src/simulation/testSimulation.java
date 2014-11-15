package simulation;

import static org.junit.Assert.*;

import org.junit.Test;

import simulation.util.GridCell;

public class testSimulation {

	@Test
	public void testEquinoxes() throws InterruptedException {
		Earth newearth = new Earth();
		newearth.configure(1, 1);
		newearth.start();
		// configuration for Earth
		newearth.setE(0.0167); 
		newearth.setTilt(23.4);
		newearth.setTimeOfEquinox();
		newearth.setcurrentStep(Earth.tauAN);
		System.out.println("The current time for Equinoxes is:" + Earth.tauAN);
		
		newearth.generate();
		GridCell newcell = new GridCell(180, 0, 0, 0, 0, 1);
		double latitude  = newcell.getSunLatitudeOnEarth();
		// The Earth latitude should be 0 at Earth.tauAN.
		assertTrue("Sun's latitude should about 0 at Equinoxes", Math.round(latitude * 10) == 0);
	}

}
