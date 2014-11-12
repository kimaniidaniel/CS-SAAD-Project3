package tests;

import EarthSim.Controller;
import EarthSim.InitiativeSetting;

public class TestDummyComponents {

	public static void main(String[] args) {

		tryControl(false, false, InitiativeSetting.MODEL);
//		tryControl(false,  true, InitiativeSetting.MODEL);
//		tryControl( true, false, InitiativeSetting.MODEL);
//		tryControl( true,  true, InitiativeSetting.MODEL);
//
//		tryControl(false, false, InitiativeSetting.VIEW);
//		tryControl(false,  true, InitiativeSetting.VIEW);
//		tryControl( true, false, InitiativeSetting.VIEW);
//		tryControl( true,  true, InitiativeSetting.VIEW);
//
//		tryControl(false, false, InitiativeSetting.THIRD_PARTY);
//		tryControl(false,  true, InitiativeSetting.THIRD_PARTY);
//		tryControl( true, false, InitiativeSetting.THIRD_PARTY);
//		tryControl( true,  true, InitiativeSetting.THIRD_PARTY);

		// GUI based routine
		
	}

	private static void tryControl(Boolean simThread, Boolean viewThread, InitiativeSetting initiative) {
		System.out.printf("\n\n=== Testing simThread=%s, viewThread=%s, initiative=%s ===\n", simThread, viewThread, initiative);
		Controller controller = new Controller(simThread, viewThread, initiative, 1);
		controller.start();
	}
	
}
