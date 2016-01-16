package jdrivetrack;

import java.awt.Point;

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
	
	public static Object[] toObjectArray(StaticMeasurement sm) {
		Object[] obj = new Object[9];
	//	obj[0] = sm.
	    obj[1] = sm.timeStamp;
	    obj[2] = sm.point.x;
	    obj[3] = sm.point.y;
	    obj[4] = sm.courseMadeGoodTrue;
    	obj[5] = sm.speedMadeGoodKPH;
    	obj[6] = sm.altitude;
    	obj[7] = sm.frequencyMHz;
    	obj[8] = sm.unit;
		return obj;
	}
	
	public static StaticMeasurement objectArrayToStaticMeasurement(Object[] obj) {
		StaticMeasurement sm = new StaticMeasurement();
		sm.timeStamp = (long) obj[1];
		sm.point.x = (double) obj[2];
		sm.point.y = (double) obj[3];
		sm.courseMadeGoodTrue = (double) obj[4];
		sm.speedMadeGoodKPH = (double) obj[5];
		sm.altitude = (double) obj[6];
		sm.frequencyMHz = (double) obj[7];
		sm.unit = (int) obj[8];
		return sm;
	}

}
