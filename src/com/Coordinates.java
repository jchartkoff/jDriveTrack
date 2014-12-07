package com;

import java.awt.Point;

public class Coordinates {
	private static double easting;
	private static double northing;
	private static int zone;
	private static double nu = 6389236.914;
	private static double S = 5103266.421;
	private static double p = -0.483084;
	private static double K1 = 5101225.115;
	private static double K2 = 3750.291596;
	private static double K3 = 1.397608151;
	private static double K4 = 214839.3105;
	private static double K5 = -2.995382942;
	private static double arc;
	private static double mu;
	private static double ei;
	private static double ca;
	private static double cb;
	private static double cc;
	private static double cd;
	private static double n0;
	private static double r0;
	private static double _a1;
	private static double dd0;
	private static double t0;
	private static double Q0;
	private static double lof1;
	private static double lof2;
	private static double lof3;
	private static double _a2;
	private static double phi1;
	private static double fact1;
	private static double fact2;
	private static double fact3;
	private static double fact4;
	private static double zoneCM;
	private static double _a3;
	
	public static final double rho = 6368573.744;
	public static final double A6 = -1.00541E-07;
	public static final String southernHemisphere = "ACDEFGHJKLM";
	public static final double equatorialRadius = 6378137.0;
	public static final double polarRadius = 6356752.314;
	public static final double flattening = 0.00335281066474748;// (equatorialRadius-polarRadius)/equatorialRadius;
	public static final double inverseFlattening = 298.257223563;// 1/flattening;
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

	public static Point convertLonLatToGridSquare(Point.Double point, Point.Double arcSeconds) throws IllegalArgumentException {
		
		Point gridSquare = new Point();
		Point.Double centerOfGrid= new Point.Double();
		
		if (arcSeconds.x == 0.0 || arcSeconds.y == 0) return gridSquare;
		
		validate(point);
		
		try {
			double verticalPosition = point.y / (arcSeconds.y / 3600.0);
			double horizontalEdge = (int) verticalPosition * (arcSeconds.y / 3600.0);
			double horizontalPosition = point.x / (arcSeconds.x / 3600.0);
			double verticalEdge = (int) horizontalPosition * (arcSeconds.x / 3600.0);

			centerOfGrid.y = horizontalEdge - ((arcSeconds.y / 3600.0) * 0.5);
			centerOfGrid.x = verticalEdge + ((arcSeconds.x / 3600.0) * 0.5);
	
			String UTM = convertLonLatToUTM(centerOfGrid);
	
			String[] utm = UTM.split(" ");
	
			if (utm.length == 4) {
				gridSquare.x = Integer.parseInt(utm[2].substring(2));
				while (utm[3].length() < 7) {
					utm[3] = "0" + utm[3];
				}
				gridSquare.y = Integer.parseInt(utm[3].substring(3));
			}
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
		}

		return gridSquare;
	}

	public static Point.Double convertLonLatToCenterOfGrid(Point.Double point, Point.Double arcSeconds) throws IllegalArgumentException {

		Point.Double centerOfGrid = new Point.Double();

		if (arcSeconds.x == 0.0 || arcSeconds.y == 0) return centerOfGrid;
		
		validate(point);
		
		double verticalPosition = point.y / (arcSeconds.y / 3600.0);
		double horizontalPosition = point.x / (arcSeconds.x / 3600.0);

		double verticalEdge = (int) horizontalPosition * (arcSeconds.x / 3600.0);
		double horizontalEdge = (int) verticalPosition * (arcSeconds.y / 3600.0);

		double verticalCenterOfGrid;
		
		if (horizontalEdge < 0) {
			verticalCenterOfGrid = horizontalEdge - ((arcSeconds.x / 3600.0) * 0.5);
		} else {
			verticalCenterOfGrid = horizontalEdge + ((arcSeconds.x / 3600.0) * 0.5);
		}
		
		double horizontalCenterOfGrid;
		
		if (verticalEdge < 0) {
			horizontalCenterOfGrid = verticalEdge - ((arcSeconds.y / 3600.0) * 0.5);
		} else {
			horizontalCenterOfGrid = verticalEdge + ((arcSeconds.y / 3600.0) * 0.5);
		}

		centerOfGrid.x = horizontalCenterOfGrid;
		centerOfGrid.y = verticalCenterOfGrid;

		return centerOfGrid;
	}

	public static String convertLonLatToUTM(Point.Double point) throws IllegalArgumentException {

		validate(point);
		
		String UTM = "";

		setVariables(point);

		String longZone = getLongZone(point.x);
		LatZones latZones = new LatZones();
		String latZone = latZones.getLatZone(point.y);

		double _easting = getEasting();
		double _northing = getNorthing(point.y);

		UTM = longZone + " " + latZone + " " + ((int) _easting) + " " + ((int) _northing);

		return UTM;
	}

	private static void validate(Point.Double point) throws IllegalArgumentException {
		if (point.y < -90.0 || point.y > 90.0 || point.x < -180.0 || point.x > 180.0) {
			throw new IllegalArgumentException("Longitude = " + point.x + " / Latitude = " + point.y);
		}
	}

	private static void setVariables(Point.Double point) {
		double y = point.y * Math.PI / 180.0;
		double x = point.x;
		
		nu = equatorialRadius / Math.pow(1.0 - Math.pow(e * Math.sin(y), 2.0), (1.0 / 2.0));

		double var1;
		
		if (x < 0.0) {
			var1 = ((int) ((180.0 + x) / 6.0)) + 1.0;
		} else {
			var1 = ((int) (x / 6.0)) + 31;
		}
		
		double var2 = (6.0 * var1) - 183.0;
		double var3 = x - var2;
		
		p = var3 * 3600.0 / 10000.0;

		S = A0 * y - B0 * Math.sin(2 * y) + C0
				* Math.sin(4 * y) - D0 * Math.sin(6 * y) + E0
				* Math.sin(8 * y);

		K1 = S * k0;
		K2 = nu * Math.sin(y) * Math.cos(y) * Math.pow(sin1, 2)
				* k0 * (100000000) / 2;
		K3 = ((Math.pow(sin1, 4) * nu * Math.sin(y) * Math.pow(
				Math.cos(y), 3)) / 24)
				* (5 - Math.pow(Math.tan(y), 2) + 9 * e1sq
						* Math.pow(Math.cos(y), 2) + 4
						* Math.pow(e1sq, 2) * Math.pow(Math.cos(y), 4))
				* k0 * (10000000000000000L);

		K4 = nu * Math.cos(y) * sin1 * k0 * 10000;

		K5 = Math.pow(sin1 * Math.cos(y), 3)
				* (nu / 6)
				* (1 - Math.pow(Math.tan(y), 2) + e1sq
						* Math.pow(Math.cos(y), 2)) * k0
				* 1000000000000L;
	}

	private static String getLongZone(double longitude) {
		double longZone = 0;
		if (longitude < 0.0) {
			longZone = ((180.0 + longitude) / 6) + 1;
		} else {
			longZone = (longitude / 6) + 31;
		}
		String val = String.valueOf((int) longZone);
		if (val.length() == 1) {
			val = "0" + val;
		}
		return val;
	}

	private static double getNorthing(double latitude) {
		double northing = K1 + K2 * p * p + K3 * Math.pow(p, 4);
		if (latitude < 0.0) {
			northing = 10000000.0 + northing;
		}
		return northing;
	}

	private static double getEasting() {
		return 500000 + (K4 * p + K5 * Math.pow(p, 3));
	}

	private static class LatZones {
		private char[] negLetters = { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'J',
				'K', 'L', 'M' };

		private int[] negDegrees = { -90, -84, -72, -64, -56, -48, -40, -32,
				-24, -16, -8 };

		private char[] posLetters = { 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
				'W', 'X', 'Z' };

		private int[] posDegrees = { 0, 8, 16, 24, 32, 40, 48, 56, 64, 72, 84 };

		public LatZones() {	}

		public String getLatZone(double latitude) {
			int latIndex = -2;
			int lat = (int) latitude;

			if (lat >= 0) {
				int len = posLetters.length;
				for (int i = 0; i < len; i++) {
					if (lat == posDegrees[i]) {
						latIndex = i;
						break;
					}

					if (lat > posDegrees[i]) {
						continue;
					} else {
						latIndex = i - 1;
						break;
					}
				}
			} else {
				int len = negLetters.length;
				for (int i = 0; i < len; i++) {
					if (lat == negDegrees[i]) {
						latIndex = i;
						break;
					}

					if (lat < negDegrees[i]) {
						latIndex = i - 1;
						break;
					} else {
						continue;
					}

				}

			}

			if (latIndex == -1) {
				latIndex = 0;
			}
			if (lat >= 0) {
				if (latIndex == -2) {
					latIndex = posLetters.length - 1;
				}
				return String.valueOf(posLetters[latIndex]);
			} else {
				if (latIndex == -2) {
					latIndex = negLetters.length - 1;
				}
				return String.valueOf(negLetters[latIndex]);

			}
		}

	}

	private static String getHemisphere(String latZone) {
		String hemisphere = "N";
		if (southernHemisphere.indexOf(latZone) > -1) {
			hemisphere = "S";
		}
		return hemisphere;
	}

	public static Point.Double convertUTMToLatLong(String UTM) throws IllegalArgumentException {
		Point.Double lonlat = new Point.Double();
		String str = UTM;
		String[] utm = UTM.split(" ");
		zone = Integer.parseInt(utm[0]);
		String latZone = utm[1];
		easting = Double.parseDouble(utm[2]);
		northing = Double.parseDouble(utm[3]);
		String hemisphere = getHemisphere(latZone);
		double latitude = 0.0;
		double longitude = 0.0;

		if (hemisphere.equals("S")) {
			northing = 10000000 - northing;
		}
		setVariables();
		
		latitude = 180 * (phi1 - fact1 * (fact2 + fact3 + fact4)) / Math.PI;

		if (zone > 0) {
			zoneCM = 6 * zone - 183.0;
		} else {
			zoneCM = 3.0;

		}

		longitude = zoneCM - _a3;
		if (hemisphere.equals("S")) {
			latitude = -latitude;
		}

		lonlat.y = latitude;
		lonlat.x = longitude;
		
		if (lonlat.y < -90.0 || lonlat.y > 90.0 || lonlat.x < -180.0 || lonlat.x > 180.0)
			throw new IllegalArgumentException("Input UTM = " + str);
		
		return lonlat;
	}

	private static void setVariables() {
		arc = northing / k0;
		mu = arc
				/ (a * (1 - Math.pow(e, 2) / 4.0 - 3 * Math.pow(e, 4) / 64.0 - 5 * Math
						.pow(e, 6) / 256.0));

		ei = (1 - Math.pow((1 - e * e), (1 / 2.0)))
				/ (1 + Math.pow((1 - e * e), (1 / 2.0)));

		ca = 3 * ei / 2 - 27 * Math.pow(ei, 3) / 32.0;

		cb = 21 * Math.pow(ei, 2) / 16 - 55 * Math.pow(ei, 4) / 32;
		cc = 151 * Math.pow(ei, 3) / 96;
		cd = 1097 * Math.pow(ei, 4) / 512;
		phi1 = mu + ca * Math.sin(2 * mu) + cb * Math.sin(4 * mu) + cc
				* Math.sin(6 * mu) + cd * Math.sin(8 * mu);

		n0 = a / Math.pow((1 - Math.pow((e * Math.sin(phi1)), 2)), (1 / 2.0));

		r0 = a * (1 - e * e)
				/ Math.pow((1 - Math.pow((e * Math.sin(phi1)), 2)), (3 / 2.0));
		fact1 = n0 * Math.tan(phi1) / r0;

		_a1 = 500000 - easting;
		dd0 = _a1 / (n0 * k0);
		fact2 = dd0 * dd0 / 2;

		t0 = Math.pow(Math.tan(phi1), 2);
		Q0 = e1sq * Math.pow(Math.cos(phi1), 2);
		fact3 = (5 + 3 * t0 + 10 * Q0 - 4 * Q0 * Q0 - 9 * e1sq)
				* Math.pow(dd0, 4) / 24;

		fact4 = (61 + 90 * t0 + 298 * Q0 + 45 * t0 * t0 - 252 * e1sq - 3 * Q0
				* Q0)
				* Math.pow(dd0, 6) / 720;

		lof1 = _a1 / (n0 * k0);
		lof2 = (1 + 2 * t0 + Q0) * Math.pow(dd0, 3) / 6.0;
		lof3 = (5 - 2 * Q0 + 28 * t0 - 3 * Math.pow(Q0, 2) + 8 * e1sq + 24 * Math
				.pow(t0, 2)) * Math.pow(dd0, 5) / 120;
		_a2 = (lof1 - lof2 + lof3) / Math.cos(phi1);
		_a3 = _a2 * 180 / Math.PI;

	}
}
