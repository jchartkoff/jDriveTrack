package com;

import java.awt.Point;
import java.text.DecimalFormat;

public class Vincenty {
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */
	/* Vincenty Direct and Inverse Solution of Geodesics on the Ellipsoid (c) Chris Veness 2002-2014  */
	/*                                                                                                */
	/* from: T Vincenty, "Direct and Inverse Solutions of Geodesics on the Ellipsoid with application */
	/*       of nested equations", Survey Review, vol XXIII no 176, 1975                              */
	/*       http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf                                             */
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */


	/**
	 * Direct and indirect solutions of geodesics on the ellipsoid using Vincenty formulae.
	 *
	 */

	private static final double FEET_PER_METER = 3.2808398950131234;

	private static void validateLonLat(Point.Double point) throws IllegalArgumentException {
		if (point.y < -90.0 || point.y > 90.0 || point.x < -180.0 || point.x > 180.0)
			throw new IllegalArgumentException("Longitude = " + point.x + " / Latitude = " + point.y);
	}
	
	private static void validateBearing(double bearing) throws IllegalArgumentException {
		if (bearing >= 360.0 || bearing <= -360.0) 
			throw new IllegalArgumentException("Bearing = " + bearing + " degrees");
	}
	
	public static double degreesToArcSeconds(double degrees) throws IllegalArgumentException {
		if (degrees < -180.0 || degrees > 180.0) throw new IllegalArgumentException("Drgrees = " + degrees);
		return degrees * 3600.0;
	}	
	
	public static double arcSecondsToDegrees(double arcSeconds) throws IllegalArgumentException {
		if (arcSeconds < (-180.0*3600.0) || arcSeconds > (180.0*3600.0)) throw new IllegalArgumentException("ArcSeconds = " + arcSeconds);
		return arcSeconds / 3600.0;
	}	
	
	public static Point.Double feetToDegrees(double feet, double latitude) throws IllegalArgumentException {
		Point.Double p = feetToArcSeconds(feet, latitude);
		return new Point.Double(p.x / 3600.0, p.y / 3600.0);	
	}
	
	public static Point.Double metersToDegrees(double meters, double latitude) throws IllegalArgumentException {
		Point.Double p = metersToArcSeconds(meters, latitude);
		return new Point.Double(p.x / 3600.0, p.y / 3600.0);	
	}
	
	public static Point.Double feetToArcSeconds(double feet, double latitude) throws IllegalArgumentException {
		if (latitude < -90.0 || latitude > 90.0) throw new IllegalArgumentException("Latitude = " + latitude + " degrees");
		Point.Double kpd = kilometersPerDegree(latitude);
		double feetPerDegreeLongitude = kpd.x * FEET_PER_METER * 1000.0;
		double feetPerDegreeLatitude = kpd.y * FEET_PER_METER * 1000.0;
		double degreesLongitudePerFoot = 1.0 / feetPerDegreeLongitude;
		double degreesLatitudePerFoot = 1.0 / feetPerDegreeLatitude;
		return new Point.Double(degreesLongitudePerFoot * 3600.0 * feet, degreesLatitudePerFoot * 3600.0 * feet);
	}
	
	public static Point.Double metersToArcSeconds(double meters, double latitude) throws IllegalArgumentException {
		if (latitude < -90.0 || latitude > 90.0) throw new IllegalArgumentException("Latitude = " + latitude + " degrees");
		Point.Double kpd = kilometersPerDegree(latitude);
		double metersPerDegreeLongitude = kpd.x * 1000.0;
		double metersPerDegreeLatitude = kpd.y * 1000.0;
		double degreesLongitudePerMeter = 1.0 / metersPerDegreeLongitude;
		double degreesLatitudePerMeter = 1.0 / metersPerDegreeLatitude;
		return new Point.Double(degreesLongitudePerMeter * 3600.0 * meters, degreesLatitudePerMeter * 3600.0 * meters);
	}
	
	public static Point.Double degreesToFeet(double degrees, double latitude) throws IllegalArgumentException {
		return arcSecondsToFeet(degrees * 3600.0, latitude);
	}
	
	public static Point.Double degreesToMeters(double degrees, double latitude) throws IllegalArgumentException {
		return arcSecondsToMeters(degrees * 3600.0, latitude);
	}

	public static Point.Double arcSecondsToFeet(double arcSeconds, double latitude) throws IllegalArgumentException {
		if (latitude < -90.0 || latitude > 90.0) throw new IllegalArgumentException("Latitude = " + latitude + " degrees");
		Point.Double kpd = kilometersPerDegree(latitude);
		double feetLongitudePerDegree = kpd.x * FEET_PER_METER;
		double feetLongitudePerArcSecond = feetLongitudePerDegree / 3600.0;
		double feetLatitudePerDegree = kpd.y * FEET_PER_METER;
		double feetLatitudePerArcSecond = feetLatitudePerDegree / 3600.0;
		return new Point.Double(feetLongitudePerArcSecond * arcSeconds, feetLatitudePerArcSecond * arcSeconds);
	}
	
	public static Point.Double arcSecondsToMeters(double arcSeconds, double latitude) throws IllegalArgumentException {
		if (latitude < -90.0 || latitude > 90.0) throw new IllegalArgumentException("Latitude = " + latitude + " degrees");
		Point.Double kpd = kilometersPerDegree(latitude);
		double metersLongitudePerDegree = kpd.x;
		double metersLongitudePerArcSecond = metersLongitudePerDegree / 3600.0;
		double metersLatitudePerDegree = kpd.y;
		double metersLatitudePerArcSecond = metersLatitudePerDegree / 3600.0;
		return new Point.Double(metersLongitudePerArcSecond * arcSeconds, metersLatitudePerArcSecond * arcSeconds);
	}
	
	public static Point.Double kilometersPerDegree(double latitude) throws IllegalArgumentException {
		if (latitude < -90.0 || latitude > 90.0) throw new IllegalArgumentException("Latitude = " + latitude + " degrees");
		if (latitude < -70) latitude = -70;
		if (latitude > 70) latitude = 70;
		double lat = latitude;
		Point.Double kpd = new Point.Double();
		kpd.x = getVincentyInverse(new Point.Double(-0.5,lat), new Point.Double(+0.5,lat)).distance / 1000.0;
		kpd.y = getVincentyInverse(new Point.Double(0.0,lat-0.5), new Point.Double(0.0,lat+0.5)).distance / 1000.0;
		return kpd;
	}
	
	public static double metersToFeet(double meters) {
		return meters * FEET_PER_METER;
	}
	
	public static double feetToMeters(double feet) {
		return feet / FEET_PER_METER;
	}
	
	public static Point.Double degreesPerKilometer(double latitude) throws IllegalArgumentException {
		return new Point.Double(1 / kilometersPerDegree(latitude).x, 1 / kilometersPerDegree(latitude).y);
	}
	
	public static double distanceTo(Point.Double p1, Point.Double p2) throws IllegalArgumentException {
		validateLonLat(p1);
		validateLonLat(p2);
		try {
	        return getVincentyInverse(p1, p2).distance;
	    } catch (Exception ae) {
	        return Double.NaN; // failed to converge
	    }
	}

	public static double initialBearingTo(Point.Double p1, Point.Double p2) throws IllegalArgumentException {
		validateLonLat(p1);
		validateLonLat(p2);
		try {
	        return getVincentyInverse(p1, p2).initialBearing;
	    } catch (Exception ae) {
	        return Double.NaN; // failed to converge
	    }
	}

	public static double finalBearingTo(Point.Double p1, Point.Double p2) throws IllegalArgumentException {
		validateLonLat(p1);
		validateLonLat(p2);
		try {
	        return getVincentyInverse(p1, p2).finalBearing;
	    } catch (Exception ae) {
	        return Double.NaN; // failed to converge
	    }
	}

	public static Point.Double destinationPoint(Point.Double p1, double initialBearing, double distance) throws IllegalArgumentException {
		validateLonLat(p1);
		validateBearing(initialBearing);
		DirectPoint directPoint = getVincentyDirect(p1, initialBearing, distance);
		return directPoint.point;
	}

	public static double finalBearingOn(Point.Double p1, double initialBearing, double distance) throws IllegalArgumentException {
		validateLonLat(p1);
	   	validateBearing(initialBearing);
		DirectPoint directPoint = getVincentyDirect(p1, initialBearing, distance);
	    return directPoint.finalBearing;
	}

	public static class DirectPoint {
		public Point.Double point = new Point.Double();
		public double finalBearing;
		public DirectPoint(Point.Double p1, double finalBearing) throws IllegalArgumentException {
			validateLonLat(p1);
			validateBearing(finalBearing);
			point.y = p1.y;
			point.x = p1.x;
			this.finalBearing = finalBearing;
		}
	}
	
	public static DirectPoint getVincentyDirect(Point.Double p1, double initialBearing, double distance) throws IllegalArgumentException {
		validateLonLat(p1);
		validateBearing(initialBearing);
		if (distance <= 0.0) throw new IllegalArgumentException("Distance = " + distance + " meters");
		
	    DirectPoint directPoint = new DirectPoint(new Point.Double(), 0.0);
		
		double φ1 = Math.toRadians(p1.y);
	    double λ1 = Math.toRadians(p1.x);
	    double α1 = Math.toRadians(initialBearing);
	    double s = distance;

	    double a = 6378137;
	 	double b = 6356752.314245;
	 	double f = 1 / 298.257223563;

	    double sinα1 = Math.sin(α1);
	    double cosα1 = Math.cos(α1);

	    double tanU1 = (1-f) * Math.tan(φ1);
	    double cosU1 = 1 / Math.sqrt((1 + tanU1*tanU1));
	    double sinU1 = tanU1 * cosU1;
	    double σ1 = Math.atan2(tanU1, cosα1);
	    double sinα = cosU1 * sinα1;
	    double cosSqα = 1 - sinα*sinα;
	    double uSq = cosSqα * (a*a - b*b) / (b*b);
	    double A = 1 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)));
	    double B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)));

	    double σ = s / (b*A);
	    double σʹ;
	    
	    int iterations = 0;
	    
	    double cos2σM;
        double sinσ;
        double cosσ;
        double Δσ;
	    
	    do {
	        cos2σM = Math.cos(2*σ1 + σ);
	        sinσ = Math.sin(σ);
	        cosσ = Math.cos(σ);
	        Δσ = B*sinσ*(cos2σM+B/4*(cosσ*(-1+2*cos2σM*cos2σM)-
	            B/6*cos2σM*(-3+4*sinσ*sinσ)*(-3+4*cos2σM*cos2σM)));
	        σʹ = σ;
	        σ = s / (b*A) + Δσ;
	    } while (Math.abs(σ-σʹ) > 1e-12 && ++iterations<100);

	    double x = sinU1*sinσ - cosU1*cosσ*cosα1;
	    double φ2 = Math.atan2(sinU1*cosσ + cosU1*sinσ*cosα1, (1-f)*Math.sqrt(sinα*sinα + x*x));
	    double λ = Math.atan2(sinσ*sinα1, cosU1*cosσ - sinU1*sinσ*cosα1);
	    double C = f/16*cosSqα*(4+f*(4-3*cosSqα));
	    double L = λ - (1-C) * f * sinα * (σ + C*sinσ*(cos2σM+C*cosσ*(-1+2*cos2σM*cos2σM)));
	    double λ2 = (λ1+L+3*Math.PI)%(2*Math.PI) - Math.PI;  // Normalize to -180...+180

	    double revAz = Math.atan2(sinα, -x);

	    directPoint.point.x = Math.toDegrees(λ2);
	    directPoint.point.y = Math.toDegrees(φ2);
	    double fb = Math.toDegrees(revAz);
	    if (fb < 0) fb += 360;
	    directPoint.finalBearing = fb;
	    
	    return directPoint;
	}
	
	public static class InversePoint {
		public double distance;
		public double initialBearing;
		public double finalBearing;
		public InversePoint(double dist, double init, double fin) throws IllegalArgumentException {
			if (init >= 360.0 || init < 0.0) throw new IllegalArgumentException("Initial Bearing = " + init + " degrees");
			if (fin >= 360.0 || fin < 0.0) throw new IllegalArgumentException("Final Bearing = " + fin + " degrees");
			distance = dist;
			initialBearing = init;
			finalBearing = fin;
		}
	}
	
	public static InversePoint getVincentyInverse(Point.Double p1, Point.Double p2) throws IllegalArgumentException {
		validateLonLat(p1);
		validateLonLat(p2);
		
		InversePoint inversePoint = new InversePoint(0,0,0);
		
	 	double a = 6378137;
	 	double b = 6356752.314245;
	 	double f = 1 / 298.257223563;
	    double φ1 = Math.toRadians(p1.y);
	    double λ1 = Math.toRadians(p1.x);
	    double φ2 = Math.toRadians(p2.y);
	    double λ2 = Math.toRadians(p2.x);

	    double L = λ2 - λ1;
	    double tanU1 = (1-f) * Math.tan(φ1);
	    double cosU1 = 1 / Math.sqrt((1 + tanU1*tanU1));
	    double sinU1 = tanU1 * cosU1;
	    double tanU2 = (1-f) * Math.tan(φ2);
	    double cosU2 = 1 / Math.sqrt((1 + tanU2*tanU2));
	    double sinU2 = tanU2 * cosU2;

	    double λ = L;
	    double λʹ;
	    int iterations = 0;
	    
	    double sinλ;
        double cosλ;
        double sinSqσ;
        double sinσ;
        
        double cosσ;
        double σ;
        double sinα;
        double cosSqα;
        double cos2σM;
        
        double A;
        double B;
        double C;
        double Δσ;
        double uSq;
        double s;
        double fwdAz;
        double revAz;
	    
	    do {
	        sinλ = Math.sin(λ);
	        cosλ = Math.cos(λ);
	        sinSqσ = (cosU2 * sinλ) * (cosU2 * sinλ) + (cosU1 * sinU2 - sinU1 * cosU2 * cosλ) * (cosU1 * sinU2 - sinU1 * cosU2 * cosλ);
	        sinσ = Math.sqrt(sinSqσ);
	        
	        if (sinσ == 0) return inversePoint;  // co-incident points
	        
	        cosσ = sinU1 * sinU2 + cosU1 * cosU2 * cosλ;
	        σ = Math.atan2(sinσ, cosσ);
	        sinα = cosU1 * cosU2 * sinλ / sinσ;
	        cosSqα = 1 - sinα * sinα;
	        cos2σM = cosσ - 2*sinU1 * sinU2 / cosSqα;
	        
	        if (Double.isNaN(cos2σM)) cos2σM = 0;  // equatorial line: cosSqα=0 (§6)
	        
	        C = f / 16 * cosSqα * (4 + f * (4 - 3 * cosSqα));
	        λʹ = λ;
	        λ = L + (1-C) * f * sinα * (σ + C*sinσ*(cos2σM+C*cosσ*(-1+2*cos2σM*cos2σM)));
	    } while (Math.abs(λ-λʹ) > 1e-12 && ++iterations<100);
	    
	    if (iterations>=100) throw new IllegalArgumentException("Arithmetic Overflow Error");

	    uSq = cosSqα * (a*a - b*b) / (b*b);
	    A = 1 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)));
	    B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)));
	    Δσ = B*sinσ*(cos2σM+B/4*(cosσ*(-1+2*cos2σM*cos2σM)-
	        B/6*cos2σM*(-3+4*sinσ*sinσ)*(-3+4*cos2σM*cos2σM)));

	    s = b*A*(σ-Δσ);

	    fwdAz = Math.atan2(cosU2*sinλ,  cosU1*sinU2-sinU1*cosU2*cosλ);
	    revAz = Math.atan2(cosU1*sinλ, -sinU1*cosU2+cosU1*sinU2*cosλ);
	    
	    double distance = Math.round(s*100.0)/100.0;
	 
		DecimalFormat df = new DecimalFormat("###.###");

		double initialBearing = Math.toDegrees(fwdAz);
		if (initialBearing < 0) initialBearing += 360;
		double finalBearing = Math.toDegrees(revAz);
		if (finalBearing < 0) finalBearing += 360;
		
	    inversePoint.distance = Double.parseDouble(df.format(distance)); // round to 1mm precision
	    inversePoint.initialBearing = initialBearing;
	    inversePoint.finalBearing = finalBearing;
	    
	    
	    
	    return inversePoint;
	}
}
