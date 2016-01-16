package interfaces;

import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.util.Calendar;

public interface GPSInterface {

	public static final String RDF_HEADING_TRUE = "RDF_HEADING_TRUE";
	public static final String RDF_HEADING_RELATIVE = "RDF_HEADING_RELATIVE";
	public static final String FIX_QUALITY = "FIX_QUALITY";
	public static final String VALID_FIX = "VALID_FIX";
	public static final String VALID_TIME = "VALID_TIME";
	public static final String VALID_POSITION = "VALID_POSITION";
	public static final String VALID_WAYPOINT = "VALID_WAYPOINT";
	public static final String COURSE_MADE_GOOD_TRUE = "COURSE_MADE_GOOD_TRUE";
	public static final String COURSE_MADE_GOOD_MAGNETIC = "COURSE_MADE_GOOD_MAGNETIC";
	public static final String FAA_MODE = "FAA_MODE";
	public static final String CRC_ERROR = "CRC_ERROR";
	public static final String NMEA_DATA = "NMEA_DATA";
	
	public enum FAAMode {
		AUTONOMOUS, DIFFERENTIAL, ESTIMATED, NOT_VALID, SIMULATOR, NOT_PROVIDED
	}

	public enum FixQuality {
		INVALID, FIX_3D, DGPS_FIX, PPS_FIX, RTK, FLOAT_RTK, ESTIMATED, MANUAL, SIMULATION, FIX_2D, OFF_LINE, ACQUIRING, ERROR
	}

	public enum RdfQuality {
		RDF_QUAL_0, RDF_QUAL_1, RDF_QUAL_2, RDF_QUAL_3, RDF_QUAL_4, RDF_QUAL_5, RDF_QUAL_6, RDF_QUAL_7, RDF_QUAL_8
	}

	public static final String[] GPS_DEVICES = {
		"Panasonic CF-30 Internal GPS"
	};
	
	void shutDown();
	
	void decode(String decode);

	double getAltitude();

	double getAltitudeOverEllipsoid();

	String getAprsIdentifier();

	Point.Double getAprsPosition();

	double getBarometricAltitudeFeet();
	
	double getCompassHeading();
	
	double getCourseMadeGoodMagnetic();

	double getCourseMadeGoodTrue();

	Calendar getDate();

	FAAMode getFaaMode();

	FixQuality getFixQuality();

	String getGPGGAMessageString();

	String getGPRMCMessageString();

	String getGPWPLMessageString();

	double getHorizontalDilutionOfPrecision();

	double getHorizontalPositionErrorMeters();

	Point.Double getPosition();

	double getMagneticVariation();

	String getMessageString();

	double getRdfHeadingRelative();

	double getRdfHeadingTrue();

	RdfQuality getRdfQuality();

	byte getSatellitesInView();
	
	double getSpeedMadeGoodKnots();

	double getSpeedMadeGoodKPH();

	double getSpeedMadeGoodMPH();

	double getSphericalEquivalentPositionErrorMeters();

	String getUTMCoordinates();

	double getVerticalPositionErrorMeters();

	boolean isValidFix();

	boolean isValidTrueRdfHeading();

	void addPropertyChangeListener(PropertyChangeListener listener);
	
	void removePropertyChangeListener(PropertyChangeListener listener);

	void inputData(String data);

	long getTimeInMillis();

	String getMGRSLocation();

	String versionUID();
	
	int getDefaultFlowControlOut();

	int getDefaultFlowControlIn();

	int getDefaultDataBits();

	int getDefaultStopBits();

	int getDefaultParity();

	int getDefaultBaudRate();

	boolean getDefaultRTS();

	boolean getDefaultDTR();

	boolean isCTSSupported();

	void startGPS();
	
	void reportCRCErrors(boolean reportCRCErrors);
	
	String[] getAvailableBaudRates();

	boolean serialParametersFixed();
}