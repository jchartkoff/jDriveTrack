package com;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.GroupLayout;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
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
import java.util.prefs.Preferences;

import javax.swing.SwingConstants;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class SerialGPSComponent extends JDialog {
	private static final long serialVersionUID = -3141263739316549058L;
	
	private boolean eventRXCHAR;
	private boolean eventRXFLAG;
	private boolean eventTXEMPTY;
	private boolean eventCTS;
	private boolean eventDSR;
	private boolean eventRLSD;
	private boolean eventERR;
	private boolean eventRING;
	private boolean eventBREAK;
    private String portName;
	private int parity;
	private int stopBits;
	private int dataBits;
	private int baudRate;
	private int flowControlIn;
	private int flowControlOut;
    private int device;
    private boolean startGpsWithSystem;
    private boolean enableGpsTracking;
    private boolean centerMapOnGPSPosition;
	private boolean reportGPSCircularRedundancyCheckFailures;
    private ButtonModel dbModel;
    private ButtonModel sbModel;
    private ButtonModel paModel;
    private JPanel gpsPanel;
    private JPanel sysPanel;
    private JPanel errorPanel;
    private JTabbedPane tabbedPane;
    private JButton okButton;
    private JButton cancelButton;
    private JButton applyButton;
    private JComboBox<String> selectDeviceComboBox;
    private JComboBox<Object> selectGpsComPortComboBox;
    private JComboBox<String> selectBaudRateComboBox;
    private JComboBox<String> selectFlowControlInComboBox;
    private JComboBox<String> selectFlowControlOutComboBox;
    private JLabel selectDeviceComboBoxLabel;
    private JLabel selectGpsComPortComboBoxLabel;
    private JLabel selectBaudRateComboBoxLabel;
    private JLabel selectFlowControlInComboBoxLabel;
    private JLabel selectFlowControlOutComboBoxLabel;
    private JLabel dataBitsButtonGroupLabel;
    private JCheckBox cbStartGpsWithSystem;
    private JCheckBox cbEnableGpsTracking;
    private JCheckBox cbCenterMapOnGPSPosition;
	private JCheckBox cbReportGPSCircularRedundancyCheckFailures;
    private JCheckBox eventRXCHARCheckBox;
	private JCheckBox eventRXFLAGCheckBox;
	private JCheckBox eventTXEMPTYCheckBox;
	private JCheckBox eventCTSCheckBox;
	private JCheckBox eventDSRCheckBox;
	private JCheckBox eventRLSDCheckBox;
	private JCheckBox eventERRCheckBox;
	private JCheckBox eventRINGCheckBox;
	private JCheckBox eventBREAKCheckBox;
    private ButtonGroup dataBitsButtonGroup;
    private JRadioButton dataBits5;
    private JRadioButton dataBits6;
    private JRadioButton dataBits7;
    private JRadioButton dataBits8;
    private JLabel stopBitsButtonGroupLabel;
    private ButtonGroup stopBitsButtonGroup;
    private JRadioButton stopBits1;
    private JRadioButton stopBits15;
    private JRadioButton stopBits2;
    private JLabel parityButtonGroupLabel;
    private ButtonGroup parityButtonGroup;
    private JRadioButton parityNone;
    private JRadioButton parityOdd;
    private JRadioButton parityEven;
    private JRadioButton parityMark;
    private JRadioButton paritySpace;
    private Preferences userPref;
    private JCheckBox cbReportFramingErrors;
	private JCheckBox cbReportConfigurationErrors;
	private JCheckBox cbReportBufferOverrunErrors;
	private JCheckBox cbReportParityMismatchErrors;
	private JCheckBox cbReportDTRNotSetErrors;
	private JCheckBox cbReportRTSNotSetErrors;
	private JCheckBox cbReportEventMaskErrors;
	private JCheckBox cbReportFlowControlErrors;
	private JCheckBox cbReportPurgeFailures;
	private JCheckBox cbReportBreakInterrupts;
	private JCheckBox cbReportTransmitFailures;
	private JCheckBox cbLogSerialPortErrors;
	private boolean reportFramingErrors;
	private boolean reportConfigurationErrors;
	private boolean reportBufferOverrunErrors;
	private boolean reportParityMismatchErrors;
	private boolean reportDTRNotSetErrors;
	private boolean reportRTSNotSetErrors;
	private boolean reportEventMaskErrors;
	private boolean reportFlowControlErrors;
	private boolean reportPurgeFailures;
	private boolean reportBreakInterrupts;
	private boolean reportTransmitFailures;
	private boolean logSerialPortErrors;
	
    protected GPSInterface gpsInterface;
    protected SerialInterface serialInterface;
    
    public SerialGPSComponent() {;
        getSettingsFromRegistry();
        initializeComponents();
        drawGraphicalUserInterface();
    }

    private void initializeComponents() {

        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

        gpsPanel = new JPanel();
        sysPanel = new JPanel();
        errorPanel = new JPanel();
        tabbedPane = new JTabbedPane();

        setTitle("GPS Settings");

        selectDeviceComboBoxLabel = new JLabel("Device Type");
        selectDeviceComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        selectGpsComPortComboBoxLabel = new JLabel("GPS Port");
        selectGpsComPortComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        selectBaudRateComboBoxLabel = new JLabel("Baud Rate");
        selectBaudRateComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        selectFlowControlInComboBoxLabel = new JLabel("Flow Control In");
        selectFlowControlInComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        selectFlowControlOutComboBoxLabel = new JLabel("Flow Control Out");
        selectFlowControlOutComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        dataBitsButtonGroupLabel = new JLabel("Data Bits");
        dataBitsButtonGroupLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        stopBitsButtonGroupLabel = new JLabel("Stop Bits");
        stopBitsButtonGroupLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        parityButtonGroupLabel = new JLabel("Parity Bits");
        parityButtonGroupLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        cbStartGpsWithSystem = new JCheckBox("Start GPS With System");
        cbEnableGpsTracking = new JCheckBox("Enable GPS Tracking");
        cbCenterMapOnGPSPosition = new JCheckBox("Center Map on GPS Position");
        cbReportGPSCircularRedundancyCheckFailures = new JCheckBox("Report GPS Circular Redundancy Check Failures");
        
        eventRXCHARCheckBox = new JCheckBox("Enable Receive");
    	eventRXFLAGCheckBox = new JCheckBox("Enable Receive Flag Events");
    	eventTXEMPTYCheckBox = new JCheckBox("Enable Transmit Buffer Empty Warnings");
    	eventCTSCheckBox = new JCheckBox("Enable Clear to Send Signaling");
    	eventDSRCheckBox = new JCheckBox("Enable Data Set Ready Signaling");
    	eventRLSDCheckBox = new JCheckBox("Enable Receive Line Signal Detection");
    	eventERRCheckBox = new JCheckBox("Enable Error Warnings");
    	eventRINGCheckBox = new JCheckBox("Enable Ring Detection");
    	eventBREAKCheckBox = new JCheckBox("Enable Break Notifications");
    	
    	cbReportConfigurationErrors = new JCheckBox("Report Serial Port Configuration Errors");
        cbReportRTSNotSetErrors = new JCheckBox("Report If Ready To Send Line Fails to Set");
        cbReportDTRNotSetErrors = new JCheckBox("Report If Data Terminal Ready Line Fails to Set");
        cbReportFramingErrors = new JCheckBox("Report Framing Errors");
        cbReportParityMismatchErrors = new JCheckBox("Report Parity Mismatch Errors");
        cbReportBufferOverrunErrors = new JCheckBox("Report Buffer Overrun Errors");
        cbReportEventMaskErrors = new JCheckBox("Report Event Mask Configuration Errors");
        cbReportFlowControlErrors = new JCheckBox("Report Flow Control Configuration Errors");
        cbReportPurgeFailures = new JCheckBox("Report Purge Failures");
        cbReportBreakInterrupts = new JCheckBox("Report Break Interrupts");
        cbReportTransmitFailures = new JCheckBox("Report Transmit Failures");
        cbLogSerialPortErrors = new JCheckBox("Log Serial Port Errors");
        
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

        String[] flowControlValues = {"None", "Xon/Xoff Out", "Xon/Xoff In", "RTS/CTS In", "RTS/CTS Out"}; 
        
        String[] baudRates = {"110", "300", "600", "1200", "4800", "9600",
            "14400", "19200", "28800", "38400", "57600", "115200",
            "128000", "256000"};
        
        String[] gpsReceiverTypes = {"Generic NMEA Receiver"};

        selectDeviceComboBox = new JComboBox<String>(gpsReceiverTypes);
        selectDeviceComboBox.setSelectedIndex(device);

        selectDeviceComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                selectDeviceComboBoxActionPerformed(event);
            }
        });
        
        String[] portNames = SerialPortList.getPortNames();

        selectGpsComPortComboBox = new JComboBox<Object>();
        
        selectGpsComPortComboBox.setModel(new SortedComboBoxModel(portNames));

        for (int i = 0; i < selectGpsComPortComboBox.getItemCount(); i++) {
            if (String.valueOf(selectGpsComPortComboBox.getItemAt(i)).equals(portName)) {
                selectGpsComPortComboBox.setSelectedIndex(i);
                break;
            }
        }

        selectGpsComPortComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                selectGpsComPortComboBoxActionPerformed(event);
            }
        });

        selectBaudRateComboBox = new JComboBox<String>(baudRates);

        for (int i = 0; i < selectBaudRateComboBox.getItemCount(); i++) {
            if (String.valueOf(selectBaudRateComboBox.getItemAt(i)).equals(String.valueOf(baudRate))) {
                selectBaudRateComboBox.setSelectedIndex(i);
                break;
            }
        }

        selectBaudRateComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                selectBaudRateComboBoxActionPerformed(event);
            }
        });

        selectFlowControlInComboBox = new JComboBox<String>(flowControlValues);

        selectFlowControlInComboBox.setSelectedIndex(flowControlIn);

        selectFlowControlInComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                flowControlIn = selectFlowControlComboBoxActionPerformed(event);
            }
        });
        
        selectFlowControlOutComboBox = new JComboBox<String>(flowControlValues);

        selectFlowControlOutComboBox.setSelectedIndex(flowControlOut);

        selectFlowControlOutComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                flowControlOut = selectFlowControlComboBoxActionPerformed(event);
            }
        });

    	eventRXCHARCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
            	eventRXCHARCheckBoxActionPerformed(event);
            }
        });

    	eventRXFLAGCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
            	eventRXFLAGCheckBoxActionPerformed(event);
            }
        });
    	
    	eventTXEMPTYCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
            	eventTXEMPTYCheckBoxActionPerformed(event);
            }
        });
    	
    	eventCTSCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
            	eventCTSCheckBoxActionPerformed(event);
            }
        });
    	
    	eventDSRCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
            	eventDSRCheckBoxActionPerformed(event);
            }
        });
    	
    	eventRLSDCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
            	eventRLSDCheckBoxActionPerformed(event);
            }
        });
    	
    	eventERRCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
            	eventERRCheckBoxActionPerformed(event);
            }
        });
    	
    	eventRINGCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
            	eventRINGCheckBoxActionPerformed(event);
            }
        });
    	
    	eventBREAKCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
            	eventBREAKCheckBoxActionPerformed(event);
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
        
        cbReportEventMaskErrors.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	cbReportEventMaskErrorsActionPerformed(event);
            }
        });

        cbReportFlowControlErrors.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	cbReportFlowControlErrorsActionPerformed(event);
            }
        });

        cbReportPurgeFailures.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	cbReportPurgeFailuresActionPerformed(event);
            }
        });

        cbReportTransmitFailures.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	cbReportTransmitFailuresActionPerformed(event);
            }
        });

        cbReportBreakInterrupts.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	cbReportBreakInterruptsActionPerformed(event);
            }
        });
        
        cbReportConfigurationErrors.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	cbReportConfigurationErrorsActionPerformed(event);
            }
        });

        cbReportRTSNotSetErrors.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	cbReportRTSNotSetErrorsActionPerformed(event);
            }
        });

        cbReportDTRNotSetErrors.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	cbReportDTRNotSetErrorsActionPerformed(event);
            }
        });

        cbReportFramingErrors.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	cbReportFramingErrorsActionPerformed(event);
            }
        });

        cbReportParityMismatchErrors.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	cbReportParityMismatchErrorsActionPerformed(event);
            }
        });

        cbReportBufferOverrunErrors.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	cbReportBufferOverrunErrorsActionPerformed(event);
            }
        });

        cbLogSerialPortErrors.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	cbLogSerialPortErrorsActionPerformed(event);
            }
        });
        
        cbReportGPSCircularRedundancyCheckFailures.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	cbReportGPSCircularRedundancyCheckFailuresActionPerformed(event);
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
	            if (SerialInterface.ERROR.equals(event.getPropertyName()) && event.getNewValue() != null) {
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
	            	gpsInterface.inputData(serialInterface.readString((int) event.getNewValue()));
	            	firePropertyChange(SerialInterface.RX_CHAR, null, event.getNewValue());
	            }
	            if (SerialInterface.RX_FLAG.equals(event.getPropertyName())) {
	            	firePropertyChange(SerialInterface.RX_FLAG, null, event.getNewValue());
	            }
	            if (SerialInterface.TX_EMPTY.equals(event.getPropertyName())) {
	            	firePropertyChange(SerialInterface.TX_EMPTY, null, event.getNewValue());
	            }
	            if (SerialInterface.ADVISE_PORT_CLOSING.equals(event.getPropertyName())) {
	            	adviseSerialPortClosing(event);
	            }
        	}
        });
        
        gpsInterface.addPropertyChangeListener(new PropertyChangeListener() {
        	@Override
        	public void propertyChange(PropertyChangeEvent event) {
	            if (GPSInterface.COURSE_MADE_GOOD_MAGNETIC.equals(event.getPropertyName())) {
	            	firePropertyChange(GPSInterface.COURSE_MADE_GOOD_MAGNETIC, null, event.getNewValue());
	            }
				if (GPSInterface.COURSE_MADE_GOOD_TRUE.equals(event.getPropertyName())) {
					firePropertyChange(GPSInterface.COURSE_MADE_GOOD_TRUE, null, event.getNewValue());         	
				}
				if (GPSInterface.CRC_ERROR.equals(event.getPropertyName())) {
					firePropertyChange(GPSInterface.CRC_ERROR, null, event.getNewValue());
				}
				if (GPSInterface.FAA_MODE.equals(event.getPropertyName())) {
					firePropertyChange(GPSInterface.FAA_MODE, null, event.getNewValue());
				}	            
				if (GPSInterface.FIX_QUALITY.equals(event.getPropertyName())) {
					firePropertyChange(GPSInterface.FIX_QUALITY, null, event.getNewValue());
				}	            
				if (GPSInterface.RDF_HEADING_RELATIVE.equals(event.getPropertyName())) {
					firePropertyChange(GPSInterface.RDF_HEADING_RELATIVE, null, event.getNewValue());
				}	            
				if (GPSInterface.RDF_HEADING_TRUE.equals(event.getPropertyName())) {
					firePropertyChange(GPSInterface.RDF_HEADING_TRUE, null, event.getNewValue());
				}	            
				if (GPSInterface.VALID_FIX.equals(event.getPropertyName())) {
					firePropertyChange(GPSInterface.VALID_FIX, null, event.getNewValue());
				}	            
				if (GPSInterface.VALID_POSITION.equals(event.getPropertyName())) {
					firePropertyChange(GPSInterface.VALID_POSITION, null, event.getNewValue());
				}
				if (GPSInterface.VALID_TIME.equals(event.getPropertyName())) {
					firePropertyChange(GPSInterface.VALID_TIME, null, event.getNewValue());
				}
				if (GPSInterface.VALID_WAYPOINT.equals(event.getPropertyName())) {
					firePropertyChange(GPSInterface.VALID_WAYPOINT, null, event.getNewValue());
				}
				if (GPSInterface.NMEA_DATA.equals(event.getPropertyName())) {
					firePropertyChange(GPSInterface.NMEA_DATA, null, event.getNewValue());
				}
				if (GPSInterface.CRC_ERROR.equals(event.getPropertyName())) {
					if (reportGPSCircularRedundancyCheckFailures)
						firePropertyChange(GPSInterface.CRC_ERROR, null, event.getNewValue());
				}
        	}
        });
        
        DataBitsRadioButtonHandler dbrbh = new DataBitsRadioButtonHandler();

        dataBits5 = new JRadioButton("5");
        dataBits6 = new JRadioButton("6");
        dataBits7 = new JRadioButton("7");
        dataBits8 = new JRadioButton("8");

        dataBitsButtonGroup = new ButtonGroup();
        dataBitsButtonGroup.add(dataBits5);
        dataBitsButtonGroup.add(dataBits6);
        dataBitsButtonGroup.add(dataBits7);
        dataBitsButtonGroup.add(dataBits8);

        dataBits5.addItemListener(dbrbh);
        dataBits6.addItemListener(dbrbh);
        dataBits7.addItemListener(dbrbh);
        dataBits8.addItemListener(dbrbh);

        switch (dataBits) {
            case SerialPort.DATABITS_5:
                dbModel = dataBits5.getModel();
                break;
            case SerialPort.DATABITS_6:
                dbModel = dataBits6.getModel();
                break;
            case SerialPort.DATABITS_7:
                dbModel = dataBits7.getModel();
                break;
            case SerialPort.DATABITS_8:
                dbModel = dataBits8.getModel();
                break;
        }

        dataBitsButtonGroup.setSelected(dbModel, true);

        StopBitsRadioButtonHandler sbrbh = new StopBitsRadioButtonHandler();
        
        stopBits1 = new JRadioButton("1");
        stopBits15 = new JRadioButton("1.5");
        stopBits2 = new JRadioButton("2");

        stopBitsButtonGroup = new ButtonGroup();
        stopBitsButtonGroup.add(stopBits1);
        stopBitsButtonGroup.add(stopBits15);
        stopBitsButtonGroup.add(stopBits2);

        stopBits1.addItemListener(sbrbh);
        stopBits15.addItemListener(sbrbh);
        stopBits2.addItemListener(sbrbh);

        switch (stopBits) {
            case SerialPort.STOPBITS_1:
                sbModel = stopBits1.getModel();
                break;
            case SerialPort.STOPBITS_1_5:
                sbModel = stopBits15.getModel();
                break;
            case SerialPort.STOPBITS_2:
                sbModel = stopBits2.getModel();
                break;
        }

        stopBitsButtonGroup.setSelected(sbModel, true);

        ParityRadioButtonHandler prbh = new ParityRadioButtonHandler();
        
        parityNone = new JRadioButton("None");
        parityOdd = new JRadioButton("Odd");
        parityEven = new JRadioButton("Even");
        parityMark = new JRadioButton("Mark");
        paritySpace = new JRadioButton("Space");

        parityButtonGroup = new ButtonGroup();
        parityButtonGroup.add(parityNone);
        parityButtonGroup.add(parityOdd);
        parityButtonGroup.add(parityEven);
        parityButtonGroup.add(parityMark);
        parityButtonGroup.add(paritySpace);

        parityNone.addItemListener(prbh);
        parityOdd.addItemListener(prbh);
        parityEven.addItemListener(prbh);
        parityMark.addItemListener(prbh);
        paritySpace.addItemListener(prbh);
        
        switch (parity) {
            case SerialPort.PARITY_NONE:
                paModel = parityNone.getModel();
                break;
            case SerialPort.PARITY_ODD:
                paModel = parityOdd.getModel();
                break;
            case SerialPort.PARITY_EVEN:
                paModel = parityEven.getModel();
                break;
            case SerialPort.PARITY_MARK:
                paModel = parityMark.getModel();
                break;
            case SerialPort.PARITY_SPACE:
                paModel = paritySpace.getModel();
                break;
        }

        parityButtonGroup.setSelected(paModel, true);
        
        cbStartGpsWithSystem.setSelected(startGpsWithSystem);
        cbEnableGpsTracking.setSelected(enableGpsTracking);
        cbCenterMapOnGPSPosition.setSelected(centerMapOnGPSPosition);
        cbLogSerialPortErrors.setSelected(logSerialPortErrors);
        cbReportBreakInterrupts.setSelected(reportBreakInterrupts);
        cbReportBufferOverrunErrors.setSelected(reportBufferOverrunErrors);
        cbReportConfigurationErrors.setSelected(reportConfigurationErrors);
        cbReportDTRNotSetErrors.setSelected(reportDTRNotSetErrors);
        cbReportEventMaskErrors.setSelected(reportEventMaskErrors);
        cbReportFlowControlErrors.setSelected(reportFlowControlErrors);
        cbReportFramingErrors.setSelected(reportFramingErrors);
        cbReportGPSCircularRedundancyCheckFailures.setSelected(reportGPSCircularRedundancyCheckFailures);
        cbReportParityMismatchErrors.setSelected(reportParityMismatchErrors);
        cbReportPurgeFailures.setSelected(reportPurgeFailures);
        cbReportRTSNotSetErrors.setSelected(reportRTSNotSetErrors);
        cbReportTransmitFailures.setSelected(reportTransmitFailures);
        
        tabbedPane.addTab("GPS Settings", null, gpsPanel, null);
        tabbedPane.addTab("System Settings", null, sysPanel, null);
        tabbedPane.addTab("Error Notification Settings", null, errorPanel, null);
    }

	private void cbReportBufferOverrunErrorsActionPerformed(ActionEvent event) {
    	reportBufferOverrunErrors = cbReportBufferOverrunErrors.isSelected();
	}

	private void cbReportParityMismatchErrorsActionPerformed(ActionEvent event) {
		reportParityMismatchErrors = cbReportParityMismatchErrors.isSelected();
	}

	private void cbReportFramingErrorsActionPerformed(ActionEvent event) {
		reportFramingErrors = cbReportFramingErrors.isSelected();
	}

	private void cbReportDTRNotSetErrorsActionPerformed(ActionEvent event) {
		reportDTRNotSetErrors = cbReportDTRNotSetErrors.isSelected();
	}

	private void cbReportRTSNotSetErrorsActionPerformed(ActionEvent event) {
		reportRTSNotSetErrors = cbReportRTSNotSetErrors.isSelected();
	}

	private void cbReportConfigurationErrorsActionPerformed(ActionEvent event) {
		reportConfigurationErrors = cbReportConfigurationErrors.isSelected();
	}

	private void cbReportBreakInterruptsActionPerformed(ActionEvent event) {
		reportBreakInterrupts = cbReportBreakInterrupts.isSelected();
	}

	private void cbReportTransmitFailuresActionPerformed(ActionEvent event) {
		reportTransmitFailures = cbReportTransmitFailures.isSelected();
	}

	private void cbReportPurgeFailuresActionPerformed(ActionEvent event) {
		reportPurgeFailures = cbReportPurgeFailures.isSelected();
	}

	private void cbReportFlowControlErrorsActionPerformed(ActionEvent event) {
		reportFlowControlErrors = cbReportFlowControlErrors.isSelected();
	}

	private void cbReportEventMaskErrorsActionPerformed(ActionEvent event) {
		reportEventMaskErrors = cbReportEventMaskErrors.isSelected();
	}

	private void cbLogSerialPortErrorsActionPerformed(ActionEvent event) {
		logSerialPortErrors = cbLogSerialPortErrors.isSelected();
	}
	
	private void cbReportGPSCircularRedundancyCheckFailuresActionPerformed(ActionEvent event) {
		reportGPSCircularRedundancyCheckFailures = cbReportGPSCircularRedundancyCheckFailures.isSelected();
	}
	
	private void adviseSerialPortClosing(PropertyChangeEvent event) {
		gpsInterface.zeroize();
	}
	
    private void eventBREAKCheckBoxActionPerformed(ActionEvent event) {
    	eventBREAK = eventBREAKCheckBox.isSelected();
	}

	private void eventRINGCheckBoxActionPerformed(ActionEvent event) {
		eventRING = eventRINGCheckBox.isSelected();
	}

	private void eventERRCheckBoxActionPerformed(ActionEvent event) {
		eventERR = eventERRCheckBox.isSelected();
	}

	private void eventRLSDCheckBoxActionPerformed(ActionEvent event) {
		eventRLSD = eventRLSDCheckBox.isSelected();
	}

	private void eventDSRCheckBoxActionPerformed(ActionEvent event) {
		eventDSR = eventDSRCheckBox.isSelected();
	}

	private void eventCTSCheckBoxActionPerformed(ActionEvent event) {
		eventCTS = eventCTSCheckBox.isSelected();
	}

	private void eventTXEMPTYCheckBoxActionPerformed(ActionEvent event) {
		eventTXEMPTY = eventTXEMPTYCheckBox.isSelected();
	}

	private void eventRXFLAGCheckBoxActionPerformed(ActionEvent event) {
		eventRXFLAG = eventRXFLAGCheckBox.isSelected();
	}

	private void eventRXCHARCheckBoxActionPerformed(ActionEvent event) {
		eventRXCHAR = eventRXCHARCheckBox.isSelected();
	}

    private void selectDeviceComboBoxActionPerformed(ActionEvent event) {
        switch (device) {
            case 0:
                gpsInterface = new SerialNmeaGPSReceiver();
                serialInterface = new ComPort();
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

    private void selectGpsComPortComboBoxActionPerformed(ActionEvent event) {
    	try {
	        JComboBox<?> cb = (JComboBox<?>) event.getSource();
	        cb.getSelectedIndex();
	        portName = (String) cb.getSelectedItem();
	        serialInterface.setOnline(portName);
    	} catch (SerialPortException ex) {
    		ex.printStackTrace();
    	}
    }

    private void selectBaudRateComboBoxActionPerformed(ActionEvent event) {
        JComboBox<?> cb = (JComboBox<?>) event.getSource();
        String strBaudRate = (String) cb.getSelectedItem();
        baudRate = Integer.parseInt(strBaudRate);
    }

    private int selectFlowControlComboBoxActionPerformed(ActionEvent event) {
        JComboBox<?> cb = (JComboBox<?>) event.getSource();
        String flowControl = (String) cb.getSelectedItem();
    	if (flowControl.equals("None")) return SerialPort.FLOWCONTROL_NONE;
    	if (flowControl.equals("Xon/Xoff Out")) return SerialPort.FLOWCONTROL_XONXOFF_OUT;
    	if (flowControl.equals("Xon/Xoff In")) return SerialPort.FLOWCONTROL_XONXOFF_IN;
    	if (flowControl.equals("RTS/CTS In")) return SerialPort.FLOWCONTROL_RTSCTS_IN;
    	if (flowControl.equals("RTS/CTS Out")) return SerialPort.FLOWCONTROL_RTSCTS_OUT;
    	return SerialPort.FLOWCONTROL_NONE;
    }
	
	private void drawGraphicalUserInterface() {
        GroupLayout gpsPanelLayout = new GroupLayout(gpsPanel);
        gpsPanel.setLayout(gpsPanelLayout);
        gpsPanelLayout.setAutoCreateGaps(true);
        gpsPanelLayout.setAutoCreateContainerGaps(true);

        gpsPanelLayout.setHorizontalGroup(
        	gpsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            	.addGroup(gpsPanelLayout.createSequentialGroup()
                .addGroup(gpsPanelLayout.createParallelGroup(
                	GroupLayout.Alignment.LEADING).addGroup(gpsPanelLayout
                .createSequentialGroup()
                .addComponent(
                dataBitsButtonGroupLabel,
                GroupLayout.PREFERRED_SIZE,
                60,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                dataBits5)
                .addComponent(
                dataBits6)
                .addComponent(
                dataBits7)
                .addComponent(
                dataBits8))
                .addGroup(
                gpsPanelLayout
                .createSequentialGroup()
                .addComponent(
                stopBitsButtonGroupLabel,
                GroupLayout.PREFERRED_SIZE,
                60,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                stopBits1)
                .addComponent(
                stopBits15)
                .addComponent(
                stopBits2))
                .addGroup(
                gpsPanelLayout
                .createSequentialGroup()
                .addComponent(
                parityButtonGroupLabel,
                GroupLayout.PREFERRED_SIZE,
                60,
                GroupLayout.PREFERRED_SIZE)
                .addGap(5)
                .addComponent(
                parityNone)
                .addComponent(
                parityOdd)
                .addComponent(
                parityEven)
                .addComponent(
                parityMark)
                .addComponent(
                paritySpace))
                .addGroup(
                gpsPanelLayout
                .createSequentialGroup()
                .addGroup(
                gpsPanelLayout
                .createParallelGroup(
                GroupLayout.Alignment.TRAILING)
                .addComponent(
                selectDeviceComboBoxLabel)
                .addComponent(
                selectGpsComPortComboBoxLabel)
                .addComponent(
                selectBaudRateComboBoxLabel))
                .addGroup(
                gpsPanelLayout
                .createParallelGroup(
                GroupLayout.Alignment.LEADING)
                .addComponent(
                selectDeviceComboBox,
                GroupLayout.PREFERRED_SIZE,
                175,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                selectGpsComPortComboBox,
                GroupLayout.PREFERRED_SIZE,
                80,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                selectBaudRateComboBox,
                GroupLayout.PREFERRED_SIZE,
                80,
                GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(15, Short.MAX_VALUE)));

        gpsPanelLayout
                .setVerticalGroup(gpsPanelLayout
                .createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                gpsPanelLayout
                .createSequentialGroup()
                .addGroup(
                gpsPanelLayout
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
                gpsPanelLayout
                .createParallelGroup(
                GroupLayout.Alignment.BASELINE)
                .addComponent(
                selectGpsComPortComboBox,
                GroupLayout.PREFERRED_SIZE,
                GroupLayout.DEFAULT_SIZE,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                selectGpsComPortComboBoxLabel))
                .addGroup(
                gpsPanelLayout
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
                gpsPanelLayout
                .createParallelGroup(
                GroupLayout.Alignment.BASELINE)
                .addGap(5))
                .addGroup(
                gpsPanelLayout
                .createParallelGroup(
                GroupLayout.Alignment.BASELINE)
                .addComponent(
                dataBitsButtonGroupLabel,
                GroupLayout.PREFERRED_SIZE,
                GroupLayout.DEFAULT_SIZE,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                dataBits5)
                .addComponent(
                dataBits6)
                .addComponent(
                dataBits7)
                .addComponent(
                dataBits8))
                .addGroup(
                gpsPanelLayout
                .createParallelGroup(
                GroupLayout.Alignment.BASELINE)
                .addGap(5))
                .addGroup(
                gpsPanelLayout
                .createParallelGroup(
                GroupLayout.Alignment.BASELINE)
                .addComponent(
                stopBitsButtonGroupLabel,
                GroupLayout.PREFERRED_SIZE,
                GroupLayout.DEFAULT_SIZE,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                stopBits1)
                .addComponent(
                stopBits15)
                .addComponent(
                stopBits2))
                .addGroup(
                gpsPanelLayout
                .createParallelGroup(
                GroupLayout.Alignment.BASELINE)
                .addGap(5))
                .addGroup(
                gpsPanelLayout
                .createParallelGroup(
                GroupLayout.Alignment.BASELINE)
                .addComponent(
                parityButtonGroupLabel,
                GroupLayout.PREFERRED_SIZE,
                GroupLayout.DEFAULT_SIZE,
                GroupLayout.PREFERRED_SIZE)
                .addComponent(
                parityNone)
                .addComponent(
                parityOdd)
                .addComponent(
                parityEven)
                .addComponent(
                parityMark)
                .addComponent(
                paritySpace))
                .addContainerGap(15, Short.MAX_VALUE)));

        GroupLayout sysPanelLayout = new GroupLayout(sysPanel);
        sysPanel.setLayout(sysPanelLayout);
        sysPanelLayout.setAutoCreateGaps(true);
        sysPanelLayout.setAutoCreateContainerGaps(true);

        sysPanelLayout.setHorizontalGroup(
            sysPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(sysPanelLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(sysPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(cbEnableGpsTracking)
                    .addComponent(cbCenterMapOnGPSPosition)
                    .addComponent(cbStartGpsWithSystem)
                    .addComponent(cbReportGPSCircularRedundancyCheckFailures))
                .addContainerGap(215, Short.MAX_VALUE)));
        
        sysPanelLayout.setVerticalGroup(
            sysPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(sysPanelLayout.createSequentialGroup()
                .addGap(59, 59, 59)
                .addComponent(cbEnableGpsTracking)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbCenterMapOnGPSPosition)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbStartGpsWithSystem)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbReportGPSCircularRedundancyCheckFailures)
                .addContainerGap(82, Short.MAX_VALUE)));

        GroupLayout errorReportLayout = new GroupLayout(errorPanel);
        errorPanel.setLayout(errorReportLayout);
        errorReportLayout.setAutoCreateGaps(true);
        errorReportLayout.setAutoCreateContainerGaps(true);
        
        errorReportLayout.setHorizontalGroup(
        		errorReportLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(errorReportLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(errorReportLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(cbReportFramingErrors)
                    .addComponent(cbReportConfigurationErrors)
                    .addComponent(cbReportBufferOverrunErrors)
                    .addComponent(cbReportParityMismatchErrors)
                    .addComponent(cbReportDTRNotSetErrors)
                    .addComponent(cbReportRTSNotSetErrors)
                    .addComponent(cbReportEventMaskErrors))
                .addGap(18, 18, 18)
                .addGroup(errorReportLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(cbReportFlowControlErrors)
                    .addComponent(cbReportPurgeFailures)
                    .addComponent(cbReportBreakInterrupts)
                    .addComponent(cbReportTransmitFailures)
                    .addComponent(cbLogSerialPortErrors))
                .addContainerGap(44, Short.MAX_VALUE)));
        
        errorReportLayout.setVerticalGroup(
            errorReportLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(errorReportLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(errorReportLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(errorReportLayout.createSequentialGroup()
                        .addComponent(cbReportFlowControlErrors)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbReportPurgeFailures)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbReportBreakInterrupts)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbReportTransmitFailures)
                        .addGap(23, 23, 23)
                        .addGap(23, 23, 23)
                        .addComponent(cbLogSerialPortErrors))
                    .addGroup(errorReportLayout.createSequentialGroup()	
                    	.addComponent(cbReportFramingErrors)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbReportConfigurationErrors)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbReportBufferOverrunErrors)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbReportParityMismatchErrors)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbReportDTRNotSetErrors)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbReportRTSNotSetErrors)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbReportEventMaskErrors)))
                .addContainerGap(39, Short.MAX_VALUE)));
        
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
        setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));
    }

    public void showSettingsDialog(boolean newShowSettingsDialog) {
        setVisible(newShowSettingsDialog);
    }

    private void testAssignedComPort(final String portName) {
    	if (!ComPort.isComPortValid(portName)) {
    		SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(SerialGPSComponent.this),
                    	"Error opening " + portName + "\n\n" +
                    	"The configured serial port " +
                    	"is not available at this time.\n" +
                    	"Please configure the GPS device...",		
                    	"Serial Port Not Available", JOptionPane.ERROR_MESSAGE);
                }
            });
    	} else {
    		this.portName = portName;
    	}
    }
    
    public String getPortName() {
    	if (ComPort.isComPortValid(portName)) return portName;
    	else return null;
    }
    
    private void transferSerialPortSettingsFromRegistryToMemory() {
    	testAssignedComPort(userPref.get("GPSReceiverComPort", "COM3"));
    	baudRate = userPref.getInt("GPSReceiverBaudRate", SerialPort.BAUDRATE_4800);
        dataBits = userPref.getInt("GPSReceiverDataBits", SerialPort.DATABITS_8);
        stopBits = userPref.getInt("GPSReceiverStopBits", SerialPort.STOPBITS_1);
        parity = userPref.getInt("GPSReceiverParity", SerialPort.PARITY_NONE);
        flowControlIn = userPref.getInt("GPSFlowControlIn", SerialPort.FLOWCONTROL_NONE);
        flowControlOut = userPref.getInt("GPSFlowControlOut", SerialPort.FLOWCONTROL_NONE);
        eventRXCHAR = userPref.getBoolean("GPSEventRXCHAR", true);
    	eventRXFLAG = userPref.getBoolean("GPSEventRXFLAG", true);
    	eventTXEMPTY = userPref.getBoolean("GPSEventTXEMPTY", true);
    	eventCTS = userPref.getBoolean("GPSEventCTS", true);
    	eventDSR = userPref.getBoolean("GPSEventDSR", true);
    	eventRLSD = userPref.getBoolean("GPSEventRLSD", true);
    	eventERR = userPref.getBoolean("GPSEventERR", true);
    	eventRING = userPref.getBoolean("GPSEventRING", true);
    	eventBREAK = userPref.getBoolean("GPSEventBREAK", true);
    	reportConfigurationErrors = userPref.getBoolean("GPSReportConfigurationErrors", true);
    	reportRTSNotSetErrors = userPref.getBoolean("GPSReportRTSErrors", true);
    	reportDTRNotSetErrors = userPref.getBoolean("GPSReportDTRErrors", true);
    	reportFramingErrors = userPref.getBoolean("GPSReportFramingErrors", true);
    	reportBufferOverrunErrors = userPref.getBoolean("GPSReportBufferOverrunErrors", true);
    	reportParityMismatchErrors = userPref.getBoolean("GPSReportParityMismatchErrors", true);
    	reportBreakInterrupts = userPref.getBoolean("GPSReportBreakInterrupts", true);
        reportEventMaskErrors = userPref.getBoolean("GPSReportEventMaskErrors", true);
        reportFlowControlErrors = userPref.getBoolean("GPSReportFlowControlErrors", true);
        reportPurgeFailures = userPref.getBoolean("GPSReportPurgeFailures", true);
        reportTransmitFailures = userPref.getBoolean("GPSReportTransmitFailures", true);
        logSerialPortErrors = userPref.getBoolean("GPSLogSerialPortErrors", true);
    }

    private void getSettingsFromRegistry() {
        userPref = Preferences.userRoot();
        device = userPref.getInt("GpsDevice", 0);

        switch (device) {
            case 0:
                gpsInterface = new SerialNmeaGPSReceiver();
                serialInterface = new ComPort();
                transferSerialPortSettingsFromRegistryToMemory();
                sendSerialPortSettingsFromMemoryToDevice();
                break;
        }
        
        enableGpsTracking = userPref.getBoolean("EnableGpsTracking", false);
        startGpsWithSystem = userPref.getBoolean("StartGpsWithSystem", false);
        centerMapOnGPSPosition = userPref.getBoolean("CenterMapOnGPSPosition", false);
        reportGPSCircularRedundancyCheckFailures = userPref.getBoolean("ReportGPSCircularRedundancyCheckFailures", true);
    }

    private void sendSerialPortSettingsFromMemoryToDevice() {
    	serialInterface.setPortName(portName);
        serialInterface.setBaudRate(baudRate);
        serialInterface.setDataBits(dataBits);
        serialInterface.setStopBits(stopBits);
        serialInterface.setParity(parity);
        serialInterface.setFlowControlIn(flowControlIn);
        serialInterface.setFlowControlOut(flowControlOut);
        serialInterface.enableBreakEvent(eventBREAK);
        serialInterface.enableCTSEvent(eventCTS);
        serialInterface.enableDSREvent(eventDSR);
        serialInterface.enableErrorEvent(eventERR);
        serialInterface.enableReceive(eventRXCHAR);
        serialInterface.enableRingEvent(eventRING);
        serialInterface.enableRLSDEvent(eventRLSD);
        serialInterface.enableRxFlagEvent(eventRXFLAG);
        serialInterface.enableTxEmptyEvent(eventTXEMPTY);
        serialInterface.reportBreakInterrupts(reportBreakInterrupts);
        serialInterface.reportBufferOverrunErrors(reportBufferOverrunErrors);
        serialInterface.reportConfigurationErrors(reportConfigurationErrors);
        serialInterface.reportDataTerminalReadyLineNotSetErrors(reportDTRNotSetErrors);
        serialInterface.reportEventMaskErrors(reportEventMaskErrors);
        serialInterface.reportFlowControlErrors(reportFlowControlErrors);
        serialInterface.reportParityMismatchErrors(reportParityMismatchErrors);
        serialInterface.reportPurgeFailures(reportPurgeFailures);
        serialInterface.reportFramingErrors(reportFramingErrors);
        serialInterface.reportReadyToSendLineNotSetErrors(reportRTSNotSetErrors);
        serialInterface.reportTransmitFailures(reportTransmitFailures); 
        serialInterface.logSerialPortErrors(logSerialPortErrors);
    }
    
    private class DataBitsRadioButtonHandler implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent ie) {
            if (ie.getSource().equals(dataBits5)) dataBits = SerialPort.DATABITS_5;
            if (ie.getSource().equals(dataBits6)) dataBits = SerialPort.DATABITS_6;
            if (ie.getSource().equals(dataBits7)) dataBits = SerialPort.DATABITS_7;
            if (ie.getSource().equals(dataBits8)) dataBits = SerialPort.DATABITS_8;
        }
    }

    private class StopBitsRadioButtonHandler implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent ie) {
            if (ie.getSource().equals(stopBits1)) stopBits = SerialPort.STOPBITS_1;
            if (ie.getSource().equals(stopBits15)) stopBits = SerialPort.STOPBITS_1_5;
            if (ie.getSource().equals(stopBits2)) stopBits = SerialPort.STOPBITS_2;
        }
    }
    
    private class ParityRadioButtonHandler implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent ie) {
            if (ie.getSource().equals(parityNone)) parity = SerialPort.PARITY_NONE;
            if (ie.getSource().equals(parityOdd)) parity = SerialPort.PARITY_ODD;
            if (ie.getSource().equals(parityEven)) parity = SerialPort.PARITY_EVEN;
            if (ie.getSource().equals(parityMark)) parity = SerialPort.PARITY_MARK;
            if (ie.getSource().equals(paritySpace)) parity = SerialPort.PARITY_SPACE;
        }
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

    public String getErrorMessage() {
    	return serialInterface.getErrorMessage();
    }
    
    private void applyButtonActionListenerEvent(ActionEvent event) {
    	if (portName == null) return;
        userPref.put("GPSReceiverComPort", portName);
        userPref.putInt("GPSDevice", device);
        userPref.putInt("GPSReceiverBaudRate", baudRate);
        userPref.putInt("GPSReceiverDataBits", dataBits);
        userPref.putInt("GPSReceiverStopBits", stopBits);
        userPref.putInt("GPSReceiverParity", parity);
        userPref.putInt("GPSReceiverFlowControlIn", flowControlIn);
        userPref.putInt("GPSReceiverFlowControlOut", flowControlOut);
        
        userPref.putBoolean("GPSEventBREAK", eventBREAK);
        userPref.putBoolean("GPSEventCTS", eventCTS);
        userPref.putBoolean("GPSEventDSR", eventDSR);
        userPref.putBoolean("GPSEventERR", eventERR);
        userPref.putBoolean("GPSEventRXCHAR", eventRXCHAR);
        userPref.putBoolean("GPSEventRING", eventRING);
        userPref.putBoolean("GPSEventRLSD", eventRLSD);
        userPref.putBoolean("GPSEventRXFLAG", eventRXFLAG);
        userPref.putBoolean("GPSEventTXEMPTY", eventTXEMPTY);
        
        userPref.putBoolean("StartGpsWithSystem", startGpsWithSystem);
        userPref.putBoolean("EnableGpsTracking", enableGpsTracking);
        userPref.putBoolean("CenterMapOnGPSPosition", centerMapOnGPSPosition);
        userPref.putBoolean("ReportGPSCircularRedundancyCheckFailures", reportGPSCircularRedundancyCheckFailures);
        
        userPref.putBoolean("GPSReportConfigurationErrors", reportConfigurationErrors);
    	userPref.putBoolean("GPSReportRTSErrors", reportRTSNotSetErrors);
    	userPref.putBoolean("GPSReportDTRErrors", reportDTRNotSetErrors);
    	userPref.putBoolean("GPSReportFramingErrors", reportFramingErrors);
    	userPref.putBoolean("GPSReportBufferOverrunErrors", reportBufferOverrunErrors);
    	userPref.putBoolean("GPSReportParityMismatchErrors", reportParityMismatchErrors);
    	userPref.putBoolean("GPSReportBreakInterrupts", reportBreakInterrupts);
        userPref.putBoolean("GPSReportEventMaskErrors", reportEventMaskErrors);
        userPref.putBoolean("GPSReportFlowControlErrors", reportFlowControlErrors);
        userPref.putBoolean("GPSReportPurgeFailures", reportPurgeFailures);
        userPref.putBoolean("GPSReportTransmitFailures", reportTransmitFailures);
        userPref.putBoolean("GPSLogSerialPortErrors", logSerialPortErrors);

        sendSerialPortSettingsFromMemoryToDevice();
    }

}