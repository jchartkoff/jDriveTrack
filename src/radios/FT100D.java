package radios;

import jssc.SerialPort;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jdrivetrack.Calibrate;
import jdrivetrack.CalibrationDataObject;
import jdrivetrack.EmissionDesignator;
import jdrivetrack.Utility;
import interfaces.RadioInterface;

public class FT100D implements RadioInterface {
	
	private static final int BAUD_RATE = SerialPort.BAUDRATE_4800;
	private static final int PARITY = SerialPort.PARITY_NONE;
	private static final int STOP_BITS = SerialPort.STOPBITS_2;
	private static final int DATA_BITS = SerialPort.DATABITS_8;
	private static final int FLOW_CONTROL_MODE = SerialPort.FLOWCONTROL_NONE;
	private static final int FLOW_CONTROL_IN = SerialPort.FLOWCONTROL_NONE;
	private static final int FLOW_CONTROL_OUT = SerialPort.FLOWCONTROL_NONE;
	private static final boolean DTR = true;
	private static final boolean RTS = true;
	
	private static final String ft100CommandGetReceiverMeter = "00000000F7";
	private static final String ft100CommandGetStatus = "0000000010";
	private static final String ft100CommandGetStatusFlags = "00000001FA";
	private static final String ft100CommandFilterSelection_2_4_kHz = "000000008C";
	private static final String ft100CommandFilterSelection_6_0_kHz = "000000018C";
	private static final String ft100CommandFilterSelection_500_Hz = "000000028C";
	private static final String ft100CommandFilterSelection_300_Hz = "000000038C";
	private static final String ft100CommandOperatingMode_LSB = "000000000C";
	private static final String ft100CommandOperatingMode_USB = "000000010C";
	private static final String ft100CommandOperatingMode_CW = "000000020C";
	private static final String ft100CommandOperatingMode_CWR = "000000030C";
	private static final String ft100CommandOperatingMode_AM = "000000040C";
	private static final String ft100CommandOperatingMode_DIG = "000000050C";
	private static final String ft100CommandOperatingMode_FM = "000000060C";
	private static final String ft100CommandOperatingMode_WFM = "000000070C";
	private static final String ft100CommandSetFrequencySuffix = "0A";

	private String modelNumber;
	private String serialNumber;
	private boolean agc;
	private boolean afc;
	private int filter;
	private double frequency;
	private int ifShift;
	private int mode;
	private int toneSquelch;
	private int digitalSquelch;
	private int rssi;
	private boolean busy;
	private boolean attenuator;
	private boolean noiseBlanker;
	private boolean voiceScan;
	private int volume;
	private int squelch;
	private String dsp;
	private String country;
	private String firmware;
	private boolean[] scanSelectList = new boolean [10];
	private double[] scanList = new double[10];
	private int[] percentList = new int[10];
	private int[] dBmList = new int[10];
	private boolean vfoMode;
	private String lastCommand = "";
	private int byteCounter = 0;
	private int[] receiveByteArray = new int[256];
	private int switchCounter = 0;
	private int scanCounter = 0;
	private int scanSelectPointer = 0;
	private int percentListPointer = 0;
	private int rssiAverageCounter = 0;
	private int rssiAverage = 0;
	private String txData;
	private String rxData;
	private int currentChannel = 0;
	private boolean sampleRSSI = false;
	private double testdBmValue = -90;
	private final int averagingFactor = 1;
	private Calibrate calibrate = null;
	private boolean getStronger = true;
	
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	private Runnable timer;
	private Runnable writeTimer;
	
	private ScheduledFuture<?> timerHandle = null;
	private ScheduledFuture<?> writeTimerHandle = null;
	
	private ArrayList<String> writeStack = new ArrayList<String>();
	
	public FT100D() {}
	
	private void startWriteTimer() {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		writeTimer = new Runnable() {@Override
		public void run() {writeMessage();}};
		writeTimerHandle = scheduler.scheduleAtFixedRate(writeTimer, 50, 50, TimeUnit.MILLISECONDS);
		scheduler.execute(writeTimer);
	}

	@Override
	public boolean isBusy() {
		return busy;
	}

	@Override
	public int getDigitalSquelch() {
		return digitalSquelch;
	}

	@Override
	public void setDigitalSquelch(int newDigitalSquelch) {
		digitalSquelch = newDigitalSquelch;
	}

	@Override
	public int getToneSquelch() {
		return toneSquelch;
	}

	@Override
	public void setToneSquelch(int newToneSquelch) {
		if (newToneSquelch >= 0 && newToneSquelch <= 39) {
			toneSquelch = newToneSquelch;
		}
	}

	@Override
	public double getFrequency() {
		return frequency;
	}

	@Override
	public void setFrequency(double newFrequency) {
		if (newFrequency >= 0.01 && newFrequency <= 970.0) {
			DecimalFormat freqFormat = new DecimalFormat("000.00000");
			frequency = newFrequency;
			String sFreq = freqFormat.format(frequency);
			String s1 = Utility.integerToDecimalString(Integer.parseInt(sFreq.substring(0, 2)));
			String s2 = Utility.integerToDecimalString(Integer.parseInt(sFreq.substring(2, 3) + sFreq.substring(4, 5)));
			String s3 = Utility.integerToDecimalString(Integer.parseInt(sFreq.substring(5, 7)));
			String s4 = Utility.integerToDecimalString(Integer.parseInt(sFreq.substring(7, 9)));
			writeStack.add(s4 + s3 + s2 + s1 + ft100CommandSetFrequencySuffix);
			startWriteTimer();
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
					writeStack.add(ft100CommandOperatingMode_LSB);
					break;
				case 1:
					this.mode = 1;
					writeStack.add(ft100CommandOperatingMode_USB);
					break;
				case 2:
					this.mode = 2;
					writeStack.add(ft100CommandOperatingMode_AM);
					break;
				case 3:
					this.mode = 3;
					writeStack.add(ft100CommandOperatingMode_CW);
					break;
				case 4:
					this.mode = 4;
					writeStack.add(ft100CommandOperatingMode_CWR);
					break;
				case 5:
					this.mode = 5;
					writeStack.add(ft100CommandOperatingMode_FM);
					break;
				case 6:
					this.mode = 6;
					writeStack.add(ft100CommandOperatingMode_DIG);
					break;
				case 7:
					this.mode = 7;
					writeStack.add(ft100CommandOperatingMode_FM);
					break;
				case 8:
					this.mode = 8;
					writeStack.add(ft100CommandOperatingMode_WFM);
					break;
			}
			startWriteTimer();
		}
	}

	@Override
	public int getRSSI() {
		return rssi;
	}

	@Override
	public int getFilter() {
		return filter;
	}

	@Override
	public void setFilter(int filter) {
		if (filter >= 0 && filter <= 7) {
			switch (filter) {
				case 0:
					this.filter = 0;
					writeStack.add(ft100CommandFilterSelection_300_Hz);
					break;
				case 1:
					this.filter = 1;
					writeStack.add(ft100CommandFilterSelection_500_Hz);
					break;
				case 2:
					this.filter = 2;
					writeStack.add(ft100CommandFilterSelection_2_4_kHz);
					break;
				case 3:
					this.filter = 3;
					writeStack.add(ft100CommandFilterSelection_2_4_kHz);
					break;
				case 4:
					this.filter = 4;
					writeStack.add(ft100CommandFilterSelection_6_0_kHz);
					break;
				case 5:
					this.filter = 5;
					writeStack.add(ft100CommandFilterSelection_6_0_kHz);
					break;
				case 6:
					this.filter = 6;
					writeStack.add(ft100CommandFilterSelection_6_0_kHz);
					break;
				case 7:
					this.filter = 7;
					writeStack.add(ft100CommandFilterSelection_6_0_kHz);
					break;
			}
			startWriteTimer();
		}
	}

	@Override
	public int getIFShift() {
		return ifShift;
	}

	@Override
	public void setIFShift(int ifShift) {
		if (ifShift >= 0 && ifShift <= 255) {
			this.ifShift = ifShift;
		}
	}

	@Override
	public boolean isAGC() {
		return agc;
	}

	@Override
	public void setAGC(boolean agc) {
		this.agc = agc;
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
	public boolean isAttenuator() {
		return attenuator;
	}

	@Override
	public void setAttenuator(boolean attenuator) {
		this.attenuator = attenuator;
	}

	@Override
	public boolean isNoiseBlanker() {
		return noiseBlanker;
	}

	@Override
	public void setNoiseBlanker(boolean noiseBlanker) {
		this.noiseBlanker = noiseBlanker;
	}

	@Override
	public boolean isVoiceScan() {
		return voiceScan;
	}

	@Override
	public void setVoiceScan(boolean voiceScan) {
		this.voiceScan = voiceScan;
	}

	@Override
	public void setVolume(int volume) {
		this.volume = volume;
	}

	@Override
	public int getVolume() {
		return volume;
	}

	@Override
	public void setSquelch(int squelch) {
		this.squelch = squelch;
	}

	@Override
	public int getSquelch() {
		return squelch;
	}

	@Override
	public String getDSP() {
		return dsp;
	}

	@Override
	public String getCountry() {
		return country;
	}

	@Override
	public String getFirmware() {
		return firmware;
	}

	@Override
	public void dataInput(byte[] buffer) {
		decode(buffer);
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

	private void queryTimer() {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		timer = new Runnable() {@Override
		public void run() {interrogate();}};
		timerHandle = scheduler.scheduleAtFixedRate(timer, 100, 100, TimeUnit.MILLISECONDS);
		scheduler.execute(timer);
	}
	
	private void interrogate() {
		switch (switchCounter) {
			case 0:
				if ((scanCounter <= 0) && !vfoMode) {
					scanCounter = 3;
					while (!scanSelectList[scanSelectPointer]) {
						if (scanSelectPointer < 9)
							scanSelectPointer ++;
						else 
							scanSelectPointer = 0;
					}
					percentListPointer = scanSelectPointer;
					currentChannel = scanSelectPointer;
					setFrequency(scanList[scanSelectPointer]);
					scanSelectPointer ++;
				}
				else {
					scanCounter --;
				}
				byteCounter = 0;
				writeStack.add(ft100CommandGetReceiverMeter);
				switchCounter = 1;
				break;
			case 1:
				byteCounter = 0;
				writeStack.add(ft100CommandGetStatus);
				switchCounter = 2;
				break;
			case 2:
				byteCounter = 0;
				writeStack.add(ft100CommandGetStatusFlags);
				switchCounter = 0;
				break;
		}
		startWriteTimer();
	}
	
	@Override
	public double getdBm() {
		return calibrate.getdBm(rssi);
	}

	@Override
	public double getPercent() {
		return 100.0 - (rssi * .537);
	}

	@Override
	public void setScanList(double[]scanList) {
		this.scanList = scanList;
	}

	@Override
	public void setScanSelectList(boolean[] scanSelectList) {
		this.scanSelectList = scanSelectList;
	}

	@Override
	public void setVfoMode(boolean vfoMode) {
		this.vfoMode = vfoMode;
		if (vfoMode) currentChannel = 0;
	}

	@Override
	public double[] getScanList() {
		return this.scanList;
	}

	@Override
	public boolean[] getScanSelectList() {
		return this.scanSelectList;
	}

	@Override
	public boolean isVfoMode() {
		return vfoMode;
	}

	@Override
	public int[] getPercentList() {
		return this.percentList;
	}
	
	@Override
	public String getTransmittedData() {
		return txData;
	}

	@Override
	public String getReceivedData() {
		return rxData;
	}

	@Override
	public int[] getdBmList() {
		return this.dBmList;
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
		return 0.0;
	}

	@Override
	public void sampleRSSIValues(boolean sampleRSSI) {
		this.sampleRSSI = sampleRSSI;
	}

	@Override
	public void sampleBERValues(boolean sampleBER) {}

	@Override
	public double[] getBERList() {
		double[] berList = {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
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
	public void setSquelchDelayLong(boolean squelchDelay) {
		
	}

	@Override
	public double getTestdBmValue() {
		if (testdBmValue <= -90) getStronger = true;
		if (testdBmValue >= -30) getStronger = false;	
		if (getStronger) testdBmValue += 0.05;
			else testdBmValue -= 0.05;
		return testdBmValue;
	}

	@Override
	public void startRadio() {
		setFrequency(frequency);
		setMode(mode);
		setFilter(filter);
		setToneSquelch(toneSquelch);
		setDigitalSquelch(digitalSquelch);
		queryTimer();
		pcs.firePropertyChange(BUSY, null, false);
		pcs.firePropertyChange(RSSI, null, 0);	
	}

	@Override
	public void initiateRadioStop() {
		timerHandle.cancel(true);
	}

	private void decode(byte[] b) {
		try {
			if (b == null) return;  
			for (int i = 0; i < b.length; i++) {
				if ((b[i] & 128) == 128)
					receiveByteArray[byteCounter] = -b[i] + 128;
				else
					receiveByteArray[byteCounter] = b[i];
				byteCounter = byteCounter + 1;
			}
			
			switch (lastCommand) {
				case ft100CommandGetReceiverMeter:
					if (byteCounter == 9) {
						rxData = "F7 : ";
						for (int i = 0; i < 8; i++) {
							rxData = rxData + receiveByteArray[i] + " ";
						}
						pcs.firePropertyChange(RX_DATA, null, rxData);
						if (percentListPointer == 0 && scanCounter == averagingFactor) {
							rssiAverage = 0;
							rssiAverageCounter = 0;
						}
						if (rssiAverageCounter < averagingFactor) {
							rssiAverage = rssiAverage + receiveByteArray[3];
							rssiAverageCounter++;
						}
						else {
							rssiAverageCounter = 0;
							rssi = rssiAverage / averagingFactor;
							if (rssi > 186) rssi = 186;
							rssiAverage = 0;
							percentList[percentListPointer] = (int) getPercent();
							dBmList[percentListPointer] = (int) getdBm();
							if (sampleRSSI) pcs.firePropertyChange(RSSI, null, rssi);
						}
						lastCommand = "";
					}
					break;
	
				case ft100CommandGetStatus:
					if (byteCounter == 17) {
						rxData = "10 : ";
						for (int i = 0; i < 16; i++) {
							rxData = rxData + receiveByteArray[i] + " ";
						}
						pcs.firePropertyChange(RX_DATA, null, rxData);
						int b1 = receiveByteArray[1] * 16777216;
						int b2 = receiveByteArray[2] * 65535;
						int b3 = receiveByteArray[3] * 256;
						int b4 = receiveByteArray[4] * 1;
						frequency = (b1 + b2 + b3 + b4) * 1.25;
						mode = receiveByteArray[5] & 15;
						filter = 255 - (receiveByteArray[5] & 240);
						if ((receiveByteArray[8] & 1) == 1)
							attenuator = true;
						else
							attenuator = false;
						if ((receiveByteArray[8] & 128) == 128)
							toneSquelch = receiveByteArray[6] + 1;
						else
							toneSquelch = 0;
						lastCommand = "";
					}
					break;
	
				case ft100CommandGetStatusFlags:
					if (byteCounter == 8) {
						rxData = "01FA : ";
						for (int i = 0; i < 7; i++) {
							rxData = rxData + receiveByteArray[i] + " ";
						}
						pcs.firePropertyChange(RX_DATA, null, rxData);
						if ((receiveByteArray[7] & 64) == 64)
							noiseBlanker = true;
						else
							noiseBlanker = false;
						if ((receiveByteArray[6] & 8) == 8)
							busy = true;
						else {
							busy = false;
							rssi = 186;
							for (int i = 0; i < percentList.length; i++) {
								percentList[i] = 0;
								dBmList[i] = 0;
							}
							pcs.firePropertyChange(RSSI, null, rssi);
						}
						if (sampleRSSI) pcs.firePropertyChange(BUSY, null, busy);
						lastCommand = "";
					}
					break;	
			}
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
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
	public void startScan() {
		
	}

	@Override
	public void stopScan() {
		
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
	public void processData(String input) {
		// TODO Auto-generated method stub
		
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
