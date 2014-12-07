package com;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import jsc.util.Logarithm;

public class ConicSection {
	private Point2D.Double vertex = new Point2D.Double();
	private Point2D.Double focus = new Point2D.Double();
	private Point2D.Double center = new Point2D.Double();
	private double dbr;
	private double catt;
	private double a;
	private double b;
	private double courseMadeGood;
	private double approachAngle;
	private double eccentricity;
	private double directrix;
	private double direction;
	private StaticMeasurement sma;
	private StaticMeasurement smb;
	private List<Point2D.Double> arcPointList;

	public ConicSection(StaticMeasurement sma, StaticMeasurement smb) {
		this.sma = sma;
		this.smb = smb;
		calculateProbableLocationOnHyperbolicArc();
		arcPointList = convertArrayToList(createHyperbola(a, b, approachAngle, center, getPreferredAsymptoteLength()));
	}
	
	private void calculateProbableLocationOnHyperbolicArc() {
		courseMadeGood = courseMadeGood(sma, smb);
		approachAngle = approachAngle(sma, smb);
		center = center(sma.point, smb.point, courseMadeGood);
		dbr = distanceBetweenMeasurements(sma, smb);
        catt = conicAngleToTarget(sma.dBm, smb.dBm, dbr, sma.frequencyMHz);
        vertex = vertex(center, catt, sma.altitude);
        a = a(center, vertex);
    	b = b(a, catt);
    	focus.x = center.x + (Math.sin(Math.toRadians(direction)) * Math.sqrt((a*a)+(b*b)));
		focus.y = center.y - (Math.cos(Math.toRadians(direction)) * Math.sqrt((a*a)+(b*b)));
		eccentricity = Math.sqrt((a*a) + (b*b)) / a;
		directrix = a / eccentricity;
	}
	
	private Point2D.Double center(Point2D.Double p1, Point2D.Double p2, double bearing) {
		double direction = Vincenty.distanceTo(p1, p2);
		return Vincenty.getVincentyDirect(p2, bearing, direction/2).point;
	}
	
	private double distanceBetweenMeasurements(StaticMeasurement sma, StaticMeasurement smb) {
		return Vincenty.distanceTo(sma.point, smb.point);
	}

	private double approachAngle(StaticMeasurement sma, StaticMeasurement smb) {
		double altitudeDelta = smb.altitude - sma.altitude;
		double horizontalRange = Vincenty.getVincentyInverse(smb.point, sma.point).distance;
		return Math.toDegrees(Math.atan(altitudeDelta / horizontalRange));
	}

    private double distanceInMetersFreeSpacePathLoss(double dBfspl, double frequencyMhz) {
    	Logarithm log = new Logarithm(10);
    	return log.antilog((dBfspl + 27.55 - (20 * log.log(frequencyMhz))) / 20);
    }

    private double conicAngleToTarget(double dBma, double dBmb, double dbr, double freq) {
    	double da = distanceInMetersFreeSpacePathLoss(dBma, freq);
    	double db = distanceInMetersFreeSpacePathLoss(dBmb, freq);
    	double direction = Math.abs(db - da);
    	double f = direction / dbr;
    	return Math.toDegrees(Math.acos(f));
    }

    private double courseMadeGood(StaticMeasurement sma, StaticMeasurement smb) {
    	return Vincenty.initialBearingTo(smb.point, sma.point);
    }
    
    private Point2D.Double vertex(Point2D.Double centerPoint, double catt, double altitude) {
    	double h2 = altitude / Math.tan(Math.toRadians(catt));
    	Point2D.Double v = Vincenty.getVincentyDirect(centerPoint, 90, h2).point;
    	return v;
    }
 
    private double a(Point2D.Double centerPoint, Point2D.Double vertex) {
    	return Vincenty.distanceTo(centerPoint, vertex);
    }
    
    private double b(double a, double catt) {
    	return a * Math.tan(Math.toRadians(catt));
    }

    private List<Point2D.Double> convertArrayToList(Point2D.Double[] array) {
    	List<Point2D.Double> l = new ArrayList<Point2D.Double>(array.length);
    	for (Point2D.Double p : array) {
    		l.add(p);
    	}
    	return l;
    }
    
    private Point2D.Double[] createHyperbola(double a, double b, double d, Point2D.Double center, double length) {
		Point2D.Double[] arcArray = createHyperbolicArc(b, a, length);
		return createHyperbolicPointArray(arcArray, center, length, d);
	}

    public static Point2D.Double[] createHyperbolicArc(double b, double a, double length) {
    	Point2D.Double[] arc = new Point2D.Double[(int) (length - a)];
    	double x = a;
    	for (int i = 0; i < arc.length; i++) {
    		double y2 = (1.0 - ((x*x) / (a*a))) * -(b*b);
    		double y =  Math.sqrt(y2);
    		arc[i] = new Point2D.Double(x,y);
    		x += 1.0;
    	}
    	return arc;
    }
    
    public static Point2D.Double[] createHyperbolicPointArray(Point2D.Double[] arcArray, Point2D.Double center, double length, double d) {
    	Point2D.Double[] pa = new Point2D.Double[(int) (arcArray.length * 2)];
    	for (int i = arcArray.length - 1; i >= 0; i--) {
    		Point2D.Double n = arcArray[i];
	        double t = Math.toDegrees(Math.atan(n.y/n.x));
	        double tp = d - t - 90;
	        double hp = Math.sqrt((n.x*n.x)+(n.y*n.y));
	        double x = Math.cos(Math.toRadians(tp)) * hp;
	    	double y = Math.sin(Math.toRadians(tp)) * hp; 
	    	pa[arcArray.length - 1 - i] = new Point2D.Double(center.x + x, center.y + y);
    	}
    	for (int i = 0; i < arcArray.length; i++) {
    		Point2D.Double n = arcArray[i];
	        double t = Math.toDegrees(Math.atan(-n.y/n.x));
	        double tp = d - t - 90;
	        double hp = Math.sqrt((n.x*n.x)+(n.y*n.y));       
	        double x = Math.cos(Math.toRadians(tp)) * hp;
	    	double y = Math.sin(Math.toRadians(tp)) * hp; 
	    	pa[arcArray.length + i] = new Point2D.Double(center.x + x, center.y + y);
    	}
    	return pa;
    }
    
    public List<Point2D.Double> getArcPointList() {
    	return arcPointList;
    }
    
    public double getHypotenuse(Point2D.Double pa, Point2D.Double pb) {
    	double a = pa.x - pb.x;
    	double b = pa.y - pb.y;
    	return Math.sqrt((a*a)+(b*b));
    }
    
    public double getHypotenuse(double a, double b) {
    	return Math.sqrt((a*a)+(b*b));
    }
    
    public StaticMeasurement getSMA() {
    	return sma;
    }
    
    public StaticMeasurement getSMB() {
    	return smb;
    }
    
    public double getA() {
    	return a;
    }
    
    public double getB() {
    	return b;
    }
 
    public double getDirection() {
    	return direction;
    }
    
    public double getDirectrix() {
    	return directrix;
    }

    public Point2D.Double getFocus() {
    	return focus;
    }
    
    public double getEccentricity() {
    	return eccentricity;
    }

    public Point2D.Double getCenter() {
    	return center;
    }
    
    public Point2D.Double getVertex() {
    	return vertex;
    }

    public double getConicAngleToTarget() {
    	return catt;
    }
    
    public double getDistanceBetweenMeasurements() {
    	return dbr;
    }

    public double getApproachAngle() {
    	return approachAngle;
    }
    
    public double getCourseMadeGood() {
    	return courseMadeGood;
    }
    
    public void setA(double a) {
    	this.a = a;
		vertex.x = center.x + (Math.cos(Math.toRadians(direction)) * a);
		vertex.y = center.y - (Math.sin(Math.toRadians(direction)) * a);
		b = Math.tan(Math.toRadians(catt)) * a;
		focus.x = center.x + (Math.cos(Math.toRadians(direction)) * Math.sqrt((a*a)+(b*b)));
		focus.y = center.y - (Math.sin(Math.toRadians(direction)) * Math.sqrt((a*a)+(b*b)));
		eccentricity = Math.sqrt((a*a) + (b*b)) / a;
		directrix = a / eccentricity;
    	calculateProbableLocationOnHyperbolicArc();
    }
    
    public void setB(double b) {
    	this.b = b;
		focus.x = center.x + (Math.cos(Math.toRadians(direction)) * Math.sqrt((a*a)+(b*b)));
		focus.y = center.y + (Math.sin(Math.toRadians(direction)) * Math.sqrt((a*a)+(b*b)));
		eccentricity = Math.sqrt((a*a) + (b*b)) / a;
		directrix = a / eccentricity;
    	calculateProbableLocationOnHyperbolicArc();
    }
    
    public void setDirection(double direction) {
    	this.direction = direction;
		vertex.x = center.x + (Math.cos(Math.toRadians(direction)) * a);
		vertex.y = center.y + (Math.sin(Math.toRadians(direction)) * a);
		focus.x = center.x + (Math.cos(Math.toRadians(direction)) * Math.sqrt((a*a)+(b*b)));
		focus.y = center.y + (Math.sin(Math.toRadians(direction)) * Math.sqrt((a*a)+(b*b)));
    	calculateProbableLocationOnHyperbolicArc();
    }

    public void setAngle(double catt) {
    	this.catt = catt;
		b = Math.tan(Math.toRadians(catt)) * a;
		focus.x = center.x + (Math.cos(Math.toRadians(direction)) * Math.sqrt((a*a)+(b*b)));
		focus.y = center.y + (Math.sin(Math.toRadians(direction)) * Math.sqrt((a*a)+(b*b)));
		eccentricity = Math.sqrt((a*a) + (b*b)) / a;
		directrix = a / eccentricity;
		calculateProbableLocationOnHyperbolicArc();
    }

    public void setEccentricity(double eccentricity) {
    	this.eccentricity = eccentricity;
    	b = Math.sqrt(((eccentricity*a)*(eccentricity*a))-(a*a));
    	catt = Math.toDegrees(Math.atan(b/a));
    	directrix = a / eccentricity;
		vertex.x = center.x + (Math.cos(Math.toRadians(direction)) * a);
		vertex.y = center.y + (Math.sin(Math.toRadians(direction)) * a);
		focus.x = center.x + (Math.cos(Math.toRadians(direction)) * Math.sqrt((a*a)+(b*b)));
		focus.y = center.y + (Math.sin(Math.toRadians(direction)) * Math.sqrt((a*a)+(b*b)));
		calculateProbableLocationOnHyperbolicArc();
    }

    public void setFocus(Point2D.Double focus) {
    	this.focus = focus;
    	double c = Math.sqrt(((focus.x-center.x)*(focus.x-center.x))+((focus.y-center.y)*(focus.y-center.y)));
    	a = Math.sqrt((c*c) - (b*b));
    	b = Math.tan(Math.toRadians(catt)) * a;
    	direction = Math.toDegrees(Math.atan(b/a)) - 90;
		vertex.x = center.x + (Math.cos(Math.toRadians(direction)) * a);
		vertex.y = center.y + (Math.sin(Math.toRadians(direction)) * a);
		eccentricity = Math.sqrt((a*a) + (b*b)) / a;
		directrix = a / eccentricity;
		calculateProbableLocationOnHyperbolicArc();
    }
    
    public void setCenter(Point2D.Double center) {
    	Point2D.Double oc = this.center;
    	this.center = center;
    	double dx = center.x - oc.x;
    	double dy = center.y - oc.y;
		vertex.x = vertex.x + dx;
		vertex.y = vertex.y + dy;
		focus.x = focus.x + dx;
		focus.y = focus.y + dy;
		calculateProbableLocationOnHyperbolicArc();
    }
    
    public void setVertex(Point2D.Double vertex) {
    	this.vertex = vertex;
    	double c = Math.sqrt(((vertex.x-center.x)*(vertex.x-center.x))+((vertex.y-center.y)*(vertex.y-center.y)));
    	a = Math.sqrt((c*c) - (b*b));
		b = Math.tan(Math.toRadians(catt)) * a;
		direction = Math.toDegrees(Math.atan(b/a)) - 90;
		focus.x = center.x + (Math.cos(Math.toRadians(direction)) * Math.sqrt((a*a)+(b*b)));
		focus.y = center.y + (Math.sin(Math.toRadians(direction)) * Math.sqrt((a*a)+(b*b)));
		eccentricity = Math.sqrt((a*a) + (b*b)) / a;
		directrix = a / eccentricity;
		calculateProbableLocationOnHyperbolicArc();
    }
    
	public void setForwardStaticMeasurement(StaticMeasurement sma) {
		this.sma = sma;
		calculateProbableLocationOnHyperbolicArc();
	}
	
	public StaticMeasurement getForwardStaticMeasurement() {
		return sma;
	}
	
	public void setAftStaticMeasurement(StaticMeasurement smb) {
		this.smb = smb;
		calculateProbableLocationOnHyperbolicArc();
	}
	
	public StaticMeasurement getAftStaticMeasurement() {
		return smb;
	}
	
    public static double getFreeSpacePathLoss(double meters, double frequencyMhz) {
    	Logarithm log = new Logarithm(10);
    	return 20 * log.log(meters) + 20 * log.log(frequencyMhz) - 27.55;
    }
    
	public double getPreferredAsymptoteLength() {		
		return (sma.altitude * 4) + Math.abs(sma.dBm * 50);
	}
}
