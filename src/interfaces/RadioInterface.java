package interfaces;

import java.beans.PropertyChangeListener;

import jdrivetrack.CalibrationDataObject;
import jdrivetrack.EmissionDesignator;

public interface RadioInterface {

	public static final String SIGNAL_OFFSET = "SIGNAL_OFFSET";
	public static final String DTMF_DECODE = "DTMF_DECODE";
	public static final String WAVEFORM_DATA = "WAVEFORM_DATA";
	public static final String SCAN_STATUS = "SCAN_STATUS";
	public static final String DSP_STATUS = "DSP_STATUS";
	public static final String BREAK = "BREAK";
	public static final String READY = "READY";
	public static final String FIRMWARE = "FIRMWARE";
	public static final String COUNTRY = "COUNTRY";
	public static final String DSP = "DSP";
	public static final String POWER = "POWER";
	public static final String PROTOCOL = "PROTOCOL";
	public static final String ERROR_MESSAGE = "ERROR_MESSAGE";
	public static final String ERROR = "ERROR";
	public static final String RSSI = "RSSI";
	public static final String BUSY = "BUSY";
	public static final String BER = "BER";
	public static final String ACK = "ACK";
	public static final String TX_DATA = "TX_DATA";
	public static final String RX_DATA = "RX_DATA";
	public static final String SEND_TO_SERIAL_PORT = "SEND_TO_SERIAL_PORT";
	public static final String CLOSE_SERIAL_PORT = "CLOSE_SERIAL_PORT"; 
	public static final String SET_BAUD_RATE = "SET_BAUD_RATE";
	public static final String CANCEL_EVENTS = "CANCEL_EVENTS";
	
	public static final String[] DEVICES = {
		"Harris P7200",
		"Harris XG75", 
		"Icom PCR1000", 
		"Icom PCR2500", 
		"Yaesu FT100"
	};
	
	boolean isReady();
	
	boolean isAGC();

	void setAGC(boolean agc);

	boolean isAFC();

	void setAFC(boolean afc);

	boolean isAttenuator();

	void setAttenuator(boolean attenuator);

	boolean isNoiseBlanker();

	void setNoiseBlanker(boolean noiseBlanker);

	boolean isVoiceScan();

	void setVoiceScan(boolean voiceScan);

	int getVolume();

	void setVolume(int volume);

	int getSquelch();

	void setSquelch(int squelch);

	int getIFShift();

	void setIFShift(int ifShift);

	int getFilter();

	void setFilter(int filter);

	int getMode();

	void setMode(int mode);

	String getDSP();

	String getCountry();

	String getFirmware();

	int getRSSI();

	boolean isBusy();

	void setFrequency(double frequency);

	double getFrequency();

	void setToneSquelch(double toneSquelch);

	double getToneSquelch();

	void setDigitalSquelch(int digitalSquelch);

	int getDigitalSquelch();

	double getdBm();

	double getPercent();
	
	void setScanList(Double[] newScanList);

	void setScanSelectList(Boolean[] scanSelect);

	void setVfoMode(boolean vfoMode);

	Double[] getScanList();

	Boolean[] getScanSelectList();

	boolean isVfoMode();

	String getTransmittedData();
	
	String getReceivedData();

	Integer[] getdBmList();

	int[] getPercentList();
	
	int getCurrentChannel();
	
	void setCalibrationDataObject(CalibrationDataObject cdo);

	String getModelNumber();

	String getSerialNumber();

	double getBER();

	void sampleRSSIValues(boolean sampleRSSI);

	void sampleBERValues(boolean sampleBER);

	Double[] getBERList();

	boolean isProgScan();

	void setProgScan(boolean scan);

	boolean isSquelchDelay();

	void setSquelchDelayLong(boolean squelchDelay);
	
	double getTestdBmValue();
	
	void addPropertyChangeListener(PropertyChangeListener listener);
	
	void removePropertyChangeListener(PropertyChangeListener listener);

	void startRadio();

	void initiateRadioStop();

	void processData(String input);

	void startScan();

	void stopScan();

	int getDefaultFlowControlOut();

	int getDefaultFlowControlIn();

	int getDefaultDataBits();

	int getDefaultStopBits();

	int getDefaultParity();

	int getDefaultBaudRate();

	boolean getDefaultRTS();

	boolean getDefaultDTR();
	
	double minimumRxFrequency();
	
	double maximumRxFrequency();
	
	double minimumTxFrequency();
	
	double maximumTxFrequency();
	
	EmissionDesignator[] getEmissionDesignators();

	String[] supportedToneSquelchCodes();
	
	String[] supportedDigitalSquelchCodes();
	
	String[] availableFilters();
	
	String versionUID();

	void dispose();

	boolean isCTSSupported();

	String[] getAvailableBaudRates();

	boolean serialParametersFixed();
}