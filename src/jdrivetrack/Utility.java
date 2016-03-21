package jdrivetrack;

import gov.nasa.worldwind.Configuration;

import java.applet.Applet;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.Window;
import java.awt.image.BufferedImage;

import jssc.SerialPortList;

public class Utility {

	public static Dimension getScreenSize(Window wnd) {
		Dimension ss;

		if (wnd == null) {
			ss = Toolkit.getDefaultToolkit().getScreenSize();
		} else {
			ss = wnd.getToolkit().getScreenSize();
		}
		return ss;
	}
	
	public static int getScreenResolution(Window wnd) {
		int sr;

		if (wnd == null) {
			sr = Toolkit.getDefaultToolkit().getScreenResolution();
		} else {
			sr = wnd.getToolkit().getScreenResolution();
		}
		return sr;
	}
	
	public static Insets getScreenInsets(Window wnd) {
		Insets si;

		if (wnd == null) {
			si = Toolkit.getDefaultToolkit().getScreenInsets(new Frame().getGraphicsConfiguration());
		} else {
			si = wnd.getToolkit().getScreenInsets(wnd.getGraphicsConfiguration());
		}
		return si;
	}

	public static BufferedImage imageToBufferedImage(Image image) {
		BufferedImage bimage = new BufferedImage(image.getWidth(null),
				image.getHeight(null), BufferedImage.TYPE_INT_RGB);
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice gs = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gs.getDefaultConfiguration();
		bimage = gc.createCompatibleImage(image.getWidth(null),
				image.getHeight(null), Transparency.BITMASK);
		Graphics gb = bimage.createGraphics();
		gb.drawImage(image, 0, 0, null);
		gb.dispose();
		return bimage;
	}

	public static double getGreatCircleDistance(double dLatA, double dLonA, double dLatB, double dLonB) {
		final double EARTH_RADIUS = 6378.136; // kilometers 6378.136
		double dDLat = StrictMath.toRadians(dLatB - dLatA);
		double dDLon = StrictMath.toRadians(dLonB - dLonA);
		dLatA = StrictMath.toRadians(dLatA);
		dLatB = StrictMath.toRadians(dLatB);

		double da = StrictMath.sin(dDLat / 2.0) * StrictMath.sin(dDLat / 2.0)
				+ StrictMath.sin(dDLon / 2.0) * StrictMath.sin(dDLon / 2.0)
				* StrictMath.cos(dLatA) * StrictMath.cos(dLatB);

		double dc = 2.0 * StrictMath.atan2(StrictMath.sqrt(da),
				StrictMath.sqrt(1.0 - da));

		double dd = EARTH_RADIUS * dc;

		return dd;
	}

	public static String integerToHex(int newInt) {
		String s = Integer.toString(newInt, 16);
		if (s.length() % 2 != 0) {
			s = "0" + s;
		}
		s = s.toUpperCase();
		return s;
	}

	public static String integerToDecimalString(int newInt) {
		String s = Integer.toString(newInt);
		if (s.length() % 2 != 0) {
			s = "0" + s;
		}
		s = s.toUpperCase();
		return s;
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
					.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public static Color dBmToColor(double dBm) {
		Color cRet;
		if (dBm >= -40) {
            cRet = new Color(0, 0, 255);
        } else if (dBm >= -45) {
            cRet = new Color(0, 0, 245);
        } else if (dBm >= -50) {
            cRet = new Color(0, 0, 235);
        } else if (dBm >= -55) {
            cRet = new Color(0, 0, 225);
        } else if (dBm >= -60) {
            cRet = new Color(0, 0, 215);
        } else if (dBm >= -65) {
            cRet = new Color(0, 0, 205);
        } else if (dBm >= -70) {
            cRet = new Color(0, 0, 195);
        } else if (dBm >= -75) {
            cRet = new Color(0, 0, 185);
        } else if (dBm >= -80) {
            cRet = new Color(0, 0, 175);
        } else if (dBm >= -85) {
            cRet = new Color(0, 0, 165);
        } else if (dBm >= -90) {
            cRet = new Color(0, 0, 155);
        } else if (dBm >= -95) {
            cRet = new Color(0, 0, 145);
        } else if (dBm >= -100) {
            cRet = new Color(0, 0, 135);
        } else if (dBm >= -105) {
            cRet = new Color(0, 0, 125);
        } else if (dBm >= -110) {
            cRet = new Color(0, 0, 115);
        } else {
            cRet = new Color(0, 0, 105);
        }
		return cRet;
	}

	public static String getIconPathNameFromSSID(String ssid) {
		String sRet = "SSID-00.png";
		if (ssid.equals("0")) {
            sRet = "SSID-00.png";
        } else if (ssid.equals("1")) {
            sRet = "SSID-01.png";
        } else if (ssid.equals("2")) {
            sRet = "SSID-02.png";
        } else if (ssid.equals("3")) {
            sRet = "SSID-03.png";
        } else if (ssid.equals("4")) {
            sRet = "SSID-04.png";
        } else if (ssid.equals("5")) {
            sRet = "SSID-05.png";
        } else if (ssid.equals("6")) {
            sRet = "SSID-06.png";
        } else if (ssid.equals("7")) {
            sRet = "SSID-07.png";
        } else if (ssid.equals("8")) {
            sRet = "SSID-08.png";
        } else if (ssid.equals("9")) {
            sRet = "SSID-09.png";
        } else if (ssid.equals("10")) {
            sRet = "SSID-10.png";
        } else if (ssid.equals("11")) {
            sRet = "SSID-11.png";
        } else if (ssid.equals("12")) {
            sRet = "SSID-12.png";
        } else if (ssid.equals("13")) {
            sRet = "SSID-13.png";
        } else if (ssid.equals("14")) {
            sRet = "SSID-14.png";
        } else if (ssid.equals("15")) {
            sRet = "SSID-15.png";
        }
		return sRet;
	}

	public static String parseCallSign(String identifier) {
		String cs = "";
		try {
			int dash = identifier.indexOf("-");
			if (dash >= 0 && dash <= 15) {
                cs = identifier.substring(0, dash);
            } else {
                cs = identifier;
            }
		} catch (NullPointerException ex) {}
		return cs;
	}

	public static String parseSSID(String identifier) {
		String id = "0";
		try {
			int dash = identifier.indexOf("-");
			if (dash >= 0 && dash <= 15) id = identifier.substring(dash + 1);
		} catch (NullPointerException ex) {}
		return id;
	}

	public static Component getTopLevelAncestor(Component c) {
		while (c != null) {
			if (c instanceof Window || c instanceof Applet) {
                break;
            }
			c = c.getParent();
	    }
	    return c;
	}
	
    public static BufferedImage getDefaultIcon(Dimension size) {
		Graphics2D g = null;
		try {
			final BufferedImage bi = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
			g = bi.createGraphics();
			g.setColor(Color.GRAY);
			g.setStroke(new BasicStroke(2.0f));
			g.drawRect(0, 0, size.width, size.height);
			g.setColor(new Color(64, 0, 0));
			g.setStroke(new BasicStroke(1.0f));
			g.drawLine(0, 0, size.width, size.height);
			g.drawLine(size.width, 0, 0, size.height);
			return bi;
		} finally {
			g.dispose();
		}
	}

	public static int getPortNumberFromName(String name) {
		int number = 0;
		if (Configuration.isWindowsOS()) {
			number = Integer.parseInt(name.substring(3)) - 1;
		}
		if (Configuration.isLinuxOS()) {
			number = Integer.parseInt(name.substring(9));
		}
		return number;
	}

	public static boolean isComPortValid(int portNumber) {
		return isComPortValid(getPortNameString(portNumber));
	}
	
	public static boolean isComPortValid(String portName) {
		boolean isAvailable = false;
		if (portName == null || portName.isEmpty() || (!portName.toUpperCase().startsWith("COM") && 
				!portName.toUpperCase().contains("TTY"))) return isAvailable;
		String[] ports = SerialPortList.getPortNames();
        for (String port : ports) {
            if (port.equals(portName)) {
            	isAvailable = true;
            	break;
            }
        }
        return isAvailable;
	}
	
	public static String getPortNameString(int number) {
		String name = null;
		if (Configuration.isWindowsOS()) {
			name =  "COM" + String.valueOf(number + 1);
		}
		if (Configuration.isLinuxOS()) {
			name = "/dev/ttyS" + String.valueOf(number);
		}
		return name;
	}
	
}
