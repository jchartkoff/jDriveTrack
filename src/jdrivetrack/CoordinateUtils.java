package jdrivetrack;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.MGRSCoord;
import gov.nasa.worldwind.geom.coords.UPSCoord;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.globes.Earth;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;

public class CoordinateUtils {
	public static final double rho = 6368573.744;
	public static final double A6 = -1.00541E-07;
	public static final String southernHemisphere = "ACDEFGHJKLM";
	public static final double equatorialRadius = 6378137.0;
	public static final double polarRadius = 6356752.314;
	public static final double flattening = 0.00335281066474748;
	public static final double inverseFlattening = 298.257223563;
	public static final double rm = Math.pow(equatorialRadius * polarRadius, 1 / 2.0);
	public static final double k0 = 0.9996;
	public static final double e = Math.sqrt(1.0 - Math.pow(polarRadius / equatorialRadius, 2.0));
	public static final double e1sq = e * e / (1.0 - e * e);
	public static final double n = (equatorialRadius - polarRadius) / (equatorialRadius + polarRadius);
	public static final double A0 = 6367449.146;
	public static final double B0 = 16038.42955;
	public static final double C0 = 16.83261333;
	public static final double D0 = 0.021984404;
	public static final double E0 = 0.000312705;
	public static final double sin1 = 4.84814E-06;
	public static final double b = 6356752.314;
	public static final double a = 6378137.0;
	
	public enum Precision { PRECISION_1_KM, PRECISION_100_M, PRECISION_10_M, PRECISION_1_M };
	
	public static MGRSCoord latLonToMGRS(Point.Double latlon, int precision) {
		return MGRSCoord.fromLatLon(Angle.fromDegreesLatitude(latlon.y), Angle.fromDegreesLongitude(latlon.x), precision);
	}

	public static UPSCoord latLonToUPS(Point.Double latlon) {
		return UPSCoord.fromLatLon(Angle.fromDegreesLatitude(latlon.y), Angle.fromDegreesLongitude(latlon.x));
	}
	
	public static UPSCoord utmToUPS(String hemisphere, double easting, double northing) {
		return UPSCoord.fromUTM(hemisphere, easting, northing);
	}
	
	public static UTMCoord latLonToUTM(Point.Double latlon) {
		return UTMCoord.fromLatLon(Angle.fromDegreesLatitude(latlon.y), Angle.fromDegreesLongitude(latlon.x));
	}
	
	public static Point.Double utmToLatLon(int zone, String hemisphere, double easting, double northing) {
		LatLon latLon = UTMCoord.locationFromUTMCoord(zone, hemisphere, easting, northing, new Earth());
		return new Point.Double(latLon.getLongitude().getDegrees(), latLon.getLatitude().getDegrees());
	}

	public static UTMTestTile latLonToTestTile(Point.Double latlon, Point.Double arcSeconds) {
		if (arcSeconds.x == 0.0 || arcSeconds.y == 0) return null;
		if (latlon.y < -90.0 || latlon.y > 90.0 || latlon.x < -180.0 || latlon.x > 180.0) return null;

		Point.Double lowerLeftCornerOfTile = latLonToInsideCornerOfTile(latlon, arcSeconds);
		UTMCoord utm = latLonToUTM(lowerLeftCornerOfTile);

		return utmPrecisionFormat(utm, Precision.PRECISION_100_M);
	}
	
	public static Position latLonToTestTilePosition(Point.Double latlon, Point.Double arcSeconds) {
		if (arcSeconds.x == 0.0 || arcSeconds.y == 0) return null;
		if (latlon.y < -90.0 || latlon.y > 90.0 || latlon.x < -180.0 || latlon.x > 180.0) return null;
		
		Point.Double pt = latLonToInsideCornerOfTile(latlon, arcSeconds);

		return new Position(LatLon.fromDegrees(pt.y, pt.x), 0);
	}
	
	private static UTMTestTile utmPrecisionFormat(UTMCoord utm, Precision precision) {
		try {
			int easting = (int) utm.getEasting();
			int northing = (int) utm.getNorthing();
			
			String strEasting = String.valueOf(easting);
			String strNorthing = String.valueOf(northing);
			
			int e = 0;
			int n = 0;
			
			switch (precision) {
				case PRECISION_1_KM: {
					e = Integer.parseInt(strEasting.substring(1, 3)); 
					n = Integer.parseInt(strNorthing.substring(2, 4));
					break;
				}
				case PRECISION_100_M: {
					e = Integer.parseInt(strEasting.substring(1, 4)); 
					n = Integer.parseInt(strNorthing.substring(2, 5));
					break;
				}
				case PRECISION_10_M: {
					e = Integer.parseInt(strEasting.substring(1, 5)); 
					n = Integer.parseInt(strNorthing.substring(2, 6));
					break;
				}
				case PRECISION_1_M: {
					e = Integer.parseInt(strEasting.substring(1, 6)); 
					n = Integer.parseInt(strNorthing.substring(2, 7));
					break;
				}
			}
			
			String zoneString = String.format("%02d", utm.getZone()) + utm.getHemisphere().substring(0,1);
			
			return new UTMTestTile(zoneString, e, n); 
		} catch (StringIndexOutOfBoundsException ex) {
			return new UTMTestTile("Off Globe");
		}
	}
	
	public static Point.Double latLonToInsideCornerOfTile(Point.Double latlon, Point.Double arcSeconds) {
		if (arcSeconds.x == 0.0 || arcSeconds.y == 0) return null;
		if (latlon.y < -90.0 || latlon.y > 90.0 || latlon.x < -180.0 || latlon.x > 180.0) return null;
		
		double verticalPosition = latlon.y / (arcSeconds.y / 3600.0);
		double horizontalPosition = latlon.x / (arcSeconds.x / 3600.0);

		double verticalEdge = (int) horizontalPosition * (arcSeconds.x / 3600.0);
		double horizontalEdge = (int) verticalPosition * (arcSeconds.y / 3600.0);

		return new Point.Double(verticalEdge, horizontalEdge);
	}
	
	public static Point2D.Double coordinateToScreenPoint(Point2D.Double coord, MapDimension mapDim, Dimension screen) {
		double x = screen.width - ((mapDim.getRightLongitude() - coord.x) * 
				(screen.width / mapDim.getMapDimensionInDegrees().x));
		double y = screen.height + ((mapDim.getLowerLatitude() - coord.y) * 
				(screen.height / mapDim.getMapDimensionInDegrees().y));
		return new Point2D.Double(x,y);
	}
	
	public static Point2D.Double[] coordinateArrayToScreenPointArray(Point2D.Double[] coord, MapDimension mapDim, Dimension screen) {
		Point2D.Double[] screenPoints = new Point2D.Double[coord.length];
		for (int i = 0; i < coord.length; i++) {
			screenPoints[i] = coordinateToScreenPoint(coord[i], mapDim, screen);
		}
		return screenPoints;
	}

} 
