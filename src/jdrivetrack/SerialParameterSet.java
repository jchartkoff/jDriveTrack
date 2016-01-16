package jdrivetrack;

import jssc.SerialPort;

public class SerialParameterSet {
	private int parity = SerialPort.PARITY_NONE;
	private int stopBits = SerialPort.STOPBITS_1;
	private int dataBits = SerialPort.DATABITS_8;
	private int baudRate = SerialPort.BAUDRATE_4800;
	private int flowControl = SerialPort.FLOWCONTROL_NONE;
	private int flowControlIn = SerialPort.FLOWCONTROL_NONE;
	private int flowControlOut = SerialPort.FLOWCONTROL_NONE;
	private boolean dtr = true;
	private boolean rts = true;
	private String[] validBaudRates = {"4800","9600"};
	private boolean deviceAssignedParametersFixed = false;
	
	public SerialParameterSet(int baudRate, int dataBits, int stopBits, int parity,
			boolean dtr, boolean rts, int flowControlIn, int flowControlOut, String[] validBaudRates,
			boolean deviceAssignedParametersFixed) {
		setDefaultCommPortParameters(baudRate, dataBits, stopBits, parity);
		setDefaultDTR(dtr);
		setDefaultDTR(rts);
		setDefaultFlowControlIn(flowControlIn);
		setDefaultFlowControlOut(flowControlOut);
		setValidBaudRates(validBaudRates);
		setDeviceAssignedParametersFixed(deviceAssignedParametersFixed);
	}
	
	public void setDefaultCommPortParameters(int baudRate, int dataBits, int stopBits, int parity) {
		this.baudRate = baudRate;
		this.dataBits = dataBits;
		this.stopBits = stopBits;
		this.parity = parity;
	}
	
	public void setDefaultDTR(boolean dtr) {
		this.dtr = dtr;
	}
	
	public void setDefaultRTS(boolean rts) {
		this.rts = rts;
	}
	
	public void setDefaultFlowControlIn(int flowControlIn) {
		this.flowControlIn = flowControlIn;
		this.flowControl = flowControlValue(flowControlIn) + flowControlOut;
	}
	
	public void setDefaultFlowControlOut(int flowControlOut) {
		this.flowControlOut = flowControlOut;
		this.flowControl = flowControlValue(flowControlOut) + flowControlIn;
	}
	
	public void setDeviceAssignedParametersFixed(boolean deviceAssignedParametersFixed) {
		this.deviceAssignedParametersFixed = deviceAssignedParametersFixed;
	}
	
	public boolean isDeviceAssignedParametersFixed() {
		return deviceAssignedParametersFixed;
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
	
	public int getFlowControl() {
		return flowControl;
	}
	
	public int getFlowControlIn() {
		return flowControlIn;
	}
	
	public int getFlowControlOut() {
		return flowControlOut;
	}
	
	public boolean getDTR() {
		return dtr;
	}
	
	public boolean getRTS() {
		return rts;
	}

	private int flowControlValue(int flowControlIndex) {
		int flow = 0;
		if (flowControlIndex == 0) flow = SerialPort.FLOWCONTROL_NONE;
        if (flowControlIndex == 1) flow = SerialPort.FLOWCONTROL_RTSCTS_OUT;
        if (flowControlIndex == 2) flow = SerialPort.FLOWCONTROL_XONXOFF_OUT;
        return flow;
	}
	
	public String[] getValidBaudRates() {
		return validBaudRates;
	}
	
	public void setValidBaudRates(String[] validBaudRates) {
		this.validBaudRates = validBaudRates;
	}
}
