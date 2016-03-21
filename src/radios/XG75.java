package radios;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import types.EmissionDesignator;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import jdrivetrack.Calibrate;
import jdrivetrack.CalibrationDataObject;
import jdrivetrack.Utility;
import interfaces.RadioInterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class XG75 implements RadioInterface {
	
	private boolean powerIsOn = false;
	private boolean closing = false;
	private boolean rxChar = false;
	private boolean cts = false;
	private boolean dsr = false;
	private boolean rlsd = false;
	private int comPort = 0;
	private double frequency; 
	private int rssi = 0;
	private boolean busy = false;
	private double[] scanList = new double[10];
	private boolean[] scanSelectList = new boolean[10];
	private int[] percentList = new int[10];
	private int[] dBmList = new int [10];
	private double[] berList = new double [10];
	private String txData = "";
	private String rxData = "";
	private int currentChannel = 0;
	private boolean ready = false;
	private double ber;
	private boolean testMode = false;
	private boolean testBerFastMode = false;
	private NumberFormat frequencyFormat = new DecimalFormat("000000000");
	private String rxBuffer = "";
	private String lastCommand = "";
	private int select = 0;
	private boolean sampleBER = false;
	private boolean sampleRSSI = false;
	private NumberFormat berFormat = new DecimalFormat("#0.000");
	private String errorState = "";
	
	private static final String BER_TEST_SET_COMMAND_FAST_MODE = "iberlin 1\r";
	private static final String BER_TEST_COMMAND_START = "iberlin 0\r";
	private static final String BER_TEST_COMMAND_STOP = "iberlin 3\r";
	private static final String BER_TEST_IS_STARTED = "P25 Linear Simulcast IBER (fast mode) test started.";
	private static final String BER_TEST_IS_STOPPED = "P25 Linear Simulcast IBER test stopped.";
	private static final String READY_PROMPT = "*";
	private static final String BER_DATA_HEADER = "P25 Linear Simulcast IBER = ";
	private static final String RSSI_TEST_COMMAND_START = "cmd 2\r";
	private static final String TEST_MODE_COMMAND_START = "cmd 0\r";
	private static final String TEST_MODE_COMMAND = "cmd 0";
	private static final String TEST_MODE_COMMAND_STOP = "cmd 49\r";
	private static final String COMMAND_NOT_IMPLEMENTED = "?Cmd not impl";
	private static final String TEST_MODE_IS_STARTED = "Starting Testmode";
	private static final String TEST_MODE_IS_STOPPED = "Exiting Testmode";
	private static final String RSSI_DATA_HEADER = "RSSI = ";
	private static final String COMMAND_PASS = "?PASS";
	private static final String SET_RECEIVE_FREQUENCY = "cmd 50 ";
	private static final String CHECK_SQUELCH_STATUS = "cmd 52 2\r";
	private static final String SQUELCH_STATUS_REPLY_BUSY = "?PASS 1";
	private static final String SQUELCH_STATUS_REPLY_CLOSED = "?PASS 0";
	private static final String REBOOT = "ATZ 0\r";

	private SerialPort serialPort;
	private Calibrate calibrate = null;

	private Timer testTimer;
	private Timer rssiTimer;
	private Timer berTimer;
	private Timer busyTimer;
	private Timer squelchTimer;
	private Timer scanTimer;
	private Timer errorTimer;

	private Utility.ErrorMode errorMode = ErrorMode.PORT_NOT_VALID;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public XG75() {
		initializeComponents();
	}

	private void initializeComponents() {

		ActionListener testTimerTimerActionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				testTimerTimerActionListenerEvent(event);
			}
		};

		testTimer = new Timer(30000, testTimerTimerActionListener);
		testTimer.setRepeats(true);

		ActionListener busyTimerActionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				busyTimerActionListenerEvent(event);
			}
		};

		busyTimer = new Timer(1750, busyTimerActionListener);
		busyTimer.setRepeats(true);

		ActionListener rssiTimerActionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				rssiTimerActionListenerEvent(event);
			}
		};

		rssiTimer = new Timer(1800, rssiTimerActionListener);
		rssiTimer.setRepeats(true);
		
		ActionListener berTimerActionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				berTimerActionListenerEvent(event);
			}
		};
		
		berTimer = new Timer(1500, berTimerActionListener);
		berTimer.setRepeats(true);
		
		ActionListener errorTimerActionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				errorTimerActionListenerEvent(event);
			}
		};
		
		errorTimer = new Timer(1500, errorTimerActionListener);
		errorTimer.setRepeats(true);
		
		ActionListener scanTimerActionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				scanTimerActionListenerEvent(event);
			}
		};

		scanTimer = new Timer(5000, scanTimerActionListener);
		scanTimer.setRepeats(true);
		scanTimer.setInitialDelay(8000);
		
		ActionListener squelchTimerActionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				squelchTimerActionListenerEvent(event);
			}
		};

		squelchTimer = new Timer(1500, squelchTimerActionListener);
		squelchTimer.setRepeats(true);
	}

	@Override
	public void setComPort(int newComPort) {
		comPort = newComPort;
	}

	@Override
	public int getComPort() {
		return comPort;
	}

	@Override
	public void setOnLine(boolean onLine) throws SerialPortException {
		if (comPort > 0) {
			Utility.ErrorMode oldErrorMode = errorMode;
			if (onLine)
			//	errorMode = Utility.isComPortAvailable(comPort);
				pcs.firePropertyChange(ERROR, oldErrorMode, errorMode);
			if (errorMode == org.signalstat.gui.jdrivetrack.ErrorMode.CLEAR) {
				if (onLine) {
					try {
						serialPort = new SerialPort("COM" + Integer.toString(comPort));
						if (!serialPort.isOpened()) serialPort.openPort();
						serialPort.setParams(19200, 8, 1, 0);
						serialPort.setDTR(true);
						serialPort.setRTS(true);
						int mask = SerialPort.MASK_RXCHAR
								+ SerialPort.MASK_RXFLAG
								+ SerialPort.MASK_TXEMPTY + SerialPort.MASK_CTS
								+ SerialPort.MASK_DSR + SerialPort.MASK_RLSD
								+ SerialPort.MASK_ERR + SerialPort.MASK_RING;
						serialPort.setEventsMask(mask);
						serialPort.addEventListener(new SerialPortReader());
						closing = false;
						setRadioOnLine(true);
					} catch (SerialPortException ex) {
						System.err.println(ex.getMessage());
					}
				} else {
					closing = true;
					setRadioOnLine(false);
					try {
						if (serialPort.isOpened())
							serialPort.closePort();
					} catch (SerialPortException ex) {
						System.err.println(ex.getMessage());
					}
				}
			} else {
				switch (errorMode) {
					case PORT_NOT_INSTALLED:
						throw new SerialPortException(String.valueOf(comPort),
								"setOnLine",
								"Selected Serial Port Is Not Available On This System");
					case PORT_IN_USE:
						throw new SerialPortException(String.valueOf(comPort),
								"setOnLine", "Serial Port Is In Use");
					case CLEAR:
						throw new SerialPortException(String.valueOf(comPort),
								"setOnLine", "Serial Port Error Clear");
					case PORT_NOT_VALID:
						throw new SerialPortException(String.valueOf(comPort),
								"setOnLine", "Serial Port Is Not Assigned");
					case OFF_LINE:
						throw new SerialPortException(String.valueOf(comPort),
								"setOnLine", "Serial Port Is Off Line");
				}
			}
		} else {
			System.err.println("Serial Port Is Not Assigned");
			throw new SerialPortException(String.valueOf(comPort), "setOnLine",
					"Serial Port Is Not Assigned");
		}
	}

	@Override
	public boolean isOnLine() {
		boolean answer = false;
		if (serialPort != null) answer = serialPort.isOpened();
		return answer;
	}

	@Override
	public String getModelNumber() {
		return null;
	}
	
	@Override
	public String getSerialNumber() {
		return null;
	}
	
	@Override
	public boolean isCTS() {
		try {
			return serialPort.isCTS();
		} catch (SerialPortException ex) {
			errorState = ex.getMessage();
			ex.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean isDSR() {
		try {
			return serialPort.isDSR();
		} catch (SerialPortException ex) {
			errorState = ex.getMessage();
			ex.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean isRLSD() {
		try {
			return serialPort.isRLSD();
		} catch (SerialPortException ex) {
			errorState = ex.getMessage();
			ex.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean isAFC() {
		return false;
	}

	@Override
	public void setAFC(boolean afc) {

	}

	@Override
	public boolean isAGC() {
		return false;
	}

	@Override
	public void setAGC(boolean agc) {
		
	}

	@Override
	public boolean isAttenuator() {
		return false;
	}
	
	@Override
	public void setAttenuator(boolean attenuator) {

	}

	@Override
	public boolean isNoiseBlanker() {
		return false;
	}

	@Override
	public void setNoiseBlanker(boolean noiseBlanker) {

	}

	@Override
	public boolean isVoiceScan() {
		return false;
	}

	@Override
	public void setVoiceScan(boolean voiceScan) {

	}

	@Override
	public String getDSP() {
		return null;
	}

	@Override
	public String getFirmware() {
		return null;
	}

	@Override
	public String getCountry() {
		return null;
	}

	@Override
	public int getIFShift() {
		return 0;
	}

	@Override
	public void setIFShift(int ifShift) {

	}

	@Override
	public int getSquelch() {
		return 0;
	}

	@Override
	public void setSquelch(int squelch) {

	}

	@Override
	public boolean isBusy() {
		return busy;
	}

	@Override
	public int getVolume() {
		return 0;
	}

	@Override
	public void setVolume(int volume) {

	}

	@Override
	public int getToneSquelch() {
		return 0;
	}

	@Override
	public void setToneSquelch(int toneSquelch) {

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
		if ((frequency >= 764.0 && frequency <= 775.99975) || (frequency >= 851.0 && frequency <= 868.9875)) {
			this.frequency = frequency;
			sendFrequencyToRadio();
		} else {
			errorState = "Invalid Frequency Value:" + frequency;
			System.err.print("Invalid Frequency Value:");
			System.err.println(frequency);
			SwingUtilities.invokeLater(new Runnable() {
                @Override
				public void run() {
                    JOptionPane.showMessageDialog(null, 
                    		"An Invalid Frequency Value has been entered", 
                    		"Parameter Exception Error",
                    		JOptionPane.ERROR_MESSAGE);
                }
            });
		}
	}
	
	private void sendFrequencyToRadio() {
		Thread tx = new Thread(new Runnable() {           
	        @Override
			public void run() {
	        	try {
		        	Thread.sleep(100);
		        	if (getFrequency() > 0 && testMode) {
		        		String freq = SET_RECEIVE_FREQUENCY + frequencyFormat.format(getFrequency() * 1E6) + "\r";
		        		sendTextStringToRadio(freq);
		        	}
	        	} catch (InterruptedException ex) {
	        		errorState = ex.getMessage();
	        		ex.printStackTrace();
	        	}
	        }
		});
		tx.start();
	}
	
	@Override
	public int getMode() {
		return 0;
	}

	@Override
	public void setMode(int mode) {

	}

	@Override
	public int getFilter() {
		return 0;
	}

	@Override
	public int getRSSI() {
		return rssi;
	}

	@Override
	public double getBER() {
		return ber;
	}
	
	@Override
	public void setFilter(int filter) {
	
	}

	class SerialPortReader implements SerialPortEventListener {
		@Override
		public void serialEvent(SerialPortEvent event) {
			if (event.isRXCHAR()) {
				pcs.firePropertyChange(RX_CHAR, rxChar, event.isRXCHAR());
				rxChar = event.isRXCHAR();
				try {
					byte[] buffer = serialPort.readBytes(event.getEventValue());
					extract(buffer);
				} catch (SerialPortException ex) {
					ex.printStackTrace();
				}
			}

			else if (event.isCTS()) {
				pcs.firePropertyChange(CTS, cts, event.isCTS());
				cts = event.isCTS();
			}

			else if (event.isDSR()) {
				pcs.firePropertyChange(DSR, dsr, event.isDSR());
				dsr = event.isDSR();
			}
			
			else if (event.isRLSD()) {
				pcs.firePropertyChange(RLSD, rlsd, event.isRLSD());
				rlsd = event.isRLSD();
			}
		}
	}

	private void extract(byte[] buffer) {
		String data = new String(buffer);
		rxBuffer = rxBuffer + data;
		if (rxBuffer.contains(READY_PROMPT)) {
				ready = true;
		}
		int endOfSentence = -1;
		for (int i = 2; i < rxBuffer.length(); i++) {
			if (rxBuffer.substring(i, i+1).equals("\r")) {
				endOfSentence = i;
				String s = rxBuffer.substring(0, endOfSentence);
				String sentence = "";
				for (int k = 0; k < s.length(); k++) {
					if (!s.substring(k,k+1).equals("\r") && !s.substring(k,k+1).equals("\n")) {
						sentence = sentence + s.substring(k,k+1);
					}
				}
				sentence = sentence + "\r";
				rxData = sentence;
				rxBuffer = rxBuffer.substring(endOfSentence);
				decode(sentence);
			}
		}
	}
	
	private void decode(String sentence) {
		try {
			if (sentence.contains(SET_RECEIVE_FREQUENCY)) {
				int s = sentence.indexOf(SET_RECEIVE_FREQUENCY) + SET_RECEIVE_FREQUENCY.length();
				frequency = Double.parseDouble(sentence.substring(s,s+9)) / 1E6;
			}
			if (sentence.contains(BER_TEST_IS_STOPPED)) {
				testBerFastMode = false;
			}
			if (sentence.contains(SQUELCH_STATUS_REPLY_BUSY) && lastCommand.contains(CHECK_SQUELCH_STATUS)) {
				pcs.firePropertyChange(BUSY, busy, true);
				busy = true;
				busyTimer.restart();
			}
			if (sentence.contains(SQUELCH_STATUS_REPLY_CLOSED) && lastCommand.contains(CHECK_SQUELCH_STATUS)) {
				pcs.firePropertyChange(BUSY, busy, false);
				busy = false;
				busyTimer.restart();
			}
			if ((sentence.contains(COMMAND_PASS) || sentence.contains(COMMAND_NOT_IMPLEMENTED)) && 
					lastCommand.contains(TEST_MODE_COMMAND)) {
				testMode = true;
			}
			if (sentence.contains(TEST_MODE_IS_STARTED)) {
				testMode = true;
			}
			if (sentence.contains(TEST_MODE_IS_STOPPED)) {
				testMode = false;
			}
			if (sentence.contains(BER_TEST_IS_STARTED)) {
				testBerFastMode = true;
			}
			if (sentence.contains(BER_DATA_HEADER)) {
				double oldBer = ber;
				ber = Double.parseDouble(sentence.substring(BER_DATA_HEADER.length(), BER_DATA_HEADER.length() + 6));
				berList[currentChannel] = ber;
				rxData = berFormat.format(ber);
				pcs.firePropertyChange(BER, oldBer, ber);
			}
			if (sentence.contains(RSSI_DATA_HEADER)) {
				int oldRssi = rssi;
				rssi = (int) Math.round(Double.parseDouble(sentence.substring(RSSI_DATA_HEADER.length(), RSSI_DATA_HEADER.length() + 5)));
				for (int i = 0; i < 10; i++) {
					if (i == currentChannel) {
						dBmList[currentChannel] = rssiTodBm(rssi);
						percentList[currentChannel] = dBmToPercent(getdBm());
					} else {
						dBmList[i] = -9999;
						percentList[i] = 0;
					}
				}
				testMode = true;
				pcs.firePropertyChange(RSSI, oldRssi, rssi);
				if (rssi < 120) {
					pcs.firePropertyChange(BUSY, busy, true);
					busy = true; 
					busyTimer.restart();
				} else {
					pcs.firePropertyChange(BUSY, busy, false);
					busy = false;
					busyTimer.restart();
				}
			}
		} catch (NumberFormatException ex) {
			errorState = ex.getMessage();
			ex.printStackTrace();
		}
	}

	private void sendTextStringToRadio(String msg) {
		if (serialPort != null) {
			if (serialPort.isOpened()) {
				try {
					lastCommand = msg;
					serialPort.writeString(msg);
					pcs.firePropertyChange(TX_DATA, txData, msg);
					txData = msg;
				} catch (SerialPortException ex) {
					errorState = ex.getMessage();
					ex.printStackTrace();
				}
			}
		}
	}

	private void startBERTest() {
		Thread tx = new Thread(new Runnable() {           
	        @Override
			public void run() {
	        	try {
	        		Thread.sleep(100);
	        		sendTextStringToRadio("\r");
	        		Thread.sleep(100);
	        		sendTextStringToRadio(TEST_MODE_COMMAND_STOP);
	        		Thread.sleep(100);
	        		sendTextStringToRadio(REBOOT);
	        		Thread.sleep(3500);
					if (ready){
						sendTextStringToRadio(BER_TEST_SET_COMMAND_FAST_MODE);
						Thread.sleep(200);
						if (testBerFastMode) {
							sendTextStringToRadio(BER_TEST_COMMAND_START);
						}
					}
	        	} catch (InterruptedException ex) {
	        		errorState = ex.getMessage();
	        		ex.printStackTrace();
	        	}
	        }
		});
		tx.start();
	}
	
	private void stopBERTest() {
		Thread tx = new Thread(new Runnable() {           
	        @Override
			public void run() {
	        	try {
	        		Thread.sleep(100);
	        		sendTextStringToRadio("\r");
	        		Thread.sleep(200);
	        		sendTextStringToRadio(BER_TEST_COMMAND_STOP);
	        	} catch (InterruptedException ex) {
	        		errorState = ex.getMessage();
	        		ex.printStackTrace();
	        	}
	        }
		});
		tx.start();
	}
	
	private void startRSSITest() {
		Thread tx = new Thread(new Runnable() {           
	        @Override
			public void run() {
	        	try {
	        		sendTextStringToRadio("\r");
	        		Thread.sleep(100);
	        		sendTextStringToRadio(BER_TEST_COMMAND_STOP);
					Thread.sleep(200);
					sendTextStringToRadio(TEST_MODE_COMMAND_START);
					Thread.sleep(200);
					sendFrequencyToRadio();
	        		Thread.sleep(300);
					sendTextStringToRadio(RSSI_TEST_COMMAND_START);	
	        	} catch (InterruptedException ex) {
	        		errorState = ex.getMessage();
	        		ex.printStackTrace();
	        	}
	        }
		});
		tx.start();
	}
	
	private void checkSquelchStatus() {
		Thread tx = new Thread(new Runnable() {           
	        @Override
			public void run() {
	        	try {
	        		Thread.sleep(50);
	        		sendTextStringToRadio(CHECK_SQUELCH_STATUS);
	        	} catch (InterruptedException ex) {
	        		errorState = ex.getMessage();
	        		ex.printStackTrace();
	        	}
	        }
		});
		tx.start();
	}
	
	private void setRadioOnLine(boolean newOnLine) {
		if (newOnLine) {
			Thread tx = new Thread(new Runnable() {           
		        @Override
				public void run() {
					try {
						if (serialPort.isOpened()) {
							rssiTimer.start();
							berTimer.start();
							busyTimer.start();
						//	squelchTimer.start();
							serialPort.setParams(19200, 8, 1, 0);
							setRTS(true);
							setDTR(true);
							Thread.sleep(100);
							sendTextStringToRadio(REBOOT);
							Thread.sleep(3500);
							if (sampleBER && sampleRSSI) {
								startRSSITest();
								testTimer.start();
							}
							else if (sampleBER)	startBERTest();
							else if (sampleRSSI) startRSSITest();
						}
					} catch (SerialPortException | InterruptedException ex) {
						if (powerIsOn) {
							errorState = ex.getMessage();
							ex.printStackTrace();
						}
					}
		        }
			});
			tx.start();
		} else {
			Thread tx = new Thread(new Runnable() {           
		        @Override
				public void run() {
					try {
						if (testBerFastMode) {
							stopBERTest();
							Thread.sleep(500);
						}
						rssiTimer.stop();
						berTimer.stop();
						busyTimer.stop();
						squelchTimer.stop();
						testTimer.stop();
						testMode = false;
						if (serialPort.isOpened()) serialPort.closePort();
					} catch (SerialPortException | InterruptedException ex) {
						errorState = ex.getMessage();
						ex.printStackTrace();
					}
		        }
			});
			tx.start();
		}
	}

	private void testTimerTimerActionListenerEvent(ActionEvent event) {
		switch (select) {
			case 0:
				startBERTest();
				select = 1;
				break;
			case 1:
				startRSSITest();
				select = 0;
				break;
		}
	}
	
	private void scanTimerActionListenerEvent(ActionEvent event) {
		int x = 0;
		do {
			x++;
			if (currentChannel < 9) {
				currentChannel++; 
			} else {
				currentChannel = 0;
			}
			if (scanSelectList[currentChannel]) {
				setFrequency(scanList[currentChannel]);
				startRSSITest();	
				x = 10;
			}
		} while (x < 10);
	}
	
	private void rssiTimerActionListenerEvent(ActionEvent event) {
		testMode = false;
		for (int i = 0; i < dBmList.length; i++) {
			dBmList[i] = 0;
		}
		for (int i = 0; i < percentList.length; i++) {
			percentList[i] = 0;
		}
		pcs.firePropertyChange(RSSI, rssi, 0);
		rssi = 0;
		rssiTimer.restart();
	}

	private void berTimerActionListenerEvent(ActionEvent event) {
		if (!closing) pcs.firePropertyChange(BER, ber, -9999);
		ber = -9999;
	}
	
	private void errorTimerActionListenerEvent(ActionEvent event) {
		if (!closing) pcs.firePropertyChange(ERROR_MESSAGE, errorState, "");
		errorState = "";
		errorTimer.restart();
	}

	private void busyTimerActionListenerEvent(ActionEvent event) {
		if (!closing) pcs.firePropertyChange(BUSY, busy, false);
		busy = false;
		busyTimer.restart();
	}
	
	private void squelchTimerActionListenerEvent(ActionEvent event) {
		checkSquelchStatus();
	}

	@Override
	public double getdBm() {
		return rssiTodBm(rssi);
	}

	@Override
	public double getPercent() {
		return dBmToPercent(getdBm());
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

	}
	
	@Override
	public boolean isVfoMode() {
		return false;
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

	private int dBmToPercent(double dBm) {
		return (int) Math.round(100 - (((dBm * -1.0) - 50) * 1.25));
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
	public void setDTR(boolean dtr) throws SerialPortException {
		serialPort.setDTR(dtr);
	}
	
	@Override
	public void setRTS(boolean rts) throws SerialPortException {
		serialPort.setRTS(rts);
	}
	
	@Override
	public void sampleRSSIValues(boolean sampleRSSI) {
		this.sampleRSSI = sampleRSSI;
	}
	
	@Override
	public void sampleBERValues(boolean sampleBER) {
		this.sampleBER = sampleBER;
	}

	@Override
	public double[] getBERList() {
		return berList;
	}

	@Override
	public String getError() {
		return errorState;
	}

	@Override
	public boolean isProgScan() {
		return false;
	}

	@Override
	public void setProgScan(boolean newScan) {
		
	}

	@Override
	public boolean isSquelchDelay() {
		return false;
	}

	@Override
	public void setSquelchDelayLong(boolean squelchDelay) {
		
	}

	@Override
	public double getTestdBmValue() {
		return 0;
	}
	
	@Override
	public boolean isRING() {
		try {
			return serialPort.isRING();
		} catch (SerialPortException e) {
			e.printStackTrace();
			return false;
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
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setToneSquelch(double toneSquelch) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getToneSquelch() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setScanList(Double[] newScanList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setScanSelectList(Boolean[] scanSelect) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double[] getScanList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean[] getScanSelectList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer[] getdBmList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCalibrationDataObject(CalibrationDataObject cdo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double[] getBERList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void startRadio() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initiateRadioStop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processData(String input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startScan() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopScan() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getDefaultFlowControlOut() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDefaultFlowControlIn() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDefaultDataBits() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDefaultStopBits() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDefaultParity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDefaultBaudRate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getDefaultRTS() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getDefaultDTR() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double minimumRxFrequency() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double maximumRxFrequency() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double minimumTxFrequency() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double maximumTxFrequency() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public EmissionDesignator[] getEmissionDesignators() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] supportedToneSquelchCodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] supportedDigitalSquelchCodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] availableFilters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String versionUID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isCTSSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] availableBaudRates() {
		// TODO Auto-generated method stub
		return null;
	}
}
