package com;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jssc.SerialPort;

public class P7200 implements RadioInterface {
	private static final int BAUD_RATE = SerialPort.BAUDRATE_38400;
	private static final int PARITY = SerialPort.PARITY_NONE;
	private static final int STOP_BITS = SerialPort.STOPBITS_1;
	private static final int DATA_BITS = SerialPort.DATABITS_8;
	private static final int FLOW_CONTROL_MODE = SerialPort.FLOWCONTROL_NONE;
	private static final int FLOW_CONTROL_IN = SerialPort.FLOWCONTROL_NONE;
	private static final int FLOW_CONTROL_OUT = SerialPort.FLOWCONTROL_NONE;
	private static final boolean DTR = true;
	private static final boolean RTS = true;

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
	private NumberFormat berFormat = new DecimalFormat("#0.000");
	private String errorState;
	private Component owner;
	
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
	private static final String RSSI_DATA_HEADER = "RSSI =  ";
	private static final String COMMAND_PASS = "?PASS";
	private static final String SET_RECEIVE_FREQUENCY = "cmd 50 ";
	private static final String CHECK_SQUELCH_STATUS = "cmd 52 2\r";
	private static final String SQUELCH_STATUS_REPLY_BUSY = "?PASS 1";
	private static final String SQUELCH_STATUS_REPLY_CLOSED = "?PASS 0";
	private static final String REBOOT = "ATZ 0\r";
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	private ArrayList<String> writeStack = new ArrayList<String>();
	
	private Calibrate calibrate = null;
	
	private Runnable testTimer;
	private Runnable scanTimer;
	private Runnable rssiClearanceTimer;
	private Runnable busyClearanceTimer;
	private Runnable berClearanceTimer;
	private Runnable errorClearanceTimer;
	private Runnable squelchClearanceTimer;
	private Runnable writeTimer;
	
	private ScheduledFuture<?> testTimerHandle = null;
	private ScheduledFuture<?> rssiClearanceTimerHandle = null;
	private ScheduledFuture<?> busyClearanceTimerHandle = null;
	private ScheduledFuture<?> scanTimerHandle = null;
	private ScheduledFuture<?> berClearanceTimerHandle = null;
	private ScheduledFuture<?> errorClearanceTimerHandle = null;
	private ScheduledFuture<?> squelchClearanceTimerHandle = null;
	private ScheduledFuture<?> writeTimerHandle = null;
	
	public P7200() {}

	private void testTimer() {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		testTimer = new Runnable() {public void run() {testCycle();}};
		testTimerHandle = scheduler.scheduleAtFixedRate(testTimer, 30000, 30000, TimeUnit.MILLISECONDS);
		scheduler.execute(testTimer);
	}
	
	private void rssiClearanceTimer() {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		rssiClearanceTimer = new Runnable() {public void run() {clearRSSI();}};
		rssiClearanceTimerHandle = scheduler.scheduleAtFixedRate(rssiClearanceTimer, 1800, 1800, TimeUnit.MILLISECONDS);
		scheduler.execute(rssiClearanceTimer);
	}
	
	private void busyClearanceTimer() {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		busyClearanceTimer = new Runnable() {public void run() {clearBusy();}};
		busyClearanceTimerHandle = scheduler.scheduleAtFixedRate(busyClearanceTimer, 1750, 1750, TimeUnit.MILLISECONDS);
		scheduler.execute(busyClearanceTimer);
	}
	
	private void berClearanceTimer() {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		berClearanceTimer = new Runnable() {public void run() {clearBER();}};
		berClearanceTimerHandle = scheduler.scheduleAtFixedRate(berClearanceTimer, 1500, 1500, TimeUnit.MILLISECONDS);
		scheduler.execute(berClearanceTimer);
	}

	private void errorClearanceTimer() {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		errorClearanceTimer = new Runnable() {public void run() {clearError();}};
		errorClearanceTimerHandle = scheduler.scheduleAtFixedRate(errorClearanceTimer, 1500, 1500, TimeUnit.MILLISECONDS);
		scheduler.execute(errorClearanceTimer);
	}
	
	private void scanTimer() {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scanTimer = new Runnable() {public void run() {scanCycle();}};
		scanTimerHandle = scheduler.scheduleAtFixedRate(scanTimer, 8000, 5000, TimeUnit.MILLISECONDS);
		scheduler.execute(scanTimer);
	}
	
	private void squelchClearanceTimer() {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		squelchClearanceTimer = new Runnable() {public void run() {clearSquelch();}};
		squelchClearanceTimerHandle = scheduler.scheduleAtFixedRate(squelchClearanceTimer, 1500, 1500, TimeUnit.MILLISECONDS);
		scheduler.execute(squelchClearanceTimer);
	}
	
	private void startWriteTimer() {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		writeTimer = new Runnable() {public void run() {writeMessage();}};
		writeTimerHandle = scheduler.scheduleAtFixedRate(writeTimer, 40, 40, TimeUnit.MILLISECONDS);
		scheduler.execute(writeTimer);
	}


	private void clearSquelch() {
		writeStack.add(CHECK_SQUELCH_STATUS);
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
	public void setFrequency(final double frequency) {
		if ((frequency >= 764.0 && frequency <= 775.99975) || (frequency >= 851.0 && frequency <= 868.9875)) {
			this.frequency = frequency;
			sendFrequencyToRadio();
		} else {
			errorState = "Invalid Frequency";
			SwingUtilities.invokeLater(new Runnable() {
                @Override
				public void run() {
                    JOptionPane.showMessageDialog(null, 
                    		"An invalid frequency value of " + frequencyFormat.format(frequency * 1E6) + " has been entered.", 
                    		"Parameter Exception Error",
                    		JOptionPane.ERROR_MESSAGE);
                }
            });
		}
	}
	
	private void sendFrequencyToRadio() {
    	if (getFrequency() > 0 && testMode) {
    		String freq = SET_RECEIVE_FREQUENCY + frequencyFormat.format(getFrequency() * 1E6) + "\r";
    		writeStack.add(freq);
    	}
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

	private void extract(String data) {
		rxBuffer = rxBuffer + data;
		if (rxBuffer.contains(READY_PROMPT)) ready = true;
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
	
	private synchronized void decode(String sentence) {
		try {
			if (sentence.contains(SET_RECEIVE_FREQUENCY)) {
				int s = sentence.indexOf(SET_RECEIVE_FREQUENCY) + SET_RECEIVE_FREQUENCY.length();
				frequency = Double.parseDouble(sentence.substring(s,s+9)) / 1E6;
			}
			if (sentence.contains(BER_TEST_IS_STOPPED)) {
				testBerFastMode = false;
			}
			if (sentence.contains(SQUELCH_STATUS_REPLY_BUSY) && lastCommand.contains(CHECK_SQUELCH_STATUS)) {
				pcs.firePropertyChange(BUSY, null, true);
				busyClearanceTimer();
			}
			if (sentence.contains(SQUELCH_STATUS_REPLY_CLOSED) && lastCommand.contains(CHECK_SQUELCH_STATUS)) {
				pcs.firePropertyChange(BUSY, null, false);
				busyClearanceTimer();
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
				ber = Double.parseDouble(sentence.substring(BER_DATA_HEADER.length(), BER_DATA_HEADER.length() + 6));
				berList[currentChannel] = ber;
				rxData = berFormat.format(ber);
				pcs.firePropertyChange(BER, null, ber);
			}
			if (sentence.contains(RSSI_DATA_HEADER)) {
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
				pcs.firePropertyChange(RSSI, null, rssi);
				if (rssi < 120) {
					pcs.firePropertyChange(BUSY, null, true);
					busyClearanceTimer();
				} else {
					pcs.firePropertyChange(BUSY, null, false);
					busyClearanceTimer();
				}
			}
		} catch (NumberFormatException ex) {
			errorState = ex.getMessage();
			ex.printStackTrace();
		}
	}

	private void startBERTest() {
		writeStack.add("\r");
		writeStack.add(TEST_MODE_COMMAND_STOP);
		writeStack.add(REBOOT);
		if (ready){
			writeStack.add(BER_TEST_SET_COMMAND_FAST_MODE);
			if (testBerFastMode) {
				writeStack.add(BER_TEST_COMMAND_START);
			}
		}
	}
	
	private void stopBERTest() {
        writeStack.add("\r");
        writeStack.add(BER_TEST_COMMAND_STOP);
        berClearanceTimerHandle.cancel(true);
	}
	
	private void startRSSITest() {
		writeStack.add("\r");
		writeStack.add(TEST_MODE_COMMAND_START);
		sendFrequencyToRadio();
		writeStack.add(RSSI_TEST_COMMAND_START);	
	}

	private void testCycle() {
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
	
	private void scanCycle() {
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
	
	private void clearRSSI() {
		testMode = false;
		for (int i = 0; i < dBmList.length; i++) {
			dBmList[i] = 0;
		}
		for (int i = 0; i < percentList.length; i++) {
			percentList[i] = 0;
		}
		pcs.firePropertyChange(RSSI, null, 0);
		rssiClearanceTimer();
	}

	private void clearBER() {
		pcs.firePropertyChange(BER, null, -9999);
	}
	
	private void clearError() {
		pcs.firePropertyChange(ERROR_MESSAGE, null, "");
		errorClearanceTimer();
	}

	private void clearBusy() {
		pcs.firePropertyChange(BUSY, null, false);
		busyClearanceTimer();
	}

	private void writeMessage() {
		if (writeStack.size() > 0) {
			int end = 0;
			String data = writeStack.get(end);
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
		return txData;
	}

	@Override
	public String getReceivedData() {
		return rxData;
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
	public void setCalibrationFile(String calFileName) throws IOException {
		calibrate = new Calibrate(calFileName);
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
	public void setSquelchDelay(boolean squelchDelay) {
		
	}

	@Override
	public double getTestdBmValue() {
		return 0;
	}

	@Override
	public void startRadio() {
		
	}

	@Override
	public void stopRadio() {
		
	}

	@Override
	public void dataInput(byte[] buffer) {
		
	}

	@Override
	public void startScan() {
		
	}

	@Override
	public void stopScan() {
		
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
