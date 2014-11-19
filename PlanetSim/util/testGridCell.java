package PlanetSim.util;

import static org.junit.Assert.*;

import org.junit.Test;

import PlanetSim.Simulator;

public class testGridCell {

	@Test
	public void testSunDistance() throws InterruptedException {
		// Tsun should be smaller with longer distance
		Simulator newearth   = new Simulator();
		newearth.setcurrentStep(0);
		newearth.configure(1,1,-1);
		newearth.generate();
		float perihelion_tmp = GridCell.getAvgSuntemp();
		
		newearth.setcurrentStep(262800);
		newearth.generate();
		float aphelion_tmp = GridCell.getAvgSuntemp();
		
		assertTrue("The average temp at perihelion should be larger than at aphelion", perihelion_tmp > aphelion_tmp);
	}
}
