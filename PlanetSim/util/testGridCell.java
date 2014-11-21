package PlanetSim.util;

import static org.junit.Assert.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.Test;

import PlanetSim.Simulator;

public class testGridCell {

	@Test
	public void testSunDistance() throws InterruptedException {
		// Tsun should be smaller with longer distance
		BlockingQueue<Object> simQueue = new ArrayBlockingQueue<Object>(1024);
		Simulator newearth   = new Simulator(simQueue);
		newearth.configure(30,1,-1);
		newearth.setcurrentStep(0);
		newearth.generate();
		newearth.generate();
		float perihelion_tmp = GridCell.getAvgSuntemp();
		
//		System.out.println(perihelion_tmp);
		GridCell prime = newearth.getGrid();
//		System.out.println(Math.pow((Simulator.a + Simulator.b)/2, 2) / Math.pow(prime.distanceFromPlanet(Simulator.currentTimeInSimulation),2));
		
		newearth.setcurrentStep(262800);
		newearth.generate();
		
		float aphelion_tmp = GridCell.getAvgSuntemp();
		
//		System.out.println(aphelion_tmp);
//		System.out.println(Math.pow((Simulator.a + Simulator.b)/2, 2) / Math.pow(prime.distanceFromPlanet(Simulator.currentTimeInSimulation),2));
		
		assertTrue("The average temp at perihelion should be larger than at aphelion", perihelion_tmp > aphelion_tmp);
	}
}
