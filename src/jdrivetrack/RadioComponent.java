package jdrivetrack;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.NumberFormatter;

import interfaces.RadioInterface;
import interfaces.SerialInterface;
import jssc.SerialPortException;
import radios.FT100D;
import radios.P7200;
import radios.PCR1000;
import radios.PCR2500;
import radios.XG75;

public class RadioComponent extends JDialog {
	private static final long serialVersionUID = -7750081756785824878L;
	
	public static final String SERIAL_PORT_ERROR = "SERIAL_PORT_ERROR";
	
	public static final String DEFAULT_CAL_FILE_DIRECTORY = 
			System.getProperty("user.home") + File.separator + "drivetrack" + File.separator + "cal";
	
	private static final int MFR = 0;
	private static final int MOD = 1;
	
	private int device;
	private int calFileIndex;
	private boolean afc;
	private boolean agc;
	private boolean attenuator;
	private boolean noiseBlanker;
	private int volume;
	private int squelch;
	private double frequency;
	private double toneSquelch;
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
	private JPanel calPanel;
	private JTabbedPane tabbedPane;
	private JButton okButton;
	private JButton cancelButton;
	private JButton applyButton;
	
	private SortedComboBoxModel calFileComboBoxModel;
	
	private JComboBox<String> deviceComboBox;
	private JComboBox<Object> calFileComboBox;
	private JComboBox<String> filterComboBox;
	private JComboBox<String> modeComboBox;
	private JComboBox<String> toneSquelchComboBox;
	private JComboBox<String> digitalSquelchComboBox;

	private JLabel deviceComboBoxLabel;
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
	private JFormattedTextField frequencyTextField;
	private JSlider volumeSlider;
	private JSlider squelchSlider;
	private JCheckBox[] scanCheckBox = new JCheckBox[10];
	private JLabel[] scanLabel = new JLabel[10];
	private JFormattedTextField[] scanTextField = new JFormattedTextField[10];
	private Double[] scan = new Double[10];
	private Boolean[] scanSelect = new Boolean[10];
	private JRadioButton vfoRadioButton;
	private JRadioButton scanListRadioButton;
	private ButtonGroup freqSelectButtonGroup;
	private boolean vfoMode;
	private ButtonModel model;
	private String calFileDir;
	private String[] calFiles;
	private int i;
	private RadioButtonHandler rbh;
	private RadioInterface radioInterface;
	private SerialConfig serialConfig;
	private Calibrate calibrate;
	private boolean isOnLine = false;
	private DecimalFormat decimalFormat; 
	private NumberFormat numberFormat;
	private NumberFormatter numberFormatter;
	
	private Preferences systemPrefs = Preferences.systemRoot().node("jdrivetrack/prefs/RadioComponent");

	public RadioComponent(int device, boolean clearAllPrefs) {
		if (clearAllPrefs) {
			try {
				systemPrefs.clear();
			} catch (BackingStoreException ex) {
				ex.printStackTrace();
			}
		}
		
		if (device == -1) device = systemPrefs.getInt("device", 2);
		
		this.device = device;
	
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setTitle("Receiver Settings");
		getGeneralPreferences();
		initializeDevice(device);
        getRadioSpecificPreferences(getDeviceID());
        serialConfig = new SerialConfig(getDeviceID(), "Radio Receiver", getDefaultSerialParameterSet());
        calibrate = new Calibrate(DEFAULT_CAL_FILE_DIRECTORY, getAllDeviceStrings());
        calPanel = calibrate.getCalibrationPanelGui();
        initializeGuiComponents();
        setSwingComponents();

        getCalFile(calFileIndex);
        
        sendParametersToRadio();
        drawGraphicalUserInterface();
        configureListeners();
        addRadioInterfaceListener();
	}
	
	public void startRadio() throws SerialPortException {
        serialConfig.getSerialInterface().setOnline(serialConfig.getPortName());
		if (!radioInterface.isCTSSupported() || serialConfig.getSerialInterface().isCTS()) {
			radioInterface.startRadio();
			isOnLine = true;
		} else {
			isOnLine = false;
			String deviceName = RadioInterface.DEVICES[device];
			String[] err = { deviceName + " failed to go online", "Device Not Ready" };
			firePropertyChange(SERIAL_PORT_ERROR, null, err);
		}
	}
	
	public boolean isRadioOnLine() {
		return isOnLine;
	}
	
	private String getDeviceID() {
		return radioInterface.versionUID();
	}

	private boolean setupListenersWhenConnected() {
		Window parentFrame = (Window) getParent();
		parentFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				if (WindowEvent.WINDOW_CLOSING == event.getID()) {
					shutDown();
				}
			}
		});
		return true;
	}
	
	private void shutDown() {
		saveGeneralPreferences();
		saveRadioSpecificPreferences(getDeviceID());
		serialConfig.saveSettings(getDeviceID());
		isOnLine = false;
	}
	
	private void configureListeners() {
		addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent event) {
				if (event.getChangeFlags() == HierarchyEvent.DISPLAYABILITY_CHANGED) {
					boolean connected = setupListenersWhenConnected();
					if (connected) {
						removeHierarchyListener(this);
					}
				}
			}
		});
		
    	frequencyTextField.addFocusListener(new FocusListener() {
    		@Override
    		public void focusGained(FocusEvent e) {
    			frequencyTextField.setFont(new Font("Calabri", Font.BOLD, 11));
    		}
    		@Override
    		public void focusLost(FocusEvent e) {
    			
    		}	
    	});

    	frequencyTextField.addKeyListener(new KeyListener() {
    		@Override
    		public void keyTyped(KeyEvent event) {
    			
    		}
    		@Override
    		public void keyPressed(KeyEvent event) {
    			if (event.getKeyCode() == KeyEvent.VK_ENTER) {
    				frequencyTextField.setFont(new Font("Calabri", Font.PLAIN, 11));
    		        frequency = Double.parseDouble(frequencyTextField.getText());
    		        radioInterface.setFrequency(frequency);
    		        frequencyTextField.transferFocus();
    			}
    		}
    		@Override
    		public void keyReleased(KeyEvent event) {

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

		deviceComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					deviceComboBoxActionPerformed(event);
				}
			}
		});
		
		calFileComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					calFileComboBoxActionPerformed(event);
				}
			}
		});
		
		filterComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					filterComboBoxActionPerformed(event);
				}
			}
		});
		
		modeComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					modeComboBoxActionPerformed(event);
				}
			}
		});
		
		toneSquelchComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					toneSquelchComboBoxActionPerformed(event);
				}
			}
		});
		
		digitalSquelchComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					digitalSquelchComboBoxActionPerformed(event);
				}
			}
		});
		
		startRadioWithSystemCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				startRadioWithSystemActionPerformed(event);
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

		for (i = 0; i < scan.length; i++) {
			scanTextField[Integer.valueOf(i)].addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
					scanTextField[Integer.valueOf(i)].setFont(new Font("Calabri", Font.BOLD, 11));
				}
				@Override
				public void focusLost(FocusEvent e) {
					
				}	
			});
			scanTextField[Integer.valueOf(i)].addKeyListener(new KeyListener() {
				@Override
				public void keyTyped(KeyEvent event) {
					
				}
				@Override
				public void keyPressed(KeyEvent event) {
					if (event.getKeyCode() == KeyEvent.VK_ENTER) {
						scanTextField[Integer.valueOf(i)].setFont(new Font("Calabri", Font.PLAIN, 11));
						scan[Integer.valueOf(i)] = Double.parseDouble(scanTextField[Integer.valueOf(i)].getText());
				        scanTextField[Integer.valueOf(i)].transferFocus();
					}
				}
				@Override
				public void keyReleased(KeyEvent event) {

				}
			});
			scanCheckBox[Integer.valueOf(i)].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					scanCheckBoxActionPerformed(event, Integer.valueOf(i));
				}
			});
		}

		String cancelName = "cancel";
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelName);
        ActionMap actionMap = getRootPane().getActionMap();
        
        actionMap.put(cancelName, new AbstractAction() {
			private static final long serialVersionUID = -674482171725340242L;
			@Override
			public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        }); 
        
        addSerialInterfaceListener();
	}
	
	private void addSerialInterfaceListener() {
		getSerialConfig().getSerialInterface().addPropertyChangeListener(serialInterfaceListener);
	}
	
	private void removeSerialInterfaceListener() {
		getSerialConfig().getSerialInterface().removePropertyChangeListener(serialInterfaceListener);
	}
	
	private PropertyChangeListener serialInterfaceListener = new PropertyChangeListener() {
    	@Override
    	public void propertyChange(PropertyChangeEvent event) {
            if (SerialInterface.RX_CHAR.equals(event.getPropertyName())) {
            	String dataFromRadio = serialConfig.getSerialInterface().readString((int) event.getNewValue());          	
            	if (dataFromRadio.length() > 0) radioInterface.processData(dataFromRadio);
            	firePropertyChange(SerialInterface.RX_CHAR, null, dataFromRadio);
            }
    	}
    };
	
	private void removeRadioInterfaceListener() {
		getRadioInterface().removePropertyChangeListener(radioInterfaceListener);
	}
	
	private void addRadioInterfaceListener() {
		getRadioInterface().addPropertyChangeListener(radioInterfaceListener);
	}
	
	private PropertyChangeListener radioInterfaceListener = new PropertyChangeListener() {
    	@Override
    	public void propertyChange(PropertyChangeEvent event) {
            if (RadioInterface.SEND_TO_SERIAL_PORT.equals(event.getPropertyName())) {
            	getSerialConfig().getSerialInterface().writeString((String) event.getNewValue());
            }
            if (RadioInterface.SET_BAUD_RATE.equals(event.getPropertyName())) {
            	getSerialConfig().getSerialInterface().setBaudRate((int) event.getNewValue());
            }
            if (RadioInterface.CLOSE_SERIAL_PORT.equals(event.getPropertyName())) {
            	try {
					getSerialConfig().getSerialInterface().closeSerialPort();
				} catch (SerialPortException ex) {
					ex.printStackTrace();
				}
            }
            if (RadioInterface.CANCEL_EVENTS.equals(event.getPropertyName())) {
            	getSerialConfig().getSerialInterface().cancelEvents();
            }
    	}
    };
	
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
		GroupLayout radioPanelLayout = new GroupLayout(radioPanel);
        radioPanel.setLayout(radioPanelLayout);

        radioPanelLayout.setHorizontalGroup(radioPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(radioPanelLayout.createSequentialGroup()
                .addContainerGap(20,20)
                .addGroup(radioPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(radioPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(radioPanelLayout.createSequentialGroup()
                            .addGroup(radioPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addComponent(frequencyTextFieldLabel)
                                .addComponent(modeComboBoxLabel)
                                .addComponent(filterComboBoxLabel)
                                .addComponent(toneSquelchComboBoxLabel))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(radioPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                .addComponent(frequencyTextField)
                                .addComponent(modeComboBox)
                                .addComponent(filterComboBox)
                                .addComponent(toneSquelchComboBox))
                            .addGap(15,15,15)
                            .addGroup(radioPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(agcCheckBox)
                                .addComponent(noiseBlankerCheckBox)
                                .addComponent(afcCheckBox)
                                .addComponent(attenuatorCheckBox)))
                                .addGap(15,15,15))))
                    .addGroup(radioPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(radioPanelLayout.createSequentialGroup()   
                            .addContainerGap(20,20) 
                        	.addGroup(radioPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addComponent(volumeSlider, 155,155,155)
                            	.addComponent(volumeSliderLabel, 155,155,155))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)	
		                    .addGroup(radioPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
		                        .addComponent(squelchSlider, 155,155,155)
		                        .addComponent(squelchSliderLabel, 155,155,155)))));
        
        radioPanelLayout.setVerticalGroup(radioPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(radioPanelLayout.createSequentialGroup()
        		.addContainerGap(20,20)
                .addGroup(radioPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(noiseBlankerCheckBox)
                    .addComponent(frequencyTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(frequencyTextFieldLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(radioPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(modeComboBoxLabel)
                    .addComponent(modeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(agcCheckBox))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(radioPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(filterComboBoxLabel)
                    .addComponent(afcCheckBox)
                    .addComponent(filterComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(radioPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(toneSquelchComboBoxLabel)
                    .addComponent(attenuatorCheckBox)
                    .addComponent(toneSquelchComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(radioPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(squelchSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(volumeSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(radioPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(volumeSliderLabel)
                    .addComponent(squelchSliderLabel))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		
		GroupLayout memoryVfoPanelLayout = new GroupLayout(memoryVfoPanel);
		memoryVfoPanel.setLayout(memoryVfoPanelLayout);
	    	
		memoryVfoPanelLayout.setHorizontalGroup(memoryVfoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(memoryVfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(memoryVfoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(vfoRadioButton)
                    .addComponent(scanListRadioButton))
                .addContainerGap()));
	
        memoryVfoPanelLayout.setVerticalGroup(memoryVfoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(memoryVfoPanelLayout.createSequentialGroup()
        		.addContainerGap()	
                .addComponent(vfoRadioButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scanListRadioButton)
                .addContainerGap()));

        GroupLayout systemPanelLayout = new GroupLayout(systemPanel);
        systemPanel.setLayout(systemPanelLayout);
        
        systemPanelLayout.setHorizontalGroup(systemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(systemPanelLayout.createSequentialGroup()
            	.addContainerGap()	
            	.addGroup(systemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)	
            		.addGroup(systemPanelLayout.createSequentialGroup()
	                    .addComponent(memoryVfoPanel)
	                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
	                    .addComponent(startRadioWithSystemCheckBox))
	                .addGroup(systemPanelLayout.createSequentialGroup()        
	                	.addGroup(systemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)  
                	.addComponent(deviceComboBoxLabel)
                    .addComponent(calFileComboBoxLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)   
                .addGroup(systemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(deviceComboBox, 160,160,160)
                    .addComponent(calFileComboBox, 400,400,400))))
        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        systemPanelLayout.setVerticalGroup(systemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(systemPanelLayout.createSequentialGroup()
        		.addContainerGap()
                .addGroup(systemPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(deviceComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(deviceComboBoxLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(systemPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(calFileComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(calFileComboBoxLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)    
                .addGroup(systemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(startRadioWithSystemCheckBox)
                    .addComponent(memoryVfoPanel))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        GroupLayout scanPanelLayout = new GroupLayout(scanPanel);
        scanPanel.setLayout(scanPanelLayout);
        
        scanPanelLayout.setHorizontalGroup(scanPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(scanPanelLayout.createSequentialGroup()
        		.addContainerGap()
                .addGroup(scanPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(scanPanelLayout.createSequentialGroup()
                    	.addComponent(scanCheckBox[4])
                		.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    	.addComponent(scanLabel[4])
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scanTextField[4], 100,100,100))
                    .addGroup(scanPanelLayout.createSequentialGroup()
                		.addComponent(scanCheckBox[3])
                		.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scanLabel[3])
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scanTextField[3], 100,100,100))
                    .addGroup(scanPanelLayout.createSequentialGroup()
                		.addComponent(scanCheckBox[2])
                		.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scanLabel[2])
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scanTextField[2], 100,100,100))
                    .addGroup(scanPanelLayout.createSequentialGroup()
                		.addComponent(scanCheckBox[1])
                		.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scanLabel[1])
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scanTextField[1], 100,100,100))
                    .addGroup(scanPanelLayout.createSequentialGroup()
                		.addComponent(scanCheckBox[0])
                		.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scanLabel[0])
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scanTextField[0], 100,100,100)))
	            .addGap(40,40,40)         
	            .addGroup(scanPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	                .addGroup(scanPanelLayout.createSequentialGroup()
                		.addComponent(scanCheckBox[9])
                		.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                    .addComponent(scanLabel[9])
	                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                    .addComponent(scanTextField[9], 100,100,100))
	                .addGroup(scanPanelLayout.createSequentialGroup()
                		.addComponent(scanCheckBox[8])
                		.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                    .addComponent(scanLabel[8])
	                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                    .addComponent(scanTextField[8], 100,100,100))
	                .addGroup(scanPanelLayout.createSequentialGroup()
                		.addComponent(scanCheckBox[7])
                		.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                    .addComponent(scanLabel[7])
	                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                    .addComponent(scanTextField[7], 100,100,100))
	                .addGroup(scanPanelLayout.createSequentialGroup()
                		.addComponent(scanCheckBox[6])
                		.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                    .addComponent(scanLabel[6])
	                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                    .addComponent(scanTextField[6], 100,100,100))
	                .addGroup(scanPanelLayout.createSequentialGroup()
                		.addComponent(scanCheckBox[5])
                		.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                    .addComponent(scanLabel[5])
	                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                    .addComponent(scanTextField[5], 100,100,100)))
	            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        
        scanPanelLayout.setVerticalGroup(scanPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(scanPanelLayout.createSequentialGroup()
        		.addContainerGap()
                .addGroup(scanPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(GroupLayout.Alignment.TRAILING, scanPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(scanLabel[5])
                        .addComponent(scanCheckBox[5])
                        .addComponent(scanTextField[5], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(GroupLayout.Alignment.TRAILING, scanPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(scanLabel[0])
                        .addComponent(scanCheckBox[0])
                        .addComponent(scanTextField[0], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(scanPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(scanPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(scanLabel[6])
                        .addComponent(scanCheckBox[6])
                        .addComponent(scanTextField[6], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(scanPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(scanLabel[1])
                        .addComponent(scanCheckBox[1])
                        .addComponent(scanTextField[1], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(scanPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(GroupLayout.Alignment.TRAILING, scanPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(scanLabel[7])
                        .addComponent(scanCheckBox[7])
                        .addComponent(scanTextField[7], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(GroupLayout.Alignment.TRAILING, scanPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(scanLabel[2])
                        .addComponent(scanCheckBox[2])
                        .addComponent(scanTextField[2], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(scanPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(GroupLayout.Alignment.TRAILING, scanPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(scanLabel[8])
                        .addComponent(scanCheckBox[8])
                        .addComponent(scanTextField[8], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(GroupLayout.Alignment.TRAILING, scanPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(scanLabel[3])
                        .addComponent(scanCheckBox[3])
                        .addComponent(scanTextField[3], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(scanPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(GroupLayout.Alignment.TRAILING, scanPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(scanCheckBox[9], GroupLayout.Alignment.TRAILING)
                        .addGroup(scanPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(scanLabel[9])
                            .addComponent(scanTextField[9], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                    .addGroup(GroupLayout.Alignment.TRAILING, scanPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(scanLabel[4])
                        .addComponent(scanCheckBox[4])
                        .addComponent(scanTextField[4], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(okButton, 90,90,90)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(applyButton, 90,90,90)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton, 90,90,90))
                    .addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(applyButton)
                    .addComponent(okButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
		pack();
		
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenSize = tk.getScreenSize();
		setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));
	}
	
	private String[] getModString(String fileName) throws IOException {
        RandomAccessFile raf = null;
        String inputArray[][] = new String[94][2];
        String model = null;
        String manufacturer = null;
        try {
            int i = 0;
            raf = new RandomAccessFile(fileName, "r");
            String inputString;
            while (raf.getFilePointer() < raf.length()) {
                inputString = raf.readLine();
                inputArray[i] = inputString.split("=");
                i++;
            }
            manufacturer = inputArray[MFR][1];
            model = inputArray[MOD][1];
        } catch (ArrayIndexOutOfBoundsException ex) {
        	deviceNotProvisionedMessage("There are no radio models provisioned for this manufacturer.");
        } finally {
        	raf.close();
        }
        return new String[] {manufacturer, model};
	}

	private void populateCalFileComboBox(String[] deviceRowSelection, String[] calFiles) {
		if (calFiles == null || calFiles.length == 0) return;
		calFileComboBoxModel.removeAllElements();
		calFileComboBox.validate();
		for (int i = 0; i < calFiles.length; i++) {
			try {
				String[] modelCheck = getModString(calFileDir + File.separator + calFiles[i]);
				if (deviceRowSelection[MOD].contains(modelCheck[MOD])) {
					calFileComboBoxModel.addElement(calFiles[i]);
				} 
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
		if (calFileComboBox.getItemCount() == 0) 
			calFileNotFoundMessage("There are no calibration files available for: " +
					deviceRowSelection[MFR] + " " + deviceRowSelection[MOD]);
	}
	
	private void calFileNotFoundMessage(String message) {
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(RadioComponent.this),
                        message, "Calibration File Not Found", JOptionPane.ERROR_MESSAGE);
            }
        });
	}

	private void deviceNotProvisionedMessage(String message) {
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(RadioComponent.this),
                        message, "No Device Has Been Provisioned", JOptionPane.ERROR_MESSAGE);
            }
        });
	}
	
	public boolean isVfoMode() {
		return vfoMode;
	}
	
	public void showSettingsDialog(boolean showSettingsDialog) {
		setVisible(showSettingsDialog);
	}

	public boolean isStartRadioWithSystem() {
		return startRadioWithSystem;
	}

	private void initializeDevice(int device) {
		switch (device) {
			case 0:
				radioInterface = new P7200();
				break;
			case 1:
				radioInterface = new XG75();
				break;
			case 2:
				radioInterface = new PCR1000();
				break;
			case 3:
				radioInterface = new PCR2500();
				break;
			case 4:
				radioInterface = new FT100D();
				break;
		}
	}
	
	public String getErrorMessage() {
		return getSerialConfig().getSerialInterface().getErrorMessage();
	}

	private void deviceComboBoxActionPerformed(ItemEvent event) {
		JComboBox<?> cb = (JComboBox<?>) event.getSource();
		device = cb.getSelectedIndex();
		setDevice(device);
	}

	private void calFileComboBoxActionPerformed(ItemEvent event) {
		JComboBox<?> cb = (JComboBox<?>) event.getSource();
		calFileIndex = cb.getSelectedIndex();
	}
	
	private void startRadioWithSystemActionPerformed(ActionEvent event) {
		startRadioWithSystem = startRadioWithSystemCheckBox.isSelected();
	}
	
	private void applyButtonActionListenerEvent(ActionEvent event) {
		saveGeneralPreferences();
		saveRadioSpecificPreferences(getDeviceID());
		serialConfig.saveSettings(getDeviceID());
		serialConfig.sendSerialPortSettingsFromMemoryToDevice();
		calibrate.getCalibrationDataObject().saveFile();
	}
	
	private void filterComboBoxActionPerformed(ItemEvent event) {
		JComboBox<?> cb = (JComboBox<?>) event.getSource();
		filter = cb.getSelectedIndex();
		radioInterface.setFilter(filter);
	}

	private void agcCheckBoxActionPerformed(ActionEvent event) {
		agc = agcCheckBox.isSelected();
		radioInterface.setAGC(agc);
	}

	private void afcCheckBoxActionPerformed(ActionEvent event) {
		afc = afcCheckBox.isSelected();
		radioInterface.setAFC(afc);
	}

	private void attenuatorCheckBoxActionPerformed(ActionEvent event) {
		attenuator = attenuatorCheckBox.isSelected();
		radioInterface.setAttenuator(attenuator);
	}

	private void noiseBlankerCheckBoxActionPerformed(ActionEvent event) {
		noiseBlanker = noiseBlankerCheckBox.isSelected();
		radioInterface.setNoiseBlanker(noiseBlanker);
	}

	private void modeComboBoxActionPerformed(ItemEvent event) {
		JComboBox<?> cb = (JComboBox<?>) event.getSource();
		mode = cb.getSelectedIndex();
		radioInterface.setMode(mode);
	}

	private void toneSquelchComboBoxActionPerformed(ItemEvent event) {
		JComboBox<?> cb = (JComboBox<?>) event.getSource();
		if (cb.getSelectedItem().toString().equals("OFF")) {
			toneSquelch = 0;
		} else {
			toneSquelch = Double.parseDouble((String)cb.getSelectedItem());
		}
		radioInterface.setToneSquelch(toneSquelch);
	}

	private void digitalSquelchComboBoxActionPerformed(ItemEvent event) {
		JComboBox<?> cb = (JComboBox<?>) event.getSource();
		digitalSquelch = cb.getSelectedIndex();
		radioInterface.setDigitalSquelch(digitalSquelch);
	}
	
	private void volumeSliderMouseDragged(MouseEvent event) {
		volume = volumeSlider.getValue();
		radioInterface.setVolume(volume);
	}

	private void squelchSliderMouseDragged(MouseEvent event) {
		squelch = squelchSlider.getValue();
		radioInterface.setSquelch(squelch);
	}

	private void scanCheckBoxActionPerformed(ActionEvent event, int i) {
		JCheckBox cb = (JCheckBox) event.getSource();
		scanSelect[i] = cb.isSelected();
	}

	public SerialConfig getSerialConfig() {
		return serialConfig;
	}

	public RadioInterface getRadioInterface() {
		return radioInterface;
	}
	
	public Calibrate getCalibrationInterface() {
		return calibrate;
	}
	
	private void saveGeneralPreferences() {
		systemPrefs.putInt("device", device);
		systemPrefs.put("calFileDir", calFileDir);
		systemPrefs.putBoolean("startRadioWithSystem", startRadioWithSystem);
		for (int i = 0; i < scan.length; i++) {
			systemPrefs.putDouble("scanF" + i, scan[i]);
			systemPrefs.putBoolean("scanSelectF" + i, scanSelect[i]);
		}
	}
	
	private void saveRadioSpecificPreferences(String deviceID) {
		systemPrefs.putInt(deviceID + "filter", filter);
		systemPrefs.putInt(deviceID + "mode", mode);
		systemPrefs.putInt(deviceID + "calFileIndex", calFileIndex);
		systemPrefs.putInt(deviceID + "volume", volume);
		systemPrefs.putInt(deviceID + "squelch", squelch);
		systemPrefs.putDouble(deviceID + "toneSquelch", toneSquelch);
		systemPrefs.putInt(deviceID + "digitalSquelch", digitalSquelch);
		systemPrefs.putDouble(deviceID + "frequency", frequency);
		systemPrefs.putInt(deviceID + "ifShift", ifShift);
		systemPrefs.putBoolean(deviceID + "agc", agc);
		systemPrefs.putBoolean(deviceID + "afc", afc);
		systemPrefs.putBoolean(deviceID + "noiseBlanker", noiseBlanker);
		systemPrefs.putBoolean(deviceID + "attenuator", attenuator);
		systemPrefs.putBoolean(deviceID + "voiceScan", voiceScan);
		systemPrefs.putBoolean(deviceID + "vfoMode", vfoMode);
	}
	
	private void getGeneralPreferences() {
		calFileDir = systemPrefs.get("calFileDir", DEFAULT_CAL_FILE_DIRECTORY);
		startRadioWithSystem = systemPrefs.getBoolean("startRadioWithSystem", false);
		for (int i = 0; i < scan.length; i++) {
			scan[i] = systemPrefs.getDouble("scanF" + i, 0.0);
			scanSelect[i] = systemPrefs.getBoolean("scanSelectF" + i, false);
		}
	}

	private void getRadioSpecificPreferences(String deviceID) {
		calFileIndex = systemPrefs.getInt(deviceID + "calFileIndex", 0);
		filter = systemPrefs.getInt(deviceID + "filter", 0);
		mode = systemPrefs.getInt(deviceID + "mode", 0);
		volume = systemPrefs.getInt(deviceID + "volume", 0);
		squelch = systemPrefs.getInt(deviceID + "squelch", 127);
		toneSquelch = systemPrefs.getInt(deviceID + "toneSquelch", 0);
		digitalSquelch = systemPrefs.getInt(deviceID + "digitalSquelch", 0);
		frequency = systemPrefs.getDouble(deviceID + "frequency", 162.4);
		ifShift = systemPrefs.getInt(deviceID + "ifShift", 127);
		agc = systemPrefs.getBoolean(deviceID + "agc", false);
		afc = systemPrefs.getBoolean(deviceID + "afc", false);
		attenuator = systemPrefs.getBoolean(deviceID + "attenuator", false);
		noiseBlanker = systemPrefs.getBoolean(deviceID + "noiseBlanker", false);
		voiceScan = systemPrefs.getBoolean(deviceID + "voiceScan", false);
		vfoMode = systemPrefs.getBoolean(deviceID + "vfoMode", true);
	}

	private void sendParametersToRadio() {
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
		radioInterface.setVfoMode(vfoMode);
		radioInterface.setScanSelectList(scanSelect);
		radioInterface.setScanList(scan);
		radioInterface.setFrequency(frequency);
		radioInterface.setCalibrationDataObject(calibrate.getCalibrationDataObject());
	}

	public void setDevice(int device) {
	    this.device = device;
	    systemPrefs.putInt("device", device);
	    removeRadioInterfaceListener();
	    removeSerialInterfaceListener();
		initializeDevice(device);
		getRadioSpecificPreferences(getDeviceID());
		setSwingComponents();
		getCalFile(calFileIndex);
		sendParametersToRadio();
		serialConfig.updateDefaultSerialParameterSet(getDeviceID(), getDefaultSerialParameterSet());
		addRadioInterfaceListener();
		addSerialInterfaceListener();
	}
	
	private SerialParameterSet getDefaultSerialParameterSet() {
		int baudRate = radioInterface.getDefaultBaudRate();
		int dataBits = radioInterface.getDefaultDataBits();
		int stopBits = radioInterface.getDefaultStopBits();
		int parity = radioInterface.getDefaultParity();
		int flowControlIn = radioInterface.getDefaultFlowControlIn();
		int flowControlOut = radioInterface.getDefaultFlowControlOut();
		boolean dtr = radioInterface.getDefaultDTR();
		boolean rts = radioInterface.getDefaultRTS();
		String[] availableBaudRates = radioInterface.getAvailableBaudRates();
		boolean deviceAssignedSerialParametersFixed = radioInterface.serialParametersFixed();
		return new SerialParameterSet(baudRate, dataBits, stopBits, parity, dtr, rts, 
				flowControlIn, flowControlOut, availableBaudRates, deviceAssignedSerialParametersFixed);
	}
	
	private void initializeGuiComponents() {
		memoryVfoPanel = new JPanel();
		radioPanel = new JPanel();
		systemPanel = new JPanel();
		scanPanel = new JPanel();
		
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab(" System Settings ", null, systemPanel, null);
		tabbedPane.addTab(" Receiver Settings ", null, radioPanel, null);
		tabbedPane.addTab(" Serial Port Settings ", null, serialConfig.getSerialConfigGui(), null);
		tabbedPane.addTab(" Scan Channels ", null, scanPanel, null);
		tabbedPane.addTab(" Calibration ", null, calPanel, null);
		
		frequencyTextFieldLabel = new JLabel("Frequency");
		deviceComboBoxLabel = new JLabel("Device Type");
		calFileComboBoxLabel = new JLabel("Calibration File");
		filterComboBoxLabel = new JLabel("IF Bandwidth");
		modeComboBoxLabel = new JLabel("Mode");
		toneSquelchComboBoxLabel = new JLabel("Tone Squelch");
		volumeSliderLabel = new JLabel("Volume");
		squelchSliderLabel = new JLabel("Squelch");
		startRadioWithSystemCheckBox = new JCheckBox("Start Radio With System");
		agcCheckBox = new JCheckBox("Automatic Gain Control");
		afcCheckBox = new JCheckBox("Automatic Frequency Control");
		attenuatorCheckBox = new JCheckBox("Attenuator");
		noiseBlankerCheckBox = new JCheckBox("Noise Blanker");
		volumeSlider = new JSlider(0, 255, 0);
		squelchSlider = new JSlider(0, 255, 0);	
		frequencyTextField = new JFormattedTextField();
        scanTextField = new JFormattedTextField[10];
		okButton = new JButton("OK");
		cancelButton = new JButton("Cancel");
		applyButton = new JButton("Apply");
		vfoRadioButton = new JRadioButton("Use VFO Frequency");
		scanListRadioButton = new JRadioButton("Use Scan List Frequencies");
		freqSelectButtonGroup = new ButtonGroup();
		rbh = new RadioButtonHandler();
		scanLabel = new JLabel[10];
		
		decimalFormat = new DecimalFormat("###0.0####");
		numberFormat = NumberFormat.getNumberInstance();
		numberFormat.setMaximumIntegerDigits(4);
		numberFormat.setMinimumIntegerDigits(1);
		numberFormat.setMaximumFractionDigits(5);
		numberFormat.setMinimumFractionDigits(1);
		numberFormatter = new NumberFormatter(numberFormat);
		numberFormatter.setCommitsOnValidEdit(false);
		numberFormatter.setValueClass(Double.class);
		numberFormatter.setAllowsInvalid(true);
		frequencyTextField = new JFormattedTextField(numberFormatter);
		frequencyTextField.setHorizontalAlignment(SwingConstants.CENTER);
		frequencyTextField.setFont(new Font("Calabri", Font.PLAIN, 11));
		frequencyTextField.setBackground(Color.BLACK);
		frequencyTextField.setForeground(Color.WHITE);
		
		memoryVfoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Memory / VFO"));
		frequencyTextFieldLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		deviceComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		calFileComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		filterComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		modeComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		toneSquelchComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		volumeSliderLabel.setHorizontalAlignment(SwingConstants.CENTER);
		squelchSliderLabel.setHorizontalAlignment(SwingConstants.CENTER);
		okButton.setMultiClickThreshhold(50L);
		cancelButton.setMultiClickThreshhold(50L);
		applyButton.setMultiClickThreshhold(50L);

		startRadioWithSystemCheckBox.setMultiClickThreshhold(50L);
		noiseBlankerCheckBox.setMultiClickThreshhold(50L);
		attenuatorCheckBox.setMultiClickThreshhold(50L);
		agcCheckBox.setMultiClickThreshhold(50L);
		afcCheckBox.setMultiClickThreshhold(50L);
		
		vfoRadioButton.setMultiClickThreshhold(50L);
		scanListRadioButton.setMultiClickThreshhold(50L);
		freqSelectButtonGroup.add(vfoRadioButton);
		freqSelectButtonGroup.add(scanListRadioButton);
		vfoRadioButton.addItemListener(rbh);
		scanListRadioButton.addItemListener(rbh);
		
        for (int i = 0; i < scan.length; i++) {
			scanLabel[i] = new JLabel("F" + i);
			scanTextField[i] = new JFormattedTextField(decimalFormat.format(scan[i]));
			scanCheckBox[i] = new JCheckBox("", scanSelect[i]);
			scanTextField[i].setHorizontalAlignment(SwingConstants.RIGHT);
			scanTextField[i].setFont(new Font("Calabri", Font.PLAIN, 11));
			scanTextField[i].setBackground(Color.BLACK);
			scanTextField[i].setForeground(Color.WHITE);
		}
        
        calFileComboBoxModel = new SortedComboBoxModel();
        
		deviceComboBox = new JComboBox<String>(RadioInterface.DEVICES);
		modeComboBox = new JComboBox<String>();
		filterComboBox = new JComboBox<String>();
		toneSquelchComboBox = new JComboBox<String>();
		digitalSquelchComboBox = new JComboBox<String>();
		calFileComboBox = new JComboBox<Object>(calFileComboBoxModel);
		deviceComboBox.setEditable(false);
		filterComboBox.setEditable(false);
		toneSquelchComboBox.setEditable(false);
		digitalSquelchComboBox.setEditable(false);
		calFileComboBox.setEditable(false);
	}
	
	private void populateModeComboBox(EmissionDesignator[] modes) {
		try {
			modeComboBox.removeAllItems();
			for (int i = 0; i < modes.length; i++) {
				modeComboBox.addItem(modes[i].getModeName());
			}
			modeComboBox.validate();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
	}
	
	private void populateToneSquelchComboBox(String[] toneSquelchValues) {
		try {
			int index = 0;
			toneSquelchComboBox.removeAllItems();
			toneSquelchComboBox.addItem(toneSquelchValues[0]);
			for (int i = 1; i < toneSquelchValues.length; i++) {
				toneSquelchComboBox.addItem(toneSquelchValues[i]);
				if (toneSquelch == Double.parseDouble(toneSquelchValues[i])) index = i;
			}
			toneSquelchComboBox.validate();
			toneSquelchComboBox.setSelectedIndex(index);
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
	}
	
	private void populateDigitalSquelchComboBox(String[] digitalSquelchValues) {
		try {
			digitalSquelchComboBox.removeAllItems();
			for (int i = 0; i < digitalSquelchValues.length; i++) {
				digitalSquelchComboBox.addItem(digitalSquelchValues[i]);
			}
			digitalSquelchComboBox.validate();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
	}
	
	private void populateFilterComboBox(String[] filters) {
		try {
			filterComboBox.removeAllItems();
			for (int i = 0; i < filters.length; i++) {
				filterComboBox.addItem(filters[i]);
			}
			filterComboBox.validate();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
	}
	
	private boolean getCalFile(int calFileIndex) {
		boolean calFileOpen = false;
		if (calFileComboBox.getItemCount() > calFileIndex) {
			calFileComboBox.setSelectedIndex(calFileIndex);
			calFileOpen = calibrate.openCalFile(calFileComboBox.getSelectedItem().toString());
		} else {
			calFileOpen = false;
		}
		return calFileOpen;
	}
	
	private void setSwingComponents() {
		deviceComboBox.setSelectedIndex(device);
		populateModeComboBox(radioInterface.getEmissionDesignators());
		populateFilterComboBox(radioInterface.availableFilters());
		populateDigitalSquelchComboBox(radioInterface.supportedDigitalSquelchCodes());
		populateToneSquelchComboBox(radioInterface.supportedToneSquelchCodes());
		String[] deviceRowSelection = deviceComboBox.getSelectedItem().toString().split(" ");
		populateCalFileComboBox(deviceRowSelection, calFiles);
		
		modeComboBox.setSelectedIndex(mode);
		filterComboBox.setSelectedIndex(filter);
		digitalSquelchComboBox.setSelectedIndex(digitalSquelch);

		startRadioWithSystemCheckBox.setSelected(startRadioWithSystem);
		noiseBlankerCheckBox.setSelected(noiseBlanker);
		attenuatorCheckBox.setSelected(attenuator);
		agcCheckBox.setSelected(agc);
		afcCheckBox.setSelected(afc);

		numberFormatter.setMinimum(radioInterface.minimumRxFrequency());
		numberFormatter.setMaximum(radioInterface.maximumRxFrequency());
		frequencyTextField.setText(decimalFormat.format(frequency));	
		
		volumeSlider.setValue(volume);
		squelchSlider.setValue(squelch);
		
		if (vfoMode) model = vfoRadioButton.getModel();
		else model = scanListRadioButton.getModel();
		freqSelectButtonGroup.setSelected(model, true); 
	}
	
	private String[][] getAllDeviceStrings() {
		File calDir = new File(calFileDir);
		calFiles = calDir.list();
		String[][] dev = new String[2][calFiles.length];
		for (int i = 0; i < calFiles.length; i++) {
			try {
				String modCheck[] = getModString(calFileDir + File.separator + calFiles[i]);
				dev[MFR][i] = modCheck[MFR];
				dev[MOD][i] = modCheck[MOD];
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
		return dev;
	}
}