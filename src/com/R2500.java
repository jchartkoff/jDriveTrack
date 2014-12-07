package com;

import jssc.SerialPort;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.text.DecimalFormat;

public class R2500 implements RadioInterface {
	private static final int DELAY = 40;
	private static final int BAUD_RATE = SerialPort.BAUDRATE_38400;
	private static final int PARITY = SerialPort.PARITY_NONE;
	private static final int STOP_BITS = SerialPort.STOPBITS_1;
	private static final int DATA_BITS = SerialPort.DATABITS_8;
	private static final int FLOW_CONTROL_MODE = SerialPort.FLOWCONTROL_NONE;
	private static final int FLOW_CONTROL_IN = SerialPort.FLOWCONTROL_NONE;
	private static final int FLOW_CONTROL_OUT = SerialPort.FLOWCONTROL_NONE;
	private static final boolean DTR = true;
	private static final boolean RTS = true;

	private String modelNumber;
	private String serialNumber;
	private String signalOffset;
	private String dtmfDecode;
	private String scanStatus;
	private String waveFormData;
	private String protocol;
	private boolean power;
	private boolean afc = false; // AFC switch
	private boolean agc = false; // AGC switch
	private boolean attenuator = false; // 10db Attenuator switch
	private boolean progScan = false;
	private boolean squelchDelay = false;
	private String country; // Radio Country code
	private String dsp; // DSP chip installed (read only)
	private int filter; // IF bandwidth (3k, 6k, 15k, 50k, 230k)
	private String firmware; // Radio firmware revision
	private double frequency; // Frequency to which receiver is tuned
	private int ifShift = 128; // Filter passband offset (0-255, 128=Center)
	private int mode; // Demodulator (LSB,USB,AM,CW,DSP,FM,WFM)
	private boolean noiseBlanker = false; // Noise Blanker switch
	private int squelch; // Squelch setting (0-255)
	private int digitalSquelch = 0; // Subaudible digital squelch (0=Off)
	private boolean voiceScan = false; // VSC switch
	private int volume; // Audio output (0-255)
	private int rssi; // RSSI (0-255)
	private boolean busy = false;
	private double[] scanList = new double[10];
	private boolean[] scanSelectList = new boolean[10];
	private int[] percentList = new int[10];
	private int[] dBmList = new int [10];
	private boolean vfoMode = true;
	private String txData;
	private String rxData;
	private int currentChannel = 0;
	private int ber;
	private double[] berList = {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
	private ArrayList<String> writeStack = new ArrayList<String>();
	private String error = "";
	
	private final double pcrdbl1e6 = 1000000.0; // MHz to Hz converter
	private final String pcrBoolOn = "01"; // Logical one string
	private final String pcrBoolOff = "00"; // Logical zero string
	private final String pcrFmt = "00"; // Leading zero format padding for commands
	private final String pcrFrFmt = "0000000000"; // Leading zero format padding for frequency
	private final String pcrQueryFirm = "G4?"; // Get firmware revision
	private final String pcrQueryDSP = "GD?"; // DSP installed?
	private final String pcrQueryCountry = "GE?"; // Get country/region code
	private final String pcrQueryRxOn = "H1?"; // Is radio still on?
	private final String pcrQuerySql = "I0?"; // Get receive state (04=Closed, 0F=Open)
	private final String pcrQuerySig = "I1?"; // Get signal strength (0-255)
	private final String pcrCommandRxOn = "H101";
	private final String pcrCommandRxOff = "H100";
	private final String pcrCommandAutoUpdateOn = "G301";
	private final String pcrCommandAutoUpdateOff = "G300";
	private final String pcrCommandBandScopeOff = "ME0000100000000000000";
	private final String pcrCommandAGCPrefixRx1 = "J45";
	private final String pcrCommandAGCPrefixRx2 = "J65";
	private final String pcrCommandATTPrefixRx1 = "J47";
	private final String pcrCommandATTPrefixRx2 = "J67";
	private final String pcrCommandNBPrefixRx1 = "J46";
	private final String pcrCommandNBPrefixRx2 = "J66";
	private final String pcrCommandSquelchPrefixRx1 = "J41";
	private final String pcrCommandSquelchPrefixRx2 = "J61";
	private final String pcrCommandSquelchDelayPrefixRx1 = "J42";
	private final String pcrCommandSquelchDelayPrefixRx2 = "J62";
	private final String pcrCommandCTCSSPrefixRx1 = "J51";
	private final String pcrCommandCTCSSPrefixRx2 = "J71";
	private final String pcrCommandVoiceSquelchPrefixRx1 = "J50";
	private final String pcrCommandVoiceSquelchPrefixRx2 = "J70";
	private final String pcrCommandVolumeLevelPrefixRx1 = "J40";
	private final String pcrCommandVolumeLevelPrefixRx2 = "J60";
	private final String pcrCommandFrequencyPrefixRx1 = "K0";
	private final String pcrCommandFrequencyPrefixRx2 = "K1";
	private final String pcrCommandIFShiftPrefixRx1 = "J43";
	private final String pcrCommandIFShiftPrefixRx2 = "J63";
	private final String pcrCommandProgScanPrefixRx1 = "J48";
	private final String pcrCommandProgScanPrefixRx2 = "J68";
	private final String pcrReplyHeaderAck = "G0";
	private final String pcrReplyHeaderReceiveStatus = "I0";
	private final String pcrReplyHeaderRSSIChange = "I1";
	private final String pcrReplyHeaderSignalOffset = "I2";
	private final String pcrReplyHeaderDTMFDecode = "I3";
	private final String pcrReplyHeaderWaveFormData = "NE1";
	private final String pcrReplyHeaderScanStatus = "H9";
	private final String pcrReplyHeaderFirmware = "G4";
	private final String pcrReplyHeaderCountry = "GE";
	private final String pcrReplyHeaderPower = "H1";
	private final String pcrReplyHeaderProtocol = "G2";
	private final String pcrCommandClearAllSettingsRx1 = "J530000";
	private final String pcrCommandClearAllSettingsRx2 = "J730000";
	private final String pcrCommandResetAntennaDiversity = "J0000";
	private final String pcrCommandAntennaDiversityOn = "J0002";

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
	
	public R2500() {}

	private void heartbeatTimer() {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		heartbeatTimer = new Runnable() {public void run() {fireHeartbeat();}};
		heartbeatTimerHandle = scheduler.scheduleAtFixedRate(heartbeatTimer, 350, 350, TimeUnit.MILLISECONDS);
		scheduler.execute(heartbeatTimer);
	}
	
	private void rssiClearanceTimer() {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		rssiClearanceTimer = new Runnable() {public void run() {clearRSSI();}};
		rssiClearanceTimerHandle = scheduler.scheduleAtFixedRate(rssiClearanceTimer, 2000, 2000, TimeUnit.MILLISECONDS);
		scheduler.execute(rssiClearanceTimer);
	}
	
	private void busyClearanceTimer() {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		busyClearanceTimer = new Runnable() {public void run() {clearBusy();}};
		busyClearanceTimerHandle = scheduler.scheduleAtFixedRate(busyClearanceTimer, 2000, 2000, TimeUnit.MILLISECONDS);
		scheduler.execute(busyClearanceTimer);
	}
	
	private void scanTimer() {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scanTimer = new Runnable() {public void run() {scanAdvance();}};
		scanTimerHandle = scheduler.scheduleAtFixedRate(scanTimer, 700, 700, TimeUnit.MILLISECONDS);
		scheduler.execute(scanTimer);
	}

	private void startWriteTimer() {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		writeTimer = new Runnable() {public void run() {writeMessage();}};
		writeTimerHandle = scheduler.scheduleAtFixedRate(writeTimer, DELAY, DELAY, TimeUnit.MILLISECONDS);
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
	}

	@Override
	public boolean isAGC() {
		return agc;
	}

	@Override
	public void setAGC(boolean agc) {
		this.agc = agc;
		cmdSwitch(agc, pcrCommandAGCPrefixRx1);
		cmdSwitch(agc, pcrCommandAGCPrefixRx2);
	}

	@Override
	public boolean isAttenuator() {
		return attenuator;
	}

	@Override
	public void setAttenuator(boolean attenuator) {
		this.attenuator = attenuator;
		cmdSwitch(attenuator, pcrCommandATTPrefixRx1);
		cmdSwitch(attenuator, pcrCommandATTPrefixRx2);
	}

	@Override
	public boolean isNoiseBlanker() {
		return noiseBlanker;
	}

	@Override
	public void setNoiseBlanker(boolean noiseBlanker) {
		this.noiseBlanker = noiseBlanker;
		cmdSwitch(noiseBlanker, pcrCommandNBPrefixRx1);
		cmdSwitch(noiseBlanker, pcrCommandNBPrefixRx2);
	}

	@Override
	public boolean isVoiceScan() {
		return voiceScan;
	}

	@Override
	public void setVoiceScan(boolean voiceScan) {
		this.voiceScan = voiceScan;
		cmdSwitch(voiceScan, pcrCommandVoiceSquelchPrefixRx1);
		cmdSwitch(voiceScan, pcrCommandVoiceSquelchPrefixRx2);
	}

	@Override
	public boolean isSquelchDelay() {
		return squelchDelay;
	}

	@Override
	public void setSquelchDelay(boolean squelchDelay) {
		this.squelchDelay = squelchDelay;
		cmdSwitch(squelchDelay, pcrCommandSquelchDelayPrefixRx1);
		cmdSwitch(squelchDelay, pcrCommandSquelchDelayPrefixRx2);
	}
	
	@Override
	public boolean isProgScan() {
		return progScan;
	}

	@Override
	public void setProgScan(boolean progScan) {
		this.progScan = progScan;
		cmdSwitch(progScan, pcrCommandProgScanPrefixRx1);
		cmdSwitch(progScan, pcrCommandProgScanPrefixRx2);
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
			writeStack.add(pcrCommandIFShiftPrefixRx1 + Utility.integerToHex(ifShift));
			writeStack.add(pcrCommandIFShiftPrefixRx2 + Utility.integerToHex(ifShift));
			startWriteTimer();
		} else {
			error = "Invalid IF Shift Value - Please select Value between 0 and 255";
			pcs.firePropertyChange(ERROR_MESSAGE, null, error);
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
			writeStack.add(pcrCommandSquelchPrefixRx1 + Utility.integerToHex(squelch));
			writeStack.add(pcrCommandSquelchPrefixRx2 + Utility.integerToHex(squelch));
			startWriteTimer();
		} else {
			error = "Invalid Squelch Value - Please select Value between 0 and 255";
			pcs.firePropertyChange(ERROR_MESSAGE, null, error);
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
		if (volume >= 0 && volume <= 255) {
			this.volume = volume;
			writeStack.add(pcrCommandVolumeLevelPrefixRx1 + Utility.integerToHex(volume));
			writeStack.add(pcrCommandVolumeLevelPrefixRx2 + Utility.integerToHex(volume));
			startWriteTimer();
		} else {
			error = "Invalid Volume Level - Please select Value between 0 and 255";
			pcs.firePropertyChange(ERROR_MESSAGE, null, error);
		}
	}

	@Override
	public int getToneSquelch() {
		return digitalSquelch;
	}

	@Override
	public void setToneSquelch(int toneSquelch) {
		if (toneSquelch >= 0 && toneSquelch <= 51) {
			digitalSquelch = toneSquelch;
			writeStack.add(pcrCommandCTCSSPrefixRx1 + Utility.integerToHex(digitalSquelch));
			writeStack.add(pcrCommandCTCSSPrefixRx2 + Utility.integerToHex(digitalSquelch));
			startWriteTimer();
		} else {
			error = "Invalid Tone Squelch Code - Please select Value between 0 and 51";
			pcs.firePropertyChange(ERROR_MESSAGE, null, error);
		}
	}

	@Override
	public int getDigitalSquelch() {
		return 0;
	}

	@Override
	public void setDigitalSquelch(int digitalSquelch) {
	}

	@Override
	public double getFrequency() {
		return frequency;
	}

	@Override
	public void setFrequency(double frequency) {
		if (frequency >= 0.01 && frequency <= 3299.999) {
			this.frequency = frequency;
			sendFrequencyToPcr(frequency, mode, filter);
		} else {
			error = "Invalid Frequency Entry - Please enter Value between 10 kHz and 3300 MHz";
			pcs.firePropertyChange(ERROR_MESSAGE, null, error);
		}
	}

	@Override
	public int getMode() {
		return mode;
	}

	@Override
	public void setMode(int mode) {
		if (mode >= 0 && mode <= 10 && mode != 11 && mode != 6 && mode != 5) {
			switch (mode) {
				case 0: // LSB
					this.mode = 0;
					break;
				case 1: // USB
					this.mode = 1;
					break;
				case 2: // AM
					this.mode = 2;
					break;
				case 3: // CW
					this.mode = 3;
					break;
				case 4: // CW_R
					this.mode = 3;
					break;
				case 5: // DSP
					this.mode = -1;
					break;
				case 6: // DIG
					this.mode = -1;
					break;
				case 7: // Narrow FM
					this.mode = 5;
					break;
				case 8: // Wide FM
					this.mode = 6;
					break;
				case 9: // P25
					this.mode = 8;
					break;
				case 10: // DSTAR
					this.mode = 7;
					break;
				case 11: // DMR
					this.mode = -1;
					break;
			}
			sendFrequencyToPcr(frequency, mode, filter);
		} else {
			error = "Invalid Mode Selection - DMR, DIG, and DSP are not supported by this radio.";
			pcs.firePropertyChange(ERROR_MESSAGE, null, error);
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
		if (filter >= 3 && filter <= 7) {
			switch (filter) {
				case 0: // 300 Hz
					this.filter = -1;
					break;
				case 1: // 500 Hz
					this.filter = -1;
					break;
				case 2: // 2.4 kHz
					this.filter = -1;
					break;
				case 3: // 3.0 kHz
					this.filter = 0;
					break;
				case 4: // 6.0 kHz
					this.filter = 1;
					break;
				case 5: // 15 kHz
					this.filter = 2;
					break;
				case 6: // 50 kHz
					this.filter = 3;
					break;
				case 7: // 230 kHz
					this.filter = 4;
					break;
			}
			sendFrequencyToPcr(frequency, mode, filter);
		} else {
			error = "Invalid Filter Bandwidth Selection - Only 3 kHz, 6 kHz, 15 kHz, 50 kHz and 230 kHz supported by this radio.";
			pcs.firePropertyChange(ERROR_MESSAGE, null, error);
		}
	}

	private void decode(String msg) {
		int iHStart;
		int iGStart;
		int iIStart;
		boolean send;
		String rStr = "";
		
		for (int i = 0; i < msg.length(); i++) {
			char c = msg.charAt(i);
			if (Character.isLetter(c) || Character.isDigit(c)) {
				rStr = rStr + c;
			}
		}

		do {
			send = false;
			iHStart = rStr.indexOf("H", 0);
			iIStart = rStr.indexOf("I", 0);
			iGStart = rStr.indexOf("G", 0);
			if (iHStart >= 0 && rStr.length() >= iHStart + 4) {
				pcrDecoder(rStr.substring(iHStart, iHStart + 4));
				if (rStr.length() >= 4) rStr = rStr.substring(iHStart + 4);
				send = true;
			}
			if (iIStart >= 0 && rStr.length() >= iIStart + 4) {
				pcrDecoder(rStr.substring(iIStart, iIStart + 4));
				if (rStr.length() >= 4)	rStr = rStr.substring(iIStart + 4);
				send = true;
			}
			if (iGStart >= 0 && rStr.length() >= iGStart + 4) {
				pcrDecoder(rStr.substring(iGStart, iGStart + 4));
				if (rStr.length() >= 4)	rStr = rStr.substring(iGStart + 4);
				send = true;
			}
		} while (send);
	}

	private void pcrDecoder(String data) {
		pcs.firePropertyChange(RX_DATA, null, data);
		rxData = data;
		switch (data.substring(0, 2)) {
			case pcrReplyHeaderAck:
				switch (data.substring(2, 4)) {
					case "01":
						System.err.println("PCR2500 - Invalid Operation Code");
						break;
					case "00":
						System.out.println("PCR2500 - Reply Header Acknowledge");
						break;
					}
			break;

		case pcrReplyHeaderReceiveStatus:
			busy = decodeBusyStatus(data.substring(2, 4));
			pcs.firePropertyChange(BUSY, null, busy);
			busyClearanceTimerHandle.cancel(true);
			busyClearanceTimer();
			break;

		case pcrReplyHeaderRSSIChange:
			if (data.length() == 4) {
				rssi = Integer.valueOf(data.substring(2, 4), 16);
				percentList[0] = (int) Math.round(getPercent());
				dBmList[0] = (int) Math.round(getdBm());
				pcs.firePropertyChange(RSSI, null, rssi);
				rssiClearanceTimerHandle.cancel(true);
				rssiClearanceTimer();
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

		case pcrReplyHeaderPower:
			power = decodePowerStatus(data.substring(2, 4));
			pcs.firePropertyChange(RadioInterface.POWER, null, power);
			break;

		case pcrReplyHeaderProtocol:
			protocol = data.substring(2, 4);
			pcs.firePropertyChange(RadioInterface.PROTOCOL, null, protocol);
			break;

		default:
			error = "The Radio has Reported Incorrect Data - Please contact technical support.";
			pcs.firePropertyChange(ERROR_MESSAGE, null, error);
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
			writeStack.trimToSize();
			writeStack.add(pcrCommandRxOn);
			writeStack.add(pcrCommandClearAllSettingsRx1);
			writeStack.add(pcrCommandClearAllSettingsRx2);
			writeStack.add(pcrCommandResetAntennaDiversity);
			writeStack.add(pcrCommandAutoUpdateOff);
			writeStack.add(pcrCommandBandScopeOff);
			writeStack.add(pcrCommandAntennaDiversityOn);
			setFrequency(frequency);
			setSquelchDelay(squelchDelay);
			setAGC(agc);
			setAttenuator(attenuator);
			setNoiseBlanker(noiseBlanker);
			setVoiceScan(voiceScan);
			setProgScan(progScan);
			setIFShift(ifShift);
			setToneSquelch(digitalSquelch);
			setVolume(volume);
			setSquelch(squelch);
			writeStack.add(pcrQueryFirm);
			writeStack.add(pcrQueryDSP);
			writeStack.add(pcrQueryCountry);
			writeStack.add(pcrCommandAutoUpdateOn);
			rssiClearanceTimer();
			busyClearanceTimer();
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
		DecimalFormat pcrFormat = new DecimalFormat(pcrFmt);
		String strFr1 = pcrCommandFrequencyPrefixRx1 + freqFormat.format(freq * pcrdbl1e6);
		strFr1 = strFr1 + pcrFormat.format(mode) + pcrFormat.format(filter);
		strFr1 = strFr1 + pcrFmt;
		String strFr2 = pcrCommandFrequencyPrefixRx2 + freqFormat.format(freq * pcrdbl1e6);
		strFr2 = strFr2 + pcrFormat.format(mode) + pcrFormat.format(filter);
		strFr2 = strFr2 + pcrFmt;
		if (mode >= 0 && filter >= 0) { 
			writeStack.add(strFr1);
			writeStack.add(strFr2);
		}
		startWriteTimer();
	}

	private void cmdSwitch(final boolean bln, final String cmd) {
		String temp = pcrBoolOff;
		if (bln) temp = pcrBoolOn;
		writeStack.add(cmd + temp);
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
		rssiClearanceTimerHandle.cancel(true);
		rssiClearanceTimer();
	}

	private void clearBusy() {
		pcs.firePropertyChange(BUSY, null, false);
		busy = false;
		busyClearanceTimerHandle.cancel(true);
		busyClearanceTimer();
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
	public double getdBm() {
		return rssiTodBm(rssi);
	}

	@Override
	public double getPercent() {
		return 100 - (((getdBm() * -1.0) - 30) * 1.25);
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
			scanTimerHandle.cancel(true);
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
	public double getTestdBmValue() {
		return 0;
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
