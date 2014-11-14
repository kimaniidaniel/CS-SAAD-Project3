package simulation.util;

import static org.junit.Assert.*;

import org.junit.Test;

import simulation.Earth;

public class testGridCell {

	@Test
	public void test() {
		GridCell newcell = new GridCell(10, 0, 0, 180, 90, 5);
		newcell.setTimeOfEquinox();
		System.out.println("\n" + "trueAnamoly " + Earth.tauAN);
	}

	
}
