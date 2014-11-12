// GUI.java
package EarthSim;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import common.State;

public class ControllerGUI extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6146431536208036768L;
	
	private Controller controller;
	
	private HashMap<String, JTextField> inputs = new HashMap<String, JTextField>();
	private HashMap<String, JButton> buttons = new HashMap<String, JButton>();

	public ControllerGUI(boolean ownSimThread, boolean ownPresThread, State initiative, long bufferSize) {
		
		// Remap initiative setting
		InitiativeSetting init2;
		switch(initiative) {
		
			case PRESENTATION:
				init2 = InitiativeSetting.VIEW;
				break;
			case SIMULATION:
				init2 = InitiativeSetting.MODEL;
				break;
			case MASTER:
				init2 = InitiativeSetting.THIRD_PARTY;
				break;
			default:
				init2 = null;
				
		}
		
		controller = new Controller(ownSimThread, ownPresThread, init2, (int)bufferSize);

		setupWindow();
		pack();
	}

	private void setupWindow() {
		
		// setup overall app ui
		setTitle("Heated Earth Diffusion Simulation");
		
		setSize(300, 200);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setLayout(new BorderLayout());
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		lowerRightWindow(); // Set window location to lower right (so we don't hide dialogs)
		setAlwaysOnTop(true);
		
		add(settingsAndControls(), BorderLayout.CENTER);
	}
	
	private void lowerRightWindow() {
	    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
	    int x = (int) (dimension.getWidth() - this.getWidth());
	    int y = (int) (dimension.getHeight() - this.getHeight());
	    this.setLocation(x, y);
	}
	
	private JPanel settingsAndControls() {
		
		JPanel sncPanel = new JPanel();
		sncPanel.setLayout(new BoxLayout(sncPanel, BoxLayout.PAGE_AXIS));
		sncPanel.setAlignmentY(Component.TOP_ALIGNMENT);

		sncPanel.add(settings(), BorderLayout.WEST);
		sncPanel.add(runControls(), BorderLayout.WEST);

		return sncPanel;
	}

	private JPanel settings() {
		
		JPanel settingsPanel = new JPanel();
		settingsPanel.setBorder(BorderFactory.createTitledBorder("Settings"));
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.PAGE_AXIS));
		settingsPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		
		settingsPanel.add(inputField("Grid Spacing", Integer.toString(Controller.DEFAULT_GRID_SPACING)));
		settingsPanel.add(inputField("Simulation Time Step",Integer.toString(Controller.DEFAULT_TIME_STEP)));
		settingsPanel.add(inputField("Presentation Rate",Float.toString(Controller.DEFAULT_PRESENTATION_RATE)));

		return settingsPanel;
	}

	private JPanel runControls() {
		
		JPanel ctrlsPanel = new JPanel(new FlowLayout());
		ctrlsPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);

		ctrlsPanel.add(button("Start"));
		ctrlsPanel.add(button("Pause"));
		ctrlsPanel.add(button("Resume"));
		ctrlsPanel.add(button("Stop"));

		buttons.get("Start").setEnabled(true);
		buttons.get("Pause").setEnabled(false);
		buttons.get("Resume").setEnabled(false);
		buttons.get("Stop").setEnabled(false);
		
		return ctrlsPanel;
	}

	private JPanel inputField(String name, String defaultText) {
		
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new FlowLayout());
		inputPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);

		JLabel l = new JLabel(name);
		l.setAlignmentX(Component.LEFT_ALIGNMENT);
		inputPanel.add(l);

		JTextField t = new JTextField(defaultText, 10);
		t.setAlignmentX(Component.RIGHT_ALIGNMENT);
		l.setLabelFor(t);
		inputPanel.add(t);

		inputs.put(name, t);
		return inputPanel;
	}

	private JButton button(String name) {
		
		JButton button = new JButton(name);
		button.setActionCommand(name);
		button.addActionListener(this);
		buttons.put(name, button);
		return button;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		String cmd = e.getActionCommand();
		
		if ("Start".equals(cmd)) {
			if (configureEngine()) {
				//do gui stuff to indicate start has occurred.
				buttons.get("Start").setEnabled(false);
				buttons.get("Pause").setEnabled(true);
				buttons.get("Resume").setEnabled(false);
				buttons.get("Stop").setEnabled(true);
			}
		}
		
		else if ("Pause".equals(cmd)) {
			controller.pause();
			buttons.get("Pause").setEnabled(false);
			buttons.get("Resume").setEnabled(true);
		}
		
		else if ("Resume".equals(cmd)) {
			controller.resume();
			buttons.get("Pause").setEnabled(true);
			buttons.get("Resume").setEnabled(false);
			
		}
		
		else if ("Stop".equals(cmd)) {
			try {
				controller.stop();
			} catch (InterruptedException e1) {
			}
			
			buttons.get("Start").setEnabled(true);
			buttons.get("Pause").setEnabled(false);
			buttons.get("Resume").setEnabled(false);
			buttons.get("Stop").setEnabled(false);
		}
	}

	private boolean configureEngine() {
		
		try {
			
			final int gs = Integer.parseInt(inputs.get("Grid Spacing").getText());
			final int timeStep = Integer.parseInt(inputs.get("Simulation Time Step").getText());
			final float presentationRate = Float.parseFloat(inputs.get("Presentation Rate").getText());

			controller.start(gs, timeStep, presentationRate);
			
			return true;

		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null,
					"Please correct input. All fields need numbers");
		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(null, "Please correct input. All fields need numbers");
		}
				
		return false;
	}
}
