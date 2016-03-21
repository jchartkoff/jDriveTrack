package jdrivetrack;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;

public class Asymptote2D implements Shape {
	private Point2D.Double center;
	private double direction;
	private double catt;
	private double length;
	private GeneralPath path = null;
	
	public Asymptote2D(Point2D.Double center, double catt, double length) {
		this(center, 0, catt, length);
	}
	
	public Asymptote2D(Point2D.Double[] points) {
		path = path(Arrays.asList(points));
		center = points[1];
		catt = calculateCATT(Arrays.asList(points));
		direction = calculateDirection(Arrays.asList(points));
	}
	
	public Asymptote2D(List<Point2D.Double> pointArray) {
		path = path(pointArray);
		center = pointArray.get(1);
		catt = calculateCATT(pointArray);
		direction = calculateDirection(pointArray);
	}
	
	public Asymptote2D(Point2D.Double center, double direction, double catt, double length) {
		this.center = center;
		this.length = length;
		this.direction = direction;
		this.catt = catt;
		path = path(center, direction, catt, length);
	}
	
	public void setLength(double length) {
		this.length = length;
		path = path(center, direction, catt, length);
	}
	
	private double calculateDirection(List<Point2D.Double> pointArray) {
		double anglePos = getInverseAngle(pointArray.get(1), pointArray.get(0));
		double angleNeg = getInverseAngle(pointArray.get(1), pointArray.get(2));
		return Math.abs(anglePos - angleNeg) / 2d;
	}
	
	private double calculateCATT(List<Point2D.Double> pointArray) {
		double anglePos = getInverseAngle(pointArray.get(1), pointArray.get(0));
		double angleNeg = getInverseAngle(pointArray.get(1), pointArray.get(2));
		return Math.abs(anglePos - angleNeg);
	}
	
	private double getInverseAngle(Point2D.Double p1, Point2D.Double p2) {
		double x = p2.x - p1.x;
		double y = p2.y - p1.y;
		return Math.toDegrees(Math.atan(y/x));
	}
	
	private GeneralPath path(List<Point2D.Double> pointArray) {
		GeneralPath path = new GeneralPath();
		path.moveTo(pointArray.get(0).x, pointArray.get(0).y);
		path.lineTo(pointArray.get(1).x, pointArray.get(1).y);
		path.lineTo(pointArray.get(2).x, pointArray.get(2).y);
		return path;
	}
	
	private GeneralPath path(Point2D.Double center, double direction, double catt, double length) {
		GeneralPath path = new GeneralPath();
		Point2D.Double n = new Point2D.Double(length, Math.tan(Math.toRadians(catt)) * length);
        double tp = direction + catt;
        double hp = Math.sqrt((n.x*n.x)+(n.y*n.y));       
        double x = Math.sin(Math.toRadians(tp)) * hp;
    	double y = Math.cos(Math.toRadians(tp)) * hp; 
    	path.moveTo(center.x + x, center.y - y);
    	path.lineTo(center.x, center.y);
    	tp = direction - catt;       
        x = Math.sin(Math.toRadians(tp)) * hp;
     	y = Math.cos(Math.toRadians(tp)) * hp; 
     	path.lineTo(center.x + x, center.y - y);
		return path;
	}

	public double getLength() {
    	return length;
    }
    
    public double getDirection() {
    	return direction;
    }
	
    public Point2D.Double getCenter() {
    	return center;
    }
    
    public double getConicAngleToTarget() {
    	return catt;
    }

    public void setDirection(double direction) {
    	this.direction = direction;
    	path = path(center, direction, catt, length);
    }
    
    public void setConicAngleToTarget(double catt) {
    	this.catt = catt;
    	path = path(center, direction, catt, length);
    }
	
    public void setCenter(Point2D.Double center) {
    	this.center = center;
    	path = path(center, direction, catt, length);
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
