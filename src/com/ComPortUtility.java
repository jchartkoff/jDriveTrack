package com;

import jssc.SerialPortList;

public class ComPortUtility {

	protected ComPortUtility() {}
	
	public static boolean isComPortValid(String portName) {
		boolean isAvailable = false;
		if (portName.isEmpty() || !portName.toUpperCase().startsWith("COM")) return isAvailable;
		String[] ports = SerialPortList.getPortNames();
        for (String port : ports) {
            if (port.equals(portName)) {
            	isAvailable = true;
            }
        }
        return isAvailable;
	}

}
