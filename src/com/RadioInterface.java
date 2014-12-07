package com;

import java.beans.PropertyChangeListener;
import java.io.IOException;

public interface RadioInterface {

	public static final String SIGNAL_OFFSET = "SIGNAL_OFFSET";
	public static final String DTMF_DECODE = "DTMF_DECODE";
	public static final String WAVEFORM_DATA = "WAVEFORM_DATA";
	public static final String SCAN_STATUS = "SCAN_STATUS";
	public static final String BREAK = "BREAK";
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

	public enum Mode { LSB, USB, AM, CW, CW_R, DSP, DIG, Narrow_FM, Wide_FM, P25, DSTAR, DMR };
	
	public enum Filter { Hz_300, Hz_500, kHz_2_4, kHz_3, kHz_6, kHz_15, kHz_50, kHz_230 };
	
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

	void setToneSquelch(int toneSquelch);

	int getToneSquelch();

	void setDigitalSquelch(int digitalSquelch);

	int getDigitalSquelch();

	double getdBm();

	double getPercent();
	
	void setScanList(double[] newScanList);

	void setScanSelectList(boolean[] scanSelect);

	void setVfoMode(boolean vfoMode);

	double[] getScanList();

	boolean[] getScanSelectList();

	boolean isVfoMode();

	String getTransmittedData();
	
	String getReceivedData();

	int[] getdBmList();

	int[] getPercentList();
	
	int getCurrentChannel();
	
	void setCalibrationFile(String calFileName) throws IOException;

	String getModelNumber();

	String getSerialNumber();

	double getBER();

	void sampleRSSIValues(boolean sampleRSSI);

	void sampleBERValues(boolean sampleBER);

	double[] getBERList();

	boolean isProgScan();

	void setProgScan(boolean scan);

	boolean isSquelchDelay();

	void setSquelchDelay(boolean squelchDelay);
	
	double getTestdBmValue();
	
	void addPropertyChangeListener(PropertyChangeListener listener);
	
	void removePropertyChangeListener(PropertyChangeListener listener);

	void startRadio();

	void stopRadio();

	void dataInput(byte[] buffer);

	void startScan();

	void stopScan();

	int getDefaultFlowControlOut();

	int getDefaultFlowControlIn();

	int getDefaultFlowControlMode();

	int getDefaultDatBits();

	int getDefaultStopBits();

	int getDefaultParity();

	int getDefaultBaudRate();

	boolean getDefaultRTS();

	boolean getDefaultDTR();

}