package com;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Hyperbola2D implements Shape {
	private GeneralPath arcPath = new GeneralPath();
	private double a;
	private double b;
	private double d;
	private Point2D.Double center;
	private double length;

	public Hyperbola2D() {}
	
	public Hyperbola2D(double a, double b, double d, Point2D.Double center, double length) {
		this.a = a;
		this.b = b;
		this.d = d;
		this.center = center;
		this.length = length;
		createHyperbola(a, b, d, center, length);
	}

    private GeneralPath arcPath(Point2D.Double[] pointArray) {
    	GeneralPath arcPath = new GeneralPath();
    	arcPath.moveTo(pointArray[0].x, pointArray[0].y);
    	for (int i = 1; i < pointArray.length; i++) {
	        Point2D.Double n = pointArray[i]; 
	    	arcPath.lineTo(n.x, n.y);
	    }
		return arcPath;
    }
    
    private void createHyperbola(double a, double b, double d, Point2D.Double center, double length) {
		Point2D.Double[] arcArray = ConicSection.createHyperbolicArc(b, a, length);
		Point2D.Double[] pointArray =  ConicSection.createHyperbolicPointArray(arcArray, center, length, d);
		arcPath = arcPath(pointArray);
	}
    
    public double getDistanceX(Point2D.Double a, Point2D.Double b) {
    	return Math.abs(b.x - a.x);
    }

    public void setA(double a) {
    	this.a = a;
    	createHyperbola(a, b, d, center, length);
    }
    
    public void setB(double b) {
    	this.b = b;
    	createHyperbola(a, b, d, center, length);
    }
    
    public void setDirection(double d) {
    	this.d = d;
    	createHyperbola(a, b, d, center, length);
    }
    
    public void setCenter(Point2D.Double center) {
    	this.center = center;
    	createHyperbola(a, b, d, center, length);
    }
    
    public void setLength(double length) {
    	this.length = length;
    	createHyperbola(a, b, d, center, length);
    }
    
    public double getA() {
    	return a;
    }
    
    public double getB() {
    	return b;
    }
    
    public double getDirection() {
    	return d;
    }
    
    public Point2D.Double getCenter() {
    	return center;
    }
    
    public double getLength() {
    	return length;
    }
    
    public double getAngle() {
    	return Math.atan(a/b);
    }
    
	@Override
	public Rectangle getBounds() {
		return arcPath.getBounds();
	}

	@Override
	public Rectangle2D getBounds2D() {
		return arcPath.getBounds2D();
	}

	@Override
	public boolean contains(double x, double y) {
		return arcPath.contains(x, y);
	}

	@Override
	public boolean contains(Point2D p) {
		return arcPath.contains(p);
	}

	@Override
	public boolean intersects(double x, double y, double w, double h) {
		return arcPath.intersects(x, y, w, h);
	}

	@Override
	public boolean intersects(Rectangle2D r) {
		return arcPath.intersects(r);
	}

	@Override
	public boolean contains(double x, double y, double w, double h) {
		return arcPath.contains(x, y, w, h);
	}

	@Override
	public boolean contains(Rectangle2D r) {
		return arcPath.contains(r);
	}

	@Override
	public PathIterator getPathIterator(final AffineTransform at) {
		return arcPath.getPathIterator(at);
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return arcPath.getPathIterator(at, flatness);
	}
}

