package com;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Asymptote2D implements Shape {
	private Point2D.Double center = new Point2D.Double();
	private double d;
	private double t;
	private double length;
	private GeneralPath path = null;
	
	public Asymptote2D() {}
	
	public Asymptote2D(Point2D.Double center, double t, double length) {
		this(center, 0, t, length);
	}
	
	public Asymptote2D(Point2D.Double center, double d, double t, double length) {
		this.center = center;
		this.length = length;
		this.d = d;
		this.t = t;
		path = path(center, d, t, length);
	}

	private GeneralPath path(Point2D.Double center, double d, double t, double length) {
		GeneralPath path = new GeneralPath();
		Point2D.Double n = new Point2D.Double();
		n.x = length;
		n.y = Math.tan(Math.toRadians(t)) * length;
        double tp = d + t;
        double hp = Math.sqrt((n.x*n.x)+(n.y*n.y));       
        double x = Math.sin(Math.toRadians(tp)) * hp;
    	double y = Math.cos(Math.toRadians(tp)) * hp; 
    	path.moveTo(center.x + x, center.y - y);
    	path.lineTo(center.x, center.y);
    	tp = d - t;       
        x = Math.sin(Math.toRadians(tp)) * hp;
     	y = Math.cos(Math.toRadians(tp)) * hp; 
     	path.lineTo(center.x + x, center.y - y);
		return path;
	}

	public double getLength() {
    	return length;
    }
    
    public double getDirection() {
    	return d;
    }
	
    public Point2D.Double getCenter() {
    	return center;
    }
    
    public double getAngle() {
    	return t;
    }
    
    public void setLength(double length) {
    	if (this.length != length) {
	    	this.length = length;
	    	path = path(center, d, t, length);
    	}
    }
    
    public void setDirection(double d) {
    	if (this.d != d) {
	    	this.d = d;
	    	path = path(center, this.d, t, length);
    	}
    }
    
    public void setAngle(double t) {
    	if (this.t != t) {
	    	this.t = t;
	    	path = path(center, d, t, length);
    	}
    }
	
	private boolean changed(Point.Double p1, Point.Double p2) {
		if (Math.abs(p1.x - p2.x) < 0.000001 && Math.abs(p1.y - p2.y) < 0.000001) {
			return false;
		}
		return true;
	}
	
    public void setCenter(Point2D.Double center) {
    	if (changed(this.center, center)) {
	    	this.center = center;
	    	path = path(center, d, t, length);
    	}
    }
    
	@Override
	public Rectangle getBounds() {
		return path.getBounds();
	}

	@Override
	public Rectangle2D getBounds2D() {
		return path.getBounds2D();
	}

	@Override
	public boolean contains(double x, double y) {
		return path.contains(x, y);
	}

	@Override
	public boolean contains(Point2D p) {
		return path.contains(p);
	}

	@Override
	public boolean intersects(double x, double y, double w, double h) {
		return path.intersects(x, y, w, h);
	}

	@Override
	public boolean intersects(Rectangle2D r) {
		return path.intersects(r);
	}

	@Override
	public boolean contains(double x, double y, double w, double h) {
		return path.contains(x, y, w, h);
	}

	@Override
	public boolean contains(Rectangle2D r) {
		return path.contains(r);
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at) {
		return path.getPathIterator(at);
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return path.getPathIterator(at, flatness);
	}

}
