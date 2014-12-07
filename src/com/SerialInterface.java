package com;

import java.beans.PropertyChangeListener;

import jssc.SerialPortException;

public interface SerialInterface {
	
	public static final String CTS = "CTS";
	public static final String DSR = "DSR";
	public static final String RLSD = "RLSD";
	public static final String RING = "RING";
	public static final String BREAK = "BREAK";
	public static final String TX_EMPTY = "TX_EMPTY";
	public static final String RX_FLAG = "RX_FLAG";
	public static final String RX_CHAR = "RX_CHAR";
	public static final String ERROR = "ERROR";
	public static final String TX_DATA = "TX_DATA";
	public static final String RX_DATA = "RX_DATA";
	public static final String DATA_RECEIVED = "DATA_RECEIVED";
	public static final String ONLINE = "ONLINE";
	public static final String INVALID_COM_PORT = "INVALID_COM_PORT";
	public static final String ADVISE_PORT_CLOSING = "ADVISE_PORT_CLOSING";
	public static final String SERIAL_PORT_CONFIGURATION_ERROR = "SERIAL_PORT_CONFIGURATION_ERROR";

	public static final int PARAMETER_ERROR = 1;
	public static final int DTR_NOT_SET = 16;
	public static final int RTS_NOT_SET = 32;
	public static final int EVENT_MASK_ERROR = 64;
	public static final int FLOW_CONTROL_ERROR = 128;
	public static final int PORT_NOT_PURGED = 256;
	
	public static final int ERROR_OVERRUN = 2;
	public static final int ERROR_PARITY = 4;
	public static final int ERROR_FRAME = 8;
	public static final int INTERRUPT_BREAK = 512;
	public static final int INTERRUPT_TX = 1024;
	public static final int INTERRUPT_FRAME = 2048;
	public static final int INTERRUPT_OVERRUN = 4096;
	public static final int INTERRUPT_PARITY = 8192;

	int getBaudRate();
	
	byte[] readBytes();
	
	byte[] readBytes(int byteCount);
	
	String readString();
	
	String readString(int byteCount);
	
	String getPortName();

	boolean isCTS();

	int getDataBits();

	boolean isDSR();
	
	boolean isRING();

	int getParity();

	int getStopBits();

	boolean isOpen();
	
	void setBaudRate(int baudRate);

	void setPortName(String portName);

	void setDataBits(int dataBits);

	void setDTR(boolean dtr);

	void setOnline(String portName) throws SerialPortException;
	
	void setRTS(boolean rts);

	void setStopBits(int stopBits);

	void setParity(int parity);

	void closeSerialPort() throws SerialPortException;

	boolean isRLSD();

	void setFlowControlIn(int flowControlIn);

	int getFlowControlIn();

	void setFlowControlOut(int flowControlOut);

	int getFlowControlOut();
	
	void enableReceive(boolean eventRXCHAR);

	void enableTxEmptyEvent(boolean eventTXEMPTY);

	void enableRxFlagEvent(boolean eventRXFLAG);

	void enableCTSEvent(boolean eventCTS);

	void enableDSREvent(boolean eventDSR);

	void enableRLSDEvent(boolean eventRLSD);

	void enableErrorEvent(boolean eventERR);

	void enableRingEvent(boolean eventRING);

	void enableBreakEvent(boolean eventBREAK);

	void setEventMask(int eventMask);

	int getEventMask();

	boolean isReceiveEnabled();

	boolean isTxEmptyEventEnabled();

	boolean isBreakEventEnabled();

	boolean isRINGEventEnabled();

	boolean isErrorEventEnabled();

	boolean isRLSDEventEnabled();

	boolean isDSREventEnabled();

	boolean isCTSEventEnabled();

	boolean isRxFlagEventEnabled();

	boolean purgeAll();

	boolean purgeTxClear();

	boolean purgeTxAbort();

	boolean purgeRxClear();

	boolean purgeRxAbort();
	
	void addPropertyChangeListener(PropertyChangeListener listener);
	
	void removePropertyChangeListener(PropertyChangeListener listener);

	boolean writeByte(byte singleByte);

	boolean writeBytes(byte[] buffer);

	boolean writeInt(int singleInt);

	boolean writeIntArray(int[] buffer);

	boolean writeString(String string);

	boolean writeString(String string, String charsetName);

	String getErrorMessage();

	void reportFramingErrors(boolean reportFramingErrors);

	void reportBufferOverrunErrors(boolean reportBufferOverrunErrors);

	void reportParityMismatchErrors(boolean reportParityMismatchErrors);

	void reportConfigurationErrors(boolean reportConfigurationErrors);

	void reportDataTerminalReadyLineNotSetErrors(boolean reportDTRNotSetErrors);

	void reportReadyToSendLineNotSetErrors(boolean reportRTSNotSetErrors);

	void reportEventMaskErrors(boolean reportEventMaskErrors);

	void reportFlowControlErrors(boolean reportFlowControlErrors);

	void reportPurgeFailures(boolean reportPurgeFailures);

	void reportBreakInterrupts(boolean reportBreakInterrupts);

	void reportTransmitFailures(boolean reportTransmitFailures);

	void logSerialPortErrors(boolean logSerialPortErrors);
	
}