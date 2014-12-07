package com;

import java.awt.Point;
import java.beans.PropertyChangeListener;

public interface APRSInterface {

	public static final String RX_CHAR = "RX_CHAR";
	public static final String RX_DATA = "RX_DATA";
	public static final String TX_DATA = "TX_DATA";
	public static final String WAYPOINT = "WAYPOINT";
	public static final String CRC_ERROR = "CRC_ERROR";
	public static final String SEND_TO_SERIAL_PORT = "SEND_TO_SERIAL_PORT";

	Point.Double getAprsPosition();
	
	String getAprsIdentifier();

	String getMessageString();

	String getGPWPLMessageString();

	void sendToAprsTnc(String msg);

	void addPropertyChangeListener(PropertyChangeListener listener);
	
	void removePropertyChangeListener(PropertyChangeListener listener);

	void inputData(String string); 
}