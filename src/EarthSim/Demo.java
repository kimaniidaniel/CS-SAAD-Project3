// Demo.java
package EarthSim;

import common.State;

import javax.swing.SwingUtilities;

public class Demo {
	
	private boolean ownSimThread = false, ownPresThread = false;

	private State initiative = State.MASTER;
	private boolean rset = false, tset = false;

	private long bufferSize = 1;

	public static void main(String[] args) {
		Demo demo = new Demo();
		demo.processArgs(args);
		demo.run();
	}

	private Demo() {
		// empty
	}

	// Note: processArgs ignore args that are not s,p,r,t or b as long as you
	// provide a max of 5 input values.
	private void processArgs(String[] args) {
		
		if (args.length > 5)
			usage();

		for (int i = 0; i < args.length; i++) {
			
			String arg = args[i];

			if ("-s".equalsIgnoreCase(arg)) 
				ownSimThread = true;
			
			else if ("-p".equalsIgnoreCase(arg)) 
				ownPresThread = true;
			
			else if ("-r".equalsIgnoreCase(arg))
				rset = true;
			
			else if ("-t".equalsIgnoreCase(arg))
				tset = true;
			
			else if ("-b".equalsIgnoreCase(arg)) {
				
				if (i == -1 || i++ >= args.length) {
					System.out.println("-b needs a value.");
					usage();
				}
				
				String bufSizeString = args[i];
				
				try {
					bufferSize = Integer.parseInt(bufSizeString);
					if(bufferSize <= 0) {
						throw new NumberFormatException();
					}
				} catch (NumberFormatException nfe) {
					System.out.println("Error reading -b value as a positive integer. Please retry.");
					usage();
				}
				
			} else
				usage();
		}

		if (rset && tset) {
			System.out.println("Cannot set both -r and -t.");
			usage();
		}
		
		initiative = rset ? State.PRESENTATION : (tset ? State.SIMULATION : State.MASTER);
	}

	private void usage() {
		System.out.println("Usage: java EarthSim.Demo [-s] [-p] [-r|-t] [-b #]");
		System.exit(-1);
	}

	private void run() {
		debug("Demo started with settings:");
		printSettings();
		createAndShowUI();
		debug("Demo running...");
	}

	private void createAndShowUI() {
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ControllerGUI ui = new ControllerGUI(ownSimThread, ownPresThread, initiative, bufferSize);
				ui.setVisible(true);
			}
		});
	}

	private void printSettings() {
		
		debug("Simulation on own thread\t:" + ownSimThread);
		debug("Presentation on own thread\t:" + ownPresThread);
		debug("Initiative\t\t\t:" + initiative);
		debug("Buffer Size\t\t\t:" + bufferSize);
		debug("");
	}

	private void debug(String s) {
		System.out.println(s);
	}
}
