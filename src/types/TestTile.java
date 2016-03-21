package types;

import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.coords.UTMCoord;

import jdrivetrack.CoordinateUtils;
import jdrivetrack.CoordinateUtils.Precision;

public class TestTile implements Serializable, Cloneable {
	private static final long serialVersionUID = -1178168605511366403L;
	
	private int id = 0;
	private String testName;
	private int zone;
	private long easting;
	private long northing;
	private String latBand;
	private Point.Double lonlat;
	private Point.Double tileSize;
	private UTMCoord utmCoord;
	private Precision precision;
	private double sinad = 0;
    private double ber = 0;
    private double dBm = 0;
    private int measurementCount = 0;
    private String message;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
	public TestTile() {}
	
	public TestTile(String message) {
		this.message = message;
	}
	
	public TestTile(String testName, Point.Double lonlat, Point.Double tileSize, Precision precision) {
		this(testName, UTMCoord.fromLatLon(Angle.fromDegreesLatitude(lonlat.y), Angle.fromDegreesLongitude(lonlat.x)), tileSize, precision);
	}
	
	public TestTile(String testName, Point.Double lonlat, Point.Double tileSize, int zone, String hemisphere, long easting, long northing, Precision precision) {
		this.testName = testName;
		this.zone = zone;
		this.easting = easting;
		this.northing = northing;
		this.latBand = hemisphere;
		this.lonlat = lonlat;
		this.precision = precision;
		this.tileSize = tileSize;
		this.utmCoord = new UTMCoord(Angle.fromDegreesLatitude(lonlat.y), Angle.fromDegreesLongitude(lonlat.x), zone, hemisphere, easting, northing);
	}

	public TestTile(String testName, UTMCoord utmCoord, Point.Double tileSize, Precision precision) {
		this.testName = testName;
		this.utmCoord = utmCoord;
		this.precision = precision;
		this.tileSize = tileSize;
		this.easting = (long) utmCoord.getEasting();
		this.northing = (long) utmCoord.getNorthing();
		this.latBand = utmCoord.getHemisphere() == AVKey.NORTH ? "N" : "S";
		this.lonlat = new Point.Double(utmCoord.getLongitude().getDegrees(), utmCoord.getLatitude().getDegrees());
		this.zone = utmCoord.getZone();
	}

	public int getID() {
		return this.id;
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
	
	public String getTileNameString() {
		return this.utmCoord.toString();
	}
	
	public void setTileSize(Point.Double tileSize) {
		this.tileSize = tileSize;
	}
	
	public Point.Double getTileSize() {
		return this.tileSize;
	}

	public int getGridZone() {
		return this.zone;
	}
	
	public Point.Double getLonLat() {
		return this.lonlat;
	}

	public void setLonLat(Point.Double lonlat) {
		this.lonlat = lonlat;
	}
	
	public String getLatBand() {
		return this.latBand;
	}
	
	public long getEasting() {
		return this.easting;
	}
	
	public long getNorthing() {
		return this.northing;
	}
	
	public Precision getPrecision() {
		return this.precision;
	}
	
	public void setEasting(long easting) {
		this.easting = easting;
	}
	
	public void setNorthing(long northing) {
		this.northing = northing;
	}
	
	public void setGridZone(int zone) {
		this.zone = zone;
	}
	
	public void setLatBand(String latBand) {
		this.latBand = latBand;
	}
	
	public void setPrecision(Precision precision) {
		this.precision = precision;
	}
	
	public void setPrecision(int ordinal) {
		this.precision = Precision.values()[ordinal];
	}

	public int getMeasurementCount() {
		return this.measurementCount;
	}
	
	public void incrementMeasurementCount() {
		this.measurementCount++;
	}
	
	public void decrementMeasurementCount() {
		this.measurementCount--;
	}
	
	public void setMeasurementCount(int measurementCount) {
		this.measurementCount = measurementCount;
	}
	
	public void setAvgSinad(double sinad) {
		this.sinad = sinad;
	}
	
	public void setAvgBer(double ber) {
		this.ber = ber;
	}
	
	public void setAvgdBm(double dBm) {
		this.dBm = dBm;
	}
	
	public void addSinad(double sinad) {
		this.sinad =+ sinad;
    }

    public double getAvgSinad() {
    	if (this.measurementCount > 0) return this.sinad / this.measurementCount;
        return 0d;
    }

    public void addBer(double ber) {
    	this.ber =+ ber;
    }

    public double getAvgBer() {
    	if (this.measurementCount > 0) return this.ber / this.measurementCount;
        return 0;
    }

    public void adddBm(double dBm) {
    	this.dBm =+ dBm;
    }

    public double getAvgdBm() {
    	if (this.measurementCount > 0) return this.dBm / this.measurementCount;
        return 0;
    }

	@Override
	public String toString() {
		if(message != null) return message;
		return new String(this.zone + " " + String.valueOf(this.easting) + "mE " + String.valueOf(this.northing) + "mN"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	public String toFormattedTestTileDesignator() {
		return CoordinateUtils.utmCoordToTestTileString(this.utmCoord, this.precision);
	}
	
	public Object[] toObjectArray() {
		Object[] obj = new Object[15];
		obj[0] = getID();
		obj[1] = getTestName();
		obj[2] = getEasting();
		obj[3] = getNorthing();
		obj[4] = getGridZone();
		obj[5] = getLonLat().x;
		obj[6] = getLonLat().y;
		obj[7] = getPrecision().ordinal();
		obj[8] = getLatBand();
		obj[9] = getAvgSinad();
		obj[10] = getAvgBer();
		obj[11] = getAvgdBm();
		obj[12] = getTileSize().x;
		obj[13] = getTileSize().y;
		obj[14] = getMeasurementCount();
		return obj;
	}

	public void fromObjectArray(Object[] obj) {
		setID((Integer) obj[0]);
		setTestName((String) obj[1]);
	    setEasting((Long) obj[2]);
		setNorthing((Long) obj[3]);
		setGridZone((Integer) obj[4]);
		setLonLat(new Point.Double((Double) obj[5], (Double) obj[6]));
		setPrecision((Integer) obj[7]);
		setLatBand((String) obj[8]);
		setAvgSinad((Double) obj[9]);
		setAvgBer((Double) obj[10]);
		setAvgdBm((Double) obj[11]);
		setTileSize(new Point.Double((Double) obj[12], (Double) obj[13]));
		setMeasurementCount((Integer) obj[14]);
	}
	
	public static TestTile toTestTile(Object[] obj) {
		TestTile testTile = new TestTile();
		testTile.setID((Integer) obj[0]);
		testTile.setTestName((String) obj[1]);
	    testTile.setEasting((Long) obj[2]);
		testTile.setNorthing((Long) obj[3]);
		testTile.setGridZone((Integer) obj[4]);
		testTile.setLonLat(new Point.Double((Double) obj[5], (Double) obj[6]));
		testTile.setPrecision((Integer) obj[7]);
		testTile.setLatBand((String) obj[8]);
		testTile.setAvgSinad((Double) obj[9]);
		testTile.setAvgBer((Double) obj[10]);
		testTile.setAvgdBm((Double) obj[11]);
		testTile.setTileSize(new Point.Double((Double) obj[12], (Double) obj[13]));
		testTile.setMeasurementCount((Integer) obj[14]);
		return testTile;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {		 
	    TestTile clone = (TestTile) super.clone();
	    return clone;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (easting ^ (easting >>> 32));
		result = prime * result + ((latBand == null) ? 0 : latBand.hashCode());
		result = prime * result + (int) (northing ^ (northing >>> 32));
		result = prime * result + ((precision == null) ? 0 : precision.hashCode());
		result = prime * result + ((testName == null) ? 0 : testName.hashCode());
		result = prime * result + zone;
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
		TestTile other = (TestTile) obj;
		if (easting != other.easting)
			return false;
		if (latBand == null) {
			if (other.latBand != null)
				return false;
		} else if (!latBand.equals(other.latBand))
			return false;
		if (northing != other.northing)
			return false;
		if (precision != other.precision)
			return false;
		if (testName == null) {
			if (other.testName != null)
				return false;
		} else if (!testName.equals(other.testName))
			return false;
		if (zone != other.zone)
			return false;
		return true;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if (!this.pcs.hasListeners(null)) {
			this.pcs.addPropertyChangeListener(listener);
		}
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

}
