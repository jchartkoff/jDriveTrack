package jdrivetrack;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class ConicSection {
	private Point.Double vertex;
	private Point.Double focus;
	private Point.Double center;
	private int unit;
	private double dbm;
	private double catt;
	private double a;
	private double b;
	private double courseMadeGood;
	private double approachAngle;
	private double eccentricity;
	private double directrix;
	private StaticMeasurement sma;
	private StaticMeasurement smb;
	private List<Point.Double> arcPointList;
	private List<Point.Double> hyperbolicPointArrayList;
	private Point.Double[] hyperbolicArc;
	private Point.Double[] hyperbolicPointArray;
	private List<LatLon> hyperbolicLatLonList;
	
	private static final int ARRAY_SIZE = 512;

	public ConicSection(StaticMeasurement sma, StaticMeasurement smb) {
		this(sma, smb, 1);
	}
	
	public ConicSection(StaticMeasurement sma, StaticMeasurement smb, int unit) {
		this.sma = sma;
		this.smb = smb;
		this.unit = unit;
		setUnit(unit);
		calculateHyperbolicArc(sma, smb);
		createPoints();
	}
	
	private void calculateHyperbolicArc(StaticMeasurement sma, StaticMeasurement smb) {
		courseMadeGood = courseMadeGood(sma, smb);
		approachAngle = approachAngle(sma, smb);
		center = center(sma.point);
		dbm = distanceBetweenMeasurements(sma, smb);
        catt = RFPath.conicAngleToTarget(sma, smb);
        a = a(center, catt, sma.altitude);
    	b = b(a, catt);
        vertex = vertex(center, a, courseMadeGood);
        focus = focus(center, courseMadeGood, a, b);
		eccentricity = eccentricity(a, b);
		directrix = a / eccentricity;
	}
	
	private void createPoints() {
		hyperbolicArc = createHyperbolicArc(b, a);
		hyperbolicPointArray = createSymmetricArcPointArray(hyperbolicArc, center, courseMadeGood);
		arcPointList = convertArrayToList(hyperbolicArc);
		hyperbolicPointArrayList = convertArrayToList(hyperbolicPointArray);
		hyperbolicLatLonList = convertArrayToLatLonList(hyperbolicPointArray);
	}
	
	private Point.Double center(Point.Double pa) {
		return pa;
	}
	
	private double distanceBetweenMeasurements(StaticMeasurement sma, StaticMeasurement smb) {
		return Vincenty.distanceToDirect(sma.point, sma.altitude, smb.point, smb.altitude);
	}

	private double approachAngle(StaticMeasurement sma, StaticMeasurement smb) {
		double altitudeDelta = sma.altitude - smb.altitude;
		double horizontalRange = Vincenty.distanceToDirect(smb.point, 0, sma.point, 0);
		return Math.toDegrees(Math.atan(altitudeDelta / horizontalRange));
	}

	private double eccentricity(double a, double b) {
		return Math.sqrt((a*a) + (b*b)) / a;
	}
	
    private double courseMadeGood(StaticMeasurement sma, StaticMeasurement smb) {
    	return Vincenty.finalBearingTo(smb.point, sma.point);
    }
    
    private Point.Double focus(Point.Double center, double cmg, double a, double b) {
    	double c = Math.sqrt((a*a)+(b*b));
    	double dm = Vincenty.degreesToMeters(c, cmg, center.y);
    	return Vincenty.getVincentyDirect(center, cmg, dm).point;
    }
    
    private Point.Double vertex(Point.Double center, double a, double cmg) {
    	double dm = Vincenty.degreesToMeters(a, cmg, center.y);
    	return Vincenty.getVincentyDirect(center, cmg, dm).point;
    }

    private double a(Point.Double center, double catt, double altitude) {
    	double da = altitude / Math.tan(Math.toRadians(catt));
    	return Vincenty.metersToDegrees(da, 90, center.y);
    }
    
    private double b(double a, double catt) {
    	return a * Math.tan(Math.toRadians(catt));
    }
    
    private List<Point.Double> convertArrayToList(Point.Double[] array) {
    	List<Point.Double> list = new ArrayList<Point.Double>(array.length);
    	for (Point.Double point : array) {
    		list.add(point);
    	}
    	return list;
    }

    private List<LatLon> convertArrayToLatLonList(Point.Double[] array) {
    	List<LatLon> list = new ArrayList<LatLon>(array.length);
    	for (Point.Double point : array) {
    		list.add(LatLon.fromDegrees(point.y, point.x));
    	}
    	return list;
    }
    
    public static Point.Double[] createHyperbolicArc(final double b, final double a) {
    	Point.Double[] arc = new Point.Double[ARRAY_SIZE];
    	double incr = 1.0 / arc.length;
    	double x = a;
    	for (int i = 0; i < arc.length; i++) {
    		double y = b * Math.sqrt(((x*x) / (a*a) - 1.0));
    		arc[i] = new Point.Double(x,y);
    		x += incr;
    	}
    	return arc;
    }

    public Point.Double[] createSymmetricArcPointArray(Point.Double[] arcArray, Point.Double c, double cmg) {
    	Point.Double[] pa = new Point.Double[arcArray.length * 2];
    	for (int i = arcArray.length - 1; i >= 0; i--) {
    		Point.Double n = arcArray[i];
    		Point.Double pk = new Point.Double(c.x + n.x, c.y + n.y);
	    	pa[arcArray.length - 1 - i] = translate(c, pk, cmg - 90.0);
    	}
    	for (int i = 0; i < arcArray.length; i++) {
    		Point.Double n = arcArray[i];
    		Point.Double pq = new Point.Double(c.x + n.x, c.y - n.y);
	    	pa[arcArray.length + i] = translate(c, pq, cmg - 90.0);
    	}
    	return pa;
    }
    
    public Point.Double translate(final Point.Double c, final Point.Double pt, final double rotate) {
    	double t = LatLon.rhumbAzimuth(LatLon.fromDegrees(c.y, c.x), LatLon.fromDegrees(pt.y, pt.x)).getDegrees();
    	Angle h = Angle.fromDegrees(c.distance(pt));
        LatLon d = LatLon.rhumbEndPosition(LatLon.fromDegrees(c.y, c.x), Angle.fromDegrees(rotate + t), h);
    	return new Point.Double(d.getLongitude().getDegrees(), d.getLatitude().getDegrees());
    }
    
    public List<LatLon> getAsymptotes() {
    	double h = center.distance(hyperbolicPointArrayList.get(0));
    	List<LatLon> list = new ArrayList<LatLon>(3);
    	double y1 = ((b/a) * h) + center.y;
    	double x = center.x + h;
    	double y2 = ((-b/a) * h) + center.y;
    	Point.Double p1 = translate(center, new Point.Double(x, y1), courseMadeGood - 90.0);
    	Point.Double p2 = translate(center, new Point.Double(x, y2), courseMadeGood - 90.0);
    	list.add(LatLon.fromDegrees(p1.y, p1.x));
    	list.add(LatLon.fromDegrees(center.y, center.x));
    	list.add(LatLon.fromDegrees(p2.y, p2.x));
    	return list;
    }
    
    public List<Point.Double> getAsymptoteCoords() {
    	List<Point.Double> coords = new ArrayList<Point.Double>(3);
    	List<LatLon> latLon = getAsymptotes();
    	for (int i = 0; i < latLon.size(); i++) {
    		coords.add(new Point.Double(latLon.get(i).longitude.degrees, latLon.get(i).latitude.degrees));
    	}
    	return coords;
    }
    
    public Point.Double[] getAsymptoteCoordArray() {
    	Point.Double[] coords = new Point.Double[3];
    	List<LatLon> latLon = getAsymptotes();
    	for (int i = 0; i < latLon.size(); i++) {
    		coords[i] = new Point.Double(latLon.get(i).longitude.degrees, latLon.get(i).latitude.degrees);
    	}
    	return coords;
    }
    
    public void setUnit(int unit) {
    	this.unit = unit;
    	sma.unit = unit;
    	smb.unit = unit;
    }
    
    public int getUnit() {
    	return unit;
    }
    
    public List<Point.Double> getArcPointList() {
    	return arcPointList;
    }
    
    public List<LatLon> getHyperbolicLatLonList() {
    	return hyperbolicLatLonList;
    }
    
    public List<Point.Double> getHyperbolicPointArrayList() {
    	return hyperbolicPointArrayList;
    }

    public StaticMeasurement getSMA() {
    	return sma;
    }
    
    public Point.Double[] getHyperbolicPointArray() {
    	return hyperbolicPointArray;
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
    	return courseMadeGood;
    }
    
    public double getDirectrix() {
    	return directrix;
    }

    public Point.Double getFocus() {
    	return focus;
    }
    
    public double getEccentricity() {
    	return eccentricity;
    }

    public Point.Double getCenter() {
    	return center;
    }
    
    public Point.Double getVertex() {
    	return vertex;
    }

    public double getConicAngleToTarget() {
    	return catt;
    }
    
    public double getDistanceBetweenMeasurements() {
    	return dbm;
    }

    public double getApproachAngle() {
    	return approachAngle;
    }
    
    public double getCourseMadeGood() {
    	return courseMadeGood;
    }
    
    public void setA(double a) {
    	this.a = a;
		vertex = vertex(center, a, courseMadeGood);
		b = Math.tan(Math.toRadians(catt)) * a;
		focus = focus(center, courseMadeGood, a, b);
		eccentricity = eccentricity(a, b);
		directrix = a / eccentricity;
    	createPoints();
    }
    
    public void setB(double b) {
    	this.b = b;
    	focus = focus(center, courseMadeGood, a, b);
		eccentricity = eccentricity(a, b);
		directrix = a / eccentricity;
    	createPoints();
    }
    
    public void setCourseMadeGood(double courseMadeGood) {
    	this.courseMadeGood = courseMadeGood;
		vertex = vertex(center, a, courseMadeGood);
		focus = focus(center, courseMadeGood, a, b);
    	createPoints();
    }

    public void setAngle(double catt) {
    	this.catt = catt;
		b = Math.tan(Math.toRadians(catt)) * a;
		focus = focus(center, courseMadeGood, a, b);
		eccentricity = eccentricity(a, b);
		directrix = a / eccentricity;
		createPoints();
    }

    public void setEccentricity(double eccentricity) {
    	this.eccentricity = eccentricity;
    	b = Math.sqrt(((eccentricity*a)*(eccentricity*a))-(a*a));
    	catt = Math.toDegrees(Math.atan(b/a));
    	directrix = a / eccentricity;
    	vertex = vertex(center, a, courseMadeGood);
		focus = focus(center, courseMadeGood, a, b);
		createPoints();
    }

    public void setFocus(Point.Double focus) {
    	this.focus = focus;
    	double c = Math.sqrt(((focus.x-center.x)*(focus.x-center.x))+((focus.y-center.y)*(focus.y-center.y)));
    	a = Math.sqrt((c*c) - (b*b));
    	b = Math.tan(Math.toRadians(catt)) * a;
    	vertex = vertex(center, a, courseMadeGood);
    	eccentricity = eccentricity(a, b);
		directrix = a / eccentricity;
		createPoints();
    }
    
    public void setCenter(Point.Double center) {
    	this.center = center;
		vertex = vertex(center, a, courseMadeGood);
		focus = focus(center, courseMadeGood, a, b);
		createPoints();
    }
    
    public void setVertex(Point.Double vertex) {
    	this.vertex = vertex;
    	double c = Math.sqrt(((vertex.x-center.x)*(vertex.x-center.x))+((vertex.y-center.y)*(vertex.y-center.y)));
    	a = Math.sqrt((c*c) - (b*b));
		b = Math.tan(Math.toRadians(catt)) * a;
		focus = focus(center, courseMadeGood, a, b);
		eccentricity = eccentricity(a, b);
		directrix = a / eccentricity;
		createPoints();
    }
    
	public void setLeadingStaticMeasurement(StaticMeasurement sma) {
		this.sma = sma;
		calculateHyperbolicArc(sma, smb);
		createPoints();
	}
	
	public StaticMeasurement getLeadingStaticMeasurement() {
		return sma;
	}
	
	public void setTrailingStaticMeasurement(StaticMeasurement smb) {
		this.smb = smb;
		calculateHyperbolicArc(sma, smb);
		createPoints();
	}
	
	public StaticMeasurement getTrailingStaticMeasurement() {
		return smb;
	}

}
