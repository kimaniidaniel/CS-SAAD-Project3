package view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;

import simulation.Earth;
import view.util.ThermalVisualizer;
import view.widgets.EarthImage;
import view.widgets.GridDisplay;
import view.widgets.SimulationStatus;
import common.IGrid;

public class EarthDisplay extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -309131746356718870L;
	
	// core display
	private final JLayeredPane display;
	
	// widgets
	private SimulationStatus simStatus;
	private EarthImage earthImage;
	private GridDisplay gridDisplay;
	
	private static final String COLORMAP = "thermal";
	private static final float OPACITY = 0.6f;
			
	private static final int EARTH = 0;
	private static final int GRID = 1;
	
	private int gs = 0, timeStep = 0;
	
	public EarthDisplay() {
		
		super("Earth Simulation");
		
		EarthDisplay.setDefaultLookAndFeelDecorated(true);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		this.setResizable(true);
		
		// Add sim settings
		simStatus = new SimulationStatus();
		this.add(simStatus, BorderLayout.SOUTH);
		
		// Add the display region
		display = new JLayeredPane();
		this.add(display, BorderLayout.CENTER);
		
		// Add EarthImage
		earthImage = new EarthImage();
		display.add(earthImage, new Integer(EARTH));
		
		int w = earthImage.getImageWidth();
		int h = earthImage.getImageHeight();

		// Add grid
		gridDisplay = new GridDisplay(new ThermalVisualizer(COLORMAP, Earth.MIN_TEMP, Earth.MAX_TEMP, OPACITY), w, h);
		display.add(gridDisplay, new Integer(GRID));
		
		this.setPreferredSize(new Dimension(w, h + 130));

	}
	
	public void display(int gs, int timeStep) {
		
		this.gs = gs;
		this.timeStep = timeStep;
		
		this.pack();
		this.setVisible(true);
		this.validate();
	}

	public void close() {
		this.dispose();
	}
	
	public void update(IGrid grid) {
		if (grid != null)
			simStatus.update(grid.getSunPositionDeg(), grid.getCurrentTime(), this.gs, this.timeStep);
		else
			simStatus.update(0, 0, this.gs, this.timeStep);
		gridDisplay.update(grid);
	}
}
