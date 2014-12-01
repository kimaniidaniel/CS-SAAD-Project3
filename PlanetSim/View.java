package PlanetSim;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.Component;

import javax.swing.JTabbedPane;

import java.awt.GridLayout;
import java.awt.FlowLayout;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JSpinner;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.border.LineBorder;

import java.awt.Color;

import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.SwingConstants;

import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

import java.awt.Font;

import javax.swing.SpinnerNumberModel;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerDateModel;

import cs6310.gui.widget.earth.EarthPanel;
import cs6310.gui.widget.earth.TemperatureGrid;

import java.util.Date;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.concurrent.TimeUnit;

import PlanetSim.util.Tools;
import Utils.QueryResult;
import Utils.SimulationConfig;
import Utils.TemperatureReading;

public class View extends JFrame implements Runnable {

	// Constants used in the GUI
	static final int WIN_WIDTH = 835;
	static final int WIN_HEIGHT = 710;
	static final int EARTH_WIDTH = 800;
	static final int EARTH_HEIGHT = 400;
	static final int DEFAULT_GRID_SPACING = 15;
	static final int DEFAULT_TIME_STEP = 1440;
	static final int DEFAULT_DISPLAY_RATE = 1;
	static final int DEFAULT_SIM_LENGTH = 12;
	static final double DEFAULT_ECCENTRICITY = 0.0167;
	static final double DEFAULT_TILT = 23.44;
	static final String DEFAULT_DATE_TIME = "04-Jan-2014 12:00:00 AM";

	// Swing variables
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTabbedPane tabbedPane;
	private JPanel simPresPane;
	private JButton btnStart;
	private JButton btnResumePause;
	private JButton btnStop;
	private JButton btnRunQuery;
	private JProgressBar progressBar;
	private JSpinner spnSimEccentricity;
	private JSpinner spnSimTilt;
	private JSpinner spnGridSpacing;
	private JSpinner spnTimeStep;
	private JSpinner spnDisplayRate;
	private JSpinner spnSimLength;
	private JTextField txtSimName;
	private JCheckBox cbSimVisibility;
	private JComboBox comboBoxQuery;
	private JLabel lblQueryEccentricityValue;
	private JLabel lblQueryTiltValue;
	private JTextArea txtAreaQueryContent;

	// Thread model
	BlockingQueue<Object> queue = null;
	Map map = null;
	private boolean running = true;
	private boolean paused = false;
	private boolean newConfig = false;
	private boolean simRunning = false;
	EarthPanel earthPanel;
	private int iter = 0;
	private float longChangePerStep = 0;
	private TemperatureGridImpl grid;

	/**
	 * This should be all we need to put this in its own thread
	 */
	@Override
	public void run() {
		// Load UI
		this.setTitle("Heated Planet Simulator");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		this.setVisible(true);
		this.pack();

		while(this.isRunning()) {
			//System.out.println("View sim running: " + this.isSimRunning());

			grid = new TemperatureGridImpl(getGSpacing());
			while(this.isSimRunning()) {
				//System.out.println("VIEW: Is Enabled");
				
				try {
					map = (Map) queue.take();
					//System.out.println("-->Sim " + map);
					if(!map.get("Iter").equals(iter)) {
						if(grid != null) {
							presentation(grid);
						}
						
						iter++;
					}

					double lat = ((Double) map.get("Lat")).intValue();
					double lon = ((Double) map.get("Lon")).intValue();
					double temp = ((Double) map.get("Temp")).intValue();
					
					grid.setTemperature((int)lat, (int)lon, (int)temp,getGSpacing());
						// System.out.println("map VIEW");
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Present an update to the grid
	 */
	public void presentation(TemperatureGridImpl grid) {
		
		try {
			if(grid != null)
			{
				//System.out.println("Update Grid");
				//System.out.println(grid.getCells().length);
				earthPanel.updateGrid(grid);
				earthPanel.moveSunPosition(longChangePerStep);
				updateProgress(iter);
			}

		} catch (Exception ex) {
			System.out.println("Animation Error: " + ex.getMessage());
		}
	}

	/**
	 * Create the frame.
	 */
	public View(BlockingQueue<Object> queue) { //TODO we still need to add the functionality to display the queue
		this.queue = queue;

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(50, 50, WIN_WIDTH, WIN_HEIGHT);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.SOUTH);

		JPanel simContentPane = new JPanel();
		tabbedPane.addTab("Simulator", null, simContentPane, null);
		tabbedPane.setEnabledAt(0, true);
		GridBagLayout gbl_simContentPane = new GridBagLayout();
		gbl_simContentPane.columnWidths = new int[] {EARTH_WIDTH};
		gbl_simContentPane.rowHeights = new int[] {EARTH_HEIGHT, 190};
		gbl_simContentPane.columnWeights = new double[]{1.0};
		gbl_simContentPane.rowWeights = new double[]{0.0, 0.0};
		simContentPane.setLayout(gbl_simContentPane);

		simPresPane = new JPanel();
		simPresPane.setBorder(new LineBorder(new Color(0, 0, 0)));
		GridBagConstraints gbc_simPresPane = new GridBagConstraints();
		gbc_simPresPane.fill = GridBagConstraints.BOTH;
		gbc_simPresPane.insets = new Insets(5, 5, 5, 5);
		gbc_simPresPane.gridx = 0;
		gbc_simPresPane.gridy = 0;
		earthPanel = new EarthPanel(new Dimension(EARTH_WIDTH,
				EARTH_HEIGHT), new Dimension(EARTH_WIDTH, EARTH_HEIGHT),
				new Dimension(EARTH_WIDTH, EARTH_HEIGHT));
		simPresPane.add(earthPanel);
		simContentPane.add(simPresPane, gbc_simPresPane);
		GridBagLayout gbl_simPresPane = new GridBagLayout();
		gbl_simPresPane.columnWidths = new int[] {EARTH_WIDTH};
		gbl_simPresPane.rowHeights = new int[] {EARTH_HEIGHT};
		gbl_simPresPane.columnWeights = new double[]{0.0};
		gbl_simPresPane.rowWeights = new double[]{0.0};
		simPresPane.setLayout(gbl_simPresPane);

		JPanel simParamsPane = new JPanel();
		GridBagConstraints gbc_simParamsPane = new GridBagConstraints();
		gbc_simParamsPane.fill = GridBagConstraints.BOTH;
		gbc_simParamsPane.gridx = 0;
		gbc_simParamsPane.gridy = 1;
		simContentPane.add(simParamsPane, gbc_simParamsPane);
		simParamsPane.setLayout(new GridLayout(1, 2, 0, 0));

		JPanel simLeftPane = new JPanel();
		simParamsPane.add(simLeftPane);
		GridBagLayout gbl_simLeftPane = new GridBagLayout();
		gbl_simLeftPane.rowHeights = new int[] {35, 170};
		gbl_simLeftPane.columnWeights = new double[]{1.0};
		gbl_simLeftPane.rowWeights = new double[]{0.0, 1.0};
		simLeftPane.setLayout(gbl_simLeftPane);

		JPanel progressPane = new JPanel();
		GridBagConstraints gbc_progressPane = new GridBagConstraints();
		gbc_progressPane.anchor = GridBagConstraints.NORTH;
		gbc_progressPane.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressPane.gridx = 0;
		gbc_progressPane.gridy = 0;
		simLeftPane.add(progressPane, gbc_progressPane);
		progressPane.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 5));

		progressBar = new JProgressBar();
		progressBar.setPreferredSize(new Dimension(380, 22));
		progressBar.setString("Click 'Start' To Begin");
		progressBar.setStringPainted(true);
		progressBar.setToolTipText("Simulation Progress");
		progressPane.add(progressBar);

		JPanel simSettingsPane = new JPanel();
		simSettingsPane.setBorder(new TitledBorder(null, "Simulation Settings", TitledBorder.LEADING, TitledBorder.TOP, new Font("Tahoma", Font.PLAIN, 12), null));
		GridBagConstraints gbc_simSettingsPane = new GridBagConstraints();
		gbc_simSettingsPane.fill = GridBagConstraints.BOTH;
		gbc_simSettingsPane.gridheight = 0;
		gbc_simSettingsPane.gridx = 0;
		gbc_simSettingsPane.gridy = 1;
		simLeftPane.add(simSettingsPane, gbc_simSettingsPane);
		simSettingsPane.setLayout(new GridLayout(5, 2, 5, 5));

		JLabel lblSimName = new JLabel("Simulation Name");
		lblSimName.setBorder(new EmptyBorder(0, 10, 0, 0));
		simSettingsPane.add(lblSimName);

		txtSimName = new JTextField();
		lblSimName.setLabelFor(txtSimName);
		txtSimName.setEditable(false);
		txtSimName.setColumns(10);
		simSettingsPane.add(txtSimName);

		JLabel lblGridSpacing = new JLabel("Grid Spacing (degrees)");
		lblGridSpacing.setBorder(new EmptyBorder(0, 10, 0, 0));
		simSettingsPane.add(lblGridSpacing);

		spnGridSpacing = new JSpinner(new SpinnerListModel(new String[] {"1", "2", "3", "4", "5", "6", "9", "10", "12", "15", "18", "20", "30", "36", "45", "60", "90", "180"}));
		spnGridSpacing.setValue(String.valueOf(DEFAULT_GRID_SPACING));
		spnGridSpacing.setFont( new Font("Arial", Font.BOLD, 12));
		lblGridSpacing.setLabelFor(spnGridSpacing);
		simSettingsPane.add(spnGridSpacing);

		JLabel lblTimeStep = new JLabel("Time Step (minutes)");
		lblTimeStep.setBorder(new EmptyBorder(0, 10, 0, 0));
		simSettingsPane.add(lblTimeStep);

		spnTimeStep = new JSpinner();
		spnTimeStep.setModel(new SpinnerNumberModel(DEFAULT_TIME_STEP, 1, 525600, 1));
		lblTimeStep.setLabelFor(spnTimeStep);
		simSettingsPane.add(spnTimeStep);

		JLabel lblDisplayRate = new JLabel("Display Rate (ms)");
		lblDisplayRate.setBorder(new EmptyBorder(0, 10, 0, 0));
		simSettingsPane.add(lblDisplayRate);

		spnDisplayRate = new JSpinner();
		spnDisplayRate.setModel(new SpinnerNumberModel(DEFAULT_DISPLAY_RATE, 1, 1000, 1));
		lblDisplayRate.setLabelFor(spnDisplayRate);
		simSettingsPane.add(spnDisplayRate);

		JLabel lblSimLength = new JLabel("Sim Length (months)");
		lblSimLength.setBorder(new EmptyBorder(0, 10, 0, 0));
		simSettingsPane.add(lblSimLength);

		spnSimLength = new JSpinner();
		spnSimLength.setModel(new SpinnerNumberModel(DEFAULT_SIM_LENGTH, 1, 1200, 1));
		lblSimLength.setLabelFor(spnSimLength);
		simSettingsPane.add(spnSimLength);

		JPanel simRightPane = new JPanel();
		simParamsPane.add(simRightPane);
		GridBagLayout gbl_simRightPane = new GridBagLayout();
		gbl_simRightPane.rowHeights = new int[] {35, 85, 85};
		gbl_simRightPane.columnWeights = new double[]{1.0};
		gbl_simRightPane.rowWeights = new double[]{0.0, 0.0, 0.0};
		simRightPane.setLayout(gbl_simRightPane);

		JPanel miscPane = new JPanel();
		FlowLayout flowLayout = (FlowLayout) miscPane.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		flowLayout.setHgap(0);
		GridBagConstraints gbc_miscPane = new GridBagConstraints();
		gbc_miscPane.anchor = GridBagConstraints.NORTH;
		gbc_miscPane.gridx = 0;
		gbc_miscPane.gridy = 0;
		simRightPane.add(miscPane, gbc_miscPane);

		JLabel lblOrbPosText = new JLabel("Orbital Position");
		miscPane.add(lblOrbPosText);
		lblOrbPosText.setVisible(false); // not sure if needed anymore

		JLabel lblOrbPosValue = new JLabel("");
		miscPane.add(lblOrbPosValue);

		JLabel lblRotPosText = new JLabel("Rotational Position");
		lblRotPosText.setBorder(new EmptyBorder(0, 25, 0, 0));
		miscPane.add(lblRotPosText);
		lblRotPosText.setVisible(false); // not sure if needed anymore

		JLabel lblRotPosValue = new JLabel("");
		miscPane.add(lblRotPosValue);

		JPanel physFactorsPane = new JPanel();
		physFactorsPane.setBorder(new TitledBorder(null, "Physical Factors", TitledBorder.LEADING, TitledBorder.TOP, new Font("Tahoma", Font.PLAIN, 12), null));
		GridBagConstraints gbc_physFactorsPane = new GridBagConstraints();
		gbc_physFactorsPane.fill = GridBagConstraints.BOTH;
		gbc_physFactorsPane.gridx = 0;
		gbc_physFactorsPane.gridy = 1;
		simRightPane.add(physFactorsPane, gbc_physFactorsPane);
		physFactorsPane.setLayout(new GridLayout(2, 2, 5, 5));

		JLabel lblSimEccentricity = new JLabel("Eccentricity");
		lblSimEccentricity.setHorizontalAlignment(SwingConstants.LEFT);
		lblSimEccentricity.setBorder(new EmptyBorder(0, 10, 0, 0));
		physFactorsPane.add(lblSimEccentricity);

		spnSimEccentricity = new JSpinner();
		spnSimEccentricity.setModel(new SpinnerNumberModel(DEFAULT_ECCENTRICITY, 0, .9999, 0.0001));
		spnSimEccentricity.setEditor(new JSpinner.NumberEditor(spnSimEccentricity,"0.0000"));
		lblSimEccentricity.setLabelFor(spnSimEccentricity);
		physFactorsPane.add(spnSimEccentricity);

		JLabel lblSimTilt = new JLabel("Axial Tilt");
		lblSimTilt.setBorder(new EmptyBorder(0, 10, 0, 0));
		physFactorsPane.add(lblSimTilt);

		spnSimTilt = new JSpinner();		
		spnSimTilt.setModel(new SpinnerNumberModel(DEFAULT_TILT, -180.0, 180.0, 0.01));
		spnSimTilt.setEditor(new JSpinner.NumberEditor(spnSimTilt,"0.00"));  
		lblSimTilt.setLabelFor(spnSimTilt);
		physFactorsPane.add(spnSimTilt);

		JPanel simControlsPane = new JPanel();
		simControlsPane.setBorder(new TitledBorder(null, "Simulation Controls", TitledBorder.LEADING, TitledBorder.TOP, new Font("Tahoma", Font.PLAIN, 12), null));
		GridBagConstraints gbc_simControlsPane = new GridBagConstraints();
		gbc_simControlsPane.fill = GridBagConstraints.BOTH;
		gbc_simControlsPane.gridx = 0;
		gbc_simControlsPane.gridy = 2;
		simRightPane.add(simControlsPane, gbc_simControlsPane);
		GridBagLayout gbl_simControlsPane = new GridBagLayout();
		gbl_simControlsPane.columnWidths = new int[]{46, 117, 67, 115, 0};
		gbl_simControlsPane.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_simControlsPane.rowWeights = new double[]{0.0, 0.0};
		simControlsPane.setLayout(gbl_simControlsPane);

		btnStart = new JButton("Start");
		btnStart.setToolTipText("Start Simulation");
		GridBagConstraints gbc_btnStart = new GridBagConstraints();
		gbc_btnStart.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnStart.insets = new Insets(0, 0, 5, 5);
		gbc_btnStart.gridx = 1;
		gbc_btnStart.gridy = 0;
		simControlsPane.add(btnStart, gbc_btnStart);

		btnResumePause = new JButton("Pause");
		btnResumePause.setEnabled(false);
		btnResumePause.setToolTipText("Toggle Simulation");
		GridBagConstraints gbc_btnResumePause = new GridBagConstraints();
		gbc_btnResumePause.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnResumePause.insets = new Insets(0, 0, 5, 5);
		gbc_btnResumePause.gridx = 2;
		gbc_btnResumePause.gridy = 0;
		simControlsPane.add(btnResumePause, gbc_btnResumePause);

		btnStop = new JButton("Stop");
		btnStop.setToolTipText("Stop Simulation");
		btnStop.setEnabled(false);
		GridBagConstraints gbc_btnStop = new GridBagConstraints();
		gbc_btnStop.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnStop.insets = new Insets(0, 0, 5, 0);
		gbc_btnStop.gridx = 3;
		gbc_btnStop.gridy = 0;
		simControlsPane.add(btnStop, gbc_btnStop);

		cbSimVisibility = new JCheckBox("If checked, simulation animation is not displayed.");
		cbSimVisibility.setActionCommand("");
		cbSimVisibility.setToolTipText("If checked, simulation animation is not displayed.");
		GridBagConstraints gbc_cbSimVisibility = new GridBagConstraints();
		gbc_cbSimVisibility.anchor = GridBagConstraints.NORTHWEST;
		gbc_cbSimVisibility.gridwidth = 3;
		gbc_cbSimVisibility.gridx = 1;
		gbc_cbSimVisibility.gridy = 1;
		simControlsPane.add(cbSimVisibility, gbc_cbSimVisibility);

		JPanel queryContentPane = new JPanel();
		tabbedPane.addTab("Query Interface", null, queryContentPane, null);
		GridBagLayout gbl_queryContentPane = new GridBagLayout();
		gbl_queryContentPane.columnWidths = new int[] {EARTH_WIDTH};
		gbl_queryContentPane.rowHeights = new int[] {300, 45, 230};
		gbl_queryContentPane.columnWeights = new double[]{1.0};
		gbl_queryContentPane.rowWeights = new double[]{0.0, 0.0, 1.0};
		queryContentPane.setLayout(gbl_queryContentPane);

		txtAreaQueryContent = new JTextArea(15, 65);
		txtAreaQueryContent.setWrapStyleWord(true);
		txtAreaQueryContent.setLineWrap(true);
		txtAreaQueryContent.setSize(400,400); 
		Font txtArea = new Font("Verdana", Font.PLAIN, 12);
		txtAreaQueryContent.setFont(txtArea);
		txtAreaQueryContent.setText("Please input your parameters and click the 'Run Seach Query' button to begin.");
		queryContentPane.add(txtAreaQueryContent);

		JScrollPane scrQueryContent = new JScrollPane(txtAreaQueryContent,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		queryContentPane.add(scrQueryContent);// Add you scroll pane to container	
		
		JPanel queryPresPane = new JPanel();
		queryPresPane.setBorder(new LineBorder(new Color(0, 0, 0)));
		GridBagConstraints gbc_queryPresPane = new GridBagConstraints();
		gbc_queryPresPane.fill = GridBagConstraints.BOTH;
		gbc_queryPresPane.insets = new Insets(5, 5, 5, 5);
		gbc_queryPresPane.gridx = 0;
		gbc_queryPresPane.gridy = 0;
		queryContentPane.add(queryPresPane, gbc_queryPresPane);
		GridBagLayout gbl_queryPresPane = new GridBagLayout();
		gbl_queryPresPane.columnWidths = new int[] {0, EARTH_WIDTH};
		gbl_queryPresPane.rowHeights = new int[] {0, 300};
		gbl_queryPresPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_queryPresPane.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		queryPresPane.setLayout(gbl_queryPresPane);

		JPanel queryDDLPane = new JPanel();
		GridBagConstraints gbc_queryDDLPane = new GridBagConstraints();
		gbc_queryDDLPane.insets = new Insets(0, 0, 5, 0);
		gbc_queryDDLPane.fill = GridBagConstraints.BOTH;
		gbc_queryDDLPane.gridx = 0;
		gbc_queryDDLPane.gridy = 1;
		queryContentPane.add(queryDDLPane, gbc_queryDDLPane);
		queryDDLPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		comboBoxQuery = new JComboBox();		
		comboBoxQuery.setModel(new DefaultComboBoxModel(new String[] {"Select a simulation..."}));
		comboBoxQuery.setPreferredSize(new Dimension(EARTH_WIDTH, 30));
		comboBoxQuery.setToolTipText("Please select a simulation.");
		queryDDLPane.add(comboBoxQuery);

		JPanel queryParamsPane = new JPanel();
		GridBagConstraints gbc_queryParamsPane = new GridBagConstraints();
		gbc_queryParamsPane.fill = GridBagConstraints.BOTH;
		gbc_queryParamsPane.gridx = 0;
		gbc_queryParamsPane.gridy = 2;
		queryContentPane.add(queryParamsPane, gbc_queryParamsPane);
		queryParamsPane.setLayout(new GridLayout(2, 2, 5, 0));

		JPanel pnlTimePeriodPane = new JPanel();
		pnlTimePeriodPane.setBorder(new TitledBorder(null, "Time Period", TitledBorder.LEADING, TitledBorder.TOP, new Font("Tahoma", Font.PLAIN, 12), null));
		queryParamsPane.add(pnlTimePeriodPane);
		GridBagLayout gbl_pnlTimePeriodPane = new GridBagLayout();
		gbl_pnlTimePeriodPane.columnWidths = new int[] {30, 0, 258};
		gbl_pnlTimePeriodPane.columnWeights = new double[]{0.0, 0.0, 0.0};
		gbl_pnlTimePeriodPane.rowWeights = new double[]{0.0, 0.0};
		pnlTimePeriodPane.setLayout(gbl_pnlTimePeriodPane);

		JLabel lblStartDate = new JLabel("Start Date");
		GridBagConstraints gbc_lblStartDate = new GridBagConstraints();
		gbc_lblStartDate.anchor = GridBagConstraints.WEST;
		gbc_lblStartDate.insets = new Insets(0, 0, 5, 5);
		gbc_lblStartDate.gridx = 0;
		gbc_lblStartDate.gridy = 0;
		pnlTimePeriodPane.add(lblStartDate, gbc_lblStartDate);

		JSpinner spnStartDate = new JSpinner();
		spnStartDate.setModel(new SpinnerDateModel(new Date(1388811600000L), new Date(-62135751600000L), new Date(253402318799000L), Calendar.HOUR_OF_DAY));
		JFormattedTextField tf = ((JSpinner.DefaultEditor)spnStartDate.getEditor()).getTextField();
		tf.setHorizontalAlignment(JFormattedTextField.RIGHT);
		lblStartDate.setLabelFor(spnStartDate);
		GridBagConstraints gbc_spnStartDate = new GridBagConstraints();
		gbc_spnStartDate.fill = GridBagConstraints.HORIZONTAL;
		gbc_spnStartDate.insets = new Insets(0, 0, 5, 0);
		gbc_spnStartDate.gridx = 2;
		gbc_spnStartDate.gridy = 0;
		pnlTimePeriodPane.add(spnStartDate, gbc_spnStartDate);

		JLabel lblEndDate = new JLabel("End Date");
		GridBagConstraints gbc_lblEndDate = new GridBagConstraints();
		gbc_lblEndDate.anchor = GridBagConstraints.WEST;
		gbc_lblEndDate.insets = new Insets(0, 0, 0, 5);
		gbc_lblEndDate.gridx = 0;
		gbc_lblEndDate.gridy = 1;
		pnlTimePeriodPane.add(lblEndDate, gbc_lblEndDate);

		JSpinner spnEndDate = new JSpinner();
		spnEndDate.setModel(new SpinnerDateModel(new Date(1388811600000L), new Date(-62135751600000L), new Date(253402318799000L), Calendar.HOUR_OF_DAY));
		tf = ((JSpinner.DefaultEditor)spnEndDate.getEditor()).getTextField();
		tf.setHorizontalAlignment(JFormattedTextField.RIGHT);
		lblEndDate.setLabelFor(spnEndDate);
		GridBagConstraints gbc_spnEndDate = new GridBagConstraints();
		gbc_spnEndDate.fill = GridBagConstraints.HORIZONTAL;
		gbc_spnEndDate.gridx = 2;
		gbc_spnEndDate.gridy = 1;
		pnlTimePeriodPane.add(spnEndDate, gbc_spnEndDate);

		JPanel pnlPhysFactorsPane = new JPanel();
		pnlPhysFactorsPane.setBorder(new TitledBorder(null, "Physical Factors", TitledBorder.LEADING, TitledBorder.TOP, new Font("Tahoma", Font.PLAIN, 12), null));
		queryParamsPane.add(pnlPhysFactorsPane);
		GridBagLayout gbl_pnlPhysFactorsPane = new GridBagLayout();
		gbl_pnlPhysFactorsPane.columnWidths = new int[] {150, 200};
		gbl_pnlPhysFactorsPane.columnWeights = new double[]{0.0, 0.0};
		gbl_pnlPhysFactorsPane.rowWeights = new double[]{0.0, 0.0};
		pnlPhysFactorsPane.setLayout(gbl_pnlPhysFactorsPane);

		JLabel lblQueryEccentricity = new JLabel("Eccentricity");
		lblQueryEccentricity.setBorder(new EmptyBorder(0, 10, 0, 0));
		lblQueryEccentricity.setAlignmentY(Component.TOP_ALIGNMENT);
		GridBagConstraints gbc_lblQueryEccentricity = new GridBagConstraints();
		gbc_lblQueryEccentricity.anchor = GridBagConstraints.WEST;
		gbc_lblQueryEccentricity.insets = new Insets(0, 0, 5, 5);
		gbc_lblQueryEccentricity.gridx = 0;
		gbc_lblQueryEccentricity.gridy = 0;
		pnlPhysFactorsPane.add(lblQueryEccentricity, gbc_lblQueryEccentricity);

		lblQueryEccentricityValue = new JLabel("---");
		GridBagConstraints gbc_spnQueryEccentricity = new GridBagConstraints();
		gbc_spnQueryEccentricity.fill = GridBagConstraints.HORIZONTAL;
		gbc_spnQueryEccentricity.insets = new Insets(0, 0, 5, 5);
		gbc_spnQueryEccentricity.gridx = 1;
		gbc_spnQueryEccentricity.gridy = 0;
		pnlPhysFactorsPane.add(lblQueryEccentricityValue, gbc_spnQueryEccentricity);

		JLabel lblQueryTilt = new JLabel("Axial Tilt");
		lblQueryTilt.setBorder(new EmptyBorder(0, 10, 0, 0));
		GridBagConstraints gbc_lblQueryTilt = new GridBagConstraints();
		gbc_lblQueryTilt.anchor = GridBagConstraints.WEST;
		gbc_lblQueryTilt.insets = new Insets(0, 0, 0, 5);
		gbc_lblQueryTilt.gridx = 0;
		gbc_lblQueryTilt.gridy = 1;
		pnlPhysFactorsPane.add(lblQueryTilt, gbc_lblQueryTilt);

		lblQueryTiltValue = new JLabel("---");
		GridBagConstraints gbc_spnQueryTilt = new GridBagConstraints();
		gbc_spnQueryTilt.fill = GridBagConstraints.HORIZONTAL;
		gbc_spnQueryTilt.insets = new Insets(0, 0, 0, 5);
		gbc_spnQueryTilt.gridx = 1;
		gbc_spnQueryTilt.gridy = 1;
		pnlPhysFactorsPane.add(lblQueryTiltValue, gbc_spnQueryTilt);

		JPanel pnlLatLonPane = new JPanel();
		pnlLatLonPane.setBorder(new TitledBorder(null, "Lat/Lon Range", TitledBorder.LEADING, TitledBorder.TOP, new Font("Tahoma", Font.PLAIN, 12), null));
		queryParamsPane.add(pnlLatLonPane);
		GridBagLayout gbl_pnlLatLonPane = new GridBagLayout();
		gbl_pnlLatLonPane.columnWidths = new int[] {30, 140, 30, 30, 140};
		gbl_pnlLatLonPane.rowHeights = new int[] {30, 0, 0, 0};
		gbl_pnlLatLonPane.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
		gbl_pnlLatLonPane.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlLatLonPane.setLayout(gbl_pnlLatLonPane);

		JLabel lblLatHeader = new JLabel("Latitude");
		GridBagConstraints gbc_lblLatHeader = new GridBagConstraints();
		gbc_lblLatHeader.gridwidth = 2;
		gbc_lblLatHeader.anchor = GridBagConstraints.SOUTHWEST;
		gbc_lblLatHeader.insets = new Insets(0, 0, 5, 5);
		gbc_lblLatHeader.gridx = 0;
		gbc_lblLatHeader.gridy = 0;
		pnlLatLonPane.add(lblLatHeader, gbc_lblLatHeader);

		JLabel lblLonHeader = new JLabel("Longitude");
		GridBagConstraints gbc_lblLonHeader = new GridBagConstraints();
		gbc_lblLonHeader.gridwidth = 2;
		gbc_lblLonHeader.anchor = GridBagConstraints.SOUTHWEST;
		gbc_lblLonHeader.insets = new Insets(0, 0, 5, 0);
		gbc_lblLonHeader.gridx = 3;
		gbc_lblLonHeader.gridy = 0;
		pnlLatLonPane.add(lblLonHeader, gbc_lblLonHeader);

		JLabel lblLatFrom = new JLabel("from");
		GridBagConstraints gbc_lblLatFrom = new GridBagConstraints();
		gbc_lblLatFrom.anchor = GridBagConstraints.WEST;
		gbc_lblLatFrom.insets = new Insets(0, 0, 5, 5);
		gbc_lblLatFrom.gridx = 0;
		gbc_lblLatFrom.gridy = 1;
		pnlLatLonPane.add(lblLatFrom, gbc_lblLatFrom);

		JSpinner spnLatFrom = new JSpinner();
		spnLatFrom.setModel(new SpinnerNumberModel(-90, -90, 90, 1));
		lblLatFrom.setLabelFor(spnLatFrom);
		GridBagConstraints gbc_spnLatFrom = new GridBagConstraints();
		gbc_spnLatFrom.insets = new Insets(0, 0, 5, 5);
		gbc_spnLatFrom.fill = GridBagConstraints.HORIZONTAL;
		gbc_spnLatFrom.gridx = 1;
		gbc_spnLatFrom.gridy = 1;
		pnlLatLonPane.add(spnLatFrom, gbc_spnLatFrom);

		JLabel lblLatTo = new JLabel("to");
		GridBagConstraints gbc_lblLatTo = new GridBagConstraints();
		gbc_lblLatTo.anchor = GridBagConstraints.WEST;
		gbc_lblLatTo.insets = new Insets(0, 0, 0, 5);
		gbc_lblLatTo.gridx = 0;
		gbc_lblLatTo.gridy = 2;
		pnlLatLonPane.add(lblLatTo, gbc_lblLatTo);

		JSpinner spnLatTo = new JSpinner();
		spnLatTo.setModel(new SpinnerNumberModel(90, -90, 90, 1));
		lblLatTo.setLabelFor(spnLatTo);
		GridBagConstraints gbc_spnLatTo = new GridBagConstraints();
		gbc_spnLatTo.insets = new Insets(0, 0, 0, 5);
		gbc_spnLatTo.fill = GridBagConstraints.HORIZONTAL;
		gbc_spnLatTo.gridx = 1;
		gbc_spnLatTo.gridy = 2;
		pnlLatLonPane.add(spnLatTo, gbc_spnLatTo);

		JLabel lblLonFrom = new JLabel("from");
		GridBagConstraints gbc_lblLonFrom = new GridBagConstraints();
		gbc_lblLonFrom.anchor = GridBagConstraints.WEST;
		gbc_lblLonFrom.insets = new Insets(0, 0, 5, 5);
		gbc_lblLonFrom.gridx = 3;
		gbc_lblLonFrom.gridy = 1;
		pnlLatLonPane.add(lblLonFrom, gbc_lblLonFrom);

		JSpinner spnLonFrom = new JSpinner();
		spnLonFrom.setModel(new SpinnerNumberModel(-180, -180, 180, 1));
		lblLonFrom.setLabelFor(spnLonFrom);
		GridBagConstraints gbc_spnLonFrom = new GridBagConstraints();
		gbc_spnLonFrom.insets = new Insets(0, 0, 5, 0);
		gbc_spnLonFrom.fill = GridBagConstraints.HORIZONTAL;
		gbc_spnLonFrom.gridx = 4;
		gbc_spnLonFrom.gridy = 1;
		pnlLatLonPane.add(spnLonFrom, gbc_spnLonFrom);

		JLabel lblLonTo = new JLabel("to");
		GridBagConstraints gbc_lblLonTo = new GridBagConstraints();
		gbc_lblLonTo.anchor = GridBagConstraints.WEST;
		gbc_lblLonTo.insets = new Insets(0, 0, 0, 5);
		gbc_lblLonTo.gridx = 3;
		gbc_lblLonTo.gridy = 2;
		pnlLatLonPane.add(lblLonTo, gbc_lblLonTo);

		JSpinner spnLonTo = new JSpinner();
		spnLonTo.setModel(new SpinnerNumberModel(180, -180, 180, 1));
		lblLonTo.setLabelFor(spnLonTo);
		GridBagConstraints gbc_spnLonTo = new GridBagConstraints();
		gbc_spnLonTo.fill = GridBagConstraints.HORIZONTAL;
		gbc_spnLonTo.gridx = 4;
		gbc_spnLonTo.gridy = 2;
		pnlLatLonPane.add(spnLonTo, gbc_spnLonTo);

		JPanel pnlQueryControlsPane = new JPanel();
		pnlQueryControlsPane.setBorder(new TitledBorder(null, "Query Controls", TitledBorder.LEADING, TitledBorder.TOP, new Font("Tahoma", Font.PLAIN, 12), null));
		queryParamsPane.add(pnlQueryControlsPane);

		btnRunQuery = new JButton("Run Search Query");
		btnRunQuery.setVerticalAlignment(SwingConstants.BOTTOM);
		pnlQueryControlsPane.add(btnRunQuery);
		tabbedPane.setEnabledAt(1, true);

		// start simulation
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {		
				// disable controls
				btnResumePause.setEnabled(true);
				toggleSimControls(false);
				progressBar.setString(DEFAULT_DATE_TIME + " (0%)");
				progressBar.setMaximum(month2Miniute(getDuration()));
				start();
			}
		});

		// resume and pause simulation
		btnResumePause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!isPaused()) {
					btnResumePause.setText("Resume");
					btnStop.setEnabled(true);
					pause();
				}
				else {
					btnResumePause.setText("Pause");
					btnStop.setEnabled(false);
					resume();
				}
			}
		});

		// stop simulation
		btnStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
			}
		});

		// toggle visibility
		cbSimVisibility.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				simPresPane.setVisible(!cbSimVisibility.isSelected());
			}
		});

		// grid spacing
		spnGridSpacing.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// redraw grid spacing
				JSpinner s = (JSpinner) e.getSource();
				earthPanel.drawGrid(Integer.parseInt(s.getValue().toString()));
			}

		});

		// interface query: run search
		btnRunQuery.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnRunQuery.setEnabled(false);
				tabbedPane.setEnabledAt(0, false);

				boolean completed = false;
				txtAreaQueryContent.setText("");
				int configId = getIdFromComboBox(comboBoxQuery.getSelectedItem().toString());

				if(configId == 0) {
					txtAreaQueryContent.setText("Please select a Simulation from the drop down.");
				}
				else
				{
					try {
						DBModel dbMod = new DBModel();
						QueryResult query = dbMod.Query(
								getIdFromComboBox(comboBoxQuery.getSelectedItem().toString()), 
								"", -1,-1,-1, "", -1,-1,-1);

						if(query.Simulation.size()>0) {
							SimulationConfig sc = query.Simulation.get(0);

							// Minimum temperature in the region, when and where it occurred; 
							txtAreaQueryContent.append(
									"Min Temp: " + sc.MinTemp.Temperatue + 
									"\u00b0 at (" + sc.MinTemp.Latitude + "," + 
									sc.MinTemp.Longitude + ")\n");

							// Maximum temperature in the region, when and where it occurred; 
							txtAreaQueryContent.append(
									"Max Temp: " + sc.MaxTemp.Temperatue + 
									"\u00b0 at (" + sc.MaxTemp.Latitude + "," + 
									sc.MaxTemp.Longitude + ") \n");
							
							// Mean temperature over the time for the requested region;

							// Mean temperature over the region for the requested times; 
							txtAreaQueryContent.append(
									"Mean Temp: " + sc.MeanTempOverRegion + "\u00b0 \n\n");

							// Actual values, whether recorded or interpolated, for the requested cells at the requested times. 
							for(int i = 0; i< sc.TemperatureReadings.size(); i++) {

								// Mean temperature over the region for the requested times; 
								txtAreaQueryContent.append("(" + sc.TemperatureReadings.get(i).Latitude + ", " +
									 sc.TemperatureReadings.get(i).Longitude + ") = " + sc.TemperatureReadings.get(i).Temperatue + ", ");
							}
							txtAreaQueryContent.setCaretPosition(0);
						}
						else
						{
							txtAreaQueryContent.append("Data for this simulation does not exist.");
						}


						// close db upon completion 
						dbMod.closeDBSession();
					}
					catch (Exception ex) {
						ex.printStackTrace();
						txtAreaQueryContent.append("Error: Simulation does not exist.");
					}
				}

				btnRunQuery.setEnabled(true);
				tabbedPane.setEnabledAt(0, true);
			}
		});

		// update textbox values on changing combo box
		comboBoxQuery.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					if(comboBoxQuery.getSelectedIndex() != 0) {
						DBModel dbMod = new DBModel();
						QueryResult query = dbMod.Query(0, "", -1,-1,-1, "", -1,-1,-1);
						double ecc = query.Simulation.get(comboBoxQuery.getSelectedIndex()-1).Orbit;
						double tilt = query.Simulation.get(comboBoxQuery.getSelectedIndex()-1).Tilt;
						lblQueryEccentricityValue.setText(Double.toString(ecc));
						lblQueryTiltValue.setText(Double.toString(tilt));
						dbMod.closeDBSession();
						txtAreaQueryContent.setText("Please input your parameters and click the 'Run Seach Query' button to begin.");
					}
				}
			}
		});

		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {	    	
				if(tabbedPane.getSelectedIndex() == 1) {
					// refresh simulation list
					DBModel dbMod = new DBModel();
					QueryResult query = dbMod.Query(0, "", -1,-1,-1, "", -1,-1,-1);
					for(int i =0; i < query.Simulation.size(); i++) {
						SimulationConfig sc = query.Simulation.get(i);
						comboBoxQuery.addItem(sc.getConfigId() + ": " + sc.Name);
					}
					dbMod.closeDBSession();
				}
				else
				{
					comboBoxQuery.setSelectedIndex(0);
					lblQueryEccentricityValue.setText("---");
					lblQueryTiltValue.setText("---");
					txtAreaQueryContent.setText("Please input your parameters and click the 'Run Seach Query' button to begin.");
					comboBoxQuery.removeAllItems();
					comboBoxQuery.addItem("Please select a simulation.");
				}
			}
		});
	}

	public void toggleSimControls(boolean bool) {
		btnStart.setEnabled(bool);
		tabbedPane.setEnabledAt(1, bool);
		spnGridSpacing.setEnabled(bool);
		spnTimeStep.setEnabled(bool);
		spnDisplayRate.setEnabled(bool);
		spnSimLength.setEnabled(bool);
		spnSimEccentricity.setEnabled(bool);
		spnSimTilt.setEnabled(bool);
	}

	public void pause(){
		this.paused = true;
		this.simRunning = false;
	}

	public void resume(){
		this.paused = false;
		this.simRunning = true;
	}

	public void stop(){
		// closes the thread/view
		// this.running = false;
		this.iter = 0; // reset iteration
		this.longChangePerStep = 0;
		this.simRunning = false;
		
		// reset controls
		btnResumePause.setText("Pause");
		btnResumePause.setEnabled(false);
		btnStop.setEnabled(false);
		progressBar.setString("Sim Completed.");
		progressBar.setValue(0);
		toggleSimControls(true);
	}

	public void start(){
		this.newConfig = true;
		this.simRunning = true;
		this.longChangePerStep = 12 * getStep() / 3600;;
	}

	public void configReset(){
		//System.out.println("View:System start Reset");
		this.newConfig = false;
	}

	public boolean isSimRunning(){
		return this.simRunning;
	}

	public boolean isRunning(){
		return this.running;
	}

	public boolean isPaused(){
		return this.paused;
	}

	public boolean newConfigStarted(){
		return this.newConfig;
	}

	public String getSimName(){
		return "";		
	}

	public double getOrbit(){
		return (Double)spnSimEccentricity.getValue();
	}

	public double getTilt(){
		return (Double)spnSimTilt.getValue();		
	}

	public int getGSpacing(){
		return Integer.parseInt(spnGridSpacing.getValue().toString());	

	}

	public int getStep(){
		return (Integer)spnTimeStep.getValue();
	}

	public int getDuration(){
		return (Integer)spnSimLength.getValue();
	}

	public int getIdFromComboBox(String combo) {
		int b = combo.indexOf(":");

		if(b == -1) 
			return 0;
		else
			return Integer.parseInt(combo.substring(0, b));
	}
	
	public int month2Miniute(int month) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(Tools.getStartDate());
		cal.add(Calendar.MONTH, month);
		long startDate = Tools.getStartDate().getTime();
		long endDate = cal.getTimeInMillis();

		return (int) (endDate - startDate) / (60000);
	}
	
	// update progress based on iterations passed
	public void updateProgress(int iteration) {
		Double percentage = (iteration * 1.0 / progressBar.getMaximum()) * 100;
		progressBar.setValue(iteration);
		
		// calculate date string from iterations
		Calendar cal = Calendar.getInstance();
		cal.setTime(Tools.getStartDate());
		cal.add(Calendar.MINUTE, (getStep() * iteration) / 60);
		progressBar.setString(cal.getTime().toString() + " ("+percentage.intValue()+"%)");
	}
}
