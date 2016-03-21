package jdrivetrack;

import gov.nasa.worldwind.geom.LatLon;
import jsc.util.Logarithm;
import types.StaticMeasurement;

public class RFPath {
	
	public static double distanceInMetersFreeSpacePathLoss(double dBfspl, double frequencyMhz) {
    	Logarithm log = new Logarithm(10);
    	return log.antilog((dBfspl + 27.55 - (20 * log.log(frequencyMhz))) / 20);
    }

    public static double conicAngleToTarget(StaticMeasurement sma, StaticMeasurement smb) {
    	double aha = distanceInMetersFreeSpacePathLoss(sma.getdBm(), sma.getFrequencyMHz());
    	double ahb = distanceInMetersFreeSpacePathLoss(smb.getdBm(), smb.getFrequencyMHz());
    	
    	double axa = Math.sqrt((aha*aha) - (sma.getAltitude() * sma.getAltitude()));
    	double dtd = Vincenty.distanceToDirect(sma.getPoint(), sma.getAltitude(), smb.getPoint(), smb.getAltitude());
    	double cmg = LatLon.rhumbAzimuth(LatLon.fromDegrees(sma.getPoint().y, sma.getPoint().x), 
    			LatLon.fromDegrees(smb.getPoint().y, smb.getPoint().x)).getDegrees();
    	double dttr = Vincenty.degreesToMeters(dtd, cmg, sma.getPoint().y);
    	
    	double mxb = axa + dttr;
    	double mhb = Math.sqrt((mxb*mxb) + (smb.getAltitude() * smb.getAltitude()));
    	double adhbha = ahb - aha;
    	double mdhbha = mhb - aha;

    	double f = adhbha / mdhbha;
   	
    	return Math.toDegrees(Math.acos(f));
    }
    
    public static double getFreeSpacePathLoss(double meters, double frequencyMhz) {
    	Logarithm log = new Logarithm(10);
    	return 20 * log.log(meters) + 20 * log.log(frequencyMhz) - 27.55;
    }
	
}
