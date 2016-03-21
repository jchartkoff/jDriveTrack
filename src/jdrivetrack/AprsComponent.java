package jdrivetrack;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import interfaces.APRSInterface;
import interfaces.SerialInterface;
import radios.AprsNMEAWaypointDecoder;

public class AprsComponent extends JDialog {
	private static final long serialVersionUID = -4152265380734428539L;
	
	public static final String[] APRS_DEVICES = { 
		"Yaesu VX-8GR Portable", 
		"Kenwood TM-D700A Mobile", 
		"Byonics TinyTrac III", 
		"Byonics TinyTrac IV" 
	};
	
	private int device;
	private int portNumber;
	private boolean startAprsWithSystem;
	private boolean enableAprsTracking;
	private boolean showAPRSIconLabels;
	private JPanel aprsConfigPanel;
	private JTabbedPane tabbedPane;
	private JButton okButton;
	private JButton cancelButton;
	private JButton applyButton;
	private JComboBox<?> selectDeviceComboBox;
	private JLabel selectDeviceComboBoxLabel;
	private JCheckBox startAprsWithSystemCheckBox;
	private JCheckBox enableAprsTrackingCheckBox;
	private JCheckBox enableAPRSIconLabelsCheckBox;
	private APRSInterface aprsInterface;
	private SerialConfig serialConfig;
	
	private Preferences systemPrefs = Preferences.userRoot().node("jdrivetrack/prefs/AprsComponent");
	
	public AprsComponent() {
		getGeneralPreferences();
		initializeComponents();
		drawGraphicalUserInterface();
	}
	
	private String getDeviceID() {
		return aprsInterface.versionUID();
	}
	
	private void setDevice(int device) {
		this.device = device;
		removeSerialConfigListener();
	    systemPrefs.putInt("device", device);
		initializeDevice(device);

	}
	
    private void initializeDevice(int device) {
    	this.device = device;
        switch (device) {
            case 0:
                aprsInterface = new AprsNMEAWaypointDecoder();
                break;
        }
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
            	aprsInterface.inputData(read);
            	firePropertyChange(SerialInterface.RX_CHAR, null, read);
            }
    	}
    };
    
	private void initializeComponents() {
		setDevice(device);
    	serialConfig = new SerialConfig(getDeviceID(), "APRS Modem", getDefaultSerialParameterSet());
		
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

		aprsConfigPanel = new JPanel();
		
		tabbedPane = new JTabbedPane();

		tabbedPane.addTab(" APRS Settings ", null, aprsConfigPanel, null);
		tabbedPane.addTab(" Serial Port Settings ", null, serialConfig.getSerialConfigGui(), null);
        tabbedPane.addTab(" Error Notification Settings ", null, serialConfig.getErrorNotificationGui(), null);
        tabbedPane.addTab(" Event Management ", null, serialConfig.getEventManagementGui(), null);
		
		setTitle("APRS Settings");

		selectDeviceComboBoxLabel = new JLabel("Device Type");
		selectDeviceComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		startAprsWithSystemCheckBox = new JCheckBox("Start TNC With System");
		enableAPRSIconLabelsCheckBox = new JCheckBox("Show APRS Icon Labels");
		enableAprsTrackingCheckBox = new JCheckBox("Enable APRS Tracking");

		okButton = new JButton("OK");
		okButton.setMultiClickThreshhold(50L);
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				applyButton.doClick();
				setVisible(false);
			}
		});

		cancelButton = new JButton("Cancel");
		cancelButton.setMultiClickThreshhold(50L);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setVisible(false);
			}
		});

		applyButton = new JButton("Apply");
		applyButton.setMultiClickThreshhold(50L);
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				applyButtonActionListenerEvent(event);
			}
		});

		selectDeviceComboBox = new JComboBox<String>(APRS_DEVICES);
		selectDeviceComboBox.setSelectedIndex(device);

		selectDeviceComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				selectDeviceComboBoxActionPerformed(event);
			}
		});

		startAprsWithSystemCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				startAprsWithSystemActionPerformed(event);
			}
		});

		enableAprsTrackingCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				enableAprsTrackingActionPerformed(event);
			}
		});

		enableAPRSIconLabelsCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				enableAprsIconLabelsActionPerformed(event);
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
		
		String cancelName = "cancel";
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelName);
        ActionMap actionMap = getRootPane().getActionMap();
        
        actionMap.put(cancelName, new AbstractAction() {
			private static final long serialVersionUID = 449545681193632268L;
			@Override
			public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        }); 
		
		startAprsWithSystemCheckBox.setSelected(startAprsWithSystem);
		enableAPRSIconLabelsCheckBox.setSelected(showAPRSIconLabels);
		enableAprsTrackingCheckBox.setSelected(enableAprsTracking);
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
		serialConfig.saveSettings(getDeviceID());
	}
	
	private void drawGraphicalUserInterface() {
		GroupLayout configPanelLayout = new GroupLayout(aprsConfigPanel);
		aprsConfigPanel.setLayout(configPanelLayout);

		configPanelLayout.setHorizontalGroup(configPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(configPanelLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(configPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(GroupLayout.Alignment.LEADING, configPanelLayout.createSequentialGroup()
						.addComponent(selectDeviceComboBoxLabel)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(selectDeviceComboBox,220,220,220))
					.addGroup(configPanelLayout.createSequentialGroup()	
						.addGroup(configPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(startAprsWithSystemCheckBox)
							.addComponent(enableAprsTrackingCheckBox)
							.addComponent(enableAPRSIconLabelsCheckBox))
						.addGap(0,0,Short.MAX_VALUE)))
				.addContainerGap()));

		configPanelLayout.setVerticalGroup(configPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(configPanelLayout.createSequentialGroup()
				.addContainerGap()	
				.addGroup(configPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(selectDeviceComboBox,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
					.addComponent(selectDeviceComboBoxLabel))
				.addGap(18,18,18)
					.addComponent(startAprsWithSystemCheckBox)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(enableAprsTrackingCheckBox)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(enableAPRSIconLabelsCheckBox)
					.addContainerGap(115,115)));

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
				.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(okButton)
					.addComponent(applyButton)
					.addComponent(cancelButton))));

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
		device = systemPrefs.getInt("device", 0);
		startAprsWithSystem = systemPrefs.getBoolean("startAprsWithSystemCheckBox", false);
		enableAprsTracking = systemPrefs.getBoolean("enableAprsTrackingCheckBox", false);
		showAPRSIconLabels = systemPrefs.getBoolean("enableAprsIconLabelsCheckBox", true);
	}

	public String getErrorMessage() {
		return getSerialConfig().getSerialInterface().getErrorMessage();
	}	

	private void startAprsWithSystemActionPerformed(ActionEvent event) {
		startAprsWithSystem = startAprsWithSystemCheckBox.isSelected();
	}

	private void enableAprsTrackingActionPerformed(ActionEvent event) {
		enableAprsTracking = enableAprsTrackingCheckBox.isSelected();
	}

	private void enableAprsIconLabelsActionPerformed(ActionEvent event) {
		showAPRSIconLabels = enableAPRSIconLabelsCheckBox.isSelected();
	}

	private void selectDeviceComboBoxActionPerformed(ActionEvent event) {
		switch (device) {
			case 0:
				aprsInterface = new AprsNMEAWaypointDecoder();
				break;
		}
	}
	
	private void applyButtonActionListenerEvent(ActionEvent event) {
		saveGeneralPreferences();
		selectDeviceComboBox.setSelectedIndex(device);
		serialConfig.saveSettings(getDeviceID());
		serialConfig.sendSerialPortSettingsFromMemoryToDevice();
	}

	private void saveGeneralPreferences() {
		systemPrefs.putInt("device", device);
		systemPrefs.putInt("portNumber", portNumber);
		systemPrefs.putBoolean("startAprsWithSystem", startAprsWithSystem);
		systemPrefs.putBoolean("enableAprsTracking", enableAprsTracking);
		systemPrefs.putBoolean("showAPRSIconLabels", showAPRSIconLabels);
	}
	
	public boolean isStartAprsWithSystem() {
		return startAprsWithSystem;
	}

	public boolean isEnableAprsTracking() {
		return enableAprsTracking;
	}
	
	public boolean isEnableAprsShowIconLabels() {
		return showAPRSIconLabels;
	}
	
	public SerialConfig getSerialConfig() {
		return serialConfig;
	}

	public APRSInterface getAPRSInterface() {
		return aprsInterface;
	}
	
	private SerialParameterSet getDefaultSerialParameterSet() {
		int baudRate = aprsInterface.getDefaultBaudRate();
		int dataBits = aprsInterface.getDefaultDataBits();
		int stopBits = aprsInterface.getDefaultStopBits();
		int parity = aprsInterface.getDefaultParity();
		int flowControlIn = aprsInterface.getDefaultFlowControlIn();
		int flowControlOut = aprsInterface.getDefaultFlowControlOut();
		boolean dtr = aprsInterface.getDefaultDTR();
		boolean rts = aprsInterface.getDefaultRTS();
		String[] availableBaudRates = aprsInterface.getAvailableBaudRates();
		boolean deviceAssignedSerialParametersFixed = aprsInterface.serialParametersFixed();
		return new SerialParameterSet(baudRate, dataBits, stopBits, parity, dtr, rts, 
			flowControlIn, flowControlOut, availableBaudRates, deviceAssignedSerialParametersFixed);
	}
}