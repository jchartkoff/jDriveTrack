package jdrivetrack;

import gov.nasa.worldwind.geom.LatLon;
import jsc.util.Logarithm;

public class RFPath {
	
	public static double distanceInMetersFreeSpacePathLoss(double dBfspl, double frequencyMhz) {
    	Logarithm log = new Logarithm(10);
    	return log.antilog((dBfspl + 27.55 - (20 * log.log(frequencyMhz))) / 20);
    }

    public static double conicAngleToTarget(StaticMeasurement sma, StaticMeasurement smb) {
    	double aha = distanceInMetersFreeSpacePathLoss(sma.dBm, sma.frequencyMHz);
    	double ahb = distanceInMetersFreeSpacePathLoss(smb.dBm, smb.frequencyMHz);
    	
    	double axa = Math.sqrt((aha*aha) - (sma.altitude*sma.altitude));
    	double dtd = Vincenty.distanceToDirect(sma.point, sma.altitude, smb.point, smb.altitude);
    	double cmg = LatLon.rhumbAzimuth(LatLon.fromDegrees(sma.point.y, sma.point.x), 
    			LatLon.fromDegrees(smb.point.y, smb.point.x)).getDegrees();
    	double dttr = Vincenty.degreesToMeters(dtd, cmg, sma.point.y);
    	
    	double mxb = axa + dttr;
    	double mhb = Math.sqrt((mxb*mxb) + (smb.altitude*smb.altitude));
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
