package view.widgets;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class SimulationStatus extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4874764682275993951L;
	
	private JTextField sunPosStats, currTimeStatus, gsStatus, timeStepStatus;
	private JLabel lblSunPos, lblCurrTime, lblGs, lblTimeStep;
	
	private static final int HEIGHT = 4;
	private static final int WIDTH = 2;
	private static final int HGAP = 1;
	private static final int VGAP = 1;
	
	//private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yy HH:mm:SS");
	
	public SimulationStatus() {
		
		this.setBorder(new EmptyBorder(10,10,10,10));
		this.setLayout(new GridLayout(HEIGHT,  WIDTH, HGAP, VGAP));
		
		sunPosStats 	= new JTextField("0");
		currTimeStatus 	= new JTextField("0");
		gsStatus 		= new JTextField("0");
		timeStepStatus 	= new JTextField("0");
		
		lblSunPos 	= new JLabel("Rotational Position:");
		lblCurrTime = new JLabel("Time:");
		lblGs 		= new JLabel("Grid Spacing:");
		lblTimeStep = new JLabel("Simulation Time Step:");
		
		sunPosStats.setPreferredSize(new Dimension(10, 10));
		sunPosStats.setMaximumSize(new Dimension(10, 10));
		sunPosStats.getFont().deriveFont(Font.PLAIN, 10);
		sunPosStats.setEditable(false);
		
		currTimeStatus.setPreferredSize(new Dimension(10, 10));
		currTimeStatus.setMaximumSize(new Dimension(10, 10));
		currTimeStatus.getFont().deriveFont(Font.PLAIN, 10);
		currTimeStatus.setEditable(false);
		
		gsStatus.setPreferredSize(new Dimension(10, 10));
		gsStatus.setMaximumSize(new Dimension(10, 10));
		gsStatus.getFont().deriveFont(Font.PLAIN, 10);
		gsStatus.setEditable(false);
		
		timeStepStatus.setPreferredSize(new Dimension(10, 10));
		timeStepStatus.setMaximumSize(new Dimension(10, 10));
		timeStepStatus.getFont().deriveFont(Font.PLAIN, 10);
		timeStepStatus.setEditable(false);
		
		lblSunPos.getFont().deriveFont(Font.PLAIN, 8);
		lblCurrTime.getFont().deriveFont(Font.PLAIN, 8);
		lblGs.getFont().deriveFont(Font.PLAIN, 8);
		lblTimeStep.getFont().deriveFont(Font.PLAIN, 8);
		
		this.add(lblSunPos);
		this.add(sunPosStats);
		
		this.add(lblCurrTime);
		this.add(currTimeStatus);
		
		this.add(lblGs);
		this.add(gsStatus);
		
		this.add(lblTimeStep);
		this.add(timeStepStatus);
	}
	
	public void init() {
		this.sunPosStats.setText("0");
		this.currTimeStatus.setText("0");
		this.gsStatus.setText("0");
		this.timeStepStatus.setText("0");
	}
	
	public void update(float sunPosition, int currentTime, int gs, int timeStep) {
		
		this.sunPosStats.setText(String.format("%.1f", sunPosition));
		this.currTimeStatus.setText(Integer.toString(currentTime));
		this.gsStatus.setText(Integer.toString(gs));
		this.timeStepStatus.setText(Integer.toString(timeStep));
		
		this.validate();
	}
}
