package radios;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.DecimalFormat;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import jdrivetrack.CalibrationDataObject;
import jdrivetrack.Utility;
import interfaces.RadioInterface;
import jssc.SerialPort;
import types.EmissionDesignator;

public class PCR2500 implements RadioInterface {
	private static final String versionUID = "3045264089232056863";
	private static final int BAUD_RATE = SerialPort.BAUDRATE_38400;
	private static final int PARITY = SerialPort.PARITY_NONE;
	private static final int STOP_BITS = SerialPort.STOPBITS_1;
	private static final int DATA_BITS = SerialPort.DATABITS_8;
	private static final int FLOW_CONTROL_IN = SerialPort.FLOWCONTROL_NONE;
	private static final int FLOW_CONTROL_OUT = SerialPort.FLOWCONTROL_NONE;
	private static final boolean CTS_SUPPORT = false;
	private static final boolean DTR = true;
	private static final boolean RTS = true;
	private static final double MINIMUM_RX_FREQ = 0.050;
	private static final double MAXIMUM_RX_FREQ = 3299.999;
	private static final double MINIMUM_TX_FREQ = -1;
	private static final double MAXIMUM_TX_FREQ = -1;
	private static final long LONG_PAUSE_PERIOD = 250;
	private static final long INTER_COMMAND_PAUSE_PERIOD = 150;
	private static final String LONG_PAUSE = "LONG_PAUSE";
	private static final String INTER_COMMAND_PAUSE = "INTER_COMMAND_PAUSE";
	private static final String SHUTDOWN_REQ = "SHUTDOWN";
	private static final int SCAN_PERIOD = 700;
	private static final int TERMINATION_WAIT_PERIOD = 250;
	private static final int BUSY_CLEARANCE_PERIOD = 500;
	private static final int RSSI_CLEARANCE_PERIOD = 500;
	private static final int HEARTBEAT_PERIOD = 350;
	private static final int NOISE_FLOOR = -120;
	private static final int WRITE_PAUSE = 30;
	
	private static final String[] AVAILABLE_BAUD_RATES = {"300","1200","4800","9600","19200","38400"};
	
	private static final String[] AVAILABLE_FILTERS = {"2.8 kHz","6 kHz","15 kHz","50 kHz","230 kHz"};

	private static final String[] TONE_SQUELCH_VALUES = {
		"OFF","67.0","69.3","71.0","71.9","74.4","77.0","79.7","82.5","85.4","88.5","91.5","94.8",
		"97.4","100.0","103.5","107.2","110.9","114.8","118.8","123.0","127.3","131.8","136.5",
		"141.3","146.2","151.4","156.7","159.8","162.2","165.5","167.9","173.8","177.3","179.9",
		"183.5","186.2","189.9","192.8","196.6","199.5","203.5","206.5","210.7","218.1","225.7",
		"229.1","233.6","241.8","250.3","254.1"};
	
	private static final String[] DIGITAL_SQUELCH_VALUES = { "OFF", "123", "693", "710" };
	
	private static final EmissionDesignator[] EMISSION_DESIGNATORS = { 
		new EmissionDesignator("2K80J3EZ"),
		new EmissionDesignator("2K80J3EY"),
		new EmissionDesignator("6K00A3E"),
		new EmissionDesignator("150HA1A"),
		new EmissionDesignator("11K2F3E"),
		new EmissionDesignator("200KF8E"),
		new EmissionDesignator("6K00F7W"),
		new EmissionDesignator("8K10F1E")
	};
	
	private static final String crlf = "\r\n";
	private static final double pcrdbl1e6 = 1000000.0;
	private static final String pcrBoolOn = "01";
	private static final String pcrBoolOff = "00";
	private static final String pcrFmt = "00";
	private static final String pcrFrFmt = "0000000000";
	
	private static final String pcrQueryFirmwareVersion = "G4?";
	private static final String pcrQueryDSP = "GD?";
	private static final String pcrQueryCountry = "GE?";
	private static final String pcrQueryRxOn = "H1?";
	private static final String pcrQuerySquelchStatus = "I0?";
	private static final String pcrQuerySignalStrength = "I1?";
	private static final String pcrReplyHeaderAcknowledge = "G0";
	private static final String pcrReplyHeaderDSPStatus = "GD";
	private static final String pcrReplyHeaderReceiveStatus = "I0";
	private static final String pcrReplyHeaderRSSIChange = "I1";
	private static final String pcrReplyHeaderSignalOffset = "I2";
	private static final String pcrReplyHeaderDTMFDecode = "I3";
	
	private static final String pcrCommandRxOn = "H101";
	private static final String pcrCommandRxOff = "H100";
	private static final String pcrCommandAutoUpdateOff = "G300";
	private static final String pcrCommandBandScopeOff = "ME0000100000000000000";
	private static final String pcrCommandAFCPrefixRx1 = "J44";
	private static final String pcrCommandAFCPrefixRx2 = "J64";
	private static final String pcrCommandFrequencyPrefixRx1 = "K0";
	private static final String pcrCommandFrequencyPrefixRx2 = "K0";
	private static final String pcrCommandSquelchDelayPrefixRx1 = "J42";
	private static final String pcrCommandSquelchDelayPrefixRx2 = "J62";
	private static final String pcrCommandAGCPrefixRx1 = "J45";
	private static final String pcrCommandAGCPrefixRx2 = "J65";
	private static final String pcrCommandATTPrefixRx1 = "J47";
	private static final String pcrCommandATTPrefixRx2 = "J67";
	private static final String pcrCommandNBPrefixRx1 = "J46";
	private static final String pcrCommandNBPrefixRx2 = "J66";
	private static final String pcrCommandSquelchPrefixRx1 = "J41";
	private static final String pcrCommandSquelchPrefixRx2 = "J61";
	private static final String pcrCommandCTCSSPrefixRx1 = "J51";
	private static final String pcrCommandCTCSSPrefixRx2 = "J71";
	private static final String pcrCommandVoiceSquelchPrefixRx1 = "J50";
	private static final String pcrCommandVoiceSquelchPrefixRx2 = "J70";
	private static final String pcrCommandVolumeLevelPrefixRx1 = "J40";
	private static final String pcrCommandVolumeLevelPrefixRx2 = "J60";
	private static final String pcrCommandIFShiftPrefixRx1 = "J43";
	private static final String pcrCommandIFShiftPrefixRx2 = "J63";
	private static final String pcrCommandProgScanPrefixRx1 = "J48";
	private static final String pcrCommandProgScanPrefixRx2 = "J68";
	private static final String pcrReplyHeaderWaveFormData = "NE1";
	private static final String pcrReplyHeaderScanStatus = "H9";
	private static final String pcrReplyHeaderFirmware = "G4";
	private static final String pcrReplyHeaderCountry = "GE";
	private static final String pcrReplyHeaderPower = "H1";
	private static final String pcrReplyHeaderProtocol = "G2";
	private static final String pcrInitialize = "LE20050" + crlf + "LE20040";
	private static final String pcrCommandClearAllSettingsRx1 = "J530000";
	private static final String pcrCommandClearAllSettingsRx2 = "J730000";
	private static final String pcrCommandResetAntennaDiversity = "J0000";
	private static final String pcrCommandAntennaDiversityOn = "J0002";
	
	private boolean progScan = false;
	private boolean voiceScan = false;
	private boolean squelchDelayLong = false;
	private String signalOffset;
	private String dtmfDecode;
	private String scanStatus;
	private String dspStatus;
	private String waveFormData;
	private String modelNumber;
	private String serialNumber;
	private String protocol;
	private boolean afc = false;
	private boolean agc = false;
	private boolean attenuator = false;
	private String country; 
	private String dsp;
	private int filter = -1;
	private String firmware;
	private double frequency = -1;
	private int ifShift;
	private int mode = -1;
	private boolean noiseBlanker = false;
	private int squelch;
	private double toneSquelch = 00;
	private int digitalSquelch = 00;
	private int volume;
	private boolean vfoMode;
	private String txData;
	private String rxData;
	private int ber;
	private double testdBmValue = -90;
	private boolean getStronger = true;
	private CalibrationDataObject cdo = null;
	private boolean ready = false;
	private long timeLastWrite;
	
	private Object onLineHold = new Object();
	
	private Runnable scanTimer;
	private ScheduledExecutorService scanTimerScheduler;
	
	private Timer busyTimer;
	private Timer rssiTimer;
	private Timer heartbeatTimer;
	
	private BlockingQueue<String> itemsToWrite = new ArrayBlockingQueue<String>(32);
	
	private volatile boolean isOnLine = false;
	private volatile static boolean shuttingDown;
	private volatile static boolean terminated;
	private volatile int currentChannel = 0;
	private volatile Integer[] dBmList = new Integer[10];
	private volatile boolean busy;
	private volatile int rssi;
	private volatile Double[] berList = new Double[10];
	private volatile Double[] scanList = new Double[10];
	private volatile Boolean[] scanSelectList = new Boolean[10];
	private volatile int[] percentList = new int[10];
	private volatile boolean allowQueueing = false;
	private volatile boolean power = false;
	private volatile PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public PCR2500() {
		new Writer();
		initializeTimers();
		initializeVariables();
	}
	
	private void initializeVariables() {
		for (int i = 0; i < 10; i++) {
			dBmList[i] = new Integer(NOISE_FLOOR);
			percentList[i] = new Integer(0);
			berList[i] = new Double(100.0);
		}
	}
	
	private void initializeTimers() {
		ActionListener busyTimerActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                clearBusy();
            }
        };
		
		busyTimer = new Timer(BUSY_CLEARANCE_PERIOD, busyTimerActionListener);
		
		ActionListener rssiTimerActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                clearRSSI();
            }
        };
		
		rssiTimer = new Timer(RSSI_CLEARANCE_PERIOD, rssiTimerActionListener);
		
		ActionListener heartbeatTimerActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                heartbeatTimer();
            }
        };
		
		heartbeatTimer = new Timer(HEARTBEAT_PERIOD, heartbeatTimerActionListener);
		
		scanTimerScheduler = Executors.newSingleThreadScheduledExecutor();
		scanTimer = new Runnable() {
			@Override
			public void run() {
				scanAdvance();
			}
		};
		
		scanTimerScheduler.execute(scanTimer);
	}

	@Override
	public void processData(String input) {
		decode(input);
	}
	
	@Override
	public boolean isAFC() {
		return afc;
	}

	@Override
	public void setAFC(boolean afc) {
		this.afc = afc;
		cmdSwitch(afc, pcrCommandAFCPrefixRx1);
		cmdSwitch(afc, pcrCommandAFCPrefixRx2);
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
	public void setSquelchDelayLong(boolean squelchDelayLong) {
		this.squelchDelayLong = squelchDelayLong;
		cmdSwitch(squelchDelayLong, pcrCommandSquelchDelayPrefixRx1);
		cmdSwitch(squelchDelayLong, pcrCommandSquelchDelayPrefixRx2);
	}
	
	@Override
	public void setProgScan(boolean progScan) {
		this.progScan = progScan;
		cmdSwitch(progScan, pcrCommandProgScanPrefixRx1);
		cmdSwitch(progScan, pcrCommandProgScanPrefixRx2);
	}
	
	@Override
	public void setIFShift(int ifShift) {
		if (ifShift >= 0 && ifShift <= 255) {
			this.ifShift = ifShift;
			if (allowQueueing) writeData(pcrCommandIFShiftPrefixRx1 + Utility.integerToHex(ifShift) + crlf);
		    if (allowQueueing) writeData(pcrCommandIFShiftPrefixRx2 + Utility.integerToHex(ifShift) + crlf);
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
			if (allowQueueing) writeData(pcrCommandSquelchPrefixRx1 + Utility.integerToHex(squelch) + crlf);
			if (allowQueueing) writeData(pcrCommandSquelchPrefixRx2 + Utility.integerToHex(squelch) + crlf);
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
			if (allowQueueing) writeData(pcrCommandVolumeLevelPrefixRx1 + Utility.integerToHex(volume) + crlf);
			if (allowQueueing) writeData(pcrCommandVolumeLevelPrefixRx2 + Utility.integerToHex(volume) + crlf);
		}
	}
	
	@Override
	public double getToneSquelch() {
		return toneSquelch;
	}

	@Override
	public void setToneSquelch(double toneSquelch) {
		if (toneSquelch >= 0.0 && toneSquelch <= Double.parseDouble(TONE_SQUELCH_VALUES[TONE_SQUELCH_VALUES.length-1])) {
			this.toneSquelch = toneSquelch;
			int toneSquelchCode = toneSquelchToPcrCode(toneSquelch);
			String toneSquelchHexCode = Utility.integerToHex(toneSquelchCode);
			if (allowQueueing) writeData(pcrCommandCTCSSPrefixRx1 + toneSquelchHexCode + crlf);
			if (allowQueueing) writeData(pcrCommandCTCSSPrefixRx2 + toneSquelchHexCode + crlf);
		}
	}
	
	private int toneSquelchToPcrCode(double toneSquelchFreq) {
		int code = 0;
		for (int i = 1; i < TONE_SQUELCH_VALUES.length; i++) {
			if (toneSquelchFreq == Double.parseDouble(TONE_SQUELCH_VALUES[i])) code = i;
		}
		return code;
	}
	
	@Override
	public int getDigitalSquelch() {
		return digitalSquelch;
	}

	@Override
	public void setDigitalSquelch(int digitalSquelch) { 
		this.digitalSquelch = digitalSquelch;
	}

	@Override
	public double getFrequency() {
		return frequency;
	}
	
	private void setFrequencyModeFilter(double frequency, int mode, int filter) {
		if (frequency >= MINIMUM_RX_FREQ && frequency <= MAXIMUM_RX_FREQ) {
			this.frequency = frequency;
			this.mode = mode;
			this.filter = filter;
			sendFrequencyToPcr(frequency, mode, filter);
		} 
	}
	
	@Override
	public void setFrequency(double frequency) {
		if (frequency >= MINIMUM_RX_FREQ && frequency <= MAXIMUM_RX_FREQ) {
			this.frequency = frequency;
			sendFrequencyToPcr(frequency, mode, filter);
		} 
	}
	
	private void adviseUnsupportedFrequencyException(double frequency) {
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	DecimalFormat decimalFormat = new DecimalFormat("##00.000##");
                JOptionPane.showMessageDialog(null, "This radio does not operate on " 
                	+ decimalFormat.format(frequency) + " MHz", 
                    "Unsupported Frequency Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
	}
	
	private void adviseUnsupportedModeException(int mode) {
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null, "This radio does not support the selected FCC emission mode.",
                    "Unsupported Emission Mode Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
	}
	
	private void adviseUnsupportedFilterException(int filter) {
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null, "This radio does not support the selected filter.", 
                    "Unsupported Filter Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
	}
	
	@Override
	public int getMode() {
		return mode;
	}
	
	@Override
	public void setMode(int mode) {
		if (mode >= 0 && mode <= 7) {
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
					this.mode = 4;
					break;
				case 5:
					this.mode = 5;
					break;
				case 6:
					this.mode = 7;
					break;	
				case 7:
					this.mode = 8;
					break;	
			}
			sendFrequencyToPcr(frequency, mode, filter);
		} else {
			adviseUnsupportedModeException(mode);
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
		if (filter >= 0 && filter <= 4) {
			switch (filter) {
				case 0:
					this.filter = 0;
					break;
				case 1:
					this.filter = 1;
					break;
				case 2:
					this.filter = 2;
					break;
				case 3:
					this.filter = 3;
					break;
				case 4:
					this.filter = 4;
					break;
			}
			sendFrequencyToPcr(frequency, mode, filter);
		} else {
			adviseUnsupportedFilterException(filter);
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
		} while (sent);
	}

	private void pcrDecoder(String data) {
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
				pcs.firePropertyChange(RX_DATA, null, data);
				rxData = data;
				switch (data.substring(0, 2)) {
					case pcrReplyHeaderAcknowledge:
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
						busyTimer.restart();
						break;
			
					case pcrReplyHeaderRSSIChange:
						rssi = Integer.valueOf(data.substring(2, 4), 16);
						percentList[currentChannel] = (int) Math.round(getPercent());
						dBmList[currentChannel] = (int) Math.round(getdBm());
						pcs.firePropertyChange(RSSI, null, getdBm());
						rssiTimer.restart();
						break;
		
					case pcrReplyHeaderSignalOffset:
						signalOffset = data.substring(2, 4);
						pcs.firePropertyChange(SIGNAL_OFFSET, null, signalOffset);
						break;
			
					case pcrReplyHeaderDTMFDecode:
						dtmfDecode = data.substring(2, 4);
						pcs.firePropertyChange(DTMF_DECODE, null, decodeDTMF(dtmfDecode));
						break;
			
					case pcrReplyHeaderWaveFormData:
						waveFormData = data.substring(2, 4);
						pcs.firePropertyChange(WAVEFORM_DATA, null, waveFormData);
						break;
			
					case pcrReplyHeaderScanStatus:
						scanStatus = data.substring(2, 4);
						pcs.firePropertyChange(SCAN_STATUS, null, scanStatus);
						break;
						
					case pcrReplyHeaderDSPStatus:
						dspStatus = data.substring(2, 4);
						pcs.firePropertyChange(DSP_STATUS, null, dspStatus);
						break;	
						
					case pcrReplyHeaderFirmware:
						firmware = data.substring(2, 4);
						pcs.firePropertyChange(FIRMWARE, null, firmware);
						break;
			
					case pcrReplyHeaderCountry:
						country = data.substring(2, 4);
						pcs.firePropertyChange(RadioInterface.COUNTRY, null, country);
						break;
		
					case pcrReplyHeaderPower:
						synchronized(onLineHold) {
							boolean currentPowerStatus = power;
							power = decodePowerStatus(data.substring(2, 4));
							pcs.firePropertyChange(RadioInterface.POWER, currentPowerStatus, power);
							if (power) {
								isOnLine = true;
								onLineHold.notifyAll();	
							}
						}
						break;
			
					case pcrReplyHeaderProtocol:
						protocol = data.substring(2, 4);
						pcs.firePropertyChange(RadioInterface.PROTOCOL, null, protocol);
						break;
						
				}
            }
		});
	}

	private boolean decodePowerStatus(String str) {
		switch (str) {
			case "00": return false;
			case "01": return true;
		}
		return false;
	}
	
	private boolean decodeBusyStatus(String str) {
		switch (str) {
			case "0F": return true;
			case "04": return false;
		}
		return false;
	}
	
	@Override
	public void startRadio() {
		ready = false;
		isOnLine = false;
		itemsToWrite.clear();
		allowQueueing = true;
		if (allowQueueing) writeData(pcrInitialize + crlf);
		if (allowQueueing) writeData(pcrCommandRxOn + crlf);
		if (allowQueueing) writeData(pcrCommandClearAllSettingsRx1 + crlf);
		if (allowQueueing) writeData(pcrCommandClearAllSettingsRx2 + crlf);
		if (allowQueueing) writeData(pcrCommandResetAntennaDiversity + crlf);
		if (allowQueueing) writeData(pcrCommandAntennaDiversityOn + crlf);
		if (allowQueueing) writeData(pcrCommandAutoUpdateOff + crlf);
		if (allowQueueing) writeData(pcrQueryFirmwareVersion + crlf);
		if (allowQueueing) writeData(pcrQueryCountry + crlf);
		if (allowQueueing) writeData(pcrQueryDSP + crlf);
		if (allowQueueing) writeData(pcrCommandBandScopeOff + crlf);
		setFrequencyModeFilter(frequency, mode, filter);
		setProgScan(progScan);
		setVolume(volume);
		setSquelch(squelch);
		setSquelchDelayLong(squelchDelayLong);
		setToneSquelch(toneSquelch);
		setDigitalSquelch(digitalSquelch);
		setAFC(afc);
		setAGC(agc);
		setNoiseBlanker(noiseBlanker);
		setAttenuator(attenuator);
		setVoiceScan(voiceScan);
		setIFShift(ifShift);
		if (allowQueueing) writeData(pcrQueryRxOn + crlf);
		requestReadyStatus();
		heartbeatTimer.start();
	}
	
	private void requestReadyStatus() {
		SwingWorker<Boolean,Void> worker = new SwingWorker<Boolean,Void>() {
			@Override
			protected Boolean doInBackground() throws Exception {
				int progress = 0;
				boolean onLine = false;
				for (int i = 0; i < 30; i++) {
					synchronized(onLineHold) {
						if (allowQueueing) writeData(pcrQueryRxOn + crlf);
						onLineHold.wait(1000);
						setProgress(progress++);
						if (isOnLine) {
							onLine = true;
							break;
						}
					}
				}
				return onLine;
			}
			@Override
			protected void done() {
				try {
					ready = get();
					pcs.firePropertyChange(READY, null, ready);
				} catch (InterruptedException | ExecutionException ex) {
					ex.printStackTrace();
				}
			}
		};
		worker.execute();
	}
	
	@Override
	public void initiateRadioStop() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					rssiTimer.stop();
					busyTimer.stop();
					heartbeatTimer.stop();
					scanTimerScheduler.shutdownNow();
					clearRSSI();
					clearBusy();
					if (allowQueueing) writeData(pcrCommandAutoUpdateOff + crlf);
					power = true;
					if (allowQueueing) writeData(pcrCommandRxOff + crlf);
					while (power) {
						sleep(WRITE_PAUSE);
						if (allowQueueing) writeData(pcrQueryRxOn + crlf);
					}		
					pcs.firePropertyChange(CLOSE_SERIAL_PORT, null, true);
					allowQueueing = false;	
					itemsToWrite.clear();
					pcs.firePropertyChange(CANCEL_EVENTS, null, true);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		};
		thread.start();
	}
	
	private void sendFrequencyToPcr(double frequency, int mode, int filter) {
		boolean send = true;
		if (frequency == -1 || mode == -1 || filter == -1) return;
		if (frequency < MINIMUM_RX_FREQ || frequency > MAXIMUM_RX_FREQ) {
			adviseUnsupportedFrequencyException(frequency);
			send = false;
		}
		if (filter < 0 || filter > 4) {
			adviseUnsupportedFilterException(filter);
			send = false;
		}
		if (mode < 0 || mode > 7) {
			adviseUnsupportedModeException(mode);
			send = false;
		}
		if (send) {
			DecimalFormat freqFormat = new DecimalFormat(pcrFrFmt);
	    	DecimalFormat pcrFormat = new DecimalFormat(pcrFmt);
	    	
			String strRx1 = pcrCommandFrequencyPrefixRx1 + freqFormat.format(frequency * pcrdbl1e6) + 
					pcrFormat.format(mode) + pcrFormat.format(filter) + pcrFmt + crlf;
			 
			String strRx2 = pcrCommandFrequencyPrefixRx2 + freqFormat.format(frequency * pcrdbl1e6) + 
					pcrFormat.format(mode) + pcrFormat.format(filter) + pcrFmt + crlf;
	
	    	if (allowQueueing) writeData(strRx1);
	    	if (allowQueueing) writeData(INTER_COMMAND_PAUSE);
	    	if (allowQueueing) writeData(strRx2);
		}
	}

	private void cmdSwitch(boolean bln, String cmd) {
		String str = pcrBoolOff;
		if (bln) str = pcrBoolOn;
		if (allowQueueing) writeData(cmd + str + crlf);
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
	
	private void heartbeatTimer() {
		if (allowQueueing) writeData(pcrQuerySquelchStatus + crlf);
		if (allowQueueing) writeData(INTER_COMMAND_PAUSE);
		if (allowQueueing) writeData(pcrQuerySignalStrength + crlf);
	}
	
	private void clearRSSI() {
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	if (rssi > 0) {
					for (int i = 0; i < dBmList.length; i++) {
						dBmList[i] = NOISE_FLOOR;
					}
					for (int i = 0; i < percentList.length; i++) {
						percentList[i] = 0;
					}
					pcs.firePropertyChange(RSSI, null, 0);
					rssi = 0;
            	}
            }
		});
	}

	private void clearBusy() {
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	pcs.firePropertyChange(BUSY, null, false);
	            busy = false;
            }
		});
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
	public double getdBm() {
		return rssiTodBm(rssi);
	}

	@Override
	public double getPercent() {
		return 100 * (rssi / 255d);
	}

	@Override
	public int[] getPercentList() {
		return percentList;
	}
	
	@Override
	public void setScanList(Double[] scanList) {
		this.scanList = scanList;
	}

	@Override
	public Double[] getScanList() {
		return scanList;
	}
	
	@Override
	public Integer[] getdBmList() {
		return dBmList;
	}
	
	@Override
	public void setScanSelectList(Boolean[] scanSelectList) {
		this.scanSelectList = scanSelectList;
	}

	@Override
	public Boolean[] getScanSelectList() {
		return scanSelectList;
	}
	
	@Override
	public void setVfoMode(boolean vfoMode) {
		this.vfoMode = vfoMode;
		try {
			if (vfoMode) {
				currentChannel = 0;
				if (scanTimer != null) scanTimerScheduler.awaitTermination(
					TERMINATION_WAIT_PERIOD, TimeUnit.MILLISECONDS);
			} else {
				scanTimerScheduler.scheduleAtFixedRate(scanTimer, SCAN_PERIOD, SCAN_PERIOD, TimeUnit.MILLISECONDS);
			}
		} catch (InterruptedException ex) {
			ex.printStackTrace();
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
		return cdo.getdBmElement(rssi);
	}

	@Override
	public int getCurrentChannel() {
		return currentChannel;
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
	public Double[] getBERList() {
		return berList;
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
	public boolean isProgScan() {
		return progScan;
	}

	@Override
	public boolean isSquelchDelay() {
		return squelchDelayLong;
	}

	@Override
	public void startScan() {
		scanTimerScheduler.scheduleAtFixedRate(scanTimer, SCAN_PERIOD, SCAN_PERIOD, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public void stopScan() {
		try {
			scanTimerScheduler.awaitTermination(TERMINATION_WAIT_PERIOD, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public double getTestdBmValue() {
		if (testdBmValue <= -90) getStronger = true;
		if (testdBmValue >= -30) getStronger = false;	
		if (getStronger) {
			testdBmValue += 0.001;
		} else {
			testdBmValue -= 0.001;
		}
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
	public int getDefaultDataBits() {
		return DATA_BITS;
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
	public boolean isCTSSupported() {
		return CTS_SUPPORT;
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
	public String versionUID() {
		return versionUID;
	}

	@Override
	public double minimumRxFrequency() {
		return MINIMUM_RX_FREQ;
	}

	@Override
	public double maximumRxFrequency() {
		return MAXIMUM_RX_FREQ;
	}

	@Override
	public double minimumTxFrequency() {
		return MINIMUM_TX_FREQ;
	}

	@Override
	public double maximumTxFrequency() {
		return MAXIMUM_TX_FREQ;
	}

	@Override
	public EmissionDesignator[] getEmissionDesignators() {
		return EMISSION_DESIGNATORS;
	}

	@Override
	public String[] supportedToneSquelchCodes() {
		return TONE_SQUELCH_VALUES;
	}

	@Override
	public String[] supportedDigitalSquelchCodes() {
		return DIGITAL_SQUELCH_VALUES;
	}

	@Override
	public String[] availableFilters() {
		return AVAILABLE_FILTERS;
	}
	
	@Override
	public String[] availableBaudRates() {
		return AVAILABLE_BAUD_RATES;
	}
	
	@Override
	public void setCalibrationDataObject(CalibrationDataObject cdo) {
		this.cdo = cdo;
	}
	
	private void writeData(String data) {
		if (shuttingDown || terminated) return;
	    try {
	    	itemsToWrite.put(data);
	    } catch (InterruptedException ex) {
	    	Thread.currentThread().interrupt();
	    	throw new RuntimeException("Unexpected Interruption");
	    }
	}
	
	@Override
	public void dispose() {
		writeData(SHUTDOWN_REQ);
		shuttingDown = true;
		busyTimer.stop();
		rssiTimer.stop();
		heartbeatTimer.stop();
		scanTimerScheduler.shutdownNow();
	}
	
	private class Writer extends Thread {
		String data = null;
		private Writer() {
			start();
		}
		@Override
		public void run() {
			try {
				while (!(data = itemsToWrite.take()).equals(SHUTDOWN_REQ)) {
	            	try {
	            		long elapsedTime = Math.min(System.currentTimeMillis() - timeLastWrite, WRITE_PAUSE);
						if (elapsedTime < WRITE_PAUSE) {
							sleep(WRITE_PAUSE - elapsedTime);
						}
						if (data.equals(LONG_PAUSE)) {
							sleep(LONG_PAUSE_PERIOD);
						}
						else if (data.equals(INTER_COMMAND_PAUSE)) {
							sleep(INTER_COMMAND_PAUSE_PERIOD);
						}
						else if (data != null) {
							pcs.firePropertyChange("SEND_TO_SERIAL_PORT", null, data);
							timeLastWrite = System.currentTimeMillis();
						}
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			} finally {
				terminated = true;
				itemsToWrite.clear();
			}
		}
	}		

	@Override
	public boolean isReady() {
		return ready;
	}
	
	private char decodeDTMF(String dtmfString) {
		switch (dtmfString) {
			case "00": return '-';
			case "10": return '0';
			case "11": return '1';
			case "12": return '2';
			case "13": return '3';
			case "14": return '4';
			case "15": return '5';
			case "16": return '6';
			case "17": return '7';
			case "18": return '8';
			case "19": return '9';
			case "1A": return 'A';
			case "1B": return 'B';
			case "1C": return 'C';
			case "1D": return 'D';
			case "1E": return '*';
			case "1F": return '#';
			default: return '-';
		}
	}

}
