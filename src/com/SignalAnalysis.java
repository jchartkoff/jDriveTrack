package com;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class SignalAnalysis {
	private StaticMeasurement sma;
	private StaticMeasurement smb;
	private List<Point2D.Double> arcPointList;
	private ConicSection cone;
	private double catt;
	private int flight;
	
	public SignalAnalysis(ConicSection cone) {
		this.cone = cone;
	}

	private double metersToPixels(double m, double t) {
		Point2D.Double c = upperLeftLonLat;
		Point2D.Double d = Vincenty.metersToDegrees(m, c.y);
		double dv = d.y - d.x;
		double f = ((100.0 / 90.0) * Math.abs(Math.abs(t - 180) - 90)) / 100;
		double dt = (f * dv) + d.x;
		return pixelsPerDegree() * dt;
	}
	
	private double pixelsPerDegree() {
		return getWidth() / Math.abs(upperLeftLonLat.x - lowerRightLonLat.x);
	}
	
    private double xToLongitude(double x) {
		double longitudeDegrees = Math.abs(upperLeftLonLat.x - lowerRightLonLat.x);
		return upperLeftLonLat.x + ((x / getSize().width) * longitudeDegrees);
	}
       
    private double yToLatitude(double y) {
		double latitudeDegrees = Math.abs(upperLeftLonLat.y - lowerRightLonLat.y);
		return upperLeftLonLat.y - ((y / getSize().height) * latitudeDegrees);
	}
    
    private Point2D.Double pixelToLonlat(Point2D.Double pixel) {
		return new Point2D.Double(xToLongitude(pixel.x), yToLatitude(pixel.y));
	}
	
	private List<Point2D.Double> convertPixelArrayToLonlatList(Point2D.Double[] pixelArray) {
		if (pixelArray == null || pixelArray.length == 0) return null;
		List<Point2D.Double> lonlatList = new ArrayList<Point2D.Double>();
		for (Point2D.Double pix : pixelArray) {
			lonlatList.add(pixelToLonlat(pix));
		}
		return lonlatList;
	}
	
	public ConicSection getCone() {
		return cone;
	}
	
	public List<Point2D.Double> getArcPointList() {
		return arcPointList;
	}

	public double getConicAngleToTarget() {
		return catt;
	}
	
	public StaticMeasurement getSMA() {
		return sma;
	}
	
	public StaticMeasurement getSMB() {
		return smb;
	}
	
	public int getFlight() {
		return flight;
	}

}
