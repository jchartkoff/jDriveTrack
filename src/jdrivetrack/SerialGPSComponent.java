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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.text.NumberFormatter;

import interfaces.GPSInterface;
import interfaces.SerialInterface;
import jssc.SerialPortException;
import radios.CF30GPSReceiver;

public class SerialGPSComponent extends JDialog {
	private static final long serialVersionUID = -3141263739316549058L;
	
	protected static final String SERIAL_PORT_ERROR = "SERIAL_PORT_ERROR";
	protected static final String SERIAL_PORT_EXCEPTION = "SERIAL_PORT_EXCEPTION";
	protected static final String GPS_RADIUS_UPDATED = "GPS_RADIUS_UPDATED";
	 
    private int device;
    private int portNumber;
    private boolean startGpsWithSystem;
    private boolean enableGpsTracking;
    private boolean centerMapOnGPSPosition;
	private boolean reportGPSCircularRedundancyCheckFailures;
    private JPanel gpsPanel;
    private JTabbedPane tabbedPane;
    private JButton okButton;
    private JButton cancelButton;
    private JButton applyButton;
    private JComboBox<String> selectDeviceComboBox;
    private JLabel selectDeviceComboBoxLabel;
    private JCheckBox cbStartGpsWithSystem;
    private JCheckBox cbEnableGpsTracking;
    private JCheckBox cbCenterMapOnGPSPosition;
	private JCheckBox cbReportGPSCircularRedundancyCheckFailures;
	private JFormattedTextField ftfGpsCursorRadius;
	private JLabel gpsSymbolRadiusLabel;
	private double gpsSymbolRadius;
	private boolean isOnLine = false;
	
	private Preferences userPref = Preferences.userRoot().node(this.getClass().getName());
	
    private GPSInterface gpsInterface;
    private SerialConfig serialConfig;
    
    public SerialGPSComponent(int device, boolean clearAllPrefs) {
		if (clearAllPrefs) {
			try {
				userPref.clear();
			} catch (BackingStoreException ex) {
				ex.printStackTrace();
			}
		}

		if (device == -1) device = userPref.getInt("device", 0);
		this.device = device;
		
		getGeneralPreferences();
		initializeDevice(device);
        initializeComponents();
        drawGraphicalUserInterface();
        configureListeners();
    }
    
    public void enableGPS(boolean enable) throws SerialPortException {
        if (enable) {
        	isOnLine = true;
	    	serialConfig.getSerialInterface().setOnline(serialConfig.getPortName());
			if (!gpsInterface.isCTSSupported()) {
				gpsInterface.startGPS();
			}
			else {
				if (serialConfig.getSerialInterface().isCTS()) gpsInterface.startGPS();
			}
        } else {
        	isOnLine = false;
        	gpsInterface.shutDown();
        }
	}
    
    public boolean isGpsOnLine() {
    	return isOnLine;
    }
    
    private void configureListeners() {
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
    	

        selectDeviceComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					setDevice(device);
	                serialConfig = new SerialConfig(getDeviceID(), "GPS Receiver", getDefaultSerialParameterSet());
	        		addSerialConfigListener();
				}
			}
		});
        
		ftfGpsCursorRadius.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				ftfGpsCursorRadius.setFont(new Font("Calabri", Font.BOLD, 11));
			}
			@Override
			public void focusLost(FocusEvent e) {
				
			}	
		});

		ftfGpsCursorRadius.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent event) {
				
			}
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
			        ftfGpsCursorRadius.setFont(new Font("Calabri", Font.PLAIN, 11));
					ftfGpsCursorRadius.transferFocus();
				}
			}
			@Override
			public void keyReleased(KeyEvent event) {

			}
		});
		
        cbStartGpsWithSystem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                startGpsWithSystemActionPerformed(event);
            }
        });

        cbEnableGpsTracking.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                enableGpsTrackingActionPerformed(event);
            }
        });

        cbCenterMapOnGPSPosition.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                centerMapOnGPSPositionActionPerformed(event);
            }
        });

        cbReportGPSCircularRedundancyCheckFailures.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent event) {
            	cbReportGPSCircularRedundancyCheckFailuresActionPerformed(event);
            }
        });
        
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
    }
    
    private void initializeComponents() {
    	serialConfig = new SerialConfig(getDeviceID(), "GPS Receiver", getDefaultSerialParameterSet());
		addSerialConfigListener();
    	setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
    	setTitle("GPS Settings");
    	
        gpsPanel = new JPanel();
        tabbedPane = new JTabbedPane();

		selectDeviceComboBoxLabel = new JLabel("Device Type");
        selectDeviceComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        cbStartGpsWithSystem = new JCheckBox("Start GPS With System");
        cbEnableGpsTracking = new JCheckBox("Enable GPS Tracking");
        cbCenterMapOnGPSPosition = new JCheckBox("Center Map on GPS Position");
        cbReportGPSCircularRedundancyCheckFailures = new JCheckBox("Report GPS Circular Redundancy Check Failures");
        
        okButton = new JButton("OK");
        okButton.setMultiClickThreshhold(50L);

        cancelButton = new JButton("Cancel");
        cancelButton.setMultiClickThreshhold(50L);

        applyButton = new JButton("Apply");
        applyButton.setMultiClickThreshhold(50L);

        selectDeviceComboBox = new JComboBox<String>(GPSInterface.GPS_DEVICES);
        selectDeviceComboBox.setSelectedIndex(device);
        
		gpsSymbolRadiusLabel = new JLabel("GPS Symbol Radius (pixels)");
		gpsSymbolRadiusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		
		DecimalFormat gpsSymbolRadiusFormat = new DecimalFormat("#0");
		
		NumberFormat intFormat = NumberFormat.getIntegerInstance();

		NumberFormatter numberFormatter = new NumberFormatter(intFormat);
		numberFormatter.setValueClass(Integer.class);
		numberFormatter.setAllowsInvalid(true);
		numberFormatter.setMinimum(2);
		numberFormatter.setMaximum(99);
		numberFormatter.setCommitsOnValidEdit(false);

		ftfGpsCursorRadius = new JFormattedTextField(numberFormatter);
		ftfGpsCursorRadius.setText(gpsSymbolRadiusFormat.format(gpsSymbolRadius));
		ftfGpsCursorRadius.setHorizontalAlignment(SwingConstants.CENTER);
		ftfGpsCursorRadius.setFont(new Font("Calabri", Font.PLAIN, 11));
		ftfGpsCursorRadius.setBackground(Color.WHITE);
		ftfGpsCursorRadius.setForeground(Color.BLACK);

        String cancelName = "cancel";
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelName);
        ActionMap actionMap = getRootPane().getActionMap();
        
        actionMap.put(cancelName, new AbstractAction() {
			private static final long serialVersionUID = 6891550706323008600L;
			@Override
			public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        }); 
        
        cbStartGpsWithSystem.setSelected(startGpsWithSystem);
        cbEnableGpsTracking.setSelected(enableGpsTracking);
        cbCenterMapOnGPSPosition.setSelected(centerMapOnGPSPosition);
        cbReportGPSCircularRedundancyCheckFailures.setSelected(reportGPSCircularRedundancyCheckFailures);
        
        tabbedPane.addTab("GPS Settings", null, gpsPanel, null);
        tabbedPane.addTab("Serial Port Settings", null, serialConfig.getSerialConfigGui(), null);
        tabbedPane.addTab("Error Notification Settings", null, serialConfig.getErrorNotificationGui(), null);
        tabbedPane.addTab("Event Management", null, serialConfig.getEventManagementGui(), null);
    }
    
    private boolean setupListenersWhenConnected() {
		Window parentFrame = (Window) getParent();
		parentFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				if (WindowEvent.WINDOW_CLOSING == event.getID()) {
					saveSettings();
				}
			}
		});
		return true;
	}
	
	private void saveSettings() {
		saveGeneralPreferences();
		serialConfig.saveSettings(getDeviceID());
	}
    
    private void addSerialConfigListener() {
    	serialConfig.getSerialInterface().addPropertyChangeListener(serialConfigListener);
    }
    
    private void removeSerialConfigListener() {
    	if (serialConfig != null)
    		serialConfig.getSerialInterface().removePropertyChangeListener(serialConfigListener);
    }
    
    private PropertyChangeListener serialConfigListener = new PropertyChangeListener() {
    	@Override
    	public void propertyChange(PropertyChangeEvent event) {
            if (SerialInterface.RX_CHAR.equals(event.getPropertyName())) {
            	String read = serialConfig.getSerialInterface().readString((int) event.getNewValue());
            	gpsInterface.inputData(read);
            	firePropertyChange(SerialInterface.RX_CHAR, null, read);
            }
    	}
    };
    
	private void cbReportGPSCircularRedundancyCheckFailuresActionPerformed(ActionEvent event) {
		reportGPSCircularRedundancyCheckFailures = cbReportGPSCircularRedundancyCheckFailures.isSelected();
	}

	private String getDeviceID() {
		return gpsInterface.versionUID();
	}
	
	private void setDevice(int device) {
		this.device = device;
		removeSerialConfigListener();
	    userPref.putInt("device", device);
		initializeDevice(device);
	}
	
    private void initializeDevice(int device) {
        switch (device) {
            case 0:
                gpsInterface = new CF30GPSReceiver();
                break;
        }
    }
    
	private void startGpsWithSystemActionPerformed(ActionEvent event) {
        startGpsWithSystem = cbStartGpsWithSystem.isSelected();
    }

    private void enableGpsTrackingActionPerformed(ActionEvent event) {
        enableGpsTracking = cbEnableGpsTracking.isSelected();
    }

    private void centerMapOnGPSPositionActionPerformed(ActionEvent event) {
        centerMapOnGPSPosition = cbCenterMapOnGPSPosition.isSelected();
    }

	private void drawGraphicalUserInterface() {
        GroupLayout gpsPanelLayout = new GroupLayout(gpsPanel);
        gpsPanel.setLayout(gpsPanelLayout);
        gpsPanelLayout.setAutoCreateGaps(true);
        gpsPanelLayout.setAutoCreateContainerGaps(true);

        gpsPanelLayout.setHorizontalGroup(gpsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(gpsPanelLayout.createSequentialGroup()
                .addGap(20,20,20)
                .addGroup(gpsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(cbReportGPSCircularRedundancyCheckFailures)
                    .addComponent(cbStartGpsWithSystem)
                    .addComponent(cbCenterMapOnGPSPosition)
                    .addComponent(cbEnableGpsTracking)
                    .addGroup(gpsPanelLayout.createSequentialGroup()
                        .addComponent(gpsSymbolRadiusLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ftfGpsCursorRadius,40,40,40))
                    .addGroup(gpsPanelLayout.createSequentialGroup()
                        .addComponent(selectDeviceComboBoxLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selectDeviceComboBox,250,250,250)))
            .addContainerGap(GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE)));

        gpsPanelLayout.setVerticalGroup(gpsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(gpsPanelLayout.createSequentialGroup()
                .addGap(30,30,30)
                .addGroup(gpsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectDeviceComboBoxLabel)
                    .addComponent(selectDeviceComboBox,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE))
                .addGap(15,15,15)
                .addComponent(cbEnableGpsTracking)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbCenterMapOnGPSPosition)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbStartGpsWithSystem)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbReportGPSCircularRedundancyCheckFailures)
                .addGap(15,15,15)
                .addGroup(gpsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(gpsSymbolRadiusLabel)
                    .addComponent(ftfGpsCursorRadius,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE))
        		.addContainerGap(GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE)));
        
		GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                	.addGroup(layout.createSequentialGroup()
		                .addContainerGap()
		                .addComponent(tabbedPane,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE))
		                .addGroup(layout.createSequentialGroup()
			                .addComponent(okButton,90,90,90)
			                .addComponent(applyButton,90,90,90)
			                .addComponent(cancelButton,90,90,90)))
		        	.addContainerGap(GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE)));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        	.addGroup(GroupLayout.Alignment.TRAILING,layout.createSequentialGroup()
        		.addComponent(tabbedPane,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	                .addComponent(okButton)
	                .addComponent(applyButton)
	                .addComponent(cancelButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE)));

        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();

        pack();
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));
    }

    public void showSettingsDialog(boolean showSettingsDialog) {
        setVisible(showSettingsDialog);
    }

    private void getGeneralPreferences() {
        portNumber = userPref.getInt("portNumber", 2);
        enableGpsTracking = userPref.getBoolean("EnableGpsTracking", false);
        startGpsWithSystem = userPref.getBoolean("StartGpsWithSystem", false);
        centerMapOnGPSPosition = userPref.getBoolean("CenterMapOnGPSPosition", false);
        reportGPSCircularRedundancyCheckFailures = userPref.getBoolean("ReportGPSCircularRedundancyCheckFailures", true);
		gpsSymbolRadius = userPref.getDouble("GPSSymbolRadius", 5d);
    }
    
    public boolean startGpsWithSystem() {
        return startGpsWithSystem;
    }

    public boolean enableGpsTracking() {
        return enableGpsTracking;
    }

    public boolean centerMapOnGPSPosition() {
        return centerMapOnGPSPosition;
    }
    
    private void saveGeneralPreferences() {
    	userPref.putInt("portNumber", portNumber);
		gpsSymbolRadius = Double.parseDouble(ftfGpsCursorRadius.getText());
        userPref.putBoolean("StartGpsWithSystem", startGpsWithSystem);
        userPref.putBoolean("EnableGpsTracking", enableGpsTracking);
        userPref.putBoolean("CenterMapOnGPSPosition", centerMapOnGPSPosition);
        userPref.putBoolean("ReportGPSCircularRedundancyCheckFailures", reportGPSCircularRedundancyCheckFailures);
		userPref.putDouble("GPSSymbolRadius", gpsSymbolRadius);
    }
    
    private void applyButtonActionListenerEvent(ActionEvent event) {
    	saveGeneralPreferences();
    	gpsInterface.reportCRCErrors(reportGPSCircularRedundancyCheckFailures);
		serialConfig.saveSettings(getDeviceID());
		serialConfig.sendSerialPortSettingsFromMemoryToDevice();
        firePropertyChange(GPS_RADIUS_UPDATED, null, gpsSymbolRadius);
    }

	public SerialConfig getSerialConfig() {
		return serialConfig;
	}

	public GPSInterface getGPSInterface() {
		return gpsInterface;
	}
	
	public double getGpsSymbolRadius() {
		return gpsSymbolRadius;
	}

	private SerialParameterSet getDefaultSerialParameterSet() {
		int baudRate = gpsInterface.getDefaultBaudRate();
		int dataBits = gpsInterface.getDefaultDataBits();
		int stopBits = gpsInterface.getDefaultStopBits();
		int parity = gpsInterface.getDefaultParity();
		int flowControlIn = gpsInterface.getDefaultFlowControlIn();
		int flowControlOut = gpsInterface.getDefaultFlowControlOut();
		boolean dtr = gpsInterface.getDefaultDTR();
		boolean rts = gpsInterface.getDefaultRTS();
		String[] availableBaudRates = gpsInterface.getAvailableBaudRates();
		boolean deviceAssignedSerialParametersFixed = gpsInterface.serialParametersFixed();
		return new SerialParameterSet(baudRate, dataBits, stopBits, parity, dtr, rts, 
			flowControlIn, flowControlOut, availableBaudRates, deviceAssignedSerialParametersFixed);
	}
}