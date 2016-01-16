package jdrivetrack;

import java.awt.Point;
import java.awt.geom.Rectangle2D;

public class MapDimension {
	private double upperLatitude;
	private double lowerLatitude;
	private double leftLongitude;
	private double rightLongitude;
	private Rectangle2D.Double mapRectangle;
	
	public enum Hemisphere { NORTH, EAST, SOUTH, WEST }
	
	public MapDimension(Rectangle2D.Double mapRectangle) {
		this(mapRectangle.y, mapRectangle.x + Math.abs(mapRectangle.width), mapRectangle.y - Math.abs(mapRectangle.height),
				mapRectangle.x);
	}
	
	public MapDimension(double upperLatitude, double rightLongitude, double lowerLatitude, double leftLongitude) {
		this.upperLatitude = upperLatitude;
		this.rightLongitude = rightLongitude;
		this.lowerLatitude = lowerLatitude;
		this.leftLongitude = leftLongitude;
		mapRectangle = new Rectangle2D.Double(leftLongitude, upperLatitude, Math.abs(rightLongitude - leftLongitude),
				Math.abs(upperLatitude - lowerLatitude));
	}
	
	public Rectangle2D.Double getMapRectangle() {
		return mapRectangle;
	}
	
	public Point.Double getMapDimensionInArcSeconds() {
		Point.Double dimension = getMapDimensionInDegrees();
		return new Point.Double(dimension.x * 3600d, dimension.y * 3600d);
	}
	
	public Point.Double getMapDimensionInDegrees() {
		double latitude = Math.abs(upperLatitude - lowerLatitude);
		double longitude = Math.abs(rightLongitude - leftLongitude);
		return new Point.Double(longitude, latitude);
	}
	
	public Point.Double getMapDimensionInMeters() {
		return Vincenty.degreesToMeters(getMapDimensionInDegrees().x, lowerLatitude);
	}
	
	public Point.Double getMapDimensionInFeet() {
		return Vincenty.degreesToFeet(getMapDimensionInDegrees().x, lowerLatitude);
	}
	
	public double getUpperLatitude() {
		return upperLatitude;
	}
	
	public double getLowerLatitude() {
		return lowerLatitude;
	}
	
	public double getRightLongitude() {
		return rightLongitude;
	}
	
	public double getLeftLongitude() {
		return leftLongitude;
	}
	
	public Hemisphere getLatitudenalHemisphere() {
		if (upperLatitude + lowerLatitude >= 0) return Hemisphere.NORTH;
		else return Hemisphere.SOUTH;
	}
	
	public Hemisphere getLongitudenalHemisphere() {
		if (leftLongitude + rightLongitude <= 0) return Hemisphere.WEST;
		else return Hemisphere.EAST;
	}
}
