package jdrivetrack;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.MGRSCoord;
import gov.nasa.worldwind.geom.coords.UPSCoord;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.globes.Earth;

import types.MapDimension;
import types.TestTile;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	public enum Precision { PRECISION_1000_KM, PRECISION_100_KM, PRECISION_10_KM, PRECISION_1_KM, PRECISION_100_M, PRECISION_10_M, PRECISION_1_M };
	
	public static MGRSCoord lonLatToMGRS(Point2D.Double lonlat, int precision) {
		return MGRSCoord.fromLatLon(Angle.fromDegreesLatitude(lonlat.y), Angle.fromDegreesLongitude(lonlat.x), precision);
	}

	public static String[] gzdParse(final MGRSCoord mgrs) {
		String[] ret = new String[2];
		String s = mgrs.toString().substring(0, 3);
        ret[0] = "";
        ret[1] = "";
        Pattern pattern = Pattern.compile("\\d");
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
        	ret[0] += matcher.group();
        }
        pattern = Pattern.compile("\\D");
        matcher = pattern.matcher(s);
        if (matcher.find()) ret[1] = matcher.group();
        return ret;
    }
	
	public static UPSCoord lonLatToUPS(Point2D.Double lonlat) {
		return UPSCoord.fromLatLon(Angle.fromDegreesLatitude(lonlat.y), Angle.fromDegreesLongitude(lonlat.x));
	}
	
	public static UPSCoord utmToUPS(String hemisphere, double easting, double northing) {
		return UPSCoord.fromUTM(hemisphere, easting, northing);
	}
	
	public static UTMCoord lonLatToUTM(Point2D.Double lonlat) {
		return UTMCoord.fromLatLon(Angle.fromDegreesLatitude(lonlat.y), Angle.fromDegreesLongitude(lonlat.x));
	}
	
	public static TestTile lonLatToUTMTestTile(Point2D.Double lonlat, Point2D.Double arcSeconds) {
		return new TestTile(null, lonLatToUTM(lonlat), arcSeconds, Precision.PRECISION_1_M);
	}
	
	public static Point2D.Double utmToLatLon(int zone, String hemisphere, double easting, double northing) {
		LatLon latLon = UTMCoord.locationFromUTMCoord(zone, hemisphere, easting, northing, new Earth());
		return new Point2D.Double(latLon.getLongitude().getDegrees(), latLon.getLatitude().getDegrees());
	}

	public static TestTile lonLatToTestTile(Point2D.Double lonLat, Point2D.Double arcSeconds, Point2D.Double reference, Point2D.Double gridSize) {
		if (arcSeconds.x == 0.0 || arcSeconds.y == 0.0) return null;
		if (lonLat.y < -90.0 || lonLat.y > 90.0 || lonLat.x < -180.0 || lonLat.x > 180.0) return null;

		Point2D.Double lowerRightCornerOfTile = lonLatToLowerLeftCornerOfTile(lonLat, arcSeconds, reference, gridSize);
		if (lowerRightCornerOfTile.x == 181 || lowerRightCornerOfTile.y == 91) return new TestTile("Off Test Grid");
		UTMCoord utm = lonLatToUTM(lowerRightCornerOfTile);

		return utmToUTMTestTile(utm, arcSeconds, Precision.PRECISION_1_M);
	}
	
	public static Position lonLatToTestTilePosition(Point2D.Double lonlat, Point2D.Double arcSeconds, Point2D.Double reference, Point2D.Double gridSize) {
		if (arcSeconds.x == 0.0 || arcSeconds.y == 0) return null;
		if (lonlat.y < -90.0 || lonlat.y > 90.0 || lonlat.x < -180.0 || lonlat.x > 180.0) return null;
		
		Point2D.Double pt = lonLatToLowerLeftCornerOfTile(lonlat, arcSeconds, reference, gridSize);

		return new Position(LatLon.fromDegrees(pt.y, pt.x), 0);
	}
	
	public static String utmCoordToTestTileString(UTMCoord utm, Precision precision) {
		try {
			int easting = (int) utm.getEasting();
			int northing = (int) utm.getNorthing();

			String strEasting = String.valueOf(easting);
			String strNorthing = String.valueOf(northing);
			
			int e = 0;
			int n = 0;
			
			switch (precision) {
				case PRECISION_1000_KM: {
					e = Integer.parseInt(strEasting.substring(0, 1)); 
					n = Integer.parseInt(strNorthing.substring(0, 1));
					break;
				}
				case PRECISION_100_KM: {
					e = Integer.parseInt(strEasting.substring(0, 2)); 
					n = Integer.parseInt(strNorthing.substring(0, 2));
					break;
				}
				case PRECISION_10_KM: {
					e = Integer.parseInt(strEasting.substring(0, 3)); 
					n = Integer.parseInt(strNorthing.substring(0, 3));
					break;
				}
				case PRECISION_1_KM: {
					e = Integer.parseInt(strEasting.substring(0, 4)); 
					n = Integer.parseInt(strNorthing.substring(0, 4));
					break;
				}
				case PRECISION_100_M: {
					e = Integer.parseInt(strEasting.substring(0, 5)); 
					n = Integer.parseInt(strNorthing.substring(0, 5));
					break;
				}
				case PRECISION_10_M: {
					e = Integer.parseInt(strEasting.substring(0, 6)); 
					n = Integer.parseInt(strNorthing.substring(0, 6));
					break;
				}
				case PRECISION_1_M: {
					e = Integer.parseInt(strEasting.substring(0, 6)); 
					n = Integer.parseInt(strNorthing.substring(0, 7));
					break;
				}
			}

			String zoneString = String.format("%02d", utm.getZone()) + utm.getHemisphere().substring(0,1).toUpperCase();
			
			return zoneString + " " + String.valueOf(e) + " " +  String.valueOf(n); 
		} catch (StringIndexOutOfBoundsException ex) {
			ex.printStackTrace();
			return "Error";
		}
	}
	
	public static TestTile utmToUTMTestTile(UTMCoord utm, Point2D.Double tileSize, Precision precision) {
		return new TestTile(null, utm, tileSize, precision);
	}

	public static Point2D.Double lonLatToLowerLeftCornerOfTile(Point2D.Double lonlat, Point2D.Double arcSeconds, Point2D.Double reference, Point2D.Double gridSize) {
		if (arcSeconds.x == 0.0 || arcSeconds.y == 0) return null;
		if (lonlat.y < -90.0 || lonlat.y > 90.0 || lonlat.x < -180.0 || lonlat.x > 180.0) return null;
		
		//  1 - get UL reference point 
		//  2 - calculate X-Y degrees per tile
		//  3 - calculate how many degrees X-Y in from UL
		//  4 - calculate number of tiles X-Y from UL
		//  5 - calculate next lowest X-Y from step 4
		//  6 - add reference to result
		//  7 - return lower left point
		
		double verticalDistanceFromUL = Math.abs(lonlat.y - reference.y);
		double horizontalDistanceFromUL = Math.abs(lonlat.x - reference.x);
		
		double verticalEdge;
		double horizontalEdge;
		
		if (verticalDistanceFromUL >= 0 && horizontalDistanceFromUL >= 0 && 
				verticalDistanceFromUL <= gridSize.y && horizontalDistanceFromUL <= gridSize.x ) {
			int verticalTileNumber = (int) (verticalDistanceFromUL / (arcSeconds.y / 3600.0)) + 1;
			int horizontalTileNumber = (int) (horizontalDistanceFromUL / (arcSeconds.x / 3600.0));
		
			verticalEdge = reference.x + (horizontalTileNumber * (arcSeconds.x / 3600.0));
			horizontalEdge = reference.y - (verticalTileNumber * (arcSeconds.y / 3600.0));
		} else {
			verticalEdge = 181;
			horizontalEdge = 91;
		}

		return new Point2D.Double(verticalEdge, horizontalEdge);
	}
	
	public static Point2D.Double coordinateToScreenPoint(Point2D.Double lonLat, MapDimension mapDim, Dimension screen) {
		double x = screen.width - ((mapDim.getRightLongitude() - lonLat.x) * 
				(screen.width / mapDim.getMapDimensionInDegrees().x));
		double y = screen.height + ((mapDim.getLowerLatitude() - lonLat.y) * 
				(screen.height / mapDim.getMapDimensionInDegrees().y));
		return new Point2D.Double(x,y);
	}
	
	public static Point2D.Double[] coordinateArrayToScreenPointArray(Point2D.Double[] coords, MapDimension mapDim, Dimension screen) {
		Point2D.Double[] screenPoints = new Point2D.Double[coords.length];
		for (int i = 0; i < coords.length; i++) {
			screenPoints[i] = coordinateToScreenPoint(coords[i], mapDim, screen);
		}
		return screenPoints;
	}

} 
