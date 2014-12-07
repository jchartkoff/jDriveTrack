package com;

import java.awt.Point;

import com.healthmarketscience.jackcess.Column;

public class StaticMeasurement {
	public Point.Double point = new Point.Double();
	public double dBm;
	public double courseMadeGoodTrue;
	public double speedMadeGoodKPH;
	public long timeStamp;
	public double altitude;
	public double frequencyMHz;
	public int unit;
	
	public StaticMeasurement() {}
	
	public StaticMeasurement(Point.Double point, long timeStamp, double dBm, double courseMadeGoodTrue, 
			double speedMadeGoodKPH, double altitude, double frequencyMHz) {
		this(point, timeStamp, dBm, courseMadeGoodTrue, speedMadeGoodKPH, altitude, frequencyMHz, 1);
	}

	public StaticMeasurement(Point.Double point, long timeStamp, double dBm, double courseMadeGoodTrue, 
			double speedMadeGoodKPH, double altitude, double frequencyMHz, int unit) {
		this.point = point;
		this.dBm = dBm;
		this.timeStamp = timeStamp;
		this.courseMadeGoodTrue = courseMadeGoodTrue;
		this.speedMadeGoodKPH = speedMadeGoodKPH;
		this.altitude = altitude;
		this.frequencyMHz = frequencyMHz;
		this.unit = unit;
	}
	
	public static String[] toStringArray(StaticMeasurement sm) {
		String[] str = new String[9];
		str[0] = Column.AUTO_NUMBER.toString();
	    str[1] = Long.toString(sm.timeStamp);
	    str[2] = Double.toString(sm.point.x);
	    str[3] = Double.toString(sm.point.y);
	    str[4] = Double.toString(sm.courseMadeGoodTrue);
    	str[5] = Double.toString(sm.speedMadeGoodKPH);
    	str[6] = Double.toString(sm.altitude);
    	str[7] = Double.toString(sm.frequencyMHz);
    	str[8] = Integer.toString(sm.unit);
		return str;
	}
	
	public static StaticMeasurement stringArrayToStaticMeasurement(String[] str) {
		StaticMeasurement sm = new StaticMeasurement();
		sm.timeStamp = Long.parseLong(str[1]);
		sm.point.x = Double.parseDouble(str[2]);
		sm.point.y = Double.parseDouble(str[3]);
		sm.courseMadeGoodTrue = Double.parseDouble(str[4]);
		sm.speedMadeGoodKPH = Double.parseDouble(str[5]);
		sm.altitude = Double.parseDouble(str[6]);
		sm.frequencyMHz = Double.parseDouble(str[7]);
		sm.unit = Integer.parseInt(str[8]);
		return sm;
	}

}
