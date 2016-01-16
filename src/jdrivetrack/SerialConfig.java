package jdrivetrack;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import interfaces.SerialInterface;
import jssc.SerialPort;
import jssc.SerialPortList;

public class SerialConfig {
	private JComboBox<?> selectComPortComboBox;
	private JComboBox<?> selectBaudRateComboBox;
	private JComboBox<?> selectFlowControlInComboBox;
	private JComboBox<?> selectFlowControlOutComboBox;
	private DefaultComboBoxModel<String> selectBaudRateComboBoxModel;
	private JLabel selectComPortComboBoxLabel;
	private JLabel selectBaudRateComboBoxLabel;
	private JLabel selectDataBitsButtonGroupLabel;
	private JLabel selectStopBitsButtonGroupLabel;
	private JLabel selectParityButtonGroupLabel;
	private JLabel selectFlowControlInComboBoxLabel;
	private JLabel selectFlowControlOutComboBoxLabel;
	private ButtonModel dataBitsModel;
	private ButtonModel stopBitsModel;
	private ButtonModel parityModel;
	private ButtonGroup dataBitsButtonGroup;
	private ButtonGroup stopBitsButtonGroup;
	private ButtonGroup parityButtonGroup;
	private JRadioButton dataBits5;
	private JRadioButton dataBits6;
	private JRadioButton dataBits7;
	private JRadioButton dataBits8;
	private JRadioButton stopBits1;
	private JRadioButton stopBits15;
	private JRadioButton stopBits2;
	private JRadioButton parityNone;
	private JRadioButton parityOdd;
	private JRadioButton parityEven;
	private JRadioButton parityMark;
	private JRadioButton paritySpace;
	private int parity;
	private int stopBits;
	private int dataBits;
	private int baudRate;
	private int portNumber;
	private int flowControl;
	private int flowControlIn;
	private int flowControlOut;
	private String portName;
	private String deviceType;
	private boolean dtr;
	private boolean rts;
	private boolean eventRXCHAR;
	private boolean eventRXFLAG;
	private boolean eventTXEMPTY;
	private boolean eventCTS;
	private boolean eventDSR;
	private boolean eventRLSD;
	private boolean eventERR;
	private boolean eventRING;
	private boolean eventBREAK;
	private JCheckBox enableDTRCheckBox;
	private JCheckBox enableRTSCheckBox;
    private JCheckBox eventRXCHARCheckBox;
	private JCheckBox eventRXFLAGCheckBox;
	private JCheckBox eventTXEMPTYCheckBox;
	private JCheckBox eventCTSCheckBox;
	private JCheckBox eventDSRCheckBox;
	private JCheckBox eventRLSDCheckBox;
	private JCheckBox eventERRCheckBox;
	private JCheckBox eventRINGCheckBox;
	private JCheckBox eventBREAKCheckBox;
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
	
	private Preferences systemPrefs = Preferences.systemRoot().node("jdrivetrack/prefs/SerialConfig");

	private SerialInterface serialInterface = new ComPort();
	
	private SerialParameterSet defaultSerialParameterSet;
	
	public SerialConfig(String deviceID, String deviceType, SerialParameterSet defaultSerialParameterSet) {
		this.defaultSerialParameterSet = defaultSerialParameterSet;
		this.deviceType = deviceType;
		
		transferSerialPortSettingsFromRegistryToMemory(deviceID);
		initializeComponents();
		enableSerialInterfaceComponents(defaultSerialParameterSet.isDeviceAssignedParametersFixed());
		sendSerialPortSettingsFromMemoryToDevice();
	}
	
	public JPanel getEventManagementGui() {
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
	
		panel.setLayout(layout);
	
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(eventRXCHARCheckBox)
                    .addComponent(eventRXFLAGCheckBox)
                    .addComponent(eventBREAKCheckBox)
                    .addComponent(eventCTSCheckBox)
                    .addComponent(eventDSRCheckBox))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(eventRINGCheckBox)
                    .addComponent(eventRLSDCheckBox)
                    .addComponent(eventTXEMPTYCheckBox)
                    .addComponent(eventERRCheckBox))
                .addContainerGap(GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE)));
        
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(eventRINGCheckBox)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventRLSDCheckBox)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventTXEMPTYCheckBox)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventERRCheckBox))
                        .addGap(64, 64, 64)
                    .addGroup(layout.createSequentialGroup()	
                    	.addComponent(eventRXCHARCheckBox)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventRXFLAGCheckBox)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventBREAKCheckBox)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventCTSCheckBox)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventDSRCheckBox)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE)));
        return panel;
	}
	
	public JPanel getErrorNotificationGui() {
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
	
		panel.setLayout(layout);
	
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(cbReportFramingErrors)
                    .addComponent(cbReportConfigurationErrors)
                    .addComponent(cbReportBufferOverrunErrors)
                    .addComponent(cbReportParityMismatchErrors)
                    .addComponent(cbReportDTRNotSetErrors)
                    .addComponent(cbReportRTSNotSetErrors)
                    .addComponent(cbReportEventMaskErrors))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(cbReportFlowControlErrors)
                    .addComponent(cbReportPurgeFailures)
                    .addComponent(cbReportBreakInterrupts)
                    .addComponent(cbReportTransmitFailures)
                    .addComponent(cbLogSerialPortErrors))
                .addContainerGap(GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE)));
        
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cbReportFlowControlErrors)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbReportPurgeFailures)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbReportBreakInterrupts)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbReportTransmitFailures)
                        .addGap(64, 64, 64)
                        .addComponent(cbLogSerialPortErrors))
                    .addGroup(layout.createSequentialGroup()	
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
                .addContainerGap(GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE)));
        return panel;
	}
	
	public JPanel getSerialConfigGui() {
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
	
		panel.setLayout(layout);
	
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
	
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(layout.createSequentialGroup()
						.addGap(20,20,20)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
							.addComponent(selectComPortComboBoxLabel)
							.addComponent(selectBaudRateComboBoxLabel))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(selectComPortComboBox,120,120,120)
							.addComponent(selectBaudRateComboBox,90,90,90))
						.addGap(20,20,20)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
							.addGroup(layout.createSequentialGroup()
								.addComponent(selectFlowControlOutComboBoxLabel)
                                .addComponent(selectFlowControlOutComboBox,100,100,100))
                            .addGroup(layout.createSequentialGroup()
                        		.addComponent(selectFlowControlInComboBoxLabel)
                                .addComponent(selectFlowControlInComboBox,100,100,100))))
					.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(selectDataBitsButtonGroupLabel,80,80,80)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(dataBits5)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(dataBits6)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(dataBits7)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(dataBits8))
					.addGroup(layout.createSequentialGroup()
						.addContainerGap()	
						.addComponent(selectStopBitsButtonGroupLabel,80,80,80)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(stopBits1)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(stopBits15)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(stopBits2))
					.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(selectParityButtonGroupLabel,80,80,80)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(parityNone)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(parityOdd)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(parityEven)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(parityMark)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(paritySpace)))
			.addContainerGap(GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE)));
	
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(30,30,30)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(selectComPortComboBox,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectComPortComboBoxLabel)
                    .addComponent(selectFlowControlInComboBoxLabel)
                    .addComponent(selectFlowControlInComboBox,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(selectBaudRateComboBox,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectBaudRateComboBoxLabel)
                    .addComponent(selectFlowControlOutComboBoxLabel)
                    .addComponent(selectFlowControlOutComboBox,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE))
                .addGap(5,5,5)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)		
					.addComponent(selectDataBitsButtonGroupLabel,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
					.addComponent(dataBits5)
					.addComponent(dataBits6)
					.addComponent(dataBits7)
					.addComponent(dataBits8))	
				.addGap(5,5,5)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)	
					.addComponent(selectStopBitsButtonGroupLabel)
					.addComponent(stopBits1)
					.addComponent(stopBits15)
					.addComponent(stopBits2))	
				.addGap(5,5,5)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)	
					.addComponent(selectParityButtonGroupLabel)
					.addComponent(parityNone)
					.addComponent(parityOdd)
					.addComponent(parityEven)
					.addComponent(parityMark)
					.addComponent(paritySpace))
				.addContainerGap(GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE)));
		return panel;
	}

	private void enableSerialInterfaceComponents(boolean disabled) {
		selectBaudRateComboBox.setEnabled(!disabled);
		selectFlowControlInComboBox.setEnabled(!disabled);
		selectFlowControlOutComboBox.setEnabled(!disabled);
		dataBits5.setEnabled(!disabled);
		dataBits6.setEnabled(!disabled);
		dataBits7.setEnabled(!disabled);
		dataBits8.setEnabled(!disabled);
		stopBits1.setEnabled(!disabled);
		stopBits15.setEnabled(!disabled);
		stopBits2.setEnabled(!disabled);
		parityNone.setEnabled(!disabled);
		parityOdd.setEnabled(!disabled);
		parityEven.setEnabled(!disabled);
		parityMark.setEnabled(!disabled);
		paritySpace.setEnabled(!disabled);
		enableDTRCheckBox.setEnabled(!disabled);
        enableRTSCheckBox.setEnabled(!disabled);
    	eventRXFLAGCheckBox.setEnabled(!disabled);
    	eventTXEMPTYCheckBox.setEnabled(!disabled);
    	eventCTSCheckBox.setEnabled(!disabled);
    	eventDSRCheckBox.setEnabled(!disabled);
    	eventRLSDCheckBox.setEnabled(!disabled);
    	eventERRCheckBox.setEnabled(!disabled);
    	eventRINGCheckBox.setEnabled(!disabled);
    	eventBREAKCheckBox.setEnabled(!disabled);
	}
	
	private void initializeComponents() {
		selectBaudRateComboBoxModel = new DefaultComboBoxModel<String>();
		selectBaudRateComboBox = new JComboBox<String>(selectBaudRateComboBoxModel);
		
		selectComPortComboBoxLabel = new JLabel("Comm Port");
		selectComPortComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		selectBaudRateComboBoxLabel = new JLabel("Baud Rate");
		selectBaudRateComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		
		selectDataBitsButtonGroupLabel = new JLabel("Data Bits");
		selectDataBitsButtonGroupLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		selectStopBitsButtonGroupLabel = new JLabel("Stop Bits");
		selectStopBitsButtonGroupLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		selectParityButtonGroupLabel = new JLabel("Parity Bits");
		selectParityButtonGroupLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		
        selectFlowControlInComboBoxLabel = new JLabel("Flow Control In");
        selectFlowControlInComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        selectFlowControlOutComboBoxLabel = new JLabel("Flow Control Out");
        selectFlowControlOutComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        enableDTRCheckBox = new JCheckBox("Enable Data Terminal Ready (DTR) Line");
        enableRTSCheckBox = new JCheckBox("Enable Ready To Send (RTS) Line");
        eventRXCHARCheckBox = new JCheckBox("Enable Receive");
    	eventRXFLAGCheckBox = new JCheckBox("Enable Receive Flag Events");
    	eventTXEMPTYCheckBox = new JCheckBox("Enable Transmit Buffer Empty Detection");
    	eventCTSCheckBox = new JCheckBox("Enable Clear to Send Signaling");
    	eventDSRCheckBox = new JCheckBox("Enable Data Set Ready Signaling");
    	eventRLSDCheckBox = new JCheckBox("Enable Receive Line Signal Detection");
    	eventERRCheckBox = new JCheckBox("Enable Error Warnings");
    	eventRINGCheckBox = new JCheckBox("Enable Ring Detection");
    	eventBREAKCheckBox = new JCheckBox("Enable Break Detection");
    	
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
        
		String[] portNames = SerialPortList.getPortNames();

		selectComPortComboBox = new JComboBox<String>(portNames);
		
		String portName = Utility.getPortNameString(portNumber);
		
		for (int i = 0; i < selectComPortComboBox.getItemCount(); i++) {
			selectComPortComboBox.setSelectedIndex(i);
			if (selectComPortComboBox.getItemAt(i).toString().equals(portName)) break;
		}
	
		selectComPortComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					selectComPortComboBoxActionPerformed(event);
				}
			}
		});
		
		setAvailableBaudRates(defaultSerialParameterSet.getValidBaudRates());
		
		for (int i = 0; i < selectBaudRateComboBox.getItemCount(); i++) {
			selectBaudRateComboBox.setSelectedIndex(i);
			if (String.valueOf(baudRate).equals(selectBaudRateComboBox.getItemAt(i).toString())) break;
		}
		
		selectBaudRateComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					selectBaudRateComboBoxActionPerformed(event);
				}
			}
		});
		
		String[] flowControlValues = {"None", "RTS/CTS", "Xon/Xoff"}; 
		
		selectFlowControlInComboBox = new JComboBox<String>(flowControlValues);
        selectFlowControlInComboBox.setSelectedIndex(flowControlIn);
        
        selectFlowControlInComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					flowControl = flowControlInComboBoxActionPerformed(event);
					serialInterface.setFlowControl(flowControl);
				}
			}
		});
        
        selectFlowControlOutComboBox = new JComboBox<String>(flowControlValues);
        selectFlowControlOutComboBox.setSelectedIndex(flowControlOut);

        selectFlowControlOutComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					flowControl = flowControlOutComboBoxActionPerformed(event);
					serialInterface.setFlowControl(flowControl);
				}
			}
		});
		
		RadioButtonHandler rbh = new RadioButtonHandler();
		
		dataBits5 = new JRadioButton("5");
		dataBits6 = new JRadioButton("6");
		dataBits7 = new JRadioButton("7");
		dataBits8 = new JRadioButton("8");
	
		dataBitsButtonGroup = new ButtonGroup();
		
		dataBitsButtonGroup.add(dataBits5);
		dataBitsButtonGroup.add(dataBits6);
		dataBitsButtonGroup.add(dataBits7);
		dataBitsButtonGroup.add(dataBits8);
	
		dataBits5.addItemListener(rbh);
		dataBits6.addItemListener(rbh);
		dataBits7.addItemListener(rbh);
		dataBits8.addItemListener(rbh);
	
	    switch (dataBits) {
	        case SerialPort.DATABITS_5:
	            dataBitsModel = dataBits5.getModel();
	            break;
	        case SerialPort.DATABITS_6:
	        	dataBitsModel = dataBits6.getModel();
	            break;
	        case SerialPort.DATABITS_7:
	        	dataBitsModel = dataBits7.getModel();
	            break;
	        case SerialPort.DATABITS_8:
	        	dataBitsModel = dataBits8.getModel();
	            break;
	    }
	    
		dataBitsButtonGroup.setSelected(dataBitsModel, true);
	
		stopBits1 = new JRadioButton("1");
		stopBits15 = new JRadioButton("1.5");
		stopBits2 = new JRadioButton("2");
	
		stopBitsButtonGroup = new ButtonGroup();
		
		stopBitsButtonGroup.add(stopBits1);
		stopBitsButtonGroup.add(stopBits15);
		stopBitsButtonGroup.add(stopBits2);
	
		stopBits1.addItemListener(rbh);
		stopBits15.addItemListener(rbh);
		stopBits2.addItemListener(rbh);
	
	    switch (stopBits) {
	        case SerialPort.STOPBITS_1:
	            stopBitsModel = stopBits1.getModel();
	            break;
	        case SerialPort.STOPBITS_1_5:
	        	stopBitsModel = stopBits15.getModel();
	            break;
	        case SerialPort.STOPBITS_2:
	        	stopBitsModel = stopBits2.getModel();
	            break;
	    }
	
		stopBitsButtonGroup.setSelected(stopBitsModel, true);
	
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
	
		parityNone.addItemListener(rbh);
		parityOdd.addItemListener(rbh);
		parityEven.addItemListener(rbh);
		parityMark.addItemListener(rbh);
		paritySpace.addItemListener(rbh);
	    
	    switch (parity) {
	        case SerialPort.PARITY_NONE:
	            parityModel = parityNone.getModel();
	            break;
	        case SerialPort.PARITY_ODD:
	        	parityModel = parityOdd.getModel();
	            break;
	        case SerialPort.PARITY_EVEN:
	        	parityModel = parityEven.getModel();
	            break;
	        case SerialPort.PARITY_MARK:
	        	parityModel = parityMark.getModel();
	            break;
	        case SerialPort.PARITY_SPACE:
	        	parityModel = paritySpace.getModel();
	            break;
	    }
	    
		parityButtonGroup.setSelected(parityModel, true);	
		
		enableDTRCheckBox.setSelected(dtr);
		enableRTSCheckBox.setSelected(rts);
		
	    eventRXCHARCheckBox.setSelected(eventRXCHAR);
		eventRXFLAGCheckBox.setSelected(eventRXFLAG);
		eventTXEMPTYCheckBox.setSelected(eventTXEMPTY);
		eventCTSCheckBox.setSelected(eventCTS);
		eventDSRCheckBox.setSelected(eventDSR);
		eventRLSDCheckBox.setSelected(eventRLSD);
		eventERRCheckBox.setSelected(eventERR);
		eventRINGCheckBox.setSelected(eventRING);
		eventBREAKCheckBox.setSelected(eventBREAK);
		
		cbLogSerialPortErrors.setSelected(logSerialPortErrors);
        cbReportBreakInterrupts.setSelected(reportBreakInterrupts);
        cbReportBufferOverrunErrors.setSelected(reportBufferOverrunErrors);
        cbReportConfigurationErrors.setSelected(reportConfigurationErrors);
        cbReportDTRNotSetErrors.setSelected(reportDTRNotSetErrors);
        cbReportEventMaskErrors.setSelected(reportEventMaskErrors);
        cbReportFlowControlErrors.setSelected(reportFlowControlErrors);
        cbReportFramingErrors.setSelected(reportFramingErrors);
        cbReportParityMismatchErrors.setSelected(reportParityMismatchErrors);
        cbReportPurgeFailures.setSelected(reportPurgeFailures);
        cbReportRTSNotSetErrors.setSelected(reportRTSNotSetErrors);
        cbReportTransmitFailures.setSelected(reportTransmitFailures);
        
        enableDTRCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
            	enableDTRCheckBoxActionPerformed(event);
            }
        });
        
        enableRTSCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
            	enableRTSCheckBoxActionPerformed(event);
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
    	
    	cbReportEventMaskErrors.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent event) {
            	cbReportEventMaskErrorsActionPerformed(event);
            }
        });

        cbReportFlowControlErrors.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent event) {
            	cbReportFlowControlErrorsActionPerformed(event);
            }
        });

        cbReportPurgeFailures.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent event) {
            	cbReportPurgeFailuresActionPerformed(event);
            }
        });

        cbReportTransmitFailures.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent event) {
            	cbReportTransmitFailuresActionPerformed(event);
            }
        });

        cbReportBreakInterrupts.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent event) {
            	cbReportBreakInterruptsActionPerformed(event);
            }
        });
        
        cbReportConfigurationErrors.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent event) {
            	cbReportConfigurationErrorsActionPerformed(event);
            }
        });

        cbReportRTSNotSetErrors.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent event) {
            	cbReportRTSNotSetErrorsActionPerformed(event);
            }
        });

        cbReportDTRNotSetErrors.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent event) {
            	cbReportDTRNotSetErrorsActionPerformed(event);
            }
        });

        cbReportFramingErrors.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent event) {
            	cbReportFramingErrorsActionPerformed(event);
            }
        });

        cbReportParityMismatchErrors.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent event) {
            	cbReportParityMismatchErrorsActionPerformed(event);
            }
        });

        cbReportBufferOverrunErrors.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent event) {
            	cbReportBufferOverrunErrorsActionPerformed(event);
            }
        });

        cbLogSerialPortErrors.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent event) {
            	cbLogSerialPortErrorsActionPerformed(event);
            }
        }); 
	}

	private void enableRTSCheckBoxActionPerformed(ActionEvent event) {
		rts = enableRTSCheckBox.isSelected();
		serialInterface.setRTS(rts);
	}
	
	private void enableDTRCheckBoxActionPerformed(ActionEvent event) {
		dtr = enableDTRCheckBox.isSelected();
		serialInterface.setDTR(dtr);
	}
	
	private void cbReportBufferOverrunErrorsActionPerformed(ActionEvent event) {
    	reportBufferOverrunErrors = cbReportBufferOverrunErrors.isSelected();
    	serialInterface.reportBufferOverrunErrors(reportBufferOverrunErrors);
	}

	private void cbReportParityMismatchErrorsActionPerformed(ActionEvent event) {
		reportParityMismatchErrors = cbReportParityMismatchErrors.isSelected();
		serialInterface.reportParityMismatchErrors(reportParityMismatchErrors);
	}

	private void cbReportFramingErrorsActionPerformed(ActionEvent event) {
		reportFramingErrors = cbReportFramingErrors.isSelected();
		serialInterface.reportFramingErrors(reportFramingErrors);
	}

	private void cbReportDTRNotSetErrorsActionPerformed(ActionEvent event) {
		reportDTRNotSetErrors = cbReportDTRNotSetErrors.isSelected();
		serialInterface.reportDataTerminalReadyLineNotSetErrors(reportDTRNotSetErrors);
	}

	private void cbReportRTSNotSetErrorsActionPerformed(ActionEvent event) {
		reportRTSNotSetErrors = cbReportRTSNotSetErrors.isSelected();
		serialInterface.reportReadyToSendLineNotSetErrors(reportRTSNotSetErrors);
	}

	private void cbReportConfigurationErrorsActionPerformed(ActionEvent event) {
		reportConfigurationErrors = cbReportConfigurationErrors.isSelected();
		serialInterface.reportConfigurationErrors(reportConfigurationErrors);
	}

	private void cbReportBreakInterruptsActionPerformed(ActionEvent event) {
		reportBreakInterrupts = cbReportBreakInterrupts.isSelected();
		serialInterface.reportBreakInterrupts(reportBreakInterrupts);
	}

	private void cbReportTransmitFailuresActionPerformed(ActionEvent event) {
		reportTransmitFailures = cbReportTransmitFailures.isSelected();
		serialInterface.reportTransmitFailures(reportTransmitFailures);
	}

	private void cbReportPurgeFailuresActionPerformed(ActionEvent event) {
		reportPurgeFailures = cbReportPurgeFailures.isSelected();
		serialInterface.reportPurgeFailures(reportPurgeFailures);
	}

	private void cbReportFlowControlErrorsActionPerformed(ActionEvent event) {
		reportFlowControlErrors = cbReportFlowControlErrors.isSelected();
		serialInterface.reportFlowControlErrors(reportFlowControlErrors);
	}

	private void cbReportEventMaskErrorsActionPerformed(ActionEvent event) {
		reportEventMaskErrors = cbReportEventMaskErrors.isSelected();
		serialInterface.reportEventMaskErrors(reportEventMaskErrors);
	}

	private void cbLogSerialPortErrorsActionPerformed(ActionEvent event) {
		logSerialPortErrors = cbLogSerialPortErrors.isSelected();
		serialInterface.logSerialPortErrors(logSerialPortErrors);
	}
	
    private void eventBREAKCheckBoxActionPerformed(ActionEvent event) {
    	eventBREAK = eventBREAKCheckBox.isSelected();
    	serialInterface.enableBreakEvent(eventBREAK);
	}

	private void eventRINGCheckBoxActionPerformed(ActionEvent event) {
		eventRING = eventRINGCheckBox.isSelected();
		serialInterface.enableRingEvent(eventRING);
	}

	private void eventERRCheckBoxActionPerformed(ActionEvent event) {
		eventERR = eventERRCheckBox.isSelected();
		serialInterface.enableErrorEvent(eventERR);
	}

	private void eventRLSDCheckBoxActionPerformed(ActionEvent event) {
		eventRLSD = eventRLSDCheckBox.isSelected();
		serialInterface.enableRLSDEvent(eventRLSD);
	}

	private void eventDSRCheckBoxActionPerformed(ActionEvent event) {
		eventDSR = eventDSRCheckBox.isSelected();
		serialInterface.enableDSREvent(eventDSR);
	}

	private void eventCTSCheckBoxActionPerformed(ActionEvent event) {
		eventCTS = eventCTSCheckBox.isSelected();
		serialInterface.enableCTSEvent(eventCTS);
	}

	private void eventTXEMPTYCheckBoxActionPerformed(ActionEvent event) {
		eventTXEMPTY = eventTXEMPTYCheckBox.isSelected();
		serialInterface.enableTxEmptyEvent(eventTXEMPTY);
	}

	private void eventRXFLAGCheckBoxActionPerformed(ActionEvent event) {
		eventRXFLAG = eventRXFLAGCheckBox.isSelected();
		serialInterface.enableRxFlagEvent(eventRXFLAG);
	}

	private void eventRXCHARCheckBoxActionPerformed(ActionEvent event) {
		eventRXCHAR = eventRXCHARCheckBox.isSelected();
		serialInterface.enableReceive(eventRXCHAR);
	}
	
	private boolean setAssignedComPort(final int portNumber, final String deviceType) {
		final String portName = Utility.getPortNameString(portNumber);
    	if (!Utility.isComPortValid(portName)) {
    		SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null,
                    	"Error opening " + portName + "\n\n" +
                    	"The configured serial port " +
                    	"is not available at this time.\n" +
                    	"Please configure the " + deviceType + " serial port parameters.",		
                    	"Serial Port Not Available", JOptionPane.ERROR_MESSAGE);
                }
            });
    		return false;
    	} else {
    		this.portNumber = portNumber;
    		serialInterface.setPortName(Utility.getPortNameString(portNumber));
    		return true;
    	}
    }
	
    private void transferSerialPortSettingsFromRegistryToMemory(String deviceID) {
    	setAssignedComPort(systemPrefs.getInt(deviceID + "portNumber", 2), deviceType);
		portName = systemPrefs.get(deviceID + "portName", Utility.getPortNameString(portNumber));
		baudRate = systemPrefs.getInt(deviceID + "baudRate", defaultSerialParameterSet.getBaudRate());
		dataBits = systemPrefs.getInt(deviceID + "dataBits", defaultSerialParameterSet.getDataBits());
		stopBits = systemPrefs.getInt(deviceID + "stopBits", defaultSerialParameterSet.getStopBits());
		parity = systemPrefs.getInt(deviceID + "parity", defaultSerialParameterSet.getParity());
		dtr = systemPrefs.getBoolean(deviceID + "dtr", defaultSerialParameterSet.getDTR());
		rts = systemPrefs.getBoolean(deviceID + "rts", defaultSerialParameterSet.getRTS());
		flowControl = systemPrefs.getInt(deviceID + "flowControl", defaultSerialParameterSet.getFlowControl());
		flowControlIn = systemPrefs.getInt(deviceID + "flowControlIn", defaultSerialParameterSet.getFlowControlIn());
		flowControlOut = systemPrefs.getInt(deviceID + "flowControlOut", defaultSerialParameterSet.getFlowControlOut());
		
		eventRXCHAR = systemPrefs.getBoolean(deviceID + "eventRXCHAR", true);
		eventRXFLAG = systemPrefs.getBoolean(deviceID + "eventRXFLAG", true);
		eventTXEMPTY = systemPrefs.getBoolean(deviceID + "eventTXEMPTY", true);
		eventCTS = systemPrefs.getBoolean(deviceID + "eventCTS", true);
		eventDSR = systemPrefs.getBoolean(deviceID + "eventDSR", true);
		eventRLSD = systemPrefs.getBoolean(deviceID + "eventRLSD", true);
		eventERR = systemPrefs.getBoolean(deviceID + "eventERR", true);
		eventRING = systemPrefs.getBoolean(deviceID + "eventRING", true);
		eventBREAK = systemPrefs.getBoolean(deviceID + "eventBREAK", true);
		
		reportConfigurationErrors = systemPrefs.getBoolean(deviceID + "reportConfigurationErrors", true);
		reportRTSNotSetErrors = systemPrefs.getBoolean(deviceID + "reportRTSErrors", true);
		reportDTRNotSetErrors = systemPrefs.getBoolean(deviceID + "reportDTRErrors", true);
		reportFramingErrors = systemPrefs.getBoolean(deviceID + "reportFramingErrors", true);
		reportBufferOverrunErrors = systemPrefs.getBoolean(deviceID + "reportBufferOverrunErrors", true);
		reportParityMismatchErrors = systemPrefs.getBoolean(deviceID + "reportParityMismatchErrors", true);
		reportBreakInterrupts = systemPrefs.getBoolean(deviceID + "reportBreakInterrupts", true);
		reportEventMaskErrors = systemPrefs.getBoolean(deviceID + "reportEventMaskErrors", true);
		reportFlowControlErrors = systemPrefs.getBoolean(deviceID + "reportFlowControlErrors", true);
		reportPurgeFailures = systemPrefs.getBoolean(deviceID + "reportPurgeFailures", true);
		reportTransmitFailures = systemPrefs.getBoolean(deviceID + "reportTransmitFailures", true);
		
		logSerialPortErrors = systemPrefs.getBoolean(deviceID + "logSerialPortErrors", true);
	}

    public void sendSerialPortSettingsFromMemoryToDevice() {
    	serialInterface.setPortName(Utility.getPortNameString(portNumber));
    	serialInterface.setParameters(baudRate, dataBits, stopBits, parity);
        serialInterface.setFlowControl(flowControl);
        serialInterface.setDTR(dtr);
        serialInterface.setRTS(rts);
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
    
	public String getPortName() {
		if (Utility.isComPortValid(portNumber)) return Utility.getPortNameString(portNumber);
    	else return null;
	}
	
	public int getBaudRate() {
		return baudRate;
	}

	public int getDataBits() {
		return dataBits;
	}

	public int getStopBits() {
		return stopBits;
	}

	public int getParity() {
		return parity;
	}
	
	public boolean getDTR() {
		return dtr;
	}
	
	public boolean getRTS() {
		return rts;
	}
	
    private class RadioButtonHandler implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent ie) {
            if (ie.getSource().equals(dataBits5)) {
                dataBits = 5;
            } else if (ie.getSource().equals(dataBits6)) {
                dataBits = 6;
            } else if (ie.getSource().equals(dataBits7)) {
                dataBits = 7;
            } else if (ie.getSource().equals(dataBits8)) {
                dataBits = 8;
            } else if (ie.getSource().equals(stopBits1)) {
                stopBits = 1;
            } else if (ie.getSource().equals(stopBits15)) {
                stopBits = 3;
            } else if (ie.getSource().equals(stopBits2)) {
                stopBits = 2;
            } else if (ie.getSource().equals(parityNone)) {
                parity = 0;
            } else if (ie.getSource().equals(parityOdd)) {
                parity = 1;
            } else if (ie.getSource().equals(parityEven)) {
                parity = 2;
            } else if (ie.getSource().equals(parityMark)) {
                parity = 3;
            } else if (ie.getSource().equals(paritySpace)) {
                parity = 4;
            }
        }
    }

	private void selectComPortComboBoxActionPerformed(ItemEvent event) {
		JComboBox<?> cb = (JComboBox<?>) event.getSource();
		String portName = (String) cb.getSelectedItem();
		portNumber = Utility.getPortNumberFromName(portName);
		setAssignedComPort(portNumber, deviceType);
		this.portName = portName;
	}

	private void selectBaudRateComboBoxActionPerformed(ItemEvent event) {
        JComboBox<?> cb = (JComboBox<?>) event.getSource();
        int baudRate = Integer.parseInt((String) cb.getSelectedItem());
        serialInterface.setBaudRate(baudRate);
        this.baudRate = baudRate;
    }
	
	public void updateDefaultSerialParameterSet(String deviceID, SerialParameterSet defaultSerialParameterSet) {
		this.defaultSerialParameterSet = defaultSerialParameterSet;
		saveSettings(deviceID);
		sendSerialPortSettingsFromMemoryToDevice();
	}
	
	public void saveSettings(String deviceID) {
		systemPrefs.put(deviceID + "portName", portName);
		systemPrefs.putInt(deviceID + "portNumber", portNumber);
		systemPrefs.putInt(deviceID + "baudRate", baudRate);
		systemPrefs.putInt(deviceID + "dataBits", dataBits);
		systemPrefs.putInt(deviceID + "stopBits", stopBits);
		systemPrefs.putInt(deviceID + "parity", parity);
		systemPrefs.putInt(deviceID + "flowControl", flowControl);
        systemPrefs.putInt(deviceID + "flowControlIn", flowControlIn);
        systemPrefs.putInt(deviceID + "flowControlOut", flowControlOut);
        systemPrefs.putBoolean(deviceID + "dtr", dtr);
        systemPrefs.putBoolean(deviceID + "rts", rts);
        
        systemPrefs.putBoolean(deviceID + "eventBREAK", eventBREAK);
        systemPrefs.putBoolean(deviceID + "eventCTS", eventCTS);
        systemPrefs.putBoolean(deviceID + "eventDSR", eventDSR);
        systemPrefs.putBoolean(deviceID + "eventERR", eventERR);
        systemPrefs.putBoolean(deviceID + "eventRXCHAR", eventRXCHAR);
        systemPrefs.putBoolean(deviceID + "eventRING", eventRING);
        systemPrefs.putBoolean(deviceID + "eventRLSD", eventRLSD);
        systemPrefs.putBoolean(deviceID + "eventRXFLAG", eventRXFLAG);
        systemPrefs.putBoolean(deviceID + "eventTXEMPTY", eventTXEMPTY);
        
        systemPrefs.putBoolean(deviceID + "reportConfigurationErrors", reportConfigurationErrors);
    	systemPrefs.putBoolean(deviceID + "reportRTSErrors", reportRTSNotSetErrors);
    	systemPrefs.putBoolean(deviceID + "reportDTRErrors", reportDTRNotSetErrors);
    	systemPrefs.putBoolean(deviceID + "reportFramingErrors", reportFramingErrors);
    	systemPrefs.putBoolean(deviceID + "reportBufferOverrunErrors", reportBufferOverrunErrors);
    	systemPrefs.putBoolean(deviceID + "reportParityMismatchErrors", reportParityMismatchErrors);
    	systemPrefs.putBoolean(deviceID + "reportBreakInterrupts", reportBreakInterrupts);
        systemPrefs.putBoolean(deviceID + "reportEventMaskErrors", reportEventMaskErrors);
        systemPrefs.putBoolean(deviceID + "reportFlowControlErrors", reportFlowControlErrors);
        systemPrefs.putBoolean(deviceID + "reportPurgeFailures", reportPurgeFailures);
        systemPrefs.putBoolean(deviceID + "reportTransmitFailures", reportTransmitFailures);
        
        systemPrefs.putBoolean(deviceID + "logSerialPortErrors", logSerialPortErrors);
	}

	private int flowControlInComboBoxActionPerformed(ItemEvent event) {
		JComboBox<?> cb = (JComboBox<?>) event.getSource();
		flowControlIn = cb.getSelectedIndex();
        return flowControlValue(flowControlIn) + flowControlOut;
	}
	
	private int flowControlOutComboBoxActionPerformed(ItemEvent event) {
		JComboBox<?> cb = (JComboBox<?>) event.getSource();
		flowControlOut = cb.getSelectedIndex();
        return flowControlValue(flowControlOut) + flowControlIn;
	}
	
	private int flowControlValue(int flowControlIndex) {
		int flow = 0;
		if (flowControlIndex == 0) flow = SerialPort.FLOWCONTROL_NONE;
        if (flowControlIndex == 1) flow = SerialPort.FLOWCONTROL_RTSCTS_OUT;
        if (flowControlIndex == 2) flow = SerialPort.FLOWCONTROL_XONXOFF_OUT;
        return flow;
	}
	
	public SerialInterface getSerialInterface() {
		return serialInterface;
	}
	
	private void setAvailableBaudRates(String[] availableBaudRates) {
		selectBaudRateComboBoxModel.removeAllElements();
		selectBaudRateComboBox.validate();
		for (String baudRate : availableBaudRates) {
			selectBaudRateComboBoxModel.addElement(baudRate);
		}
	}
}
