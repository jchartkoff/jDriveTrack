package jdrivetrack;

import java.awt.Point;
import java.text.DecimalFormat;

public class Vincenty {  // WGS-84
	
	public static final double EQUATORIAL_RADIUS = 6378137.0;
	public static final double POLAR_RADIUS = 6356752.314245;
	public static final double FIRST_ECCENTRICITY = 0.08181919092890624;
	public static final double FIRST_ECCENTRICITY_SQUARED = 0.006694380004260827;
	public static final double SECOND_ECCENTRICITY = 0.08209443803685366;
	public static final double SECOND_ECCENTRICITY_SQUARED = 0.006739496756586903;		
	public static final double FLATTENING = 0.0033528106718309896;
	public static final double FLATTENING_INVERSE = 298.2572229328697;
	public static final double FEET_PER_METER = 3.2808398950131234;
	public static final double FEET_PER_MILE = 5280.0;
	
	private static void validateLatitude(double latitude) throws IllegalArgumentException {
		if (latitude < -90.0 || latitude > 90.0)
			throw new IllegalArgumentException("Latitude = " + latitude + " / Degrees");
	}
	
	private static void validateDegrees(double degrees) throws IllegalArgumentException {
		if (degrees < -180.0 || degrees > 180.0) 
			throw new IllegalArgumentException("Degrees = " + degrees);
	}
	
	private static void validateArcSeconds(double arcSeconds) throws IllegalArgumentException {
		if (arcSeconds < (-180.0 * 3600.0) || arcSeconds > (180.0 * 3600.0)) 
			throw new IllegalArgumentException("ArcSeconds = " + arcSeconds);
	}
	
	private static void validateLonLat(Point.Double point) throws IllegalArgumentException {
		if (point.y < -90.0 || point.y > 90.0 || point.x < -180.0 || point.x > 180.0)
			throw new IllegalArgumentException("Longitude = " + point.x + " / Latitude = " + point.y);
	}
	
	private static void validateBearing(double bearing) throws IllegalArgumentException {
		if (bearing >= 360.0 || bearing <= -360.0) 
			throw new IllegalArgumentException("Bearing = " + bearing + " degrees");
	}
	
	public static double degreesToArcSeconds(double degrees) throws IllegalArgumentException {
		validateDegrees(degrees);
		return degrees * 3600.0;
	}	
	
	public static double arcSecondsToDegrees(double arcSeconds) throws IllegalArgumentException {
		validateArcSeconds(arcSeconds);
		return arcSeconds / 3600.0;
	}	
	
	public static double feetToDegrees(double feet, double bearing, double latitude) throws IllegalArgumentException {
		validateBearing(bearing);
		validateLatitude(latitude);
		return feetToArcSeconds(feet, bearing, latitude) / 3600.0;	
	}
	
	public static double metersToDegrees(double meters, double bearing, double latitude) throws IllegalArgumentException {
		return metersToArcSeconds(meters, bearing, latitude) / 3600.0;	
	}

	public static double feetToArcSeconds(double feet, double bearing, double latitude) throws IllegalArgumentException {
		validateLatitude(latitude);
		validateBearing(bearing);
		double kpd = kilometersPerDegree(bearing, latitude);
		double fpd = kpd * FEET_PER_METER * 1000.0;
		double dpf = 1.0 / fpd;
		return dpf * 3600.0 * feet;
	}
	
	
	public static double milesToDegrees(double miles, double bearing, double latitude)  throws IllegalArgumentException {
		validateBearing(bearing);
		validateLatitude(latitude);
		return feetToArcSeconds(miles * FEET_PER_MILE, bearing, latitude) / 3600.0;
	}
	
	public static Point.Double feetToArcSeconds(double feet, double latitude) {
		double x = feetToArcSeconds(feet, 90, latitude);
		double y = feetToArcSeconds(feet, 0, latitude);
		return new Point.Double(x, y);
	}
	
	public static double metersToArcSeconds(double meters, double bearing, double latitude) throws IllegalArgumentException {
		validateLatitude(latitude);
		validateBearing(bearing);
		double kpd = kilometersPerDegree(bearing, latitude);
		double mpd = kpd * 1000.0;
		double dpm = 1.0 / mpd;
		return dpm * 3600.0 * meters;
	}
	
	public static double degreesToFeet(double degrees, double bearing, double latitude) throws IllegalArgumentException {
		validateLatitude(latitude);
		validateBearing(bearing);
		validateDegrees(degrees);
		return arcSecondsToFeet(degrees * 3600.0, bearing, latitude);
	}
	
	public static double degreesToMiles(double degrees, double bearing, double latitude) throws IllegalArgumentException {
		validateLatitude(latitude);
		validateBearing(bearing);
		validateDegrees(degrees);
		return arcSecondsToFeet(degrees * 3600.0, bearing, latitude) / FEET_PER_MILE;
	}
	
	public static Point.Double degreesToFeet(double degrees, double latitude) {
		double x = degreesToFeet(degrees, 90, latitude);
		double y = degreesToFeet(degrees, 0, latitude);
		return new Point.Double(x, y);
	}
	
	public static double degreesToMeters(double degrees, double bearing, double latitude) throws IllegalArgumentException {
		validateLatitude(latitude);
		validateBearing(bearing);
		validateDegrees(degrees);
		return arcSecondsToMeters(degrees * 3600.0, bearing, latitude);
	}

	public static Point.Double degreesToMeters(double degrees, double latitude) {
		double x = degreesToMeters(degrees, 90, latitude);
		double y = degreesToMeters(degrees, 0, latitude);
		return new Point.Double(x, y);
	}
	
	public static double arcSecondsToFeet(double arcSeconds, double bearing, double latitude) throws IllegalArgumentException {
		validateLatitude(latitude);
		validateBearing(bearing);
		validateArcSeconds(arcSeconds);
		double kpd = kilometersPerDegree(bearing, latitude);
		double fpd = kpd * 1000.0 * FEET_PER_METER;
		return arcSeconds * fpd / 3600.0;
	}
	
	public static double arcSecondsToMeters(double arcSeconds, double bearing, double latitude) throws IllegalArgumentException {
		validateLatitude(latitude);
		validateBearing(bearing);
		validateArcSeconds(arcSeconds);
		double kpd = kilometersPerDegree(bearing, latitude);
		double mpd = kpd * 1000.0;
		return arcSeconds * mpd / 3600.0;
	}
	
	public static double kilometersPerDegree(double bearing, double latitude) throws IllegalArgumentException {
		validateLatitude(latitude);
		validateBearing(bearing);
		double x = getVincentyInverse(new Point.Double(-0.5, latitude), new Point.Double(+0.5, latitude)).distance / 1000.0;
		double y = getVincentyInverse(new Point.Double(0.0, latitude - 0.5), new Point.Double(0.0, latitude + 0.5)).distance / 1000.0;
		double a = Math.abs(y - x);
		return y + Math.abs(Math.sin(Math.toRadians(bearing) * a));
	}
	
	public static double metersToFeet(double meters) {
		return meters * FEET_PER_METER;
	}
	
	public static double feetToMeters(double feet) {
		return feet / FEET_PER_METER;
	}
	
	public static double degreesPerKilometer(double bearing, double latitude) throws IllegalArgumentException {
		return 1.0 / kilometersPerDegree(bearing, latitude);
	}
	
	public static double distanceToOnSurface(Point.Double p1, Point.Double p2) throws IllegalArgumentException {
		validateLonLat(p1);
		validateLonLat(p2);
		try {
	        return getVincentyInverse(p1, p2).distance;
	    } catch (Exception ae) {
	        throw new IllegalArgumentException("Points do not converge. The result is not a number.");
	    }
	}

	public static double distanceToDirect(Point.Double p1, double p1a, Point.Double p2, double p2a) throws IllegalArgumentException {
		validateLonLat(p1);
		validateLonLat(p2);
		double dp = Math.abs(p1.distance(p2));
		double dv = Math.abs(p1a - p2a);
		return Math.sqrt((dp*dp) + (dv*dv));
	}
	
	public static double initialBearingTo(Point.Double p1, Point.Double p2) throws IllegalArgumentException {
		validateLonLat(p1);
		validateLonLat(p2);
		try {
	        return getVincentyInverse(p1, p2).initialBearing;
	    } catch (Exception ae) {
	    	throw new IllegalArgumentException("Points do not converge. The result is not a number.");
	    }
	}

	public static double finalBearingTo(Point.Double p1, Point.Double p2) throws IllegalArgumentException {
		validateLonLat(p1);
		validateLonLat(p2);
		try {
	        return getVincentyInverse(p1, p2).finalBearing;
	    } catch (Exception ae) {
	    	throw new IllegalArgumentException("Points do not converge. The result is not a number.");
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
		public double x;
		public double y;
		public DirectPoint(Point.Double point, double finalBearing) throws IllegalArgumentException {
			validateLonLat(point);
			validateBearing(finalBearing);
			this.point = point;
			this.x = point.x;
			this.y = point.y;
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

	    double a = EQUATORIAL_RADIUS;
	 	double b = POLAR_RADIUS;
	 	double f = FLATTENING;

	    double sinα1 = Math.sin(α1);
	    double cosα1 = Math.cos(α1);

	    double tanU1 = (1-f) * Math.tan(φ1);
	    double cosU1 = 1 / Math.sqrt((1 + tanU1*tanU1));
	    double sinU1 = tanU1 * cosU1;
	    double σ1 = Math.atan2(tanU1, cosα1);
	    double sinα = cosU1 * sinα1;
	    double cosSqα = 1 - sinα*sinα;
	    double uSq = cosSqα * (a*a - b*b) / (b*b);
	    double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
	    double B = uSq / 1024 * (256 + uSq *(-128 + uSq * (74 - 47 * uSq)));

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
	    } while (Math.abs(σ-σʹ) > 1e-12 && ++iterations < 200);
	    
	    if (iterations >= 200) throw new IllegalArgumentException();
	    
	    double x = sinU1*sinσ - cosU1*cosσ*cosα1;
	    double φ2 = Math.atan2(sinU1*cosσ + cosU1*sinσ*cosα1, (1-f)*Math.sqrt(sinα*sinα + x*x));
	    double λ = Math.atan2(sinσ*sinα1, cosU1*cosσ - sinU1*sinσ*cosα1);
	    double C = f/16*cosSqα*(4+f*(4-3*cosSqα));
	    double L = λ - (1-C) * f * sinα * (σ + C*sinσ*(cos2σM+C*cosσ*(-1+2*cos2σM*cos2σM)));
	    double λ2 = (λ1+L+3*Math.PI)%(2*Math.PI) - Math.PI;
	    double α2 = Math.atan2(sinα, -x);
	    
	    α2 = (α2 + 2*Math.PI) % (2*Math.PI);

	    directPoint.point.x = Math.toDegrees(λ2);
	    directPoint.point.y = Math.toDegrees(φ2);

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
		
	 	double a = EQUATORIAL_RADIUS;
	 	double b = POLAR_RADIUS;
	 	double f = FLATTENING;
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
	    
	    double distance = Math.round(s * 100.0) / 100.0;
	 
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
