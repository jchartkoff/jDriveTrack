package com;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jssc.SerialPort;

public class Pcr1k implements RadioInterface {
	private static final int BAUD_RATE = SerialPort.BAUDRATE_38400;
	private static final int PARITY = SerialPort.PARITY_NONE;
	private static final int STOP_BITS = SerialPort.STOPBITS_1;
	private static final int DATA_BITS = SerialPort.DATABITS_8;
	private static final int FLOW_CONTROL_MODE = SerialPort.FLOWCONTROL_NONE;
	private static final int FLOW_CONTROL_IN = SerialPort.FLOWCONTROL_NONE;
	private static final int FLOW_CONTROL_OUT = SerialPort.FLOWCONTROL_NONE;
	private static final boolean DTR = true;
	private static final boolean RTS = true;
	
	private static final double pcrdbl1e6 = 1000000.0; // MHz to Hz converter
	private static final String pcrBoolOn = "01"; // Logical one string
	private static final String pcrBoolOff = "00"; // Logical zero string
	private static final String pcrFmt = "00"; // Leading zero format padding for commands
	private static final String pcrFrFmt = "0000000000"; // Leading zero format padding for frequency
	private static final String pcrQueryFirm = "G4?"; // Get firmware revision
	private static final String pcrQueryDSP = "GD?"; // DSP installed?
	private static final String pcrQueryCountry = "GE?"; // Get country/region code
	private static final String pcrQueryRxOn = "H1?"; // Is radio still on?
	private static final String pcrQuerySql = "I0?"; // Get receive state (04=Closed, 07=Open)
	private static final String pcrQuerySig = "I1?"; // Get signal strength (0-255)
	private static final String pcrCommandRxOn = "H101";
	private static final String pcrCommandRxOff = "H100";
	private static final String pcrCommandAutoUpdateOn = "G301";
	private static final String pcrCommandAutoUpdateOff = "G300";
	private static final String pcrCommandBandScopeOff = "ME0000100000000000000";
	private static final String pcrCommandAFCPrefix = "J44";
	private static final String pcrCommandAGCPrefix = "J45";
	private static final String pcrCommandATTPrefix = "J47";
	private static final String pcrCommandNBPrefix = "J46";
	private static final String pcrCommandSquelchPrefix = "J41";
	private static final String pcrCommandCTCSSPrefix = "J51";
	private static final String pcrCommandVoiceScanPrefix = "J50";
	private static final String pcrCommandVolumeLevelPrefix = "J40";
	private static final String pcrCommandFrequencyPrefix = "K0";
	private static final String pcrCommandIFShiftPrefix = "J43";
	private static final String pcrReplyHeaderAck = "G0";
	private static final String pcrReplyHeaderReceiveStatus = "I0";
	private static final String pcrReplyHeaderRSSIChange = "I1";
	private static final String pcrReplyHeaderSignalOffset = "I2";
	private static final String pcrReplyHeaderDTMFDecode = "I3";
	private static final String pcrReplyHeaderWaveFormData = "NE1";
	private static final String pcrReplyHeaderScanStatus = "H9";
	private static final String pcrReplyHeaderFirmware = "G4";
	private static final String pcrReplyHeaderCountry = "GE";
	private static final String pcrReplyHeaderOptionalDevice = "GD";
	private static final String pcrReplyHeaderPower = "H1";
	private static final String pcrReplyHeaderProtocol = "G2";

	private String signalOffset;
	private String dtmfDecode;
	private String scanStatus;
	private String waveFormData;
	private String modelNumber;
	private String serialNumber;
	private String protocol;
	private boolean power;
	private boolean afc;
	private boolean agc;
	private boolean attenuator;
	private String country; 
	private String dsp;
	private int filter;
	private String firmware;
	private double frequency;
	private int ifShift;
	private int mode;
	private boolean noiseBlanker;
	private int squelch;
	private int digitalSquelch;
	private boolean voiceScan;
	private int volume;
	private int rssi;
	private boolean busy;
	private double[] scanList = new double[10];
	private boolean[] scanSelectList = new boolean[10];
	private int[] percentList = new int[10];
	private int[] dBmList = new int [10];
	private boolean vfoMode;
	private String txData;
	private String rxData;
	private int currentChannel = 0;
	private int ber;
	private double[] berList = new double [10];
	private double testdBmValue = -90;
	private boolean getStronger = true;
	
	private Calibrate calibrate = null;
	
	private Runnable scanTimer;
	private Runnable heartbeatTimer;
	private Runnable rssiClearanceTimer;
	private Runnable busyClearanceTimer;
	private Runnable writeTimer;
	
	private ScheduledFuture<?> heartbeatTimerHandle = null;
	private ScheduledFuture<?> rssiClearanceTimerHandle = null;
	private ScheduledFuture<?> busyClearanceTimerHandle = null;
	private ScheduledFuture<?> scanTimerHandle = null;
	private ScheduledFuture<?> writeTimerHandle = null;
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private ArrayList<String> writeStack = new ArrayList<String>();
	
	public Pcr1k() { }

	private void heartbeatTimer() {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		heartbeatTimer = new Runnable() {public void run() {fireHeartbeat();}};
		heartbeatTimerHandle = scheduler.scheduleAtFixedRate(heartbeatTimer, 250, 250, TimeUnit.MILLISECONDS);
		scheduler.execute(heartbeatTimer);
	}
	
	private void rssiClearanceTimer() {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		rssiClearanceTimer = new Runnable() {public void run() {clearRSSI();}};
		rssiClearanceTimerHandle = scheduler.scheduleAtFixedRate(rssiClearanceTimer, 1500, 1500, TimeUnit.MILLISECONDS);
		scheduler.execute(rssiClearanceTimer);
	}
	
	private void busyClearanceTimer() {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		busyClearanceTimer = new Runnable() {public void run() {clearBusy();}};
		busyClearanceTimerHandle = scheduler.scheduleAtFixedRate(busyClearanceTimer, 1000, 1000, TimeUnit.MILLISECONDS);
		scheduler.execute(busyClearanceTimer);
	}
	
	private void scanTimer() {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scanTimer = new Runnable() {public void run() {scanAdvance();}};
		scanTimerHandle = scheduler.scheduleAtFixedRate(scanTimer, 700, 700, TimeUnit.MILLISECONDS);
		scheduler.execute(scanTimer);
	}

	private void startWriteTimer() {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		writeTimer = new Runnable() {public void run() {writeMessage();}};
		writeTimerHandle = scheduler.scheduleAtFixedRate(writeTimer, 50, 50, TimeUnit.MILLISECONDS);
		scheduler.execute(writeTimer);
	}
	
	@Override
	public void dataInput(byte[] buffer) {
		decode(new String(buffer));
	}
	
	@Override
	public boolean isAFC() {
		return afc;
	}

	@Override
	public void setAFC(boolean afc) {
		this.afc = afc;
		cmdSwitch(afc, pcrCommandAFCPrefix);
	}

	@Override
	public boolean isAGC() {
		return agc;
	}

	@Override
	public void setAGC(boolean agc) {
		this.agc = agc;
		cmdSwitch(agc, pcrCommandAGCPrefix);
	}

	@Override
	public boolean isAttenuator() {
		return attenuator;
	}

	@Override
	public void setAttenuator(boolean attenuator) {
		this.attenuator = attenuator;
		cmdSwitch(attenuator, pcrCommandATTPrefix);
	}

	@Override
	public boolean isNoiseBlanker() {
		return noiseBlanker;
	}

	@Override
	public void setNoiseBlanker(boolean noiseBlanker) {
		this.noiseBlanker = noiseBlanker;
		cmdSwitch(noiseBlanker, pcrCommandNBPrefix);
	}

	@Override
	public boolean isVoiceScan() {
		return voiceScan;
	}

	@Override
	public void setVoiceScan(boolean voiceScan) {
		this.voiceScan = voiceScan;
		cmdSwitch(voiceScan, pcrCommandNBPrefix);
	}

	@Override
	public String getDSP() {
		return dsp;
	}

	@Override
	public String getFirmware() {
		return firmware;
	}

	@Override
	public String getCountry() {
		return country;
	}

	@Override
	public int getIFShift() {
		return ifShift;
	}

	@Override
	public void setIFShift(int ifShift) {
		if (ifShift >= 0 && ifShift <= 255) {
			this.ifShift = ifShift;
		    writeStack.add(pcrCommandIFShiftPrefix + Utility.integerToHex(ifShift));
		    startWriteTimer();
		}
	}

	@Override
	public int getSquelch() {
		return squelch;
	}

	@Override
	public void setSquelch(int squelch) {
		if (squelch >= 0 && squelch <= 255) {
			this.squelch = squelch;
		    writeStack.add(pcrCommandSquelchPrefix + Utility.integerToHex(squelch));
		    startWriteTimer();
		}
	}

	@Override
	public boolean isBusy() {
		return busy;
	}

	@Override
	public int getVolume() {
		return volume;
	}

	@Override
	public void setVolume(int volume) {
		if (squelch >= 0 && squelch <= 255) {
			this.volume = volume;
		    writeStack.add(pcrCommandVolumeLevelPrefix + Utility.integerToHex(volume));
		    startWriteTimer();
		}
	}
	
	@Override
	public int getToneSquelch() {
		return digitalSquelch;
	}

	@Override
	public void setToneSquelch(int toneSquelch) {
		if (toneSquelch >= 0 && toneSquelch <= 51) {
			this.digitalSquelch = toneSquelch;
		    writeStack.add(pcrCommandCTCSSPrefix	+ Utility.integerToHex(toneSquelch));
		    startWriteTimer();
		}
	}
	
	@Override
	public int getDigitalSquelch() {
		return 0;
	}

	@Override
	public void setDigitalSquelch(int digitalSquelch) { }

	@Override
	public double getFrequency() {
		return frequency;
	}

	@Override
	public void setFrequency(double frequency) {
		if (frequency >= 0.01 && frequency <= 1300.0) {
			this.frequency = frequency;
		    sendFrequencyToPcr(frequency, mode, filter);
		}
	}
	
	@Override
	public int getMode() {
		return mode;
	}

	@Override
	public void setMode(int mode) {
		if (mode >= 0 && mode <= 8) {
			switch (mode) {
				case 0:
					this.mode = 0;
					break;
				case 1:
					this.mode = 1;
					break;
				case 2:
					this.mode = 2;
					break;
				case 3:
					this.mode = 3;
					break;
				case 4:
					this.mode = 3;
					break;
				case 5:
					this.mode = 4;
					break;
				case 6:
					this.mode = 5;
					break;
				case 7:
					this.mode = 5;
					break;
				case 8:
					this.mode = 6;
					break;
			}
			sendFrequencyToPcr(frequency, mode, filter);
		}
	}

	@Override
	public int getFilter() {
		return filter;
	}

	@Override
	public int getRSSI() {
		return rssi;
	}

	@Override
	public void setFilter(int filter) {
		if (filter >= 0 && filter <= 7) {
			switch (filter) {
				case 0:
					this.filter = 0;
					break;
				case 1:
					this.filter = 0;
					break;
				case 2:
					this.filter = 0;
					break;
				case 3:
					this.filter = 0;
					break;
				case 4:
					this.filter = 1;
					break;
				case 5:
					this.filter = 2;
					break;
				case 6:
					this.filter = 3;
					break;
				case 7:
					this.filter = 4;
					break;
			}
			sendFrequencyToPcr(frequency, mode, filter);
		}
	}
	
	private void decode(String msg) {
		int iHStart = 0;
		int iGStart = 0;
		int iIStart = 0;
		boolean sent;
		String rStr = "";

		for (int i = 0; i < msg.length(); i++) {
			char c = msg.charAt(i);
			if (Character.isLetter(c) || Character.isDigit(c)) {
				rStr = rStr + c;
			}
		}

		do {
			sent = false;
			iHStart = rStr.indexOf("H", 0);
			iIStart = rStr.indexOf("I", 0);
			iGStart = rStr.indexOf("G", 0);
			if (iHStart >= 0 && rStr.length() >= iHStart + 4) {
				pcrDecoder(rStr.substring(iHStart, iHStart + 4));
				if (rStr.length() >= 4)
					rStr = rStr.substring(iHStart + 4);
				sent = true;
			}
			if (iIStart >= 0 && rStr.length() >= iIStart + 4) {
				pcrDecoder(rStr.substring(iIStart, iIStart + 4));
				if (rStr.length() >= 4)
					rStr = rStr.substring(iIStart + 4);
				sent = true;
			}
			if (iGStart >= 0 && rStr.length() >= iGStart + 4) {
				pcrDecoder(rStr.substring(iGStart, iGStart + 4));
				if (rStr.length() >= 4)
					rStr = rStr.substring(iGStart + 4);
				sent = true;
			}
		} while(sent);
	}

	private void pcrDecoder(String data) {
		pcs.firePropertyChange(RX_DATA, null, data);
		rxData = data;
		switch (data.substring(0, 2)) {
			case pcrReplyHeaderAck:
				switch (data.substring(2, 4)) {
					case "01":
						pcs.firePropertyChange(ERROR, null, "Received Data Error");
						break;
					case "00":
						pcs.firePropertyChange(ACK, null, "Data Acknowledged");
						break;
					}
				break;

			case pcrReplyHeaderReceiveStatus:
				busy = decodeBusyStatus(data.substring(2, 4));
				pcs.firePropertyChange(RadioInterface.BUSY, null, busy);
				if (busyClearanceTimer != null) busyClearanceTimerHandle.cancel(true);
				break;
	
			case pcrReplyHeaderRSSIChange:
				if (data.length() == 4) {
					rssi = Integer.valueOf(data.substring(2, 4), 16);
					percentList[0] = (int) Math.round(getPercent());
					dBmList[0] = (int) Math.round(getdBm());
					pcs.firePropertyChange(RadioInterface.RSSI, null, rssi);
					if (rssiClearanceTimer != null) rssiClearanceTimerHandle.cancel(true);
				}
				break;

			case pcrReplyHeaderSignalOffset:
				signalOffset = data.substring(2, 4);
				pcs.firePropertyChange(RadioInterface.SIGNAL_OFFSET, null, signalOffset);
				break;
	
			case pcrReplyHeaderDTMFDecode:
				dtmfDecode = data.substring(2, 4);
				pcs.firePropertyChange(RadioInterface.DTMF_DECODE, null, dtmfDecode);
				break;
	
			case pcrReplyHeaderWaveFormData:
				waveFormData = data.substring(2, 4);
				pcs.firePropertyChange(RadioInterface.WAVEFORM_DATA, null, waveFormData);
				break;
	
			case pcrReplyHeaderScanStatus:
				scanStatus = data.substring(2, 4);
				pcs.firePropertyChange(RadioInterface.SCAN_STATUS, null, scanStatus);
				break;
	
			case pcrReplyHeaderFirmware:
				firmware = data.substring(2, 4);
				pcs.firePropertyChange(RadioInterface.FIRMWARE, null, firmware);
				break;
	
			case pcrReplyHeaderCountry:
				country = data.substring(2, 4);
				pcs.firePropertyChange(RadioInterface.COUNTRY, null, country);
				break;
	
			case pcrReplyHeaderOptionalDevice:
				dsp = data.substring(2, 4);
				pcs.firePropertyChange(RadioInterface.DSP, null, dsp);
				break;
	
			case pcrReplyHeaderPower:
				power = decodePowerStatus(data.substring(2, 4));
				pcs.firePropertyChange(RadioInterface.POWER, null, power);
				break;
	
			case pcrReplyHeaderProtocol:
				protocol = data.substring(2, 4);
				pcs.firePropertyChange(RadioInterface.PROTOCOL, null, protocol);
				break;
	
			default:
				System.err.println("PCR1000 - Invalid Property Value");
				break;
		}
	}

	private boolean decodePowerStatus(String str) {
		switch (str) {
			case "00":
				return false;
			case "01":
				return true;
		}
		return false;
	}
	
	private boolean decodeBusyStatus(String str) {
		switch (str) {
			case "04":
				return false;
			case "07":
				return true;
			}
		return false;
	}

	private void writeMessage() {
		if (writeStack.size() > 0) {
			int end = 0;
			String data = writeStack.get(end);
			data += "\n";
			txData = data;
			pcs.firePropertyChange(TX_DATA, null, data);
			if (writeStack.get(end).equals("serialPortClose")) {
				pcs.firePropertyChange(CLOSE_SERIAL_PORT, null, true);
			} else {
				pcs.firePropertyChange(SEND_TO_SERIAL_PORT, null, data);
			}
			writeStack.remove(end);
			writeStack.trimToSize();
			if (writeStack.size() == 0) writeTimerHandle.cancel(true);
		}
	}
	
	@Override
	public void startRadio() {
		if (writeStack.size() > 0) writeStack.removeAll(writeStack);
    	rssiClearanceTimer();
		busyClearanceTimer();
		writeStack.add(pcrCommandRxOn);
		writeStack.add(pcrCommandAutoUpdateOff);
		writeStack.add(pcrCommandBandScopeOff);
		cmdSwitch(afc, pcrCommandAFCPrefix);
		cmdSwitch(agc, pcrCommandAGCPrefix);
		cmdSwitch(attenuator, pcrCommandATTPrefix);
		cmdSwitch(noiseBlanker, pcrCommandNBPrefix);
		cmdSwitch(voiceScan, pcrCommandVoiceScanPrefix);
		setFrequency(frequency);
		setIFShift(ifShift);
		setToneSquelch(digitalSquelch);
		setVolume(volume);
		setSquelch(squelch);
		writeStack.add(pcrCommandAutoUpdateOn);
		writeStack.add(pcrQueryFirm);
		writeStack.add(pcrQueryDSP);
		writeStack.add(pcrQueryCountry);
		heartbeatTimer();
		startWriteTimer();
	}

	@Override
	public void stopRadio() {
		if (rssiClearanceTimer != null) rssiClearanceTimerHandle.cancel(true);
		if (busyClearanceTimer != null) busyClearanceTimerHandle.cancel(true);
		if (heartbeatTimer != null) heartbeatTimerHandle.cancel(true);
		if (scanTimer != null) scanTimerHandle.cancel(true);
		writeStack.add(pcrCommandAutoUpdateOff);
		writeStack.add(pcrCommandRxOff);
		writeStack.add(pcrQueryRxOn);
		startWriteTimer();
	}	

	private void sendFrequencyToPcr(double freq, int mode, int filter) {
    	DecimalFormat freqFormat = new DecimalFormat(pcrFrFmt);
		String strFr = pcrCommandFrequencyPrefix + freqFormat.format(freq * pcrdbl1e6);
		DecimalFormat pcrFormat = new DecimalFormat(pcrFmt);
		strFr = strFr + pcrFormat.format(mode) + pcrFormat.format(filter);
		strFr = strFr + pcrFmt;
    	writeStack.add(strFr);
    	startWriteTimer();
	}

	private void cmdSwitch(boolean bln, String cmd) {
		String strTemp = pcrBoolOff;
		if (bln) strTemp = pcrBoolOn;
		writeStack.add(cmd + strTemp);
		startWriteTimer();
	}

	private void fireHeartbeat() {
		writeStack.add(pcrQueryRxOn);
		writeStack.add(pcrQuerySql);
		writeStack.add(pcrQuerySig);
		startWriteTimer();
	}
	
	private void scanAdvance() {
		for (int i = 0; i < scanList.length; i++) {
			if (scanSelectList[i]) {
				currentChannel = i;
				setFrequency(scanList[i]);
				percentList[i] = (int) Math.round(getPercent());
				dBmList[i] = (int) Math.round(getdBm());
			}
		}
	}
	
	private void clearRSSI() {
		pcs.firePropertyChange(RSSI, null, 0);
		rssi = 0;
		for (int i = 0; i < dBmList.length; i++) {
			dBmList[i] = 0;
		}
		for (int i = 0; i < percentList.length; i++) {
			percentList[i] = 0;
		}
		if (rssiClearanceTimer != null) rssiClearanceTimerHandle.cancel(true);
		rssiClearanceTimer();
	}

	private void clearBusy() {
		pcs.firePropertyChange(BUSY, null, false);
		busy = false;
		if (busyClearanceTimer != null) busyClearanceTimerHandle.cancel(true);
		busyClearanceTimer();
	}

	@Override
	public double getdBm() {
		return rssiTodBm(rssi);
	}

	@Override
	public double getPercent() {
		double temp = 100 - (((getdBm() * -1.0) - 30) * 1.25);
		return temp;
	}

	@Override
	public int[] getPercentList() {
		return percentList;
	}
	
	@Override
	public void setScanList(double[] scanList) {
		this.scanList = scanList;
	}

	@Override
	public double[] getScanList() {
		return scanList;
	}
	
	@Override
	public int[] getdBmList() {
		return dBmList;
	}
	
	@Override
	public void setScanSelectList(boolean[] scanSelectList) {
		this.scanSelectList = scanSelectList;
	}

	@Override
	public boolean[] getScanSelectList() {
		return scanSelectList;
	}
	
	@Override
	public void setVfoMode(boolean vfoMode) {
		this.vfoMode = vfoMode;
		if (vfoMode) {
			currentChannel = 0;
			if (scanTimer != null) scanTimerHandle.cancel(true);
		} else {
			scanTimer();
		}
	}
	
	@Override
	public boolean isVfoMode() {
		return vfoMode;
	}

	@Override
	public String getTransmittedData() {
		return "Tx Data: " + txData;
	}

	@Override
	public String getReceivedData() {
		return "Rx Data: " + rxData;
	}

	private int rssiTodBm(int rssi) {
		return calibrate.getdBm(rssi);
	}

	@Override
	public int getCurrentChannel() {
		return currentChannel;
	}

	@Override
	public void setCalibrationFile(String calFileName) {
		try {
			calibrate = new Calibrate(calFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getModelNumber() {
		return modelNumber;
	}
	
	@Override
	public String getSerialNumber() {
		return serialNumber;
	}

	@Override
	public double getBER() {
		return ber;
	}

	@Override
	public void sampleRSSIValues(boolean sampleRSSI) {

	}

	@Override
	public void sampleBERValues(boolean sampleBER) {
		
	}

	@Override
	public double[] getBERList() {
		return berList;
	}

	@Override
	public boolean isProgScan() {
		return false;
	}

	@Override
	public void setProgScan(boolean scan) {
		
	}

	@Override
	public boolean isSquelchDelay() {
		return false;
	}

	@Override
	public void startScan() {
		scanTimer();
	}
	
	@Override
	public void stopScan() {
		scanTimerHandle.cancel(true);
	}
	
	@Override
	public void setSquelchDelay(boolean squelchDelay) {
		
	}

	@Override
	public double getTestdBmValue() {
		if (testdBmValue <= -90) getStronger = true;
		if (testdBmValue >= -30) getStronger = false;	
		if (getStronger) testdBmValue += 0.001;
			else testdBmValue -= 0.001;
		return testdBmValue;
	}

	@Override
	public boolean getDefaultDTR() {
		return DTR;
	}
	
	@Override
	public boolean getDefaultRTS() {
		return RTS;
	}
	
	@Override
	public int getDefaultBaudRate() {
		return BAUD_RATE;
	}
	
	@Override
	public int getDefaultParity() {
		return PARITY;
	}
	
	@Override
	public int getDefaultStopBits() {
		return STOP_BITS;
	}
	
	@Override
	public int getDefaultDatBits() {
		return DATA_BITS;
	}
	
	@Override
	public int getDefaultFlowControlMode() {
		return FLOW_CONTROL_MODE;
	}
	
	@Override
	public int getDefaultFlowControlIn() {
		return FLOW_CONTROL_IN;
	}
	
	@Override
	public int getDefaultFlowControlOut() {
		return FLOW_CONTROL_OUT;
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
