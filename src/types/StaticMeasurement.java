package types;

import java.awt.Point;
import java.io.Serializable;

public class StaticMeasurement implements Serializable {
	private static final long serialVersionUID = -6158353305914019511L;
	
	private int id = 0;
	private String testName;
	private Point.Double point = new Point.Double();
	private double dBm;
	private double courseMadeGoodTrue;
	private double speedMadeGoodKPH;
	private long timeStamp;
	private double altitude;
	private double frequencyMHz;
	private int unit;
	
	public StaticMeasurement() {}
	
	public StaticMeasurement(String testName, Point.Double point, long timeStamp, double dBm, double courseMadeGoodTrue, 
			double speedMadeGoodKPH, double altitude, double frequencyMHz, int unit) {
		this.testName = testName;
		this.point = point;
		this.dBm = dBm;
		this.timeStamp = timeStamp;
		this.courseMadeGoodTrue = courseMadeGoodTrue;
		this.speedMadeGoodKPH = speedMadeGoodKPH;
		this.altitude = altitude;
		this.frequencyMHz = frequencyMHz;
		this.unit = unit;
	}

	public static Object[] staticMeasurementToObjectArray(StaticMeasurement sm) {
		Object[] obj = new Object[11];
		obj[0] = sm.id;
		obj[1] = sm.testName;
	    obj[2] = sm.timeStamp;
	    obj[3] = sm.point.x;
	    obj[4] = sm.point.y;
	    obj[5] = sm.courseMadeGoodTrue;
    	obj[6] = sm.speedMadeGoodKPH;
    	obj[7] = sm.altitude;
    	obj[8] = sm.frequencyMHz;
    	obj[9] = sm.dBm;
    	obj[10] = sm.unit;
		return obj;
	}
	
	public static StaticMeasurement objectArrayToStaticMeasurement(Object[] obj) {
		StaticMeasurement sm = new StaticMeasurement();
		sm.id = (int) obj[0];
		sm.testName = (String) obj[1];
		sm.timeStamp = (long) obj[2];
		sm.point.x = (double) obj[3];
		sm.point.y = (double) obj[4];
		sm.courseMadeGoodTrue = (double) obj[5];
		sm.speedMadeGoodKPH = (double) obj[6];
		sm.altitude = (double) obj[7];
		sm.frequencyMHz = (double) obj[8];
		sm.dBm = (double) obj[9];
		sm.unit = (int) obj[10];
		return sm;
	}
	
	public int getID() {
		return id;
	}
	 
	public void setID(int id) {
		this.id = id;
	}
	
	public void setTestName(String testName) {
		this.testName = testName;
	}
	
	public String getTestName() {
		return testName;
	}
	
	public Point.Double getPoint() {
		return point;
	}

	public void setPoint(final Point.Double point) {
		this.point = point;
	}

	public double getdBm() {
		return dBm;
	}

	public void setdBm(final double dBm) {
		this.dBm = dBm;
	}

	public double getCourseMadeGoodTrue() {
		return courseMadeGoodTrue;
	}

	public void setCourseMadeGoodTrue(final double courseMadeGoodTrue) {
		this.courseMadeGoodTrue = courseMadeGoodTrue;
	}

	public double getSpeedMadeGoodKPH() {
		return speedMadeGoodKPH;
	}

	public void setSpeedMadeGoodKPH(final double speedMadeGoodKPH) {
		this.speedMadeGoodKPH = speedMadeGoodKPH;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(final long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(final double altitude) {
		this.altitude = altitude;
	}

	public double getFrequencyMHz() {
		return frequencyMHz;
	}

	public void setFrequencyMHz(final double frequencyMHz) {
		this.frequencyMHz = frequencyMHz;
	}

	public int getUnit() {
		return unit;
	}

	public void setUnit(final int unit) {
		this.unit = unit;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		StaticMeasurement clone = (StaticMeasurement) super.clone();
	    return clone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(altitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(courseMadeGoodTrue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(dBm);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(frequencyMHz);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + id;
		result = prime * result + ((point == null) ? 0 : point.hashCode());
		temp = Double.doubleToLongBits(speedMadeGoodKPH);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((testName == null) ? 0 : testName.hashCode());
		result = prime * result + (int) (timeStamp ^ (timeStamp >>> 32));
		result = prime * result + unit;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StaticMeasurement other = (StaticMeasurement) obj;
		if (Double.doubleToLongBits(altitude) != Double.doubleToLongBits(other.altitude))
			return false;
		if (Double.doubleToLongBits(courseMadeGoodTrue) != Double.doubleToLongBits(other.courseMadeGoodTrue))
			return false;
		if (Double.doubleToLongBits(dBm) != Double.doubleToLongBits(other.dBm))
			return false;
		if (Double.doubleToLongBits(frequencyMHz) != Double.doubleToLongBits(other.frequencyMHz))
			return false;
		if (id != other.id)
			return false;
		if (point == null) {
			if (other.point != null)
				return false;
		} else if (!point.equals(other.point))
			return false;
		if (Double.doubleToLongBits(speedMadeGoodKPH) != Double.doubleToLongBits(other.speedMadeGoodKPH))
			return false;
		if (testName == null) {
			if (other.testName != null)
				return false;
		} else if (!testName.equals(other.testName))
			return false;
		if (timeStamp != other.timeStamp)
			return false;
		if (unit != other.unit)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StaticMeasurement [id=" + id + ", testName=" + testName + ", point=" + point + ", dBm=" + dBm
				+ ", courseMadeGoodTrue=" + courseMadeGoodTrue + ", speedMadeGoodKPH=" + speedMadeGoodKPH
				+ ", timeStamp=" + timeStamp + ", altitude=" + altitude + ", frequencyMHz=" + frequencyMHz + ", unit="
				+ unit + "]";
	}

}
