package com;

import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class AprsNMEAWaypointDecoder implements APRSInterface {

	private String messageString;
	private String gpwplMessageString;
	private String rStr;
	private Point.Double aprsPosition = null;
	private String aprsIdent;

	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public AprsNMEAWaypointDecoder() {}
	
	@Override
	public String getMessageString() {
		return messageString;
	}

	@Override
	public String getGPWPLMessageString() {
		return gpwplMessageString;
	}

	@Override
	public Point.Double getAprsPosition() {
		return aprsPosition;
	}

	@Override
	public String getAprsIdentifier() {
		return aprsIdent;
	}

	private void nmeaDecoder(String msg) {
		double latTry;
		double lngTry;
		String[] a;
		try {
			messageString = msg;
			if (msg.substring(0, 1).equals("$")) {
				if (checksum(msg)) {
					String completeMsg = msg;
					msg = msg.substring(0, msg.indexOf("*"));
					a = msg.split(",");
					if (a[0].equals("$GPWPL")) {
						if (a[1].length() != 0 && a[3].length() != 0) {
							latTry = (Double.parseDouble(a[1].substring(0, 2)) * 1000000)
									+ (Double.parseDouble(a[1].substring(2)) * 16666.6666667);
							if (a[2].equals("S")) latTry = -latTry;
							lngTry = (Double.parseDouble(a[3].substring(0, 3)) * 1000000)
									+ (Double.parseDouble(a[3].substring(3)) * 16666.6666667);
							if (a[4].equals("W")) lngTry = -lngTry;
							aprsPosition.y = latTry / 1000000;
							aprsPosition.x = lngTry / 1000000;
							if (a[5].length() != 0) aprsIdent = (a[5]);
								else aprsIdent = "";
							pcs.firePropertyChange(WAYPOINT, null, completeMsg);
						}
					}
				}
			}
		} catch (NumberFormatException ex) {
			System.err.println(ex.getMessage());
		} catch (NullPointerException ex) {
			System.err.println(ex.getMessage());
		}
	}

	@Override
	public void sendToAprsTnc(String msg) {
			String txData = msg + "\n";
			pcs.firePropertyChange(SEND_TO_SERIAL_PORT, null, txData);
			pcs.firePropertyChange(TX_DATA, null, msg);
	}

	private boolean checksum(String input) {
		String chkDat;
		String[] dat;
		String chkSum;
		if (input.indexOf("*", 2) < 2) {
			pcs.firePropertyChange(CRC_ERROR, null, input);
			return false;
		} else {
			try {
				chkDat = input.substring(1, input.indexOf("*"));

				dat = input.split(",");

				chkSum = dat[dat.length - 1];
				chkSum = chkSum.substring(chkSum.indexOf("*") + 1);

				int s = chkDat.charAt(0);

				for (int i = 1; i < chkDat.length(); i++) {
					s = s ^ chkDat.charAt(i);
				}

				if (s == Integer.valueOf(chkSum, 16).intValue())
					return true;
				else
					pcs.firePropertyChange(CRC_ERROR, null, input);
					return false;
			}

			catch (NumberFormatException ex) {
				pcs.firePropertyChange(CRC_ERROR, null, input);
				return false;
			}
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
	public void inputData(String instr) {
		int iStart, iEnd, iTemp;
		rStr = rStr + instr;
		while (rStr.length() > 0) {
			iStart = rStr.indexOf("$", 0);
			if (iStart >= 0) {
				iTemp = rStr.indexOf("*", iStart + 1);
				if (iTemp > 0) iEnd = iTemp + 3;
				else iEnd = 0;
			} else iEnd = 0;
			if (iStart >= 0 && rStr.length() >= iEnd && iEnd > 0) {
				nmeaDecoder(rStr.substring(iStart, iEnd));
				if (rStr.length() > iEnd + 1) rStr = rStr.substring(iEnd + 1);
				else rStr = "";
			} else break;
		}
	}

}