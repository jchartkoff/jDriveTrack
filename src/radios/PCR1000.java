package radios;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import types.CalibrationDataObject;
import jdrivetrack.Sinad;
import jdrivetrack.Utility;
import interfaces.RadioInterface;
import jssc.SerialPort;
import types.EmissionDesignator;

public class PCR1000 implements RadioInterface {
	private static final String versionUID = "3045764079202066863"; 
	private static final int BAUD_RATE = SerialPort.BAUDRATE_9600;
	private static final int PARITY = SerialPort.PARITY_NONE;
	private static final int STOP_BITS = SerialPort.STOPBITS_1;
	private static final int DATA_BITS = SerialPort.DATABITS_8;
	private static final int FLOW_CONTROL_IN = SerialPort.FLOWCONTROL_NONE;
	private static final int FLOW_CONTROL_OUT = SerialPort.FLOWCONTROL_NONE;
	private static final boolean DTR = true;
	private static final boolean RTS = true;
	private static final double MINIMUM_RX_FREQ = 0.050;
	private static final double MAXIMUM_RX_FREQ = 1300.0;
	private static final double MINIMUM_TX_FREQ = -1;
	private static final double MAXIMUM_TX_FREQ = -1;
	private static final String PAUSE_250MS = "PAUSE_250MS"; 
	private static final String SHUTDOWN_REQ = "SHUTDOWN"; 
	private static final int UPDATE_PERIOD = 100;
	private static final int SCAN_PERIOD = 700;
	private static final int SINAD_SAMPLE_PERIOD = 400;
	private static final int RECEIVER_SETTLE_PERIOD = 20;
	private static final int TERMINATION_WAIT_PERIOD = 250;
	private static final int BUSY_CLEARANCE_PERIOD = 500;
	private static final int RSSI_CLEARANCE_PERIOD = 500;
	private static final int WRITE_PAUSE = 20;
	
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
		new EmissionDesignator("200KF8E"), 
		new EmissionDesignator("20K0F3E"), 
		new EmissionDesignator("11K2F3E"), 
		new EmissionDesignator("6K00A3E"), 
		new EmissionDesignator("2K80J3EY"), 
		new EmissionDesignator("2K80J3EZ") 
	};

	public static final String crlf = "\r\n"; 
	public static final double pcrdbl1e6 = 1000000.0;
	public static final String pcrBoolOn = "01"; 
	public static final String pcrBoolOff = "00"; 
	public static final String pcrFmt = "00"; 
	public static final String pcrFrFmt = "0000000000"; 
	public static final String pcrQueryFirmwareVersion = "G4?"; 
	public static final String pcrQueryDSP = "GD?"; 
	public static final String pcrQueryCountry = "GE?"; 
	public static final String pcrQueryRxOn = "H1?"; 
	public static final String pcrQuerySquelchSetting = "I0?"; 
	public static final String pcrQuerySignalStrength = "I1?"; 
	public static final String pcrQueryFrequencyOffset = "I2?"; 
	public static final String pcrQueryDTMFTone = "I3?"; 
	public static final String pcrCommandRxOn = "H101"; 
	public static final String pcrCommandRxOff = "H100"; 
	public static final String pcrCommandAutoUpdateOn = "G301"; 
	public static final String pcrCommandAutoUpdateOff = "G300"; 
	public static final String pcrCommandBandScopeOff = "ME0000100000000000000"; 
	public static final String pcrCommandAFCPrefix = "J44"; 
	public static final String pcrCommandAGCPrefix = "J45"; 
	public static final String pcrCommandATTPrefix = "J47"; 
	public static final String pcrCommandNBPrefix = "J46"; 
	public static final String pcrCommandSquelchPrefix = "J41"; 
	public static final String pcrCommandCTCSSPrefix = "J51"; 
	public static final String pcrCommandVoiceScanPrefix = "J50"; 
	public static final String pcrCommandVolumeLevelPrefix = "J40"; 
	public static final String pcrCommandFrequencyPrefix = "K0"; 
	public static final String pcrCommandIFShiftPrefix = "J43"; 
	public static final String pcrReplyHeaderAck = "G0"; 
	public static final String pcrReplyHeaderReceiveStatus = "I0"; 
	public static final String pcrReplyHeaderRSSIChange = "I1"; 
	public static final String pcrReplyHeaderSignalOffset = "I2"; 
	public static final String pcrReplyHeaderDTMFDecode = "I3"; 
	public static final String pcrReplyHeaderWaveFormData = "NE10"; 
	public static final String pcrReplyHeaderScanStatus = "H9"; 
	public static final String pcrReplyHeaderFirmware = "G4"; 
	public static final String pcrReplyHeaderCountry = "GE"; 
	public static final String pcrReplyHeaderOptionalDevice = "GD"; 
	public static final String pcrReplyHeaderPower = "H1"; 
	public static final String pcrReplyHeaderProtocol = "G2"; 
	public static final String pcrInitialize = "LE20050" + crlf + "LE20040";  //$NON-NLS-2$
	public static final String pcrCommandTrackingFilterAutomatic = "LD82NN"; 
			
	private String signalOffset;
	private String dtmfDecode;
	private String scanStatus;
	private String waveFormData;
	private String modelNumber;
	private String serialNumber;
	private String protocol;
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
	private double toneSquelch;
	private int digitalSquelch;
	private boolean voiceScan;
	private int volume;
	private boolean vfoMode;
	private String txData;
	private String rxData;
	private CalibrationDataObject cdo = null;
	private boolean ready = false;
	private boolean sinadEnabled = false;
	private boolean rssiEnabled = false;
	private boolean scanEnabled = false;
	private int rssi;
	
	private Sinad sinad;
	
	private Object onLineHold = new Object();
	
	private Runnable scanTimer;
	private ScheduledExecutorService scanTimerScheduler;
	
	private Runnable updateTimer;
	private ScheduledExecutorService updateTimerScheduler;
	
	private Timer busyTimer;
	private Timer rssiTimer;
	private Timer sinadTimer;
	private Timer receiverSettleTimer;
	
	private volatile BlockingQueue<String> itemsToWrite = new ArrayBlockingQueue<>(32);
	
	private volatile boolean isOnLine = false;
	private static boolean shuttingDown = false;
	private volatile static boolean terminated = false;
	private volatile int currentChannel = 0;
	private volatile boolean busy;
	private volatile List<Integer> rssiList = new ArrayList<>(10);
	private volatile List<Double> berList = new ArrayList<>(10);
	private volatile List<Double> dBmList = new ArrayList<>(10);
	private volatile List<Double> sinadList = new ArrayList<>(10);
	private volatile List<Double> scanList = new ArrayList<>(10);
	private volatile List<Boolean> scanSelectList = new ArrayList<>(10);
	private volatile boolean allowQueueing = false;
	private volatile boolean power = false;
	
	private volatile PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public PCR1000() {
		new Writer();
		initializeArrays();
		initializeTimers();
	}
	
	private void initializeArrays() {
		this.rssiList = new ArrayList<>(Collections.nCopies(10, 0));
		this.berList = new ArrayList<>(Collections.nCopies(10, 0d));
		this.dBmList = new ArrayList<>(Collections.nCopies(10, -130d));
		this.sinadList = new ArrayList<>(Collections.nCopies(10, 0d));
		this.scanList = new ArrayList<>(Collections.nCopies(10, 162.4d));
		this.scanSelectList = new ArrayList<>(Collections.nCopies(10, false));
	}
	
	private void initializeTimers() {
		ActionListener busyTimerActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                clearBusy();
            }
        };
		
		this.busyTimer = new Timer(BUSY_CLEARANCE_PERIOD, busyTimerActionListener);
		
		ActionListener rssiTimerActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                clearRSSI();
            }
        };
		
		this.rssiTimer = new Timer(RSSI_CLEARANCE_PERIOD, rssiTimerActionListener);
		
		this.scanTimerScheduler = Executors.newSingleThreadScheduledExecutor();
		this.scanTimer = new Runnable() {
			@Override
			public void run() {
				scanAdvance();
			}
		};
		this.scanTimerScheduler.execute(this.scanTimer);
		
		this.updateTimerScheduler = Executors.newSingleThreadScheduledExecutor();
		this.updateTimer = new Runnable() {
			@Override
			public void run() {
				updateRequest();
			}
		};
		this.updateTimerScheduler.execute(this.updateTimer);
		
		ActionListener sinadTimerActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                sinadSample();
            }
        };
        
        this.sinadTimer = new Timer(SINAD_SAMPLE_PERIOD, sinadTimerActionListener);
        this.sinadTimer.setRepeats(false);
        
        ActionListener receiverSettledTimerActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                receiverSettled();
            }
        };
        
        this.receiverSettleTimer = new Timer(RECEIVER_SETTLE_PERIOD, receiverSettledTimerActionListener);
        this.receiverSettleTimer.setRepeats(false);
	}

	@Override
	public void processData(String input) {
		decode(input);
	}
	
	@Override
	public boolean isAFC() {
		return this.afc;
	}

	@Override
	public void setAFC(boolean afc) {
		this.afc = afc;
		cmdSwitch(afc, pcrCommandAFCPrefix);
	}

	@Override
	public void startSinad() {
		this.sinadEnabled = true;
		this.sinad = new Sinad();
		this.sinad.start();
	}
	
	@Override
	public void stopSinad() {
		this.sinadEnabled = false;
		if (this.sinad != null) this.sinad.stopSinad();
		this.sinadTimer.stop();
	}
	
	@Override
	public boolean isSinadEnabled() {
		return this.sinadEnabled;
	}
	
	@Override
	public boolean isAGC() {
		return this.agc;
	}

	@Override
	public void setAGC(boolean agc) {
		this.agc = agc;
		cmdSwitch(agc, pcrCommandAGCPrefix);
	}

	@Override
	public boolean isAttenuator() {
		return this.attenuator;
	}

	@Override
	public void setAttenuator(boolean attenuator) {
		this.attenuator = attenuator;
		cmdSwitch(attenuator, pcrCommandATTPrefix);
	}

	@Override
	public boolean isNoiseBlanker() {
		return this.noiseBlanker;
	}

	@Override
	public void setNoiseBlanker(boolean noiseBlanker) {
		this.noiseBlanker = noiseBlanker;
		cmdSwitch(noiseBlanker, pcrCommandNBPrefix);
	}

	@Override
	public boolean isVoiceScan() {
		return this.voiceScan;
	}

	@Override
	public void setVoiceScan(boolean voiceScan) {
		this.voiceScan = voiceScan;
		cmdSwitch(voiceScan, pcrCommandNBPrefix);
	}

	@Override
	public String getDSP() {
		return this.dsp;
	}

	@Override
	public String getFirmware() {
		return this.firmware;
	}

	@Override
	public String getCountry() {
		return this.country;
	}

	@Override
	public int getIFShift() {
		return this.ifShift;
	}

	@Override
	public void setIFShift(int ifShift) {
		if (ifShift >= 0 && ifShift <= 255) {
			this.ifShift = ifShift;
		    if (this.allowQueueing) writeData(pcrCommandIFShiftPrefix + Utility.integerToHex(ifShift) + crlf);
		}
	}

	@Override
	public int getSquelch() {
		return this.squelch;
	}

	@Override
	public void setSquelch(int squelch) {
		if (squelch >= 0 && squelch <= 255) {
			this.squelch = squelch;
			if (this.allowQueueing) writeData(pcrCommandSquelchPrefix + Utility.integerToHex(squelch) + crlf);
		}
	}

	@Override
	public boolean isBusy() {
		return this.busy;
	}

	@Override
	public int getVolume() {
		return this.volume;
	}

	@Override
	public void setVolume(int volume) {
		if (this.squelch >= 0 && this.squelch <= 255) {
			this.volume = volume;
			if (this.allowQueueing) writeData(pcrCommandVolumeLevelPrefix + Utility.integerToHex(volume) + crlf);
		}
	}
	
	@Override
	public double getToneSquelch() {
		return this.toneSquelch;
	}

	@Override
	public void setToneSquelch(double toneSquelch) {
		if (toneSquelch >= 0.0 && toneSquelch <= Double.parseDouble(TONE_SQUELCH_VALUES[TONE_SQUELCH_VALUES.length-1])) {
			this.toneSquelch = toneSquelch;
			int toneSquelchCode = toneSquelchToPcrCode(toneSquelch);
			String toneSquelchHexCode = Utility.integerToHex(toneSquelchCode);
			if (this.allowQueueing) writeData(pcrCommandCTCSSPrefix + toneSquelchHexCode + crlf);
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
		return this.digitalSquelch;
	}

	@Override
	public void setDigitalSquelch(int digitalSquelch) { 
		this.digitalSquelch = digitalSquelch;
	}

	private void setFrequencyModeFilter(double frequency, int mode, int filter) {
		if (frequency >= MINIMUM_RX_FREQ && frequency <= MAXIMUM_RX_FREQ) {
			this.frequency = frequency;
			this.mode = mode;
			this.filter = filter;
			sendFrequencyToPcr(frequency, mode, filter);
		} else {
			adviseUnsupportedFrequencyException(frequency);
		}
	}
	
	@Override
	public void setFrequency(double frequency) {
		if (frequency >= MINIMUM_RX_FREQ && frequency <= MAXIMUM_RX_FREQ) {
			this.frequency = frequency;
			sendFrequencyToPcr(frequency, this.mode, this.filter);
		} else {
			adviseUnsupportedFrequencyException(frequency);
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
	
	@Override
	public int getMode() {
		return this.mode;
	}
	
	@Override
	public void setMode(int mode) {
		if (mode >= 0 && mode <= 5) {
			switch (mode) {
				case 0:
					this.mode = 6;
					break;
				case 1:
					this.mode = 5;
					break;
				case 2:
					this.mode = 5;
					break;
				case 3:
					this.mode = 2;
					break;
				case 4:
					this.mode = 1;
					break;
				case 5:
					this.mode = 0;
					break;
			}
			sendFrequencyToPcr(this.frequency, mode, this.filter);
		}
	}

	@Override
	public int getFilter() {
		return this.filter;
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
			sendFrequencyToPcr(this.frequency, this.mode, filter);
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
		this.pcs.firePropertyChange(RX_DATA, null, data);
		this.rxData = data;
		switch (data.substring(0, 2)) {
			case pcrReplyHeaderAck:
				switch (data.substring(2, 4)) {
					case "01": 
						this.pcs.firePropertyChange(ERROR, null, "Received Data Error"); 
						break;
					case "00": 
						this.pcs.firePropertyChange(ACK, null, "Data Acknowledged"); 
						break;
					}
				break;

			case pcrReplyHeaderReceiveStatus:
				this.busy = decodeBusyStatus(data.substring(2, 4));
				this.pcs.firePropertyChange(RadioInterface.BUSY, null, this.busy);
				this.busyTimer.restart();
				break;
	
			case pcrReplyHeaderRSSIChange:
				if (data.length() == 4) {
					this.rssi = Integer.valueOf(data.substring(2, 4), 16);
					this.dBmList.set(this.currentChannel, this.cdo.getdBmElement(this.rssi));
					if (this.rssiEnabled && !this.scanEnabled) this.pcs.firePropertyChange(RadioInterface.RSSI, null, this.rssi);
					this.rssiTimer.restart();
				}
				break;

			case pcrReplyHeaderSignalOffset:
				this.signalOffset = data.substring(2, 4);
				this.pcs.firePropertyChange(RadioInterface.SIGNAL_OFFSET, null, this.signalOffset);
				break;
	
			case pcrReplyHeaderDTMFDecode:
				this.dtmfDecode = data.substring(2, 4);
				this.pcs.firePropertyChange(RadioInterface.DTMF_DECODE, null, this.dtmfDecode);
				break;
	
			case pcrReplyHeaderWaveFormData:
				this.waveFormData = data.substring(2, 4);
				this.pcs.firePropertyChange(RadioInterface.WAVEFORM_DATA, null, this.waveFormData);
				break;
	
			case pcrReplyHeaderScanStatus:
				this.scanStatus = data.substring(2, 4);
				this.pcs.firePropertyChange(RadioInterface.SCAN_STATUS, null, this.scanStatus);
				break;
	
			case pcrReplyHeaderFirmware:
				this.firmware = data.substring(2, 4);
				this.pcs.firePropertyChange(RadioInterface.FIRMWARE, null, this.firmware);
				break;
	
			case pcrReplyHeaderCountry:
				this.country = data.substring(2, 4);
				this.pcs.firePropertyChange(RadioInterface.COUNTRY, null, this.country);
				break;
	
			case pcrReplyHeaderOptionalDevice:
				this.dsp = data.substring(2, 4);
				this.pcs.firePropertyChange(RadioInterface.DSP, null, this.dsp);
				break;
	
			case pcrReplyHeaderPower:
				synchronized(this.onLineHold) {
					this.power = decodePowerStatus(data.substring(2, 4));
					this.pcs.firePropertyChange(RadioInterface.POWER, null, this.power);
					if (this.power) {
						this.isOnLine = true;
						this.onLineHold.notifyAll();	
					}
				}
				break;
	
			case pcrReplyHeaderProtocol:
				this.protocol = data.substring(2, 4);
				this.pcs.firePropertyChange(RadioInterface.PROTOCOL, null, this.protocol);
				break;
	
			default:
				System.err.println("PCR1000 - Invalid Property Value : " + data); 
				break;
		}
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
			case "04": return false; 
			case "07": return true; 
		}
		return false;
	}

	@Override
	public void startRadio() {
		this.ready = false;
		this.isOnLine = false;
		this.itemsToWrite.clear();
		this.allowQueueing = true;
		if (this.allowQueueing) writeData(pcrInitialize + crlf);
		if (this.allowQueueing) writeData(pcrCommandRxOn + crlf);
		if (this.allowQueueing) writeData(pcrCommandAutoUpdateOff + crlf);
		if (this.allowQueueing) writeData(pcrQueryFirmwareVersion + crlf);
		if (this.allowQueueing) writeData(pcrQueryCountry + crlf);
		if (this.allowQueueing) writeData(pcrQueryDSP + crlf);
		setFrequencyModeFilter(this.frequency, this.mode, this.filter);
		setVolume(this.volume);
		setSquelch(this.squelch);
		setToneSquelch(this.toneSquelch);
		cmdSwitch(this.voiceScan, pcrCommandVoiceScanPrefix);
		setIFShift(this.ifShift);
		cmdSwitch(this.agc, pcrCommandAGCPrefix);
		cmdSwitch(this.afc, pcrCommandAFCPrefix);
		cmdSwitch(this.noiseBlanker, pcrCommandNBPrefix);
		cmdSwitch(this.attenuator, pcrCommandATTPrefix);
		if (this.allowQueueing) writeData(pcrCommandBandScopeOff + crlf);
		if (this.allowQueueing) writeData(pcrQueryRxOn + crlf);
		if (this.sinadEnabled) startSinad();
		if (!this.vfoMode) startScan();
		requestReadyStatus();
	}
	
	private void requestReadyStatus() {
		SwingWorker<Boolean,Void> worker = new SwingWorker<Boolean,Void>() {
			@Override
			protected Boolean doInBackground() throws Exception {
				int progress = 0;
				boolean onLine = false;
				for (int i = 0; i < 30; i++) {
					synchronized(PCR1000.this.onLineHold) {
						if (PCR1000.this.allowQueueing) writeData(pcrQueryRxOn + crlf);
						PCR1000.this.onLineHold.wait(1000);
						setProgress(progress++);
						if (PCR1000.this.isOnLine) {
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
					PCR1000.this.ready = get();
					PCR1000.this.updateTimerScheduler.scheduleWithFixedDelay(PCR1000.this.updateTimer, UPDATE_PERIOD, UPDATE_PERIOD, TimeUnit.MILLISECONDS);
					PCR1000.this.pcs.firePropertyChange(READY, null, PCR1000.this.ready);
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
					stopScan();
					stopSinad();
					PCR1000.this.updateTimerScheduler.shutdown();
					PCR1000.this.rssiTimer.stop();
					PCR1000.this.busyTimer.stop();
					PCR1000.this.receiverSettleTimer.stop();
					PCR1000.this.itemsToWrite.clear();
					if (PCR1000.this.allowQueueing) writeData(pcrCommandAutoUpdateOff + crlf);
					PCR1000.this.power = true;
					if (PCR1000.this.allowQueueing) writeData(pcrCommandRxOff + crlf);
					sleep(80);
					if (PCR1000.this.allowQueueing) writeData(pcrQueryRxOn + crlf);
					sleep(40);
					while (PCR1000.this.power) {
						if (PCR1000.this.allowQueueing) writeData(pcrQueryRxOn + crlf);
						sleep(40);
					}
					PCR1000.this.allowQueueing = false;
					PCR1000.this.itemsToWrite.clear();
					clearRSSI();
					clearBusy();
					PCR1000.this.pcs.firePropertyChange(CANCEL_EVENTS, null, true);
					PCR1000.this.scanTimerScheduler.awaitTermination(TERMINATION_WAIT_PERIOD, TimeUnit.MILLISECONDS);
					PCR1000.this.updateTimerScheduler.awaitTermination(TERMINATION_WAIT_PERIOD, TimeUnit.MILLISECONDS);
					PCR1000.this.pcs.firePropertyChange(CLOSE_SERIAL_PORT, null, true);
					PCR1000.this.pcs.firePropertyChange(RADIO_THREADS_TERMINATED, null, true);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		};
		thread.start();
	}
	
	private void sendFrequencyToPcr(double freq, int mode, int filter) {
    	DecimalFormat freqFormat = new DecimalFormat(pcrFrFmt);
		String strFr = pcrCommandFrequencyPrefix + freqFormat.format(freq * pcrdbl1e6);
		DecimalFormat pcrFormat = new DecimalFormat(pcrFmt);
		strFr = strFr + pcrFormat.format(mode) + pcrFormat.format(filter);
		strFr = strFr + pcrFmt;
    	if (this.allowQueueing) writeData(strFr + crlf);
	}

	private void cmdSwitch(boolean bln, String cmd) {
		String str = pcrBoolOff;
		if (bln) str = pcrBoolOn;
		if (this.allowQueueing) writeData(cmd + str + crlf);
	}
	
	private void scanAdvance() {
		int numberOfSelectedChannels = 0;
		
		for (int i = 0; i < this.scanSelectList.size(); i++) {
			if (this.scanSelectList.get(i)) numberOfSelectedChannels++;
		}
		
		if (numberOfSelectedChannels == 0) return;
		
		do {
			this.currentChannel++;
			if (this.currentChannel > 9) this.currentChannel = 0;

			if (this.scanSelectList.get(this.currentChannel) && this.scanList.get(this.currentChannel) >= MINIMUM_RX_FREQ &&
					this.scanList.get(this.currentChannel) <= MAXIMUM_RX_FREQ) {
				setFrequency(this.scanList.get(this.currentChannel));
				this.receiverSettleTimer.start();
				if (this.sinad != null) this.sinadTimer.start();
				break;
			}

		} while (!this.scanSelectList.get(this.currentChannel) && this.scanList.get(this.currentChannel) >= MINIMUM_RX_FREQ &&
				this.scanList.get(this.currentChannel) <= MAXIMUM_RX_FREQ);
	}
	
	private void updateRequest() {
		if (this.allowQueueing) writeData(pcrQuerySignalStrength + crlf);
		if (this.allowQueueing) writeData(pcrQuerySquelchSetting + crlf);
	}
	
	private void sinadSample() {
		this.sinadList.set(this.currentChannel, this.sinad.getSINAD());
		if (this.sinadEnabled) scanChannelReady();
	}
	
	void receiverSettled() {
		this.rssiList.set(this.currentChannel, this.rssi);
		if (!this.sinadEnabled) scanChannelReady();
	}
	
	private void scanChannelReady() {
		this.pcs.firePropertyChange(SCAN_CHANNEL_READY, null, this.currentChannel);
	}
	
	@Override
	public double getSinad(int index) {
		return this.sinadList.get(index);
	}
	
	@Override
	public Double getBer(int index) {
		return this.berList.get(index);
	}
	
	private void clearRSSI() {
		this.rssi = 0;
        if (this.rssiEnabled && !this.scanEnabled) this.pcs.firePropertyChange(RSSI, null, this.rssi);
	}

	void clearBusy() {
		this.busy = false;
		this.pcs.firePropertyChange(BUSY, null, this.busy);
	}
	
	@Override
	public Integer[] getPercentArray() {
		Integer[] percentArray = new Integer[10];
		for (int i = 0; i < 10; i++) {
			percentArray[i] = (int) dBmToPercent(this.dBmList.get(i));
		}
		return percentArray;
	}
	
	@Override
	public List<Integer> getPercentList() {
		List<Integer> percentList = new ArrayList<>(10);
		for (int i = 0; i < 10; i++) {
			percentList.add(i, (int) dBmToPercent(this.dBmList.get(i)));
		//	System.out.println("percent: " + dBmToPercent(dBmList.get(i)) + "  "  + "dBm: " + dBmList.get(i));
		}
		return percentList;
	}
	
	@Override
	public List<Double> getdBmList() {
		return this.dBmList;
	}

	@Override
	public List<Double> getBerList() {
		return this.berList;
	}
	
	@Override
	public void setScanList(List<Double> scanList) {
		this.scanList = scanList;
	}

	@Override
	public List<Double> getScanList() {
		return this.scanList;
	}
	
	@Override
	public Double getScanList(int index) {
		return this.scanList.get(index);
	}
	
	@Override
	public void setSinadEnabled(boolean sinadEnabled) {
		this.sinadEnabled = sinadEnabled;
	}
	
	@Override
	public List<Double> getSinadList() {
		return this.sinadList;
	}
	
	@Override
	public void setScanSelectList(List<Boolean> scanSelectList) {
		this.scanSelectList = scanSelectList;
	}

	@Override
	public List<Boolean> getScanSelectList() {
		return this.scanSelectList;
	}
	
	@Override
	public Boolean getScanSelectList(int index) {
		return this.scanSelectList.get(index);
	}
	
	@Override
	public void setVfoMode(boolean vfoMode) {
		this.vfoMode = vfoMode;
		if (this.isOnLine && vfoMode) stopScan();
		if (this.isOnLine && !vfoMode) startScan();
	}
	
	@Override
	public boolean isVfoMode() {
		return this.vfoMode;
	}

	@Override
	public String getTransmittedData() {
		return "Tx Data: " + this.txData; 
	}

	@Override
	public String getReceivedData() {
		return "Rx Data: " + this.rxData; 
	}

	@Override
	public int getCurrentChannel() {
		return this.currentChannel;
	}

	@Override
	public String getModelNumber() {
		return this.modelNumber;
	}
	
	@Override
	public String getSerialNumber() {
		return this.serialNumber;
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
		this.scanEnabled = true;
		this.scanTimerScheduler.scheduleWithFixedDelay(this.scanTimer, SCAN_PERIOD, SCAN_PERIOD, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public void stopScan() {
		this.scanEnabled = false;
		this.scanTimerScheduler.shutdownNow();;
		this.currentChannel = 0;
		setFrequency(this.frequency);
	}
	
	@Override
	public void setSquelchDelayLong(boolean squelchDelay) {
		
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
		return true;
	}
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

	@Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
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
	public boolean supportsSinad() {
		return true;
	}
	
	@Override 
	public boolean suportsBer() {
		return false;
	}
	
	@Override 
	public boolean supportsRssi() {
		return true;
	}
	
	@Override
	public void setCalibrationDataObject(CalibrationDataObject cdo) {
		this.cdo = cdo;
	}
	
	private void writeData(String data) {
		if (shuttingDown || terminated) return;
	    try {
	    	this.itemsToWrite.put(data);
	    } catch (InterruptedException ex) {
	    	Thread.currentThread().interrupt();
	    	throw new RuntimeException("Unexpected Interruption"); 
	    }
	}
	
	@Override
	public void dispose() {
		shuttingDown = true;
		writeData(SHUTDOWN_REQ);
		initiateRadioStop();
	}
	
	private class Writer extends Thread {
		String data = null;
		private Writer() {
			start();
		}
		@Override
		public void run() {
			try {
				while (!(this.data = PCR1000.this.itemsToWrite.take()).equals(SHUTDOWN_REQ)) {
	            	try {
						sleep(WRITE_PAUSE);
						PCR1000.this.pcs.firePropertyChange("SEND_TO_SERIAL_PORT", null, this.data); 
						if (this.data.equals(PAUSE_250MS)) sleep(250);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			} finally {
				terminated = true;
				PCR1000.this.itemsToWrite.clear();
			}
		}
	}		

	@Override
	public boolean isReady() {
		return this.ready;
	}

	@Override
	public String[] getAvailableBaudRates() {
		return AVAILABLE_BAUD_RATES;
	}
	
	@Override
	public boolean serialParametersFixed() {
		return true;
	}

	@Override
	public int getRSSI(int index) {
		return this.rssiList.get(index);
	}

	@Override
	public double getFrequency(int index) {
		return this.scanList.get(index);
	}

	@Override
	public double getdBm(int index) {
		return this.dBmList.get(index);
	}

	@Override
	public void sampleRssiValues(boolean sample) {
		this.rssiEnabled = sample;
	}

	@Override
	public void sampleBerValues(boolean sample) {

	}
	
	@Override
	public void setScanList(Double[] scan) {
		this.scanList = Arrays.asList(scan);
	}

	@Override
	public void setScanSelectList(Boolean[] scanSelect) {
		this.scanSelectList = Arrays.asList(scanSelect);
	}

	private double dBmToPercent(double dBm) {
		double minDBM = -130;
		double maxDBM = -50;		
		return (100 - minDBM * (maxDBM - dBm) / maxDBM) * -1;
	}
}
