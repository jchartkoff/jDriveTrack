package com;

import jssc.SerialPort;
import jssc.SerialPortList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.GroupLayout;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.Preferences;

import javax.swing.SwingConstants;

import org.apache.commons.lang3.CharSet;

public class AprsComponent extends JDialog {
	private static final long serialVersionUID = 5604287791789463465L;
	
	private String aprsPortName;
	private int aprsDevice;
	private boolean startAprsWithSystem;
	private boolean enableAprsTracking;
	private boolean showAPRSIconLabels;
	private int aprsParity;
	private int aprsStopBits;
	private int aprsDataBits;
	private int aprsBaudRate;
	private ButtonModel model;
	private JPanel aprsPanel;
	private JPanel sysPanel;
	private JTabbedPane tabbedPane;
	private JButton okButton;
	private JButton cancelButton;
	private JButton applyButton;
	private JComboBox<?> selectDeviceComboBox;
	private JComboBox<?> selectAprsComPortComboBox;
	private JComboBox<?> selectBaudRateComboBox;
	private JLabel selectDeviceComboBoxLabel;
	private JLabel selectAprsComPortComboBoxLabel;
	private JLabel selectBaudRateComboBoxLabel;
	private JLabel aprsDataBitsButtonGroupLabel;
	private JCheckBox startAprsWithSystemCheckBox;
	private JCheckBox enableAprsTrackingCheckBox;
	private JCheckBox enableAPRSIconLabelsCheckBox;
	private ButtonGroup aprsDataBitsButtonGroup;
	private JRadioButton aprsDataBits5;
	private JRadioButton aprsDataBits6;
	private JRadioButton aprsDataBits7;
	private JRadioButton aprsDataBits8;
	private JLabel aprsStopBitsButtonGroupLabel;
	private ButtonGroup aprsStopBitsButtonGroup;
	private JRadioButton aprsStopBits1;
	private JRadioButton aprsStopBits15;
	private JRadioButton aprsStopBits2;
	private JLabel aprsParityButtonGroupLabel;
	private ButtonGroup aprsParityButtonGroup;
	private JRadioButton aprsParityNone;
	private JRadioButton aprsParityOdd;
	private JRadioButton aprsParityEven;
	private JRadioButton aprsParityMark;
	private JRadioButton aprsParitySpace;
	private Preferences userPref;
	
	protected APRSInterface aprsInterface;
	protected SerialInterface serialInterface;
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public AprsComponent() {
		getSettingsFromRegistry();
		initializeComponents();
		drawGraphicalUserInterface();
	}

	private void initializeComponents() {
		
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		
		aprsPanel = new JPanel();
		sysPanel = new JPanel();
		tabbedPane = new JTabbedPane();

		setTitle("APRS Settings");

		selectDeviceComboBoxLabel = new JLabel("Device Type");
		selectDeviceComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		selectAprsComPortComboBoxLabel = new JLabel("Comm Port");
		selectAprsComPortComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		selectBaudRateComboBoxLabel = new JLabel("Baud Rate");
		selectBaudRateComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		aprsDataBitsButtonGroupLabel = new JLabel("Data Bits");
		aprsDataBitsButtonGroupLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		aprsStopBitsButtonGroupLabel = new JLabel("Stop Bits");
		aprsStopBitsButtonGroupLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		aprsParityButtonGroupLabel = new JLabel("Parity Bits");
		aprsParityButtonGroupLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		startAprsWithSystemCheckBox = new JCheckBox("Start TNC With System");
		enableAPRSIconLabelsCheckBox = new JCheckBox("Show APRS Icon Labels");
		enableAprsTrackingCheckBox = new JCheckBox("Enable APRS Tracking");

		okButton = new JButton("OK");

		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				applyButton.doClick();
				setVisible(false);
			}
		});

		cancelButton = new JButton("Cancel");

		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setVisible(false);
			}
		});

		applyButton = new JButton("Apply");

		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				applyButtonActionListenerEvent(event);
			}
		});

		String[] portNames = SerialPortList.getPortNames();
		String[] aprsBaudRates = { "110", "300", "600", "1200", "4800", "9600",
				"14400", "19200", "28800", "38400", "57600", "115200",
				"128000", "256000" };
		String[] aprsTncTypes = { "Yaesu VX-8GR Transceiver" };

		selectDeviceComboBox = new JComboBox<String>(aprsTncTypes);
		selectDeviceComboBox.setSelectedIndex(aprsDevice);

		selectDeviceComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				selectDeviceComboBoxActionPerformed(event);
			}
		});

		selectAprsComPortComboBox = new JComboBox<String>(portNames);

		for (int i = 0; i < selectAprsComPortComboBox.getItemCount(); i++) {
			if (String.valueOf(selectAprsComPortComboBox.getItemAt(i))
					.substring(3).equals(String.valueOf(aprsPortName))) {
				selectAprsComPortComboBox.setSelectedIndex(i);
				break;
			}
		}

		selectAprsComPortComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				selectAprsComPortComboBoxActionPerformed(event);
			}
		});

		selectBaudRateComboBox = new JComboBox<String>(aprsBaudRates);

		selectBaudRateComboBox.setSelectedIndex(aprsBaudRate);

		selectBaudRateComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				selectBaudRateComboBoxActionPerformed(event);
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
		
		RadioButtonHandler rbh = new RadioButtonHandler();

		aprsDataBits5 = new JRadioButton("5");
		aprsDataBits6 = new JRadioButton("6");
		aprsDataBits7 = new JRadioButton("7");
		aprsDataBits8 = new JRadioButton("8");

		aprsDataBitsButtonGroup = new ButtonGroup();
		aprsDataBitsButtonGroup.add(aprsDataBits5);
		aprsDataBitsButtonGroup.add(aprsDataBits6);
		aprsDataBitsButtonGroup.add(aprsDataBits7);
		aprsDataBitsButtonGroup.add(aprsDataBits8);

		aprsDataBits5.addItemListener(rbh);
		aprsDataBits6.addItemListener(rbh);
		aprsDataBits7.addItemListener(rbh);
		aprsDataBits8.addItemListener(rbh);

        switch (aprsDataBits) {
            case SerialPort.DATABITS_5:
                model = aprsDataBits5.getModel();
                break;
            case SerialPort.DATABITS_6:
                model = aprsDataBits6.getModel();
                break;
            case SerialPort.DATABITS_7:
                model = aprsDataBits7.getModel();
                break;
            case SerialPort.DATABITS_8:
                model = aprsDataBits8.getModel();
                break;
        }
        
		aprsDataBitsButtonGroup.setSelected(model, true);

		aprsStopBits1 = new JRadioButton("1");
		aprsStopBits15 = new JRadioButton("1.5");
		aprsStopBits2 = new JRadioButton("2");

		aprsStopBitsButtonGroup = new ButtonGroup();
		aprsStopBitsButtonGroup.add(aprsStopBits1);
		aprsStopBitsButtonGroup.add(aprsStopBits15);
		aprsStopBitsButtonGroup.add(aprsStopBits2);

		aprsStopBits1.addItemListener(rbh);
		aprsStopBits15.addItemListener(rbh);
		aprsStopBits2.addItemListener(rbh);

        switch (aprsStopBits) {
            case SerialPort.STOPBITS_1:
                model = aprsStopBits1.getModel();
                break;
            case SerialPort.STOPBITS_1_5:
                model = aprsStopBits15.getModel();
                break;
            case SerialPort.STOPBITS_2:
                model = aprsStopBits2.getModel();
                break;
        }

		aprsStopBitsButtonGroup.setSelected(model, true);

		aprsParityNone = new JRadioButton("None");
		aprsParityOdd = new JRadioButton("Odd");
		aprsParityEven = new JRadioButton("Even");
		aprsParityMark = new JRadioButton("Mark");
		aprsParitySpace = new JRadioButton("Space");

		aprsParityButtonGroup = new ButtonGroup();
		aprsParityButtonGroup.add(aprsParityNone);
		aprsParityButtonGroup.add(aprsParityOdd);
		aprsParityButtonGroup.add(aprsParityEven);
		aprsParityButtonGroup.add(aprsParityMark);
		aprsParityButtonGroup.add(aprsParitySpace);

		aprsParityNone.addItemListener(rbh);
		aprsParityOdd.addItemListener(rbh);
		aprsParityEven.addItemListener(rbh);
		aprsParityMark.addItemListener(rbh);
		aprsParitySpace.addItemListener(rbh);
        
        switch (aprsParity) {
            case SerialPort.PARITY_NONE:
                model = aprsParityNone.getModel();
                break;
            case SerialPort.PARITY_ODD:
                model = aprsParityOdd.getModel();
                break;
            case SerialPort.PARITY_EVEN:
                model = aprsParityEven.getModel();
                break;
            case SerialPort.PARITY_MARK:
                model = aprsParityMark.getModel();
                break;
            case SerialPort.PARITY_SPACE:
                model = aprsParitySpace.getModel();
                break;
        }
        
		aprsParityButtonGroup.setSelected(model, true);

		startAprsWithSystemCheckBox.setSelected(startAprsWithSystem);
		enableAPRSIconLabelsCheckBox.setSelected(showAPRSIconLabels);
		enableAprsTrackingCheckBox.setSelected(enableAprsTracking);

		tabbedPane.addTab(" TNC Settings ", null, aprsPanel, null);
		tabbedPane.addTab(" System Settings ", null, sysPanel, null);
		
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
	            	aprsInterface.inputData(serialInterface.readString((int) event.getNewValue()));
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
		
		serialInterface.addPropertyChangeListener(new PropertyChangeListener() {
        	@Override
        	public void propertyChange(PropertyChangeEvent event) {
	            if (APRSInterface.CRC_ERROR.equals(event.getPropertyName())) {
	            	firePropertyChange(SerialInterface.ONLINE, null, event.getNewValue());
	            }
	            if (APRSInterface.WAYPOINT.equals(event.getPropertyName())) {
	            	firePropertyChange(SerialInterface.BREAK, null, event.getNewValue());
	            }
	            if (APRSInterface.SEND_TO_SERIAL_PORT.equals(event.getPropertyName())) {
	            	serialInterface.writeString((String) event.getNewValue(), CharSet.ASCII_ALPHA.toString());
	            }
        	}
        });
	}

	private void drawGraphicalUserInterface() {
		GroupLayout aprsPanelLayout = new GroupLayout(aprsPanel);

		aprsPanel.setLayout(aprsPanelLayout);

		aprsPanelLayout.setAutoCreateGaps(true);

		aprsPanelLayout.setAutoCreateContainerGaps(true);

		aprsPanelLayout
				.setHorizontalGroup(aprsPanelLayout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(
								aprsPanelLayout
										.createSequentialGroup()
										.addGroup(
												aprsPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.LEADING)
														.addGroup(
																aprsPanelLayout
																		.createSequentialGroup()
																		.addComponent(
																				aprsDataBitsButtonGroupLabel,
																				GroupLayout.PREFERRED_SIZE,
																				60,
																				GroupLayout.PREFERRED_SIZE)
																		.addGap(5)
																		.addComponent(
																				aprsDataBits5)
																		.addComponent(
																				aprsDataBits6)
																		.addComponent(
																				aprsDataBits7)
																		.addComponent(
																				aprsDataBits8))
														.addGroup(
																aprsPanelLayout
																		.createSequentialGroup()
																		.addComponent(
																				aprsStopBitsButtonGroupLabel,
																				GroupLayout.PREFERRED_SIZE,
																				60,
																				GroupLayout.PREFERRED_SIZE)
																		.addGap(5)
																		.addComponent(
																				aprsStopBits1)
																		.addComponent(
																				aprsStopBits15)
																		.addComponent(
																				aprsStopBits2))
														.addGroup(
																aprsPanelLayout
																		.createSequentialGroup()
																		.addComponent(
																				aprsParityButtonGroupLabel,
																				GroupLayout.PREFERRED_SIZE,
																				60,
																				GroupLayout.PREFERRED_SIZE)
																		.addGap(5)
																		.addComponent(
																				aprsParityNone)
																		.addComponent(
																				aprsParityOdd)
																		.addComponent(
																				aprsParityEven)
																		.addComponent(
																				aprsParityMark)
																		.addComponent(
																				aprsParitySpace))
														.addGroup(
																aprsPanelLayout
																		.createSequentialGroup()
																		.addGroup(
																				aprsPanelLayout
																						.createParallelGroup(
																								GroupLayout.Alignment.TRAILING)
																						.addComponent(
																								selectDeviceComboBoxLabel)
																						.addComponent(
																								selectAprsComPortComboBoxLabel)
																						.addComponent(
																								selectBaudRateComboBoxLabel))
																		.addGroup(
																				aprsPanelLayout
																						.createParallelGroup(
																								GroupLayout.Alignment.LEADING)
																						.addComponent(
																								selectDeviceComboBox,
																								GroupLayout.PREFERRED_SIZE,
																								175,
																								GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								selectAprsComPortComboBox,
																								GroupLayout.PREFERRED_SIZE,
																								80,
																								GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								selectBaudRateComboBox,
																								GroupLayout.PREFERRED_SIZE,
																								80,
																								GroupLayout.PREFERRED_SIZE))))
										.addContainerGap(15, Short.MAX_VALUE)));

		aprsPanelLayout
				.setVerticalGroup(aprsPanelLayout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(
								aprsPanelLayout
										.createSequentialGroup()
										.addGroup(
												aprsPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(
																selectDeviceComboBox,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																selectDeviceComboBoxLabel))
										.addGroup(
												aprsPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(
																selectAprsComPortComboBox,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																selectAprsComPortComboBoxLabel))
										.addGroup(
												aprsPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(
																selectBaudRateComboBox,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																selectBaudRateComboBoxLabel))
										.addGroup(
												aprsPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addGap(5))
										.addGroup(
												aprsPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(
																aprsDataBitsButtonGroupLabel,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																aprsDataBits5)
														.addComponent(
																aprsDataBits6)
														.addComponent(
																aprsDataBits7)
														.addComponent(
																aprsDataBits8))
										.addGroup(
												aprsPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addGap(5))
										.addGroup(
												aprsPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(
																aprsStopBitsButtonGroupLabel,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																aprsStopBits1)
														.addComponent(
																aprsStopBits15)
														.addComponent(
																aprsStopBits2))
										.addGroup(
												aprsPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addGap(5))
										.addGroup(
												aprsPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(
																aprsParityButtonGroupLabel,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																aprsParityNone)
														.addComponent(
																aprsParityOdd)
														.addComponent(
																aprsParityEven)
														.addComponent(
																aprsParityMark)
														.addComponent(
																aprsParitySpace))
										.addContainerGap(15, Short.MAX_VALUE)));

		GroupLayout sysPanelLayout = new GroupLayout(sysPanel);
		sysPanel.setLayout(sysPanelLayout);
		sysPanelLayout.setHorizontalGroup(sysPanelLayout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addGroup(
				sysPanelLayout
						.createSequentialGroup()
						.addContainerGap(5, Short.MAX_VALUE)
						.addGroup(
								sysPanelLayout
										.createParallelGroup(
												GroupLayout.Alignment.LEADING)
										.addComponent(
												startAprsWithSystemCheckBox)
										.addComponent(
												enableAprsTrackingCheckBox)
										.addComponent(
												enableAPRSIconLabelsCheckBox))
						.addContainerGap(15, Short.MAX_VALUE)));

		sysPanelLayout.setVerticalGroup(sysPanelLayout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addGroup(
				sysPanelLayout.createSequentialGroup()
						.addContainerGap(5, Short.MAX_VALUE)
						.addComponent(startAprsWithSystemCheckBox).addGap(5)
						.addComponent(enableAprsTrackingCheckBox).addGap(5)
						.addComponent(enableAPRSIconLabelsCheckBox).addGap(5)
						.addContainerGap(15, Short.MAX_VALUE)));

		GroupLayout layout = new GroupLayout(getContentPane());

		getContentPane().setLayout(layout);

		layout.setAutoCreateGaps(true);

		layout.setAutoCreateContainerGaps(true);

		layout.setHorizontalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addGroup(
										layout.createParallelGroup(
												GroupLayout.Alignment.TRAILING)
												.addGroup(
														layout.createSequentialGroup()
																.addContainerGap()
																.addComponent(
																		tabbedPane,
																		GroupLayout.PREFERRED_SIZE,
																		GroupLayout.DEFAULT_SIZE,
																		GroupLayout.PREFERRED_SIZE))
												.addGroup(
														layout.createSequentialGroup()
																.addComponent(
																		okButton,
																		GroupLayout.PREFERRED_SIZE,
																		80,
																		GroupLayout.PREFERRED_SIZE)
																.addComponent(
																		applyButton,
																		GroupLayout.PREFERRED_SIZE,
																		80,
																		GroupLayout.PREFERRED_SIZE)
																.addComponent(
																		cancelButton,
																		GroupLayout.PREFERRED_SIZE,
																		80,
																		GroupLayout.PREFERRED_SIZE)))
								.addContainerGap(GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)));

		layout.setVerticalGroup(layout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addGroup(
				GroupLayout.Alignment.TRAILING,
				layout.createSequentialGroup()
						.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE,
								GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGroup(
								layout.createParallelGroup(
										GroupLayout.Alignment.BASELINE)
										.addComponent(okButton)
										.addComponent(applyButton)
										.addComponent(cancelButton))));

		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenSize = tk.getScreenSize();

		pack();
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setLocation((screenSize.width / 2) - (getWidth() / 2),
				(screenSize.height / 2) - (getHeight() / 2));
	}

	public void showSettingsDialog(boolean showSettingsDialog) {
		setVisible(showSettingsDialog);
	}

	private void getSettingsFromRegistry() {
		userPref = Preferences.userRoot();
		aprsDevice = userPref.getInt("AprsDevice", 0);
		switch (aprsDevice) {
			case 0:
				aprsInterface = new AprsNMEAWaypointDecoder();
				serialInterface = new ComPort();
				break;
		}

		enableAprsTracking = userPref.getBoolean("EnableAprsTracking", false);
		startAprsWithSystem = userPref.getBoolean("StartAprsWithSystem", false);
		showAPRSIconLabels = userPref.getBoolean("ShowAPRSIconLabels", false);
		aprsPortName = userPref.get("AprsComPort", "COM1");
		aprsBaudRate = userPref.getInt("AprsBaudRate", 4);
		aprsDataBits = userPref.getInt("AprsDataBits", 3);
		aprsStopBits = userPref.getInt("AprsStopBits", 0);
		aprsParity = userPref.getInt("AprsParity", 0);
		serialInterface.setPortName(aprsPortName);
		serialInterface.setBaudRate(aprsBaudRate);
		serialInterface.setDataBits(aprsDataBits);
		serialInterface.setStopBits(aprsStopBits);
		serialInterface.setParity(aprsParity);
	}

	public String getErrorMessage() {
		return serialInterface.getErrorMessage();
	}
	
	public String getPortName() {
		return aprsPortName;
	}
	
    private class RadioButtonHandler implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent ie) {
            if (ie.getSource().equals(aprsDataBits5)) {
                aprsDataBits = 5;
            } else if (ie.getSource().equals(aprsDataBits6)) {
                aprsDataBits = 6;
            } else if (ie.getSource().equals(aprsDataBits7)) {
                aprsDataBits = 7;
            } else if (ie.getSource().equals(aprsDataBits8)) {
                aprsDataBits = 8;
            } else if (ie.getSource().equals(aprsStopBits1)) {
                aprsStopBits = 1;
            } else if (ie.getSource().equals(aprsStopBits15)) {
                aprsStopBits = 3;
            } else if (ie.getSource().equals(aprsStopBits2)) {
                aprsStopBits = 2;
            } else if (ie.getSource().equals(aprsParityNone)) {
                aprsParity = 0;
            } else if (ie.getSource().equals(aprsParityOdd)) {
                aprsParity = 1;
            } else if (ie.getSource().equals(aprsParityEven)) {
                aprsParity = 2;
            } else if (ie.getSource().equals(aprsParityMark)) {
                aprsParity = 3;
            } else if (ie.getSource().equals(aprsParitySpace)) {
                aprsParity = 4;
            }
        }
    }

	private void selectDeviceComboBoxActionPerformed(ActionEvent event) {
		switch (aprsDevice) {
			case 0:
				aprsInterface = new AprsNMEAWaypointDecoder();
				serialInterface = new ComPort();
				break;
			}
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
	
	private void selectAprsComPortComboBoxActionPerformed(ActionEvent event) {
		JComboBox<?> cb = (JComboBox<?>) event.getSource();
		cb.getSelectedIndex();
		String strComPort = (String) cb.getSelectedItem();
		aprsPortName = strComPort;
		serialInterface.setPortName(aprsPortName);
	}

    private void selectBaudRateComboBoxActionPerformed(ActionEvent event) {
        JComboBox<?> cb = (JComboBox<?>) event.getSource();
        String strBaudRate = (String) cb.getSelectedItem();
        if (strBaudRate.equals("110")) {
            aprsBaudRate = 110;
        } else if (strBaudRate.equals("300")) {
            aprsBaudRate = 300;
        } else if (strBaudRate.equals("600")) {
            aprsBaudRate = 600;
        } else if (strBaudRate.equals("1200")) {
            aprsBaudRate = 1200;
        } else if (strBaudRate.equals("4800")) {
            aprsBaudRate = 4800;
        } else if (strBaudRate.equals("9600")) {
            aprsBaudRate = 9600;
        } else if (strBaudRate.equals("14400")) {
            aprsBaudRate = 14400;
        } else if (strBaudRate.equals("19200")) {
            aprsBaudRate = 19200;
        } else if (strBaudRate.equals("28800")) {
            aprsBaudRate = 28800;
        } else if (strBaudRate.equals("38400")) {
            aprsBaudRate = 38400;
        } else if (strBaudRate.equals("57600")) {
            aprsBaudRate = 57600;
        } else if (strBaudRate.equals("115200")) {
            aprsBaudRate = 115200;
        } else if (strBaudRate.equals("128000")) {
            aprsBaudRate = 128000;
        } else if (strBaudRate.equals("256000")) {
            aprsBaudRate = 256000;
        }
    }

	private void applyButtonActionListenerEvent(ActionEvent event) {
		aprsPortName = (String) selectAprsComPortComboBox.getSelectedItem();
		userPref.put("AprsComPort", aprsPortName);
		userPref.putInt("AprsDevice", aprsDevice);
		userPref.putInt("AprsBaudRate", aprsBaudRate);
		userPref.putInt("AprsDataBits", aprsDataBits);
		userPref.putInt("AprsStopBits", aprsStopBits);
		userPref.putInt("AprsParity", aprsParity);
		userPref.putBoolean("StartAprsWithSystem", startAprsWithSystem);
		userPref.putBoolean("EnableAprsTracking", enableAprsTracking);
		userPref.putBoolean("ShowAPRSIconLabels", showAPRSIconLabels);
		
		serialInterface.setPortName(aprsPortName);
		serialInterface.setBaudRate(aprsBaudRate);
		serialInterface.setDataBits(aprsDataBits);
		serialInterface.setStopBits(aprsStopBits);
		serialInterface.setParity(aprsParity);
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

	public String getAprsPortName() {
		return aprsPortName;
	}

	public int getAprsBaudRate() {
		return aprsBaudRate;
	}

	public int getAprsDataBits() {
		return aprsDataBits;
	}

	public int getAprsStopBits() {
		return aprsStopBits;
	}

	public int getAprsParity() {
		return aprsParity;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

	@Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
}