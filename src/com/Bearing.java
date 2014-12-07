package com;

import java.awt.Color;
import java.awt.Point;

public class Bearing {
	private int index;
    private Point.Double point;
    private double bearing;
    private double distance;
    private int quality;
    private Color color;

    public Bearing(int index, Point.Double point, double bearing, double distance, int quality, Color color) {
        this.index = index;
    	this.point = point;
        this.bearing = bearing;
        this.distance = distance;
        this.quality = quality;
        this.color = color;
    }

    public int getIndex() {
    	return index;
    }
    
    public Point.Double getBearingPosition() {
        return point;
    }

    public double getBearing() {
        return bearing;
    }
    
    public double getDistance() {
    	return distance;
    }
    
    public int getQuality() {
    	return quality;
    }
    
    public Color getColor() {
    	return color;
    }
    
    public void setIndex(int index) {
    	this.index = index;
    }
    
    public void setBearingPosition(Point.Double point) {
    	this.point = point;
    }
    
    public void setBearing(double bearing) {
    	this.bearing = bearing;
    }
    
    public void setDistance(double distance) {
    	this.distance = distance;
    }
    
    public void setQuality(int quality) {
    	this.quality = quality;
    }
    
    public void setColor(Color color) {
    	this.color = color;
    }
}
