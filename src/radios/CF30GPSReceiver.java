package radios;

import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Calendar;

import interfaces.GPSInterface;
import jdrivetrack.CoordinateUtils;
import jssc.SerialPort;

public class CF30GPSReceiver implements GPSInterface {
	private static final String versionUID = "7459016608933909050";
	
	private FixQuality fixQuality = FixQuality.INVALID;
	private FAAMode faaMode;
	private boolean validFix;
	private boolean validTrueRdfHeading = false;
	private Point.Double position = null;
	private double courseMadeGoodTrue;
	private double courseMadeGoodMagnetic;
	private double speedMadeGoodMPH;
	private double speedMadeGoodKnots;
	private double speedMadeGoodKPH;
	private double altitude;
	private double altitudeOverEllipsoid;
	private byte satellitesInView;
	private double magneticVariation;
	private double horizontalPositionErrorMeters;
	private double verticalPositionErrorMeters;
	private double sphericalEquivalentPositionErrorMeters;
	private double barometricAltitudeFeet;
	private double compassHeading;
	private double horizontalDilutionOfPrecision;
	private double rdfHeadingRelative;
	private double rdfHeadingTrue;
	private RdfQuality rdfQuality;
	private String messageString;
	private String gprmcMessageString;
	private String gpggaMessageString;
	private String gpwplMessageString;
	private String rStr = "";
	private Point.Double aprsPosition = null;
	private String aprsIdent;
	private Calendar date = Calendar.getInstance();
	private Point.Double prePosition = null;
	private boolean enableEvents = false;
	private boolean reportCRCErrors = false;
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public CF30GPSReceiver() {}
	
	@Override
	public void startGPS() {
		enableEvents = true;;
		rStr = "";
		if (enableEvents) {
			pcs.firePropertyChange(FIX_QUALITY, null, FixQuality.ACQUIRING);
		} else {
			pcs.firePropertyChange(FIX_QUALITY, null, FixQuality.OFF_LINE);
		}
	}
	
	@Override
	public boolean isValidFix() {
		return validFix;
	}

	@Override
	public boolean isValidTrueRdfHeading() {
		return validTrueRdfHeading;
	}
	
	@Override
	public void decode(String decode) {
		nmeaDecoder(decode);
	}

	@Override
	public String getMessageString() {
		return messageString;
	}

	@Override
	public String getGPRMCMessageString() {
		return gprmcMessageString;
	}

	@Override
	public String getGPGGAMessageString() {
		return gpggaMessageString;
	}

	@Override
	public String getGPWPLMessageString() {
		return gpwplMessageString;
	}

	@Override
	public FixQuality getFixQuality() {
		return fixQuality;
	}

	@Override
	public FAAMode getFaaMode() {
		return faaMode;
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

	@Override
	public long getTimeInMillis() {
		return date.getTimeInMillis();
	}
	
	@Override
	public Point.Double getAprsPosition() {
		return aprsPosition;
	}

	@Override
	public String getAprsIdentifier() {
		return aprsIdent;
	}

	@Override
	public Point.Double getPosition() {
		return position;
	}

	@Override
	public double getCourseMadeGoodTrue() {
		return courseMadeGoodTrue;
	}

	@Override
	public double getCourseMadeGoodMagnetic() {
		return courseMadeGoodMagnetic;
	}

	@Override
	public double getSpeedMadeGoodMPH() {
		return speedMadeGoodMPH;
	}

	@Override
	public double getSpeedMadeGoodKnots() {
		return speedMadeGoodKnots;
	}

	@Override
	public double getSpeedMadeGoodKPH() {
		return speedMadeGoodKPH;
	}

	@Override
	public double getAltitude() {
		return altitude;
	}

	@Override
	public double getAltitudeOverEllipsoid() {
		return altitudeOverEllipsoid;
	}

	@Override
	public byte getSatellitesInView() {
		return satellitesInView;
	}

	@Override
	public double getMagneticVariation() {
		return magneticVariation;
	}

	@Override
	public Calendar getDate() {
		return date;	
	}

	@Override
	public double getHorizontalPositionErrorMeters() {
		return horizontalPositionErrorMeters;
	}

	@Override
	public double getVerticalPositionErrorMeters() {
		return verticalPositionErrorMeters;
	}

	@Override
	public double getSphericalEquivalentPositionErrorMeters() {
		return sphericalEquivalentPositionErrorMeters;
	}

	@Override
	public double getBarometricAltitudeFeet() {
		return barometricAltitudeFeet;
	}

	@Override
	public double getCompassHeading() {
		return compassHeading;
	}

	@Override
	public double getHorizontalDilutionOfPrecision() {
		return horizontalDilutionOfPrecision;
	}

	@Override
	public double getRdfHeadingRelative() {
		return rdfHeadingRelative;
	}

	@Override
	public RdfQuality getRdfQuality() {
		return rdfQuality;
	}

	@Override
	public double getRdfHeadingTrue() {
		return rdfHeadingTrue;
	}
	
	private void nmeaDecoder(String message) {
		try {
			double lat;
			double lon;
			String[] a;
			
			String msg = message;

			if (enableEvents) pcs.firePropertyChange(GPSInterface.NMEA_DATA, null, msg);
			
			messageString = msg;
			
			validTrueRdfHeading = false;

			if (msg.substring(0, 1).equals("%")) {
				if (msg.trim().length() == 6) {
					rdfHeadingRelative = Integer.parseInt(msg.substring(1, 4));
					rdfQuality = getRdfQuality(Integer.parseInt(msg.substring(5, 6)));
					if (speedMadeGoodMPH >= 1) {
						rdfHeadingTrue = courseMadeGoodTrue + rdfHeadingRelative;
						if (enableEvents) pcs.firePropertyChange(RDF_HEADING_TRUE, null, rdfHeadingTrue);
						validTrueRdfHeading = true;
					} else {
						if (enableEvents) pcs.firePropertyChange(RDF_HEADING_RELATIVE, null, rdfHeadingRelative);
						validTrueRdfHeading = false;
					}
				}
			}

			else if (msg.substring(0, 1).equals("$")) {
				if (!checksum(msg)) {
					if (enableEvents) pcs.firePropertyChange(FIX_QUALITY, null, FixQuality.INVALID);
					fixQuality = FixQuality.INVALID;
				} else {
					String completeMsg = msg;
					msg = msg.substring(0, msg.indexOf("*"));
					
					a = msg.split(",");

					if (a[0].equals("$GPRMC")) {
						gprmcMessageString = completeMsg;
						
						if (a[1].length() != 0 && a[9].length() != 0) {
							date.set(Calendar.HOUR_OF_DAY, Integer.parseInt(a[1].substring(0, 2)));
							date.set(Calendar.MINUTE, Integer.parseInt(a[1].substring(2, 4)));
							date.set(Calendar.SECOND, Integer.parseInt(a[1].substring(4, 6)));
							date.set(Calendar.DATE, Integer.parseInt(a[9].substring(0, 2)));
							date.set(Calendar.MONTH, Integer.parseInt(a[9].substring(2, 4)) - 1);
							date.set(Calendar.YEAR, Integer.parseInt("20" + a[9].substring(4, 6)));
							date.set(Calendar.MILLISECOND, Integer.parseInt(a[1].substring(7, 10)));
							if (enableEvents) pcs.firePropertyChange(VALID_TIME, null, date.getTimeInMillis());
						}
						
						if (a[3].length() != 0 && a[5].length() != 0) {
							lat = (Double.parseDouble(a[3].substring(0, 2)) * 1000000)
									+ (Double.parseDouble(a[3].substring(2)) * 16666.6666667);
							if (a[4].equals("S"))
								lat = -lat;
							lon = (Double.parseDouble(a[5].substring(0, 3)) * 1000000)
									+ (Double.parseDouble(a[5].substring(3)) * 16666.6666667);
							if (a[6].equals("W"))
								lon = -lon;
							position = new Point.Double(lon / 1000000, lat / 1000000);
						}

						if (a[2].equals("A")) {
							validFix = true;
							if (enableEvents) pcs.firePropertyChange(VALID_FIX, null, validFix);
						} else if (a[2].equals("V")) {
							validFix = false;
							if (enableEvents) pcs.firePropertyChange(VALID_FIX, null, validFix);
						}

						if (a[7].length() != 0) speedMadeGoodMPH = Double.parseDouble(a[7]) * 1.15077945;
						
						if (a[7].length() != 0) speedMadeGoodKPH = Double.parseDouble(a[7]) * 1.852;
						
						if (a[7].length() != 0) speedMadeGoodKnots = Double.parseDouble(a[7]);

						if (a[10].length() != 0) {
							magneticVariation = Double.parseDouble(a[10]);
							if (a[11].equals("E")) magneticVariation = -magneticVariation;
						}

						if (a[8].length() != 0) {
							if (speedMadeGoodMPH > 5.0) {
								courseMadeGoodTrue = Double.parseDouble(a[8]);
								courseMadeGoodMagnetic = courseMadeGoodTrue + magneticVariation;
								if (enableEvents) pcs.firePropertyChange(COURSE_MADE_GOOD_TRUE, null, courseMadeGoodTrue);
								if (enableEvents) pcs.firePropertyChange(COURSE_MADE_GOOD_MAGNETIC, null, courseMadeGoodMagnetic);
							}
						}
						if (a.length > 12) {
							if (a[12].length() != 0) {
								faaMode = getFAAMode(a[12]);
							} else {
								faaMode = FAAMode.NOT_PROVIDED;
							}
							if (enableEvents) pcs.firePropertyChange(FAA_MODE, null, faaMode);
						}

					} else if (a[0].equals("$GPGGA")) {
						gpggaMessageString = completeMsg;
						if (a[2].length() != 0 && a[4].length() != 0) {
							lat = (Double.parseDouble(a[2].substring(0, 2)) * 1000000.0)
									+ (Double.parseDouble(a[2].substring(2)) * 16666.6666667);
							if (a[3].equals("S"))
								lat = -lat;
							lon = (Double.parseDouble(a[4].substring(0, 3)) * 1000000.0)
									+ (Double.parseDouble(a[4].substring(3)) * 16666.6666667);
							if (a[5].equals("W"))
								lon = -lon;
							position = new Point.Double(lon / 1000000.0, lat / 1000000.0);
						}
						
						if (a[6].length() != 0) {
							fixQuality = getFixQuality(a[6]);
							if (!fixQuality.equals(FixQuality.ERROR)) if (enableEvents) pcs.firePropertyChange(FIX_QUALITY, null, fixQuality);
						}

						if (a[9].length() != 0) altitude = Double.parseDouble(a[9]);
						
						if (a[11].length() != 0) altitudeOverEllipsoid = Double.parseDouble(a[11]);
						
						if (a[8].length() != 0) horizontalDilutionOfPrecision = Double.parseDouble(a[8]);
						
						if (a[7].length() != 0) satellitesInView = Byte.parseByte(a[7]);

					} else if (a[0].equals("$GPWPL")) {
						gpwplMessageString = completeMsg;
						if (a[2].length() != 0 && a[4].length() != 0) {
							lat = (Double.parseDouble(a[2].substring(0, 2)) * 1000000.0)
									+ (Double.parseDouble(a[2].substring(2)) * 16666.6666667);
							if (a[3].equals("S")) lat = -lat;
							lon = (Double.parseDouble(a[4].substring(0, 3)) * 1000000.0)
									+ (Double.parseDouble(a[4].substring(3)) * 16666.6666667);
							if (a[5].equals("W")) lon = -lon;
							aprsPosition.y = lat / 1000000.0;
							aprsPosition.x = lon / 1000000.0;
							if (a[6].length() != 0) aprsIdent = (a[6]);
							else aprsIdent = "";
							if (enableEvents) pcs.firePropertyChange(VALID_WAYPOINT, null, aprsPosition);
						}
					}
					
					if (position != null && prePosition != null && Math.abs(prePosition.distance(position)) > 0.000003) {
						if (enableEvents) pcs.firePropertyChange(VALID_POSITION, prePosition, position);
					}
					
					if (position != null && prePosition == null) {
						if (enableEvents) pcs.firePropertyChange(VALID_POSITION, null, position);
					}
					
					if (position != null) prePosition = (Point.Double) position.clone();
				}
			}
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
	}

	private boolean checksum(String input) {
		String chkDat;
		String[] dat;
		String chkSum;
		
		if (input.indexOf("*", 2) < 2) {
			if (enableEvents) pcs.firePropertyChange(CRC_ERROR, null, input);
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
					if (enableEvents && reportCRCErrors) pcs.firePropertyChange(CRC_ERROR, null, input);
					return false;
			}

			catch (NumberFormatException ex) {
				if (enableEvents && reportCRCErrors) pcs.firePropertyChange(CRC_ERROR, null, input);
				return false;
			}
		}
	}

	@Override
	public String getUTMCoordinates() {
		return CoordinateUtils.latLonToUTM(position).toString();
	}
	
	@Override
	public String getMGRSLocation() {
		return CoordinateUtils.latLonToMGRS(position, 5).toString();
	}
	
    private FixQuality getFixQuality(String fq) {
        switch (fq) {
	        case "1": return FixQuality.FIX_3D;
	        case "2": return FixQuality.DGPS_FIX;
	        case "3": return FixQuality.PPS_FIX;
	        case "4": return FixQuality.RTK;
	        case "5": return FixQuality.FLOAT_RTK;
	        case "6": return FixQuality.ESTIMATED;
	        case "7": return FixQuality.MANUAL;
	        case "8": return FixQuality.SIMULATION;
	        case "9": return FixQuality.ACQUIRING;
	        default: return  FixQuality.ERROR;
        }
	}
    
    private FAAMode getFAAMode(String m) {
		FAAMode faaMode;
	    switch (m) {
		    case "A":
		    	faaMode = FAAMode.AUTONOMOUS;
		    	break;
		    case "D":
	        	faaMode = FAAMode.DIFFERENTIAL;
	        	break;
		    case "E":
		    	faaMode = FAAMode.ESTIMATED;
		    	break;
		    case "N":
		    	faaMode = FAAMode.NOT_VALID;
		    	break;
		    case "S":
		    	faaMode = FAAMode.SIMULATOR;
		    	break;
		    default:
		    	faaMode = FAAMode.NOT_VALID;
	    }
        return faaMode;
	}
    
    private RdfQuality getRdfQuality(int q) {
    	RdfQuality rdfQual;
    	switch (q) {
    		case 0:
    			rdfQual = RdfQuality.RDF_QUAL_0;
	    		break;
	    	case 1:
	    		rdfQual = RdfQuality.RDF_QUAL_1;
	    		break;
	    	case 2:
	    		rdfQual = RdfQuality.RDF_QUAL_2;
	    		break;
	    	case 3:
	    		rdfQual = RdfQuality.RDF_QUAL_3;
	    		break;
	    	case 4:
	    		rdfQual = RdfQuality.RDF_QUAL_4;
	    		break;
	    	case 5:
	    		rdfQual = RdfQuality.RDF_QUAL_5;
	    		break;
	    	case 6:
	    		rdfQual = RdfQuality.RDF_QUAL_6;
	    		break;
	    	case 7:
	    		rdfQual = RdfQuality.RDF_QUAL_7;
	    		break;
	    	case 8:
	    		rdfQual = RdfQuality.RDF_QUAL_8;
	    		break;
	    	default:
	    		rdfQual = RdfQuality.RDF_QUAL_8;
	    		break;
    	}
        return rdfQual;
	}
    
    @Override
    public void shutDown() {
    	enableEvents = false;
    	fixQuality = FixQuality.OFF_LINE;
    	faaMode = FAAMode.NOT_VALID;
    	validFix = false;
    	validTrueRdfHeading = false;
    	rdfHeadingTrue = -1;
    	rdfHeadingRelative = -1;
    	speedMadeGoodMPH = 0;
    	speedMadeGoodKnots = 0;
    	speedMadeGoodKPH = 0;
    	altitude = 0;
    	altitudeOverEllipsoid = 0;
    	satellitesInView = 0;
    	horizontalPositionErrorMeters = -1;
    	verticalPositionErrorMeters = -1;
    	sphericalEquivalentPositionErrorMeters = -1;
    	barometricAltitudeFeet = -1;
    	horizontalDilutionOfPrecision = -1;
    	rdfQuality = RdfQuality.RDF_QUAL_0;
    	messageString = "";
    	gprmcMessageString = "";
    	gpggaMessageString = "";
    	gpwplMessageString = "";
    	rStr = "";
    	aprsIdent = "";
    	pcs.firePropertyChange(FIX_QUALITY, null, fixQuality);
    	pcs.firePropertyChange(VALID_FIX, null, validFix);
    	pcs.firePropertyChange(FAA_MODE, null, faaMode);
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
	public String versionUID() {
		return versionUID;
	}

	@Override
	public int getDefaultFlowControlOut() {
		return SerialPort.FLOWCONTROL_NONE;
	}

	@Override
	public int getDefaultFlowControlIn() {
		return SerialPort.FLOWCONTROL_NONE;
	}

	@Override
	public int getDefaultDataBits() {
		return SerialPort.DATABITS_8;
	}

	@Override
	public int getDefaultStopBits() {
		return SerialPort.STOPBITS_1;
	}

	@Override
	public int getDefaultParity() {
		return SerialPort.PARITY_NONE;
	}

	@Override
	public int getDefaultBaudRate() {
		return SerialPort.BAUDRATE_4800;
	}

	@Override
	public boolean getDefaultRTS() {
		return true;
	}

	@Override
	public boolean getDefaultDTR() {
		return true;
	}
	
	@Override
	public boolean isCTSSupported() {
		return false;
	}

	@Override
	public void reportCRCErrors(boolean reportCRCErrors) {
		this.reportCRCErrors = reportCRCErrors;
	}

	@Override
	public String[] getAvailableBaudRates() {
		String[] availableBaudRates = {"4800"};
		return availableBaudRates;
	}
	
	@Override
	public boolean serialParametersFixed() {
		return true;
	}
}