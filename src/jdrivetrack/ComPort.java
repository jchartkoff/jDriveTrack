package jdrivetrack;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

import interfaces.SerialInterface;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class ComPort implements SerialInterface {
	private boolean eventRXCHAR;
	private boolean eventRXFLAG;
	private boolean eventTXEMPTY;
	private boolean eventCTS;
	private boolean eventDSR;
	private boolean eventRLSD;
	private boolean eventERR;
	private boolean eventRING;
	private boolean eventBREAK;
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
	private int eventMask;
	private int parity;
	private int stopBits;
	private int dataBits;
	private int baudRate;
	private int flowControl;
    private String portName;
    private boolean dtr;
    private boolean rts;
    private String errorMessage;
    private boolean allowEvents = false;
    private SerialPort serialPort;
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public ComPort() {}
	
	@Override
	public String getErrorMessage() {
		return errorMessage;
	}
	
	@Override
	public void setPortName(String portName) {
		if (Utility.isComPortValid(portName)) {
			try {
				if (isOpen()) {
					setOnline(portName);
				} else {
					this.portName = portName;
				}
			} catch (SerialPortException ex) {
				logException(ex);
				if (reportConfigurationErrors) pcs.firePropertyChange(ERROR, null, ex.getMessage());
			}
		}
	}

	private void logException(SerialPortException ex) {
		errorMessage = ex.getMessage();
		if (logSerialPortErrors) Main.log.log(Level.WARNING, "SerialPortException", ex);
	}
	
	private void logException(UnsupportedEncodingException ex) {
		errorMessage = ex.getMessage();
		if (logSerialPortErrors) Main.log.log(Level.WARNING, "UnsupportedEncodingException", ex);
	}
	
	private void logException(Exception ex) {
		errorMessage = ex.getMessage();
		if (logSerialPortErrors) Main.log.log(Level.WARNING, "Exception", ex);
	}

	@Override
	public String getPortName() {
		return portName;
	}

	@Override
	public void reportFramingErrors(boolean reportFramingErrors) {
		this.reportFramingErrors = reportFramingErrors;
	}
	
	@Override
	public void reportBufferOverrunErrors(boolean reportBufferOverrunErrors) {
		this.reportBufferOverrunErrors = reportBufferOverrunErrors;
	}
	
	@Override
	public void reportParityMismatchErrors(boolean reportParityMismatchErrors) {
		this.reportParityMismatchErrors = reportParityMismatchErrors;
	}
	
	@Override
	public void reportConfigurationErrors(boolean reportConfigurationErrors) {
		this.reportConfigurationErrors = reportConfigurationErrors;
	}
	
	@Override
	public void reportDataTerminalReadyLineNotSetErrors(boolean reportDTRNotSetErrors) {
		this.reportDTRNotSetErrors = reportDTRNotSetErrors;
	}
	
	@Override
	public void reportReadyToSendLineNotSetErrors(boolean reportRTSNotSetErrors) {
		this.reportRTSNotSetErrors = reportRTSNotSetErrors;
	}
	
	@Override
	public void reportEventMaskErrors(boolean reportEventMaskErrors) {
		this.reportEventMaskErrors = reportEventMaskErrors;
	}
	
	@Override
	public void reportFlowControlErrors(boolean reportFlowControlErrors) {
		this.reportFlowControlErrors = reportFlowControlErrors;
	}
	
	@Override
	public void reportPurgeFailures(boolean reportPurgeFailures) {
		this.reportPurgeFailures = reportPurgeFailures;
	}
	
	@Override
	public void reportBreakInterrupts(boolean reportBreakInterrupts) {
		this.reportBreakInterrupts = reportBreakInterrupts;
	}
	
	@Override
	public void reportTransmitFailures(boolean reportTransmitFailures) {
		this.reportTransmitFailures = reportTransmitFailures;
	}

	@Override
	public void logSerialPortErrors(boolean logSerialPortErrors) {
		this.logSerialPortErrors = logSerialPortErrors;
	}
	
	@Override
	public void setEventMask(int eventMask) {
		try {
			this.eventMask = eventMask;
			if (serialPort != null && serialPort.isOpened()) serialPort.setEventsMask(eventMask);
		} catch (SerialPortException ex) {
			logException(ex);
			if (reportEventMaskErrors) pcs.firePropertyChange(ERROR, null, ex.getMessage());
		}
	}
	
	@Override
	public boolean purgeAll() {
		try {
			return serialPort.purgePort(SerialPort.PURGE_RXABORT + SerialPort.PURGE_RXCLEAR + SerialPort.PURGE_TXABORT +
						SerialPort.PURGE_TXCLEAR);
		} catch (SerialPortException ex) {
			logException(ex);
			if (reportPurgeFailures) pcs.firePropertyChange(ERROR, null, ex.getMessage());
			return false;
		}
	}
	
	@Override
	public boolean purgeRxAbort() {
		try {
			return serialPort.purgePort(SerialPort.PURGE_RXABORT);
		} catch (SerialPortException ex) {
			logException(ex);
			if (reportPurgeFailures) pcs.firePropertyChange(ERROR, null, ex.getMessage());
			return false;
		}
	}
	
	@Override
	public boolean purgeRxClear() {
		try {
			return serialPort.purgePort(SerialPort.PURGE_RXCLEAR);
		} catch (SerialPortException ex) {
			logException(ex);
			if (reportPurgeFailures) pcs.firePropertyChange(ERROR, null, ex.getMessage());
			return false;
		}
	}
	
	@Override
	public boolean purgeTxAbort() {
		try {
			return serialPort.purgePort(SerialPort.PURGE_TXABORT);
		} catch (SerialPortException ex) {
			logException(ex);
			if (reportPurgeFailures) pcs.firePropertyChange(ERROR, null, ex.getMessage());
			return false;
		}
	}
	
	@Override
	public boolean purgeTxClear() {
		try {
			return serialPort.purgePort(SerialPort.PURGE_TXCLEAR);
		} catch (SerialPortException ex) {
			logException(ex);
			if (reportPurgeFailures) pcs.firePropertyChange(ERROR, null, ex.getMessage());
			return false;
		}
	}
	
	@Override
	public int getEventMask() {
		return eventMask;
	}
	
	@Override
	public void enableReceive(boolean eventRXCHAR) {
		if (eventRXCHAR) 
			eventMask |= 0b1;
		else
			eventMask &= ~0b1;
		setEventMask(eventMask);
		this.eventRXCHAR = eventRXCHAR;
	}
	
	@Override
	public boolean isReceiveEnabled() {
		return eventRXCHAR;
	}
	
	@Override
	public void enableTxEmptyEvent(boolean eventTXEMPTY) {
		if (eventTXEMPTY) 
			eventMask |= 0b100;
		else
			eventMask &= ~0b100;
		setEventMask(eventMask);
		this.eventTXEMPTY = eventTXEMPTY;
	}
	
	@Override
	public boolean isTxEmptyEventEnabled() {
		return eventTXEMPTY;
	}
	
	@Override
	public void enableRxFlagEvent(boolean eventRXFLAG) {
		if (eventRXFLAG) 
			eventMask |= 0b10;
		else
			eventMask &= ~0b10;
		setEventMask(eventMask);
		this.eventRXFLAG = eventRXFLAG;
	}

	@Override
	public boolean isRxFlagEventEnabled() {
		return eventRXFLAG;
	}
	
	@Override
	public void enableCTSEvent(boolean eventCTS) {
		if (eventCTS) 
			eventMask |= 0b1000;
		else
			eventMask &= ~0b1000;
		setEventMask(eventMask);
		this.eventCTS = eventCTS;
	}
	
	@Override
	public boolean isCTSEventEnabled() {
		return eventCTS;
	}
	
	@Override
	public void enableDSREvent(boolean eventDSR) {
		if (eventDSR) 
			eventMask |= 0b10000;
		else
			eventMask &= ~0b10000;
		setEventMask(eventMask);
		this.eventDSR = eventDSR;
	}
	
	@Override
	public boolean isDSREventEnabled() {
		return eventDSR;
	}
	
	@Override
	public void enableRLSDEvent(boolean eventRLSD) {
		if (eventRLSD) 
			eventMask |= 0b100000;
		else
			eventMask &= ~0b100000;
		setEventMask(eventMask);
		this.eventRLSD = eventRLSD;
	}
	
	@Override
	public boolean isRLSDEventEnabled() {
		return eventRLSD;
	}

	@Override
	public void enableErrorEvent(boolean eventERR) {
		if (eventERR) 
			eventMask |= 0b10000000;
		else
			eventMask &= ~0b10000000;
		setEventMask(eventMask);
		this.eventERR = eventERR;
	}

	@Override
	public boolean isErrorEventEnabled() {
		return eventERR;
	}

	@Override
	public void enableRingEvent(boolean eventRING) {
		if (eventRING) 
			eventMask |= 0b100000000;
		else
			eventMask &= ~0b100000000;
		setEventMask(eventMask);
		this.eventRING = eventRING;
	}

	@Override
	public boolean isRINGEventEnabled() {
		return eventRING;
	}

	@Override
	public void enableBreakEvent(boolean eventBREAK) {
		if (eventBREAK) 
			eventMask |= 0b1000000;
		else
			eventMask &= ~0b1000000;
		setEventMask(eventMask);
		this.eventBREAK = eventBREAK;
	}

	@Override
	public boolean isBreakEventEnabled() {
		return eventBREAK;
	}

	@Override
	public boolean isRLSD() {
		try {
			return serialPort.isRLSD();
		} catch (SerialPortException ex) {
			logException(ex);
			pcs.firePropertyChange(ERROR, null, ex.getMessage());
		}
		return false;
	}

	@Override
	public void setDTR(boolean dtr) {
		try {
			if (serialPort != null && serialPort.isOpened()) serialPort.setDTR(dtr);
			this.dtr = dtr;
		} catch (SerialPortException ex) {
			logException(ex);
			if (reportDTRNotSetErrors) pcs.firePropertyChange(ERROR, null, ex.getMessage());
		}
	}

	@Override
	public void setRTS(boolean rts) {
		try {
			if (serialPort != null && serialPort.isOpened()) serialPort.setRTS(rts);
			this.rts = rts;
		} catch (SerialPortException ex) {
			logException(ex);
			if (reportRTSNotSetErrors) pcs.firePropertyChange(ERROR, null, ex.getMessage());
		}
	}

	@Override
	public boolean isCTS() {
		try {
			return serialPort.isCTS();
		} catch (SerialPortException ex) {
			logException(ex);
			pcs.firePropertyChange(ERROR, null, ex.getMessage());
		}
		return false;
	}

	@Override
	public boolean isDSR() {
		try {
			return serialPort.isDSR();
		} catch (SerialPortException ex) {
			logException(ex);
			pcs.firePropertyChange(ERROR, null, ex.getMessage());
		}
		return false;
	}

	@Override
	public boolean isRING() {
		try {
			return serialPort.isRING();
		} catch (SerialPortException ex) {
			logException(ex);
			pcs.firePropertyChange(ERROR, null, ex.getMessage());
		}
		return false;
	}

	@Override
	public void setOnline() throws SerialPortException {
		if (portName != null) {
			setOnline(portName);
		} else {
			String[] ports = SerialPortList.getPortNames();
			if (ports != null && ports.length > 0) setOnline(ports[0]);
		}
	}
	
	@Override
	public void setOnline(String portName) throws SerialPortException {
		try {
			if (Utility.isComPortValid(portName)) {
				if (serialPort != null && this.portName == portName && serialPort.isOpened()) return;
				if (serialPort != null && this.portName != portName && serialPort.isOpened()) serialPort.closePort(); 
				if (serialPort == null || this.portName != portName) serialPort = new SerialPort(portName);
				allowEvents = true;
				this.portName = portName;
				if (!serialPort.isOpened()) serialPort.openPort();
				boolean paramsOk = serialPort.setParams(baudRate, dataBits, stopBits, parity);
				if (!paramsOk) {
					errorMessage = "Communication Parameters Not Set";
					if (reportConfigurationErrors) pcs.firePropertyChange(SERIAL_PORT_CONFIGURATION_ERROR, null, PARAMETER_ERROR);
				}
				boolean dtrOk = serialPort.setDTR(dtr);
				if (!dtrOk) {
					errorMessage = "Data Terminal Ready Line Not Set";
					if (reportDTRNotSetErrors) pcs.firePropertyChange(SERIAL_PORT_CONFIGURATION_ERROR, null, DTR_NOT_SET);
				}
				boolean rtsOk = serialPort.setRTS(rts);
				if (!rtsOk) {
					errorMessage = "Ready To Send Line Not Set";
					if (reportRTSNotSetErrors) pcs.firePropertyChange(SERIAL_PORT_CONFIGURATION_ERROR, null, RTS_NOT_SET);
				}
				boolean eventMaskOk = serialPort.setEventsMask(eventMask);
				if (!eventMaskOk) {
					errorMessage = "Event Mask Not Set";
					if (reportEventMaskErrors) pcs.firePropertyChange(SERIAL_PORT_CONFIGURATION_ERROR, null, EVENT_MASK_ERROR);
				}
				boolean flowOk = serialPort.setFlowControlMode(flowControl);
				if (!flowOk) {
					errorMessage = "Flow Control Not Set";
					if (reportFlowControlErrors) pcs.firePropertyChange(SERIAL_PORT_CONFIGURATION_ERROR, null, FLOW_CONTROL_ERROR);
				}
				boolean purgeOk = purgeAll();
				if (!purgeOk) {
					errorMessage = "Serial Port Not Purged";
					if (reportPurgeFailures) pcs.firePropertyChange(SERIAL_PORT_CONFIGURATION_ERROR, null, PORT_NOT_PURGED);
				}
				serialPort.addEventListener(new SerialPortReader());
				pcs.firePropertyChange(ONLINE, null, true);
			} else {
				pcs.firePropertyChange(ONLINE, null, false);
				pcs.firePropertyChange(INVALID_COM_PORT, null, portName);
			}
		} catch (SerialPortException ex) {
			logException(ex);
			ex.printStackTrace();
			pcs.firePropertyChange(ONLINE, null, false);
		}
	}

	@Override
	public boolean isOpen() {
		if (serialPort != null) return serialPort.isOpened();
		return false;
	}
	
	@Override
	public void monitorHandshaking(String portName) throws SerialPortException {
		try {
			if (Utility.isComPortValid(portName)) {
				if (serialPort != null && this.portName == portName && serialPort.isOpened()) return;
				if (serialPort != null && this.portName != portName && serialPort.isOpened()) serialPort.closePort(); 
				if (serialPort == null || this.portName != portName) serialPort = new SerialPort(portName);
				allowEvents = true;
				this.portName = "";
				if (!serialPort.isOpened()) serialPort.openPort();
				serialPort.setParams(baudRate, dataBits, stopBits, parity);
				serialPort.setDTR(false);
				serialPort.setRTS(false);
				serialPort.setEventsMask(SerialPortEvent.CTS + SerialPortEvent.DSR + SerialPortEvent.RING + SerialPortEvent.RLSD);
				serialPort.addEventListener(new SerialPortReader());
				pcs.firePropertyChange(ONLINE, null, true);
				if (serialPort.isCTS()) pcs.firePropertyChange(CTS, null, true);
			}
		} catch (SerialPortException ex) {
			pcs.firePropertyChange(ONLINE, null, false);
			throw new SerialPortException(portName, "Start Monitor Handshaking", SerialPortException.TYPE_PORT_BUSY);
		}
	}
	
	@Override
	public void closeSerialPort() throws SerialPortException {
		try {
			pcs.firePropertyChange(ADVISE_PORT_CLOSING, null, true);
			if (serialPort != null && serialPort.isOpened()) {
		    	purgeAll();
		    	allowEvents(false);
				serialPort.removeEventListener();
				serialPort.setDTR(false);
				serialPort.setRTS(false);
		    	serialPort.closePort();
		    	pcs.firePropertyChange(ONLINE, null, false);
			}
		} catch (SerialPortException ex) {
			logException(ex);
			pcs.firePropertyChange(ERROR, null, ex.getMessage());
		} finally {
			pcs.firePropertyChange(PORT_CLOSED, null, true);
		}
	}
	
	@Override
	public void cancelEvents() {
		allowEvents = false;
	}
	
	private void allowEvents(boolean allowEvents) {
		this.allowEvents = allowEvents;
	}

	@Override
	public int getFlowControl() {
		return flowControl;
	}

	@Override
	public void setFlowControl(int flowControl) {
		try {
			this.flowControl = flowControl;
			if (serialPort != null && serialPort.isOpened()) serialPort.setFlowControlMode(flowControl);
		} catch (SerialPortException ex) {
			logException(ex);
			if (reportFlowControlErrors) pcs.firePropertyChange(ERROR, null, ex.getMessage());
		}
	}

	@Override
	public void setParity(int parity) {
		this.parity = parity;
		applyPortParameters();
	}

	@Override
	public int getParity() {
		return parity;
	}

	@Override
	public void setStopBits(int stopBits) {
		this.stopBits = stopBits;
		applyPortParameters();
	}

	@Override
	public int getStopBits() {
		return stopBits;
	}

	@Override
	public void setDataBits(int dataBits) {
		this.dataBits = dataBits;
		applyPortParameters();
	}

	@Override
	public int getDataBits() {
		return dataBits;
	}

	@Override
	public void setBaudRate(int baudRate) {
		this.baudRate = baudRate;
		applyPortParameters();
	}

	@Override
	public void setParameters(int baudRate, int dataBits, int stopBits, int parity) {
		this.baudRate = baudRate;
		this.dataBits = dataBits;
		this.stopBits = stopBits;
		this.parity = parity;
		applyPortParameters();
	}
	
	private void applyPortParameters() {
		try {
			boolean paramsOk = false;
			if (serialPort != null && serialPort.isOpened()) {
				paramsOk = serialPort.setParams(baudRate, dataBits, stopBits, parity);
				if (!paramsOk) {
					errorMessage = "Communications Parameters Not Set";
					if (reportConfigurationErrors) pcs.firePropertyChange(SERIAL_PORT_CONFIGURATION_ERROR, null, PARAMETER_ERROR);
				}
			}
		} catch (SerialPortException ex) {
			logException(ex);
			if (reportConfigurationErrors) pcs.firePropertyChange(ERROR, null, ex.getMessage());
		}
	}
	
	@Override
	public int getBaudRate() {
		return baudRate;
	}
    
	@Override
	public boolean writeByte(byte singleByte) {
		try {
			if (serialPort != null && serialPort.isOpened()) {
				pcs.firePropertyChange(TX_DATA, null, String.valueOf(singleByte));
				return serialPort.writeByte(singleByte);
			} else {
				return false;
			}
		} catch (SerialPortException ex) {
			logException(ex);
			if (reportTransmitFailures) pcs.firePropertyChange(ERROR, null, ex.getMessage());
			return false;
		}
	}
	
	@Override
	public boolean writeBytes(byte[] buffer) {
		try {
			if (serialPort != null && serialPort.isOpened()) {
				pcs.firePropertyChange(TX_DATA, null, buffer.toString());
				return serialPort.writeBytes(buffer);
			} else {
				return false;
			}
		} catch (SerialPortException ex) {
			logException(ex);
			if (reportTransmitFailures) pcs.firePropertyChange(ERROR, null, ex.getMessage());
			return false;
		}
	}
	
	@Override
	public boolean writeInt(int singleInt) {
		try {
			if (serialPort != null && serialPort.isOpened()) {
				pcs.firePropertyChange(TX_DATA, null, String.valueOf(singleInt));
				return serialPort.writeInt(singleInt);
			} else {
				return false;
			}
		} catch (SerialPortException ex) {
			logException(ex);
			if (reportTransmitFailures) pcs.firePropertyChange(ERROR, null, ex.getMessage());
			return false;
		}
	}
	
	@Override
	public boolean writeIntArray(int[] buffer) {
		try {
			if (serialPort != null && serialPort.isOpened()) {
				pcs.firePropertyChange(TX_DATA, null, buffer.toString());
				return serialPort.writeIntArray(buffer);
			} else {
				return false;
			}
		} catch (SerialPortException ex) {
			logException(ex);
			if (reportTransmitFailures) pcs.firePropertyChange(ERROR, null, ex.getMessage());
			return false;
		}
	}
	
	@Override
	public boolean writeString(String string) {
		try {
			if (serialPort != null && serialPort.isOpened()) {
				try {
					pcs.firePropertyChange(TX_DATA, null, string);
					return serialPort.writeString(string, StandardCharsets.US_ASCII.name());
				} catch (UnsupportedEncodingException ex) {
					ex.printStackTrace();
					logException(ex);
					if (reportTransmitFailures) pcs.firePropertyChange(ERROR, null, ex.getMessage());
					return false;
				}
			} else {
				return false;
			}
		} catch (SerialPortException ex) {
			ex.printStackTrace();
			logException(ex);
			if (reportTransmitFailures) pcs.firePropertyChange(ERROR, null, ex.getMessage());
			return false;
		}
	}
	
	@Override
	public boolean writeString(String string, String charsetName) {
		try {
			if (serialPort != null && serialPort.isOpened()) {
				pcs.firePropertyChange(TX_DATA, null, string);
				return serialPort.writeString(string, charsetName);
			} else {
				return false;
			}
		} catch (UnsupportedEncodingException | SerialPortException ex) {
			logException(ex);
			if (reportTransmitFailures) pcs.firePropertyChange(ERROR, null, ex.getMessage());
			return false;
		}
	}
	
	@Override
	public byte[] readBytes() {
		try {
			if (serialPort != null && serialPort.isOpened()) return serialPort.readBytes();
		} catch (SerialPortException ex) {
			logException(ex);
			pcs.firePropertyChange(ERROR, null, ex.getMessage());
		}
		return null;
	}
	
	@Override
	public byte[] readBytes(int byteCount) {
		try {
			if (serialPort != null && serialPort.isOpened()) return serialPort.readBytes(byteCount);
		} catch (SerialPortException ex) {
			logException(ex);
			pcs.firePropertyChange(ERROR, null, ex.getMessage());
		}
		return null;
	}
	
	@Override
	public String readString() {
		try {
			if (serialPort != null && serialPort.isOpened()) return serialPort.readString();
		} catch (SerialPortException ex) {
			logException(ex);
			pcs.firePropertyChange(ERROR, null, ex.getMessage());
		}
		return null;
	}
	
	@Override
	public String readString(int byteCount) {
		try {
			String input = null;
			if (serialPort != null && serialPort.isOpened()) {
				input = serialPort.readString(byteCount);
				return input.replace("\n", "");
			}
		} catch (SerialPortException ex) {
			logException(ex);
			pcs.firePropertyChange(ERROR, null, ex.getMessage());
		}
		return null;
	}

    private class SerialPortReader implements SerialPortEventListener {
		@Override
		public void serialEvent(SerialPortEvent event) {
			if (event.isBREAK()) {
				if (reportBreakInterrupts) pcs.firePropertyChange(BREAK, null, (event.getEventValue() == 1) ? true : false);
			}
			if (event.isRLSD()) {
				if (allowEvents) pcs.firePropertyChange(RLSD, null, (event.getEventValue() == 1) ? true : false);
			}
			if (event.isCTS()) {
				if (allowEvents) pcs.firePropertyChange(CTS, null, (event.getEventValue() == 1) ? true : false);
			}
			if (event.isRXCHAR()) {
				if (allowEvents) pcs.firePropertyChange(RX_CHAR, null, event.getEventValue());
			}
			if (event.isDSR()) {
				if (allowEvents) pcs.firePropertyChange(DSR, null, (event.getEventValue() == 1) ? true : false);
			}
			if (event.isERR()) {
				int errValue = event.getEventValue();
				if (reportFramingErrors && (errValue == ERROR_FRAME || errValue == INTERRUPT_FRAME)) 
					pcs.firePropertyChange(ERROR, null, errValue);
				if (reportParityMismatchErrors && (errValue == ERROR_PARITY || errValue == INTERRUPT_PARITY)) 
					pcs.firePropertyChange(ERROR, null, errValue);
				if (reportBufferOverrunErrors && (errValue == ERROR_OVERRUN || errValue == INTERRUPT_OVERRUN)) 
					pcs.firePropertyChange(ERROR, null, errValue);
				if (reportBreakInterrupts && (errValue == INTERRUPT_BREAK)) 
					pcs.firePropertyChange(ERROR, null, errValue);
				if (reportTransmitFailures && (errValue == INTERRUPT_TX)) 
					pcs.firePropertyChange(ERROR, null, errValue);
			}
			if (event.isTXEMPTY()) {
				if (allowEvents) pcs.firePropertyChange(TX_EMPTY, null, event.getEventValue());
			}
			if (event.isRXFLAG()) {
				if (allowEvents) pcs.firePropertyChange(RX_FLAG, null, event.getEventValue());
			}
			if (event.isRING()) {
				if (allowEvents) pcs.firePropertyChange(RING, null, (event.getEventValue() == 1) ? true : false);
			}
		}
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}
	
	@Override
	public boolean isPropertyChangeListenerRegistered(PropertyChangeListener listener) {
		boolean isRegistered = false;
		PropertyChangeListener[] pcls = pcs.getPropertyChangeListeners();
		for (PropertyChangeListener pcl : pcls) {
			if (pcls.equals(pcl)) isRegistered = true;
		}
		return isRegistered;
	}
}