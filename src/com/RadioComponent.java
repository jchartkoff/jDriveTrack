package com;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.prefs.Preferences;

import javax.swing.SwingConstants;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JTextField;
import javax.swing.JSlider;

import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import jssc.SerialPortException;
import jssc.SerialPortList;

public class RadioComponent extends JDialog {
	private static final long serialVersionUID = -7750081756785824878L;
	
	private static final String[] MANUFACTURER = {"Harris", "Harris", "Icom",    "Icom",    "Yaesu"}; 
	private static final String[] MODEL_NUMBER  = {"P7200",  "XG75",   "PCR1000", "PCR2500", "FT100"};
	
	private int device;
	private int calibrationFile;
	private String portName;
	private boolean afc;
	private boolean agc;
	private boolean attenuator;
	private boolean noiseBlanker;
	private int volume;
	private int squelch;
	private double frequency;
	private int toneSquelch;
	private int digitalSquelch;
	private int mode;
	private int filter;
	private int ifShift;
	private boolean voiceScan;
	private boolean startRadioWithSystem;
	private JPanel memoryVfoPanel;
	private JPanel radioPanel;
	private JPanel systemPanel;
	private JPanel scanPanel;
	private JPanel calibrationPanel;
	private JPanel chartPanel;
	private JTabbedPane tabbedPane;
	private JButton okButton;
	private JButton cancelButton;
	private JButton applyButton;
	private JComboBox<?> deviceComboBox;
	private JComboBox<?> calFileComboBox;
	private JComboBox<Object> filterComboBox;
	private JComboBox<Object> modeComboBox;
	private JComboBox<Object> toneSquelchComboBox;
	private JComboBox<String> comPortComboBox;
	private JLabel deviceComboBoxLabel;
	private JLabel comPortComboBoxLabel;
	private JLabel calFileComboBoxLabel;
	private JLabel filterComboBoxLabel;
	private JLabel modeComboBoxLabel;
	private JLabel toneSquelchComboBoxLabel;
	private JLabel frequencyTextFieldLabel;
	private JLabel volumeSliderLabel;
	private JLabel squelchSliderLabel;
	private JCheckBox startRadioWithSystemCheckBox;
	private JCheckBox agcCheckBox;
	private JCheckBox afcCheckBox;
	private JCheckBox attenuatorCheckBox;
	private JCheckBox noiseBlankerCheckBox;
	private JTextField frequencyTextField;
	private JSlider volumeSlider;
	private JSlider squelchSlider;
	private Preferences userPref = Preferences.userRoot();;
	private DecimalFormat frequencyFormat;
	private JCheckBox jCheckBoxF10;
	private JCheckBox jCheckBoxF11;
	private JCheckBox jCheckBoxF12;
	private JCheckBox jCheckBoxF13;
	private JCheckBox jCheckBoxF14;
	private JCheckBox jCheckBoxF15;
	private JCheckBox jCheckBoxF16;
	private JCheckBox jCheckBoxF17;
	private JCheckBox jCheckBoxF18;
	private JCheckBox jCheckBoxF19;
	private JLabel jLabelF10;
	private JLabel jLabelF11;
	private JLabel jLabelF12;
	private JLabel jLabelF13;
	private JLabel jLabelF14;
	private JLabel jLabelF15;
	private JLabel jLabelF16;
	private JLabel jLabelF17;
	private JLabel jLabelF18;
	private JLabel jLabelF19;
	private JTextField jTextFieldF10;
	private JTextField jTextFieldF11;
	private JTextField jTextFieldF12;
	private JTextField jTextFieldF13;
	private JTextField jTextFieldF14;
	private JTextField jTextFieldF15;
	private JTextField jTextFieldF16;
	private JTextField jTextFieldF17;
	private JTextField jTextFieldF18;
	private JTextField jTextFieldF19;
	private JComboBox<?> calFileEditorComboBox;
    private JLabel calFileEditorComboBoxLabel;
    private JPanel calibrationGraphic;
    private JComboBox<?> dBmComboBox;
    private JPanel dBmComboBoxPanel;
    private JLabel equalSignLabel;
    private JButton fitToCurveButton;
    private JLabel manufacturerLabel;
    private JLabel manufacturerLabelLabel;
    private JComboBox<?> modelNumberComboBox;
    private JLabel modelNumberTextFieldLabel;
    private JButton newFileButton;
    private JTextField rssiTextField;
    private JPanel rssiComboBoxPanel;
    private JTextField snTextField;
    private JLabel snTextFieldLabel;
	private JRadioButton vfoRadioButton;
	private JRadioButton scanListRadioButton;
	private ButtonGroup freqSelectButtonGroup;
	private boolean vfoMode;
	private ButtonModel model;
	private String calFileDirPath;
	private File calDir = null;
	private String[] calFiles;
	private double scan[] = new double[10];
	private boolean scanSelect[] = new boolean[10];
	private SortedComboBoxModel calFileComboBoxModel;
	private DefaultComboBoxModel<Object> deviceComboBoxModel;
	private DefaultComboBoxModel<Object> modelNumberComboBoxModel;
	private SortedComboBoxModel calFileEditorComboBoxModel;
	private Calibrate calibrate;
	private Chart chart;
	private String[] rssiArray = new String[91];
	private JLabel rssiCurrentLabel;
	private JLabel rssiCurrentFieldLabel;
	private JButton rssiSetButton;
	private int rssiSetValue = 0;
	private boolean calFileSelected = false;
	
	protected RadioInterface radioInterface;
	protected SerialInterface serialInterface;
	
	public RadioComponent() {
		
		device = userPref.getInt("Device", 0);
		setDevice(device);

		deviceComboBoxModel = new DefaultComboBoxModel<Object>();
		modelNumberComboBoxModel = new DefaultComboBoxModel<Object>();
		
		for (int i = 0; i < MANUFACTURER.length; i++) {
			deviceComboBoxModel.addElement(MANUFACTURER[i] + " " + MODEL_NUMBER[i]);
			modelNumberComboBoxModel.addElement(MODEL_NUMBER[i]);
		}
		
		deviceComboBox = new JComboBox<Object>(deviceComboBoxModel);		
		deviceComboBox.setSelectedIndex(device);
		
		modelNumberComboBox = new JComboBox<Object>(modelNumberComboBoxModel);
		modelNumberComboBox.setSelectedIndex(device);
		
		calFileDirPath = userPref.get("CalFileDirectoryPath", System.getProperty("user.dir")) + File.separator + "cal";
		calDir = new File(calFileDirPath);
		calFiles = calDir.list();
		calFileComboBoxModel = new SortedComboBoxModel();
		calFileComboBox = new JComboBox<Object>(calFileComboBoxModel);
		
		String[] elements = deviceComboBox.getSelectedItem().toString().split(" ");
		setSelectedCalFileList(elements[1]);
		
		calibrationFile = userPref.getInt("CalibrationFile", 0);
		if (calFileComboBox.getItemCount() > calibrationFile) calFileComboBox.setSelectedIndex(calibrationFile);

		startRadioWithSystem = userPref.getBoolean("StartRadioWithSystem", false);
		portName = userPref.get("RadioComPort", "COM1");
		filter = userPref.getInt("RadioFilter", 2);
		mode = userPref.getInt("RadioMode", 5);
		volume = userPref.getInt("RadioVolume", 20);
		squelch = userPref.getInt("RadioSquelch", 127);
		toneSquelch = userPref.getInt("RadioToneSquelch", 0);
		digitalSquelch = userPref.getInt("RadioDigitalSquelch", 0);
		frequency = userPref.getDouble("RadioFrequency", 162.4);
		ifShift = userPref.getInt("RadioIfShift", 127);
		agc = userPref.getBoolean("RadioAGC", false);
		afc = userPref.getBoolean("RadioAFC", false);
		attenuator = userPref.getBoolean("RadioAttenuator", false);
		noiseBlanker = userPref.getBoolean("RadioNoiseBlanker", false);
		voiceScan = userPref.getBoolean("RadioVoiceScan", false);
		vfoMode = userPref.getBoolean("vfoMode", true);
		scan[0] = userPref.getDouble("scanF0", 0.0);
		scan[1] = userPref.getDouble("scanF1", 0.0);
		scan[2] = userPref.getDouble("scanF2", 0.0);
		scan[3] = userPref.getDouble("scanF3", 0.0);
		scan[4] = userPref.getDouble("scanF4", 0.0);
		scan[5] = userPref.getDouble("scanF5", 0.0);
		scan[6] = userPref.getDouble("scanF6", 0.0);
		scan[7] = userPref.getDouble("scanF7", 0.0);
		scan[8] = userPref.getDouble("scanF8", 0.0);
		scan[9] = userPref.getDouble("scanF9", 0.0);
		scanSelect[0] = userPref.getBoolean("scanSelectF0", false);
		scanSelect[1] = userPref.getBoolean("scanSelectF1", false);
		scanSelect[2] = userPref.getBoolean("scanSelectF2", false);
		scanSelect[3] = userPref.getBoolean("scanSelectF3", false);
		scanSelect[4] = userPref.getBoolean("scanSelectF4", false);
		scanSelect[5] = userPref.getBoolean("scanSelectF5", false);
		scanSelect[6] = userPref.getBoolean("scanSelectF6", false);
		scanSelect[7] = userPref.getBoolean("scanSelectF7", false);
		scanSelect[8] = userPref.getBoolean("scanSelectF8", false);
		scanSelect[9] = userPref.getBoolean("scanSelectF9", false);
		serialInterface.setPortName(portName);
		radioInterface.setFilter(filter);
		radioInterface.setMode(mode);
		radioInterface.setVolume(volume);
		radioInterface.setSquelch(squelch);
		radioInterface.setToneSquelch(toneSquelch);
		radioInterface.setDigitalSquelch(digitalSquelch);
		radioInterface.setFrequency(frequency);
		radioInterface.setIFShift(ifShift);
		radioInterface.setAGC(agc);
		radioInterface.setAFC(afc);
		radioInterface.setAttenuator(attenuator);
		radioInterface.setNoiseBlanker(noiseBlanker);
		radioInterface.setVoiceScan(voiceScan);
		radioInterface.setVfoMode(vfoMode);
		radioInterface.setScanSelectList(scanSelect);
		radioInterface.setScanList(scan);
		try {
			if (calFileComboBox.getItemCount() == 0) return;
			radioInterface.setCalibrationFile(calFileDirPath + File.separator + (String) calFileComboBox.getItemAt(calibrationFile));
		} catch (final IOException ex) {
			ex.printStackTrace();
			SwingUtilities.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(RadioComponent.this),
	                        ex.getLocalizedMessage(), "Selected Calibration File Not Found", JOptionPane.ERROR_MESSAGE);
	            }
	        });
		}
		
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		
		memoryVfoPanel = new JPanel();
		radioPanel = new JPanel();
		systemPanel = new JPanel();
		scanPanel = new JPanel();
		calibrationPanel = new JPanel();
		tabbedPane = new JTabbedPane();

		rssiCurrentLabel = new JLabel();
		rssiCurrentFieldLabel = new JLabel("0");
		rssiSetButton = new JButton();
		
		setTitle("Receiver Settings");

		rssiCurrentLabel.setText("Measured RSSI");

        rssiCurrentFieldLabel.setBackground(new java.awt.Color(255, 255, 255));
        rssiCurrentFieldLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rssiCurrentFieldLabel.setToolTipText("The current RSSI reported by the radio");
        rssiCurrentFieldLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        rssiCurrentFieldLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        rssiSetButton.setText("Set RSSI");
		
		memoryVfoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Memory / VFO"));
		
		frequencyTextFieldLabel = new JLabel("Frequency");
		frequencyTextFieldLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		deviceComboBoxLabel = new JLabel("Device Type");
		deviceComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		calFileComboBoxLabel = new JLabel("Calibration File");
		calFileComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		
		comPortComboBoxLabel = new JLabel("Comm Port");
		comPortComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		filterComboBoxLabel = new JLabel("IF Bandwidth");
		filterComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		modeComboBoxLabel = new JLabel("Mode");
		modeComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		toneSquelchComboBoxLabel = new JLabel("Tone Squelch");
		toneSquelchComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		volumeSliderLabel = new JLabel("Volume");
		volumeSliderLabel.setHorizontalAlignment(SwingConstants.CENTER);

		squelchSliderLabel = new JLabel("Squelch");
		squelchSliderLabel.setHorizontalAlignment(SwingConstants.CENTER);

		startRadioWithSystemCheckBox = new JCheckBox("Start Radio With System");

		agcCheckBox = new JCheckBox("Automatic Gain Control");

		afcCheckBox = new JCheckBox("Automatic Frequency Control");

		attenuatorCheckBox = new JCheckBox("Attenuator");

		noiseBlankerCheckBox = new JCheckBox("Noise Blanker");

		volumeSlider = new JSlider(0, 255, 0);

		squelchSlider = new JSlider(0, 255, 0);

		frequencyTextField = new JTextField();

		okButton = new JButton("OK");
		cancelButton = new JButton("Cancel");
		applyButton = new JButton("Apply");

		String[] modes = new String[] { "LSB", "USB", "AM", "CW", "CW-R",
				"DSP", "DIG", "Narrow FM", "Wide FM", "P25", "DSTAR", "DMR" };
		String[] filters = new String[] { "300 Hz", "500 Hz", "2.4 kHz",
				"3 kHz", "6 kHz", "15 kHz", "50 kHz", "230 kHz" };
		String[] toneSquelchValues = new String[] { "OFF", "67.0", "69.3",
				"71.0" };

		String[] portNames = SerialPortList.getPortNames();

        comPortComboBox = new JComboBox<String>(portNames);
        comPortComboBox.setModel(new DefaultComboBoxModel<String>());

		modeComboBox = new JComboBox<Object>(modes);
		filterComboBox = new JComboBox<Object>(filters);
		toneSquelchComboBox = new JComboBox<Object>(toneSquelchValues);

		startRadioWithSystemCheckBox.setSelected(startRadioWithSystem);
		noiseBlankerCheckBox.setSelected(noiseBlanker);
		attenuatorCheckBox.setSelected(attenuator);
		agcCheckBox.setSelected(agc);
		afcCheckBox.setSelected(afc);

		frequencyFormat = new DecimalFormat("###0.000000");

		frequencyTextField.setText(frequencyFormat.format(frequency));

		volumeSlider.setValue(volume);
		squelchSlider.setValue(squelch);

		modeComboBox.setSelectedIndex(mode);
		filterComboBox.setSelectedIndex(filter);
		toneSquelchComboBox.setSelectedIndex(toneSquelch);

		for (int i = 0; i < comPortComboBox.getItemCount(); i++) {
			if (String.valueOf(comPortComboBox.getItemAt(i)).equals(String.valueOf(portName))) {
				comPortComboBox.setSelectedIndex(i);
				break;
			}
		}

		tabbedPane.addTab(" System Settings ", null, systemPanel, null);
		tabbedPane.addTab(" Receiver Settings ", null, radioPanel, null);
		tabbedPane.addTab(" Scan Channels ", null, scanPanel, null);
		tabbedPane.addTab(" Calibration ",  null, calibrationPanel, null);

		jTextFieldF10 = new JTextField();
		jLabelF10 = new JLabel();
		jCheckBoxF10 = new JCheckBox();
		jLabelF11 = new JLabel();
		jCheckBoxF11 = new JCheckBox();
		jTextFieldF11 = new JTextField();
		jTextFieldF12 = new JTextField();
		jCheckBoxF12 = new JCheckBox();
		jLabelF12 = new JLabel();
		jTextFieldF13 = new JTextField();
		jLabelF13 = new JLabel();
		jCheckBoxF13 = new JCheckBox();
		jTextFieldF14 = new JTextField();
		jLabelF14 = new JLabel();
		jCheckBoxF14 = new JCheckBox();
		jTextFieldF15 = new JTextField();
		jLabelF15 = new JLabel();
		jCheckBoxF15 = new JCheckBox();
		jLabelF16 = new JLabel();
		jCheckBoxF16 = new JCheckBox();
		jTextFieldF16 = new JTextField();
		jLabelF17 = new JLabel();
		jCheckBoxF17 = new JCheckBox();
		jTextFieldF17 = new JTextField();
		jTextFieldF18 = new JTextField();
		jCheckBoxF18 = new JCheckBox();
		jLabelF18 = new JLabel();
		jLabelF19 = new JLabel();
		jCheckBoxF19 = new JCheckBox();
		jTextFieldF19 = new JTextField();

		chartPanel = new JPanel();
		
		vfoRadioButton = new JRadioButton();
		scanListRadioButton = new JRadioButton();

		jLabelF10.setText("F1");
		jLabelF11.setText("F2");
		jLabelF12.setText("F3");
		jLabelF13.setText("F4");
		jLabelF14.setText("F5");
		jLabelF15.setText("F6");
		jLabelF16.setText("F7");
		jLabelF17.setText("F8");
		jLabelF18.setText("F9");
		jLabelF19.setText("F10");

		jTextFieldF10.setText(frequencyFormat.format(scan[0]));
		jTextFieldF11.setText(frequencyFormat.format(scan[1]));
		jTextFieldF12.setText(frequencyFormat.format(scan[2]));
		jTextFieldF13.setText(frequencyFormat.format(scan[3]));
		jTextFieldF14.setText(frequencyFormat.format(scan[4]));
		jTextFieldF15.setText(frequencyFormat.format(scan[5]));
		jTextFieldF16.setText(frequencyFormat.format(scan[6]));
		jTextFieldF17.setText(frequencyFormat.format(scan[7]));
		jTextFieldF18.setText(frequencyFormat.format(scan[8]));
		jTextFieldF19.setText(frequencyFormat.format(scan[9]));

		jTextFieldF10.setHorizontalAlignment(SwingConstants.RIGHT);
		jTextFieldF11.setHorizontalAlignment(SwingConstants.RIGHT);
		jTextFieldF12.setHorizontalAlignment(SwingConstants.RIGHT);
		jTextFieldF13.setHorizontalAlignment(SwingConstants.RIGHT);
		jTextFieldF14.setHorizontalAlignment(SwingConstants.RIGHT);
		jTextFieldF15.setHorizontalAlignment(SwingConstants.RIGHT);
		jTextFieldF16.setHorizontalAlignment(SwingConstants.RIGHT);
		jTextFieldF17.setHorizontalAlignment(SwingConstants.RIGHT);
		jTextFieldF18.setHorizontalAlignment(SwingConstants.RIGHT);
		jTextFieldF19.setHorizontalAlignment(SwingConstants.RIGHT);

		jCheckBoxF10.setSelected(scanSelect[0]);
		jCheckBoxF11.setSelected(scanSelect[1]);
		jCheckBoxF12.setSelected(scanSelect[2]);
		jCheckBoxF13.setSelected(scanSelect[3]);
		jCheckBoxF14.setSelected(scanSelect[4]);
		jCheckBoxF15.setSelected(scanSelect[5]);
		jCheckBoxF16.setSelected(scanSelect[6]);
		jCheckBoxF17.setSelected(scanSelect[7]);
		jCheckBoxF18.setSelected(scanSelect[8]);
		jCheckBoxF19.setSelected(scanSelect[9]);
		
		vfoRadioButton = new JRadioButton("Use VFO Frequency");
		scanListRadioButton = new JRadioButton("Use Scan List Frequencies");
		freqSelectButtonGroup = new ButtonGroup();
		freqSelectButtonGroup.add(vfoRadioButton);
		freqSelectButtonGroup.add(scanListRadioButton);

		RadioButtonHandler rbh = new RadioButtonHandler();

		vfoRadioButton.addItemListener(rbh);
		scanListRadioButton.addItemListener(rbh);

		if (vfoMode)
			model = vfoRadioButton.getModel();
		else	
			model = scanListRadioButton.getModel();

		freqSelectButtonGroup.setSelected(model, true);
		
		calibrationGraphic = new JPanel();
        rssiComboBoxPanel = new JPanel();
        
        calFileEditorComboBoxModel = new SortedComboBoxModel(calFiles);
        calFileEditorComboBox = new JComboBox<Object>(calFileEditorComboBoxModel);

        rssiTextField = new JTextField();
        
        dBmComboBoxPanel = new JPanel();
        dBmComboBox = new JComboBox<Object>();
        
        calFileEditorComboBoxLabel = new JLabel();
        equalSignLabel = new JLabel();
        fitToCurveButton = new JButton();
        newFileButton = new JButton();
        snTextField = new JTextField();
        manufacturerLabel = new JLabel();
        snTextFieldLabel = new JLabel();
        manufacturerLabelLabel = new JLabel();
        modelNumberTextFieldLabel = new JLabel();
        
        calibrationGraphic.setBorder(BorderFactory.createTitledBorder("RSSI x dBm"));
        
        rssiComboBoxPanel.setBorder(BorderFactory.createTitledBorder("RSSI"));

        dBmComboBoxPanel.setBorder(BorderFactory.createTitledBorder("dBm"));
        
        try {
            calibrate = new Calibrate(calFileDirPath + File.separator + calFileEditorComboBoxModel.getSelectedItem());
        } catch (IOException ex) {
			ex.printStackTrace();
		}
        
        dBmComboBox = new JComboBox<>(calibrate.getdBmArray());
        dBmComboBox.setEditable(false);
        
        calFileEditorComboBoxModel.setSelectedItem(calFileComboBoxModel.getSelectedItem());
        
        updateCalibrationScreen();
        
        calFileEditorComboBoxLabel.setText("Cal File");

        equalSignLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        equalSignLabel.setText("=");

        fitToCurveButton.setText("Fit to Curve");

        newFileButton.setText("New File");

        manufacturerLabel.setBorder(BorderFactory.createEtchedBorder());
        manufacturerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        manufacturerLabel.setToolTipText("Manufacturer of Radio");

        modelNumberComboBox.setToolTipText("Model Number of Radio");

        snTextField.setBorder(BorderFactory.createEtchedBorder());
        snTextField.setHorizontalAlignment(SwingConstants.RIGHT);
        snTextField.setToolTipText("Serial Number of Radio");
        
        snTextFieldLabel.setText("Serial Number");
        
        manufacturerLabelLabel.setText("Manufacturer");

        modelNumberTextFieldLabel.setText("Model Number");
		
        newFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				newFileButtonActionPerformed(event);
			}
		});
        
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				applyButton.doClick();
				setVisible(false);
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setVisible(false);
			}
		});

		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				applyButtonActionListenerEvent(event);
			}
		});

		rssiSetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				rssiSetButtonActionListenerEvent(event);
			}
		});
		
		deviceComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				deviceComboBoxActionPerformed(event);
			}
		});

		modelNumberComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				modelNumberComboBoxActionPerformed(event);
			}
		});
		
		calFileComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				calFileComboBoxActionPerformed(event);
			}
		});
		
		calFileEditorComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				calFileEditorComboBoxActionPerformed(event);
			}
		});

		rssiTextField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				rssiTextFieldActionPerformed(event);
			}
		});
		
		rssiTextField.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				rssiTextFieldKeyReleasedEvent(arg0);
			}

			@Override
			public void keyTyped(KeyEvent arg0) {	
			}
		});
		
		dBmComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				dBmComboBoxActionPerformed(event);
			}
		});
		
		comPortComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				comPortComboBoxActionPerformed(event);
			}
		});

		startRadioWithSystemCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				startRadioWithSystemActionPerformed(event);
			}
		});

		filterComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				filterComboBoxActionPerformed(event);
			}
		});

		agcCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				agcCheckBoxActionPerformed(event);
			}
		});

		afcCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				afcCheckBoxActionPerformed(event);
			}
		});

		attenuatorCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				attenuatorCheckBoxActionPerformed(event);
			}
		});

		noiseBlankerCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				noiseBlankerCheckBoxActionPerformed(event);
			}
		});

		modeComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				modeComboBoxActionPerformed(event);
			}
		});

		toneSquelchComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				toneSquelchComboBoxActionPerformed(event);
			}
		});

		volumeSlider.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(java.awt.event.MouseEvent event) {
				volumeSliderMouseDragged(event);
			}
		});

		squelchSlider.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent event) {
				squelchSliderMouseDragged(event);
			}
		});

		frequencyTextField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				frequencyTextFieldLostFocusEvent(event);
			}
		});

		jTextFieldF10.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				jTextFieldF10LostFocusEvent(event);
			}
		});

		jTextFieldF11.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				jTextFieldF11LostFocusEvent(event);
			}
		});

		jTextFieldF12.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				jTextFieldF12LostFocusEvent(event);
			}
		});

		jTextFieldF13.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				jTextFieldF13LostFocusEvent(event);
			}
		});

		jTextFieldF14.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				jTextFieldF14LostFocusEvent(event);
			}
		});

		jTextFieldF15.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				jTextFieldF15LostFocusEvent(event);
			}
		});

		jTextFieldF16.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				jTextFieldF16LostFocusEvent(event);
			}
		});

		jTextFieldF17.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				jTextFieldF17LostFocusEvent(event);
			}
		});

		jTextFieldF18.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				jTextFieldF18LostFocusEvent(event);
			}
		});

		jTextFieldF19.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				jTextFieldF19LostFocusEvent(event);
			}
		});

		jCheckBoxF10.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				jCheckBoxF10ActionPerformed(event);
			}
		});

		jCheckBoxF11.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				jCheckBoxF11ActionPerformed(event);
			}
		});

		jCheckBoxF12.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				jCheckBoxF12ActionPerformed(event);
			}
		});

		jCheckBoxF13.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				jCheckBoxF13ActionPerformed(event);
			}
		});

		jCheckBoxF14.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				jCheckBoxF14ActionPerformed(event);
			}
		});

		jCheckBoxF15.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				jCheckBoxF15ActionPerformed(event);
			}
		});

		jCheckBoxF16.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				jCheckBoxF16ActionPerformed(event);
			}
		});

		jCheckBoxF17.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				jCheckBoxF17ActionPerformed(event);
			}
		});

		jCheckBoxF18.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				jCheckBoxF18ActionPerformed(event);
			}
		});

		jCheckBoxF19.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				jCheckBoxF19ActionPerformed(event);
			}
		});

		String cancelName = "cancel";
		
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelName);
        
        ActionMap actionMap = getRootPane().getActionMap();
        
        actionMap.put(cancelName, new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
		
        radioInterface.addPropertyChangeListener(new PropertyChangeListener() {
        	@Override
        	public void propertyChange(PropertyChangeEvent event) {
	            if (RadioInterface.BER.equals(event.getPropertyName())) {
	            	firePropertyChange(RadioInterface.BER, null, event.getNewValue());
	            }
	            if (RadioInterface.BUSY.equals(event.getPropertyName())) {
	            	firePropertyChange(RadioInterface.BUSY, null, event.getNewValue());
	            }
	            if (RadioInterface.CLOSE_SERIAL_PORT.equals(event.getPropertyName())) {
	            	try {
						serialInterface.closeSerialPort();
					} catch (SerialPortException e) {
						e.printStackTrace();
					}
	            }
	            if (RadioInterface.RSSI.equals(event.getPropertyName())) {
	            	firePropertyChange(RadioInterface.RSSI, null, event.getNewValue());
	            }
	            if (RadioInterface.POWER.equals(event.getPropertyName())) {
	            	firePropertyChange(RadioInterface.POWER, null, event.getNewValue());
	            }
	            if (RadioInterface.RX_DATA.equals(event.getPropertyName())) {
	            	firePropertyChange(RadioInterface.RX_DATA, null, event.getNewValue());
	            }
	            if (RadioInterface.SEND_TO_SERIAL_PORT.equals(event.getPropertyName())) {
	            	serialInterface.writeString((String) event.getNewValue());
	            }
	            if (RadioInterface.TX_DATA.equals(event.getPropertyName())) {
	            	firePropertyChange(RadioInterface.TX_DATA, null, event.getNewValue());
	            }
        	}
        });
        
        serialInterface.addPropertyChangeListener(new PropertyChangeListener() {
        	@Override
        	public void propertyChange(PropertyChangeEvent event) {
	            if (SerialInterface.ONLINE.equals(event.getPropertyName())) {
	            	firePropertyChange(SerialInterface.ONLINE, null, event.getNewValue());
	            }
	            if (SerialInterface.BREAK.equals(event.getPropertyName())) {
	            	firePropertyChange(SerialInterface.BREAK, null, event.getNewValue());
	            }
	            if (SerialInterface.CTS.equals(event.getPropertyName())) {
	            	firePropertyChange(SerialInterface.CTS, null, event.getNewValue());
	            }
	            if (SerialInterface.DSR.equals(event.getPropertyName())) {
	            	firePropertyChange(SerialInterface.DSR, null, event.getNewValue());
	            }
	            if (SerialInterface.ERROR.equals(event.getPropertyName())) {
	            	firePropertyChange(SerialInterface.ERROR, null, event.getNewValue());
	            }
	            if (SerialInterface.INVALID_COM_PORT.equals(event.getPropertyName())) {
	            	firePropertyChange(SerialInterface.INVALID_COM_PORT, null, event.getNewValue());
	            }
	            if (SerialInterface.RING.equals(event.getPropertyName())) {
	            	firePropertyChange(SerialInterface.RING, null, event.getNewValue());
	            }
	            if (SerialInterface.RLSD.equals(event.getPropertyName())) {
	            	firePropertyChange(SerialInterface.RLSD, null, event.getNewValue());
	            }
	            if (SerialInterface.RX_CHAR.equals(event.getPropertyName())) {
	            	radioInterface.dataInput(serialInterface.readBytes((int) event.getNewValue()));
	            	firePropertyChange(SerialInterface.RX_CHAR, null, event.getNewValue());
	            }
	            if (SerialInterface.RX_FLAG.equals(event.getPropertyName())) {
	            	firePropertyChange(SerialInterface.RX_FLAG, null, event.getNewValue());
	            }
	            if (SerialInterface.TX_EMPTY.equals(event.getPropertyName())) {
	            	firePropertyChange(SerialInterface.TX_EMPTY, null, event.getNewValue());
	            }
        	}
        });
        
        drawGraphicalUserInterface();
	}

	private void newFileButtonActionPerformed(ActionEvent event) {
		calFileSelected = true;
		modelNumberComboBox.setEnabled(true);
		snTextField.setEnabled(true);
		snTextField.setText("");
		manufacturerLabel.setText(MANUFACTURER[modelNumberComboBox.getSelectedIndex()]);
	}

	private void rssiSetButtonActionListenerEvent(ActionEvent event) {
		rssiTextField.setText(String.valueOf(rssiSetValue));
		rssiArray[dBmComboBox.getSelectedIndex()] = String.valueOf(rssiSetValue);
		calibrate.setRSSI(String.valueOf(rssiSetValue), dBmComboBox.getSelectedIndex());
		rssiArray = calibrate.getRssiArray();
	}

	private void rssiTextFieldKeyReleasedEvent(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
			dBmComboBox.requestFocusInWindow();
		}
	}

	private void rssiTextFieldActionPerformed(ActionEvent event) {
		JTextField tf = (JTextField)event.getSource();
		if (event.getID() == 1001) {
			calibrate.setRSSI(tf.getText(), dBmComboBox.getSelectedIndex());
			rssiArray = calibrate.getRssiArray();
			updateCalibrationScreen();
		}
	}

	private void dBmComboBoxActionPerformed(ActionEvent event) {
		JComboBox<?> cb = (JComboBox<?>)event.getSource();
		if (event.getID() == 1001) {
			rssiTextField.setText(rssiArray[cb.getSelectedIndex()]);
		}
	}

	private void calFileEditorComboBoxActionPerformed(ActionEvent event) {
		updateCalibrationScreen();
	}

	private void updateCalibrationScreen() {
		try {
			calibrate = new Calibrate(calFileDirPath + File.separator + calFileEditorComboBoxModel.getSelectedItem());
        } catch (IOException e1) {
			e1.printStackTrace();
		}
		
		rssiArray = calibrate.getRssiArray();
		
		String[] dBmArray = new String[91];
		
		dBmArray = calibrate.getdBmArray();

		rssiTextField.setText(rssiArray[dBmComboBox.getSelectedIndex()]);
		
		manufacturerLabel.setText(calibrate.getManufacturer());
		
		snTextField.setText(calibrate.getSerialNumber());

		for (int i = 0; i < MODEL_NUMBER.length; i++) {
			if (modelNumberComboBoxModel.getElementAt(i).toString().equals(calibrate.getModel())) {
				modelNumberComboBox.setSelectedIndex(i);
				break;
			}
		}
		
		snTextField.setEnabled(false);
		manufacturerLabel.setEnabled(false);
		modelNumberComboBox.setEnabled(false);
		
		if (chart != null) chartPanel.remove(chart);
		
		int[][] iData = new int[91][2];
		
		for (int i = 0; i < iData.length; i++) {
			iData[i][0] = Integer.parseInt(rssiArray[i]);
			iData[i][1] = Integer.parseInt(dBmArray[i]);
		}
		
		chart = new Chart(iData, "dBm", "dBm vs RSSI", "RSSI", "dBm");
		
		chartPanel.add(chart, BorderLayout.CENTER);
		
		revalidate();
		repaint();
	}

	private class RadioButtonHandler implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent ie) {
			if (ie.getSource() == vfoRadioButton)
				vfoMode = true;
			else if (ie.getSource() == scanListRadioButton)
				vfoMode = false;
			radioInterface.setVfoMode(vfoMode);
		}
	}

	private void drawGraphicalUserInterface() {
		javax.swing.GroupLayout calibrationPanelLayout = new javax.swing.GroupLayout(calibrationPanel);
        calibrationPanel.setLayout(calibrationPanelLayout);
        calibrationPanelLayout.setHorizontalGroup(
                calibrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(calibrationPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(calibrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, calibrationPanelLayout.createSequentialGroup()
                            .addComponent(rssiCurrentLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(rssiCurrentFieldLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(103, 103, 103))
                        .addGroup(calibrationPanelLayout.createSequentialGroup()
                            .addGroup(calibrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(calibrationPanelLayout.createSequentialGroup()
                                    .addComponent(calFileEditorComboBoxLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(calFileEditorComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(calibrationPanelLayout.createSequentialGroup()
                                    .addGroup(calibrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, calibrationPanelLayout.createSequentialGroup()
                                            .addGap(1, 1, 1)
                                            .addComponent(fitToCurveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(rssiComboBoxPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(equalSignLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(calibrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(newFileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(dBmComboBoxPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(calibrationPanelLayout.createSequentialGroup()
                                    .addGroup(calibrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(snTextFieldLabel)
                                        .addGap(10,10,10)
                                        .addComponent(manufacturerLabelLabel)
                                        .addGap(10,10,10)
                                        .addComponent(modelNumberTextFieldLabel)
                                        .addGap(10,10,10))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(calibrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    	.addComponent(modelNumberComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                                        .addComponent(manufacturerLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                                        .addComponent(snTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)))
                                .addGroup(calibrationPanelLayout.createSequentialGroup()
                                    .addGap(131, 131, 131)
                                    .addComponent(rssiSetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGap(8, 8, 8)))
                    .addComponent(calibrationGraphic, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()));
        
            calibrationPanelLayout.setVerticalGroup(
                calibrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, calibrationPanelLayout.createSequentialGroup()
                    .addGroup(calibrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, calibrationPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(calibrationGraphic, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(calibrationPanelLayout.createSequentialGroup()
                            .addGap(19, 19, 19)
                            .addGroup(calibrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(calFileEditorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(calFileEditorComboBoxLabel))
                            .addGap(18, 18, 18)
                            .addGroup(calibrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(snTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(snTextFieldLabel))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(calibrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(manufacturerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(manufacturerLabelLabel))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(calibrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(modelNumberComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(modelNumberTextFieldLabel))
                            .addGroup(calibrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(calibrationPanelLayout.createSequentialGroup()
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(rssiComboBoxPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, calibrationPanelLayout.createSequentialGroup()
                                    .addGap(12, 12, 12)
                                    .addGroup(calibrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(dBmComboBoxPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, calibrationPanelLayout.createSequentialGroup()
                                            .addComponent(equalSignLabel)
                                            .addGap(20, 20, 20)))))
                            .addGap(18, 18, 18)
                            .addGroup(calibrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(rssiSetButton)
                                .addGroup(calibrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(rssiCurrentLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(rssiCurrentFieldLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 76, Short.MAX_VALUE)
                            .addGroup(calibrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(fitToCurveButton)
                                .addComponent(newFileButton))))
                    .addContainerGap()));
        
        javax.swing.GroupLayout calibrationGraphicLayout = new javax.swing.GroupLayout(calibrationGraphic);
        calibrationGraphic.setLayout(calibrationGraphicLayout);
        calibrationGraphicLayout.setHorizontalGroup(
            calibrationGraphicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(chartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

        calibrationGraphicLayout.setVerticalGroup(
            calibrationGraphicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(chartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        
        javax.swing.GroupLayout rssiComboBoxPanelLayout = new javax.swing.GroupLayout(rssiComboBoxPanel);
        rssiComboBoxPanel.setLayout(rssiComboBoxPanelLayout);
        rssiComboBoxPanelLayout.setHorizontalGroup(
            rssiComboBoxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(rssiTextField, 0, 78, Short.MAX_VALUE));
        
        rssiComboBoxPanelLayout.setVerticalGroup(
            rssiComboBoxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rssiComboBoxPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rssiTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE)));

        javax.swing.GroupLayout dBmComboBoxPanelLayout = new javax.swing.GroupLayout(dBmComboBoxPanel);
        dBmComboBoxPanel.setLayout(dBmComboBoxPanelLayout);
        dBmComboBoxPanelLayout.setHorizontalGroup(
            dBmComboBoxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(dBmComboBox, 0, 77, Short.MAX_VALUE));
        
        dBmComboBoxPanelLayout.setVerticalGroup(
            dBmComboBoxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dBmComboBoxPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dBmComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
		org.jdesktop.layout.GroupLayout radioPanelLayout = new org.jdesktop.layout.GroupLayout(radioPanel);
        radioPanel.setLayout(radioPanelLayout);
		radioPanelLayout.setAutocreateContainerGaps(true);
        radioPanelLayout.setAutocreateGaps(true);
        radioPanelLayout.setHorizontalGroup(
            radioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(radioPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(radioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(radioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(radioPanelLayout.createSequentialGroup()
                            .add(radioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                .add(frequencyTextFieldLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(radioPanelLayout.createSequentialGroup()
                                    .add(1, 1, 1)
                                    .add(radioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                        .add(modeComboBoxLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(filterComboBoxLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 73, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                .add(toneSquelchComboBoxLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(radioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(frequencyTextField)
                                .add(modeComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(filterComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(toneSquelchComboBox, 0, 102, Short.MAX_VALUE))
                            .add(26, 26, 26)
                            .add(radioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(agcCheckBox)
                                .add(noiseBlankerCheckBox)
                                .add(afcCheckBox)
                                .add(radioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(squelchSliderLabel)
                                    .add(attenuatorCheckBox))))
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, radioPanelLayout.createSequentialGroup()
                            .add(volumeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 155, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(squelchSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 148, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(32, 32, 32)))
                    .add(radioPanelLayout.createSequentialGroup()
                        .add(99, 99, 99)
                        .add(volumeSliderLabel)))
                .add(22, 22, 22)));
        
        radioPanelLayout.setVerticalGroup(
            radioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(radioPanelLayout.createSequentialGroup()
                .add(21, 21, 21)
                .add(radioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(noiseBlankerCheckBox)
                    .add(frequencyTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(frequencyTextFieldLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(radioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(modeComboBoxLabel)
                    .add(modeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(agcCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(radioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(filterComboBoxLabel)
                    .add(afcCheckBox)
                    .add(filterComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(radioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(toneSquelchComboBoxLabel)
                    .add(attenuatorCheckBox)
                    .add(toneSquelchComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(radioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(squelchSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(volumeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(radioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(volumeSliderLabel)
                    .add(squelchSliderLabel))
                .addContainerGap(22, Short.MAX_VALUE)));
		
		javax.swing.GroupLayout memoryVfoPanelLayout = new javax.swing.GroupLayout(memoryVfoPanel);
	    
		memoryVfoPanel.setLayout(memoryVfoPanelLayout);
	    	
		memoryVfoPanelLayout.setHorizontalGroup(
            memoryVfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(memoryVfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(memoryVfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(vfoRadioButton)
                    .addComponent(scanListRadioButton))
                .addContainerGap(58, Short.MAX_VALUE)));
	
        memoryVfoPanelLayout.setVerticalGroup(
            memoryVfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(memoryVfoPanelLayout.createSequentialGroup()
                .addComponent(vfoRadioButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scanListRadioButton)));

        javax.swing.GroupLayout systemPanelLayout = new javax.swing.GroupLayout(systemPanel);
        systemPanel.setLayout(systemPanelLayout);
        systemPanelLayout.setHorizontalGroup(
            systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(systemPanelLayout.createSequentialGroup()
                .addGroup(systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(systemPanelLayout.createSequentialGroup()
                        .addGap(43, 43, 43)
                        .addComponent(memoryVfoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(startRadioWithSystemCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, systemPanelLayout.createSequentialGroup()
                        .addGap(51, 51, 51)
                        .addGroup(systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(deviceComboBoxLabel)
                            .addComponent(calFileComboBoxLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(deviceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(systemPanelLayout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(calFileComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addComponent(comPortComboBoxLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comPortComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(52, Short.MAX_VALUE)));
        
        systemPanelLayout.setVerticalGroup(
            systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(systemPanelLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deviceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deviceComboBoxLabel)
                    .addComponent(comPortComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comPortComboBoxLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(calFileComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(calFileComboBoxLabel))
                .addGroup(systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(systemPanelLayout.createSequentialGroup()
                        .addGap(54, 54, 54)
                        .addComponent(startRadioWithSystemCheckBox))
                    .addGroup(systemPanelLayout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(memoryVfoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(54, Short.MAX_VALUE)));
        
        javax.swing.GroupLayout scanPanelLayout = new javax.swing.GroupLayout(scanPanel);
        scanPanel.setLayout(scanPanelLayout);
        scanPanelLayout.setHorizontalGroup(
            scanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scanPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(scanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(scanPanelLayout.createSequentialGroup()
                        .addComponent(jLabelF12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldF12, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxF12)
                        .addGap(33, 33, 33)
                        .addComponent(jLabelF17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldF17, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxF17))
                    .addGroup(scanPanelLayout.createSequentialGroup()
                        .addGroup(scanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(scanPanelLayout.createSequentialGroup()
                                .addComponent(jLabelF13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldF13, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxF13))
                            .addGroup(scanPanelLayout.createSequentialGroup()
                                .addComponent(jLabelF14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldF14, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxF14)))
                        .addGroup(scanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(scanPanelLayout.createSequentialGroup()
                                .addGap(33, 33, 33)
                                .addComponent(jLabelF18)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldF18, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxF18))
                            .addGroup(scanPanelLayout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addComponent(jLabelF19)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldF19, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxF19))))
                    .addGroup(scanPanelLayout.createSequentialGroup()
                        .addComponent(jLabelF11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldF11, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxF11)
                        .addGap(33, 33, 33)
                        .addComponent(jLabelF16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldF16, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxF16))
                    .addGroup(scanPanelLayout.createSequentialGroup()
                        .addComponent(jLabelF10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldF10, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxF10)
                        .addGap(33, 33, 33)
                        .addComponent(jLabelF15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldF15, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxF15)))
                .addContainerGap(15, Short.MAX_VALUE)));
        
        scanPanelLayout.setVerticalGroup(
            scanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scanPanelLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(scanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabelF15)
                        .addComponent(jCheckBoxF15)
                        .addComponent(jTextFieldF15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabelF10)
                        .addComponent(jCheckBoxF10)
                        .addComponent(jTextFieldF10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(9, 9, 9)
                .addGroup(scanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(scanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabelF16)
                        .addComponent(jCheckBoxF16)
                        .addComponent(jTextFieldF16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(scanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabelF11)
                        .addComponent(jCheckBoxF11)
                        .addComponent(jTextFieldF11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(scanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabelF17)
                        .addComponent(jCheckBoxF17)
                        .addComponent(jTextFieldF17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabelF12)
                        .addComponent(jCheckBoxF12)
                        .addComponent(jTextFieldF12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(9, 9, 9)
                .addGroup(scanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabelF18)
                        .addComponent(jCheckBoxF18)
                        .addComponent(jTextFieldF18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabelF13)
                        .addComponent(jCheckBoxF13)
                        .addComponent(jTextFieldF13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(scanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jCheckBoxF19, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(scanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelF19)
                            .addComponent(jTextFieldF19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabelF14)
                        .addComponent(jCheckBoxF14)
                        .addComponent(jTextFieldF14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(18, Short.MAX_VALUE)));

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setAutocreateGaps(true);
		layout.setAutocreateContainerGaps(true);

        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(applyButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 77, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 82, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, tabbedPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap()));
        
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(tabbedPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(applyButton)
                    .add(okButton))
                .addContainerGap()));

		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenSize = tk.getScreenSize();

		pack();
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setLocation((screenSize.width / 2) - (getWidth() / 2),
				(screenSize.height / 2) - (getHeight() / 2));
	}

	private void setSelectedCalFileList(String selectedModel) {
		if (calFiles == null || calFiles.length == 0) return;
		calFileComboBoxModel.removeAllElements();
		for (int i = 0; i < calFiles.length; i++) {
			try {
				Calibrate modelCheck = new Calibrate(calFileDirPath + File.separator + calFiles[i]);
				if (modelCheck.getModel().equals(selectedModel)) {
					calFileComboBoxModel.addElement(calFiles[i]);
				}
			} catch (final IOException e) {
				e.printStackTrace();
				SwingUtilities.invokeLater(new Runnable() {
	                @Override
	                public void run() {
	                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(null),
	                            e.getLocalizedMessage(), "Requested Calibration File Not Found", JOptionPane.ERROR_MESSAGE);
	                }
	            });
			}
		}
	}

	public boolean isVfoMode() {
		return vfoMode;
	}
	
	public void setCurrentRSSILevel(int rssiCurrent) {
		rssiCurrentFieldLabel.setText(String.valueOf(rssiCurrent));
		rssiSetValue = rssiCurrent;
	}
	
	public void showSettingsDialog(boolean showSettingsDialog) {
		setVisible(showSettingsDialog);
	}

	public boolean isStartRadioWithSystem() {
		return startRadioWithSystem;
	}

	private void setDevice(int device) {
		switch (device) {
			case 0:
				radioInterface = new P7200();
				serialInterface = new ComPort();
				break;
			case 1:
				radioInterface = new XG75();
				serialInterface = new ComPort();
				break;
			case 2:
				radioInterface = new Pcr1k();
				serialInterface = new ComPort();
				break;
			case 3:
				radioInterface = new R2500();
				serialInterface = new ComPort();
				break;
			case 4:
				radioInterface = new Ft100d();
				serialInterface = new ComPort();
				break;
		}
	}
	
	public String getErrorMessage() {
		return serialInterface.getErrorMessage();
	}
	
	private void modelNumberComboBoxActionPerformed(ActionEvent event) {
		JComboBox<?> cb = (JComboBox<?>) event.getSource();
		manufacturerLabel.setText(MANUFACTURER[cb.getSelectedIndex()]);
	}
	
	private void deviceComboBoxActionPerformed(ActionEvent event) {
		JComboBox<?> cb = (JComboBox<?>) event.getSource();
		device = cb.getSelectedIndex();
		setDevice(device);
		String[] elements = cb.getSelectedItem().toString().split(" ");
		setSelectedCalFileList(elements[1]);
		if (calFileComboBoxModel.getSize() > 0) calFileComboBox.setSelectedIndex(0);
	}

	private void calFileComboBoxActionPerformed(ActionEvent event) {
		JComboBox<?> cb = (JComboBox<?>) event.getSource();
		calibrationFile = cb.getSelectedIndex();
		calFileEditorComboBoxModel.setSelectedItem(cb.getSelectedItem());
		updateCalibrationScreen();
	}
	
	private void startRadioWithSystemActionPerformed(ActionEvent event) {
		startRadioWithSystem = startRadioWithSystemCheckBox.isSelected();
	}

	private void comPortComboBoxActionPerformed(ActionEvent event) {
		JComboBox<?> cb = (JComboBox<?>) event.getSource();
		String strPort = (String) cb.getSelectedItem();
		serialInterface.setPortName(strPort);
	}

	private void applyButtonActionListenerEvent(ActionEvent event) {
		userPref.put("RadioComPort", portName);
		userPref.putBoolean("StartRadioWithSystem", startRadioWithSystem);
		userPref.putInt("RadioFilter", filter);
		userPref.putInt("RadioMode", mode);
		userPref.putInt("Device", device);
		userPref.putInt("CalibrationFile", calibrationFile);
		userPref.putInt("RadioVolume", volume);
		userPref.putInt("RadioSquelch", squelch);
		userPref.putInt("RadioToneSquelch", toneSquelch);
		userPref.putInt("RadioDigitalSquelch", digitalSquelch);
		userPref.putDouble("RadioFrequency", frequency);
		userPref.putInt("RadioIFShift", ifShift);
		userPref.putBoolean("RadioAGC", agc);
		userPref.putBoolean("RadioAFC", afc);
		userPref.putBoolean("RadioNoiseBlanker", noiseBlanker);
		userPref.putBoolean("RadioAttenuator", attenuator);
		userPref.putBoolean("RadioVoiceScan", voiceScan);
		userPref.putBoolean("vfoMode", vfoMode);
		userPref.putDouble("scanF0", scan[0]);
		userPref.putDouble("scanF1", scan[1]);
		userPref.putDouble("scanF2", scan[2]);
		userPref.putDouble("scanF3", scan[3]);
		userPref.putDouble("scanF4", scan[4]);
		userPref.putDouble("scanF5", scan[5]);
		userPref.putDouble("scanF6", scan[6]);
		userPref.putDouble("scanF7", scan[7]);
		userPref.putDouble("scanF8", scan[8]);
		userPref.putDouble("scanF9", scan[9]);
		userPref.putBoolean("scanSelectF0", scanSelect[0]);
		userPref.putBoolean("scanSelectF1", scanSelect[1]);
		userPref.putBoolean("scanSelectF2", scanSelect[2]);
		userPref.putBoolean("scanSelectF3", scanSelect[3]);
		userPref.putBoolean("scanSelectF4", scanSelect[4]);
		userPref.putBoolean("scanSelectF5", scanSelect[5]);
		userPref.putBoolean("scanSelectF6", scanSelect[6]);
		userPref.putBoolean("scanSelectF7", scanSelect[7]);
		userPref.putBoolean("scanSelectF8", scanSelect[8]);
		userPref.putBoolean("scanSelectF9", scanSelect[9]);
		serialInterface.setPortName(portName);
		radioInterface.setFilter(filter);
		radioInterface.setMode(mode);
		radioInterface.setVolume(volume);
		radioInterface.setSquelch(squelch);
		radioInterface.setToneSquelch(toneSquelch);
		radioInterface.setDigitalSquelch(digitalSquelch);
		radioInterface.setIFShift(ifShift);
		radioInterface.setAGC(agc);
		radioInterface.setAFC(afc);
		radioInterface.setAttenuator(attenuator);
		radioInterface.setNoiseBlanker(noiseBlanker);
		radioInterface.setVoiceScan(voiceScan);
		radioInterface.setScanList(scan);
		radioInterface.setScanSelectList(scanSelect);
		try {
			if (calFileComboBox.getItemCount() == 0) return;
			radioInterface.setCalibrationFile(calFileDirPath + File.separator + (String) calFileComboBox.getItemAt(calibrationFile));
		} catch (final IOException ex) {
			ex.printStackTrace();
			SwingUtilities.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(RadioComponent.this),
	                        ex.getLocalizedMessage(), "Configured Calibration File Not Found", JOptionPane.ERROR_MESSAGE);
	            }
	        });
		}
		if (vfoMode) {
			radioInterface.setFrequency(frequency);
		}
		if (calFileSelected) {
			File file = new File(System.getProperty("user.dir") + File.separator + "cal" + File.separator + modelNumberComboBox.getSelectedItem().toString() + 
					"_" + snTextField.getText() + ".cal");
	        BufferedWriter output = null;
			try {
				output = new BufferedWriter(new FileWriter(file));
				output.write("MANUFACTURER=" + manufacturerLabel.getText() + System.lineSeparator());
				output.write("MODEL=" + modelNumberComboBox.getSelectedItem().toString() + System.lineSeparator());
				output.write("SN=" + snTextField.getText() + System.lineSeparator());
				for (int i = -30; i >= -120; i--) {
					output.write("0=" + i + System.lineSeparator());
				}
				output.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			calFileSelected = false;
			manufacturerLabel.setEnabled(false);
			snTextField.setEnabled(false);
			modelNumberComboBox.setEnabled(false);
			String calFileEditorString = modelNumberComboBox.getSelectedItem().toString() + "_" + 
					snTextField.getText() + ".cal";
			calFileEditorComboBoxModel.addElement(calFileEditorString);
			calFileEditorComboBox.setSelectedItem(calFileEditorString);
			updateCalibrationScreen();
		}
		
	}

	public String getPortName() {
		return portName;
	}
	
	private void filterComboBoxActionPerformed(ActionEvent event) {
		JComboBox<?> cb = (JComboBox<?>) event.getSource();
		filter = cb.getSelectedIndex();
	}

	private void agcCheckBoxActionPerformed(ActionEvent event) {
		agc = agcCheckBox.isSelected();
	}

	private void afcCheckBoxActionPerformed(ActionEvent event) {
		afc = afcCheckBox.isSelected();
	}

	private void attenuatorCheckBoxActionPerformed(ActionEvent event) {
		attenuator = attenuatorCheckBox.isSelected();
	}

	private void noiseBlankerCheckBoxActionPerformed(ActionEvent event) {
		noiseBlanker = noiseBlankerCheckBox.isSelected();
	}

	private void frequencyTextFieldLostFocusEvent(FocusEvent event) {
		frequency = Double.parseDouble(frequencyTextField.getText());
		frequencyTextField.setText(frequencyFormat.format(frequency));
	}

	private void modeComboBoxActionPerformed(ActionEvent event) {
		JComboBox<?> cb = (JComboBox<?>) event.getSource();
		mode = cb.getSelectedIndex();
	}

	private void toneSquelchComboBoxActionPerformed(ActionEvent event) {
		JComboBox<?> cb = (JComboBox<?>) event.getSource();
		toneSquelch = cb.getSelectedIndex();
	}

	private void volumeSliderMouseDragged(MouseEvent event) {
		volume = volumeSlider.getValue();
		radioInterface.setVolume(volume);
	}

	private void squelchSliderMouseDragged(MouseEvent event) {
		squelch = squelchSlider.getValue();
		radioInterface.setSquelch(squelch);
	}

	private void jTextFieldF10LostFocusEvent(FocusEvent event) {
		scan[0] = Double.parseDouble(jTextFieldF10.getText());
		jTextFieldF10.setText(frequencyFormat.format(scan[0]));
	}

	private void jTextFieldF11LostFocusEvent(FocusEvent event) {
		scan[1] = Double.parseDouble(jTextFieldF11.getText());
		jTextFieldF11.setText(frequencyFormat.format(scan[1]));
	}

	private void jTextFieldF12LostFocusEvent(FocusEvent event) {
		scan[2] = Double.parseDouble(jTextFieldF12.getText());
		jTextFieldF12.setText(frequencyFormat.format(scan[2]));
	}

	private void jTextFieldF13LostFocusEvent(FocusEvent event) {
		scan[3] = Double.parseDouble(jTextFieldF13.getText());
		jTextFieldF13.setText(frequencyFormat.format(scan[3]));
	}

	private void jTextFieldF14LostFocusEvent(FocusEvent event) {
		scan[4] = Double.parseDouble(jTextFieldF14.getText());
		jTextFieldF14.setText(frequencyFormat.format(scan[4]));
	}

	private void jTextFieldF15LostFocusEvent(FocusEvent event) {
		scan[5] = Double.parseDouble(jTextFieldF15.getText());
		jTextFieldF15.setText(frequencyFormat.format(scan[5]));
	}

	private void jTextFieldF16LostFocusEvent(FocusEvent event) {
		scan[6] = Double.parseDouble(jTextFieldF16.getText());
		jTextFieldF16.setText(frequencyFormat.format(scan[6]));
	}

	private void jTextFieldF17LostFocusEvent(FocusEvent event) {
		scan[7] = Double.parseDouble(jTextFieldF17.getText());
		jTextFieldF17.setText(frequencyFormat.format(scan[7]));
	}

	private void jTextFieldF18LostFocusEvent(FocusEvent event) {
		scan[8] = Double.parseDouble(jTextFieldF18.getText());
		jTextFieldF18.setText(frequencyFormat.format(scan[8]));
	}

	private void jTextFieldF19LostFocusEvent(FocusEvent event) {
		scan[9] = Double.parseDouble(jTextFieldF19.getText());
		jTextFieldF19.setText(frequencyFormat.format(scan[9]));
	}

	private void jCheckBoxF10ActionPerformed(ActionEvent event) {
		scanSelect[0] = jCheckBoxF10.isSelected();
	}

	private void jCheckBoxF11ActionPerformed(ActionEvent event) {
		scanSelect[1] = jCheckBoxF11.isSelected();
	}

	private void jCheckBoxF12ActionPerformed(ActionEvent event) {
		scanSelect[2] = jCheckBoxF12.isSelected();
	}

	private void jCheckBoxF13ActionPerformed(ActionEvent event) {
		scanSelect[3] = jCheckBoxF13.isSelected();
	}

	private void jCheckBoxF14ActionPerformed(ActionEvent event) {
		scanSelect[4] = jCheckBoxF14.isSelected();
	}

	private void jCheckBoxF15ActionPerformed(ActionEvent event) {
		scanSelect[5] = jCheckBoxF15.isSelected();
	}

	private void jCheckBoxF16ActionPerformed(ActionEvent event) {
		scanSelect[6] = jCheckBoxF16.isSelected();
	}

	private void jCheckBoxF17ActionPerformed(ActionEvent event) {
		scanSelect[7] = jCheckBoxF17.isSelected();
	}

	private void jCheckBoxF18ActionPerformed(ActionEvent event) {
		scanSelect[8] = jCheckBoxF18.isSelected();
	}

	private void jCheckBoxF19ActionPerformed(ActionEvent event) {
		scanSelect[9] = jCheckBoxF19.isSelected();
	}

}