package jdrivetrack;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JPanel;

import jsc.util.Logarithm;

public class SignalLocationProjection extends JPanel {
	private static final long serialVersionUID = -3143479088440322492L;
	
	private static final Color DEFAULT_ASYMPTOTE_COLOR = Color.CYAN;
	private static final Color DEFAULT_ARC_COLOR = Color.RED;
	private static final Color DEFAULT_CURSOR_COLOR = Color.BLACK;
	private static final Color DEFAULT_TRAIL_COLOR = Color.GREEN;
	private static final boolean DEFAULT_SHOW_ASYMPTOTE = false;
	private static final boolean DEFAULT_SHOW_ARC = true;
	private static final boolean DEFAULT_SHOW_CURSOR = false;
	private static final boolean DEFAULT_SHOW_TRAIL = true;
	private static final double DEFAULT_ASYMPTOTE_LENGTH = -1;
	private static final int DEFAULT_CURSOR_DIAMETER = 6;
	private static final int DEFAULT_FLIGHT_NUMBER = 0;
	
	private int flight;
	private StaticMeasurement sma;
	private StaticMeasurement smb;
	private Point2D.Double center = new Point2D.Double();
	private Hyperbola2D hyperbola = null;
	private double dbr;
	private double catt;
	private double a;
	private double b;
	private double courseMadeGood;
	private double approachAngle;
	private Point2D.Double vertex = new Point2D.Double();
	private boolean showAsymptote = false;
	private boolean showArc = false;
	private boolean showTrail = false;
	private boolean showCursor = false;
	private Color asymptoteColor;
	private Color arcColor;
	private Color trailColor;
	private Color cursorColor;
	private int cursorDiameter;
	private double asymptoteLength;
	private List<Point2D.Double> arcPointList;
	private Point.Double upperLeftLonLat;
	private Point.Double lowerRightLonLat;
	
	public SignalLocationProjection() {}

	public SignalLocationProjection(Point.Double upperLeftLonLat, Point.Double lowerRightLonLat, StaticMeasurement sma, 
			StaticMeasurement smb, Dimension mapSize, Color arcColor, boolean showCursor) throws InterruptedException, ExecutionException {
		this(upperLeftLonLat, lowerRightLonLat, sma, smb, mapSize, arcColor, DEFAULT_SHOW_ARC, 
				DEFAULT_ASYMPTOTE_LENGTH, DEFAULT_ASYMPTOTE_COLOR, DEFAULT_SHOW_ASYMPTOTE, DEFAULT_CURSOR_DIAMETER, 
				DEFAULT_CURSOR_COLOR, showCursor, DEFAULT_TRAIL_COLOR, DEFAULT_SHOW_TRAIL, DEFAULT_FLIGHT_NUMBER);
	}
	
	public SignalLocationProjection(Point.Double upperLeftLonLat, Point.Double lowerRightLonLat, StaticMeasurement sma, 
			StaticMeasurement smb, Dimension mapSize, Color arcColor) throws InterruptedException, ExecutionException {
		this(upperLeftLonLat, lowerRightLonLat, sma, smb, mapSize, arcColor, DEFAULT_SHOW_ARC, 
				DEFAULT_ASYMPTOTE_LENGTH, DEFAULT_ASYMPTOTE_COLOR, DEFAULT_SHOW_ASYMPTOTE, DEFAULT_CURSOR_DIAMETER, 
				DEFAULT_CURSOR_COLOR, DEFAULT_SHOW_CURSOR, DEFAULT_TRAIL_COLOR, DEFAULT_SHOW_TRAIL, DEFAULT_FLIGHT_NUMBER);
	}
	
	public SignalLocationProjection(Point.Double upperLeftLonLat, Point.Double lowerRightLonLat, StaticMeasurement sma, 
			StaticMeasurement smb, Dimension mapSize) throws InterruptedException, ExecutionException {
		this(upperLeftLonLat, lowerRightLonLat, sma, smb, mapSize, DEFAULT_ARC_COLOR, DEFAULT_SHOW_ARC, 
				DEFAULT_ASYMPTOTE_LENGTH, DEFAULT_ASYMPTOTE_COLOR, DEFAULT_SHOW_ASYMPTOTE, DEFAULT_CURSOR_DIAMETER, 
				DEFAULT_CURSOR_COLOR, DEFAULT_SHOW_CURSOR, DEFAULT_TRAIL_COLOR, DEFAULT_SHOW_TRAIL, DEFAULT_FLIGHT_NUMBER);
	}

	public SignalLocationProjection(Point.Double upperLeftLonLat, Point.Double lowerRightLonLat, StaticMeasurement sma, 
			StaticMeasurement smb, Dimension mapSize, Color arcColor, boolean showArc, double asymptoteLength, 
			Color asymptoteColor, boolean showAsymptote, int cursorDiameter, Color cursorColor, boolean showCursor, 
			Color trailColor, boolean showTrail, int flight) throws InterruptedException, ExecutionException {
		
		this.arcColor = arcColor;
		this.showArc = showArc;
		this.showTrail = showTrail;
		this.showCursor = showCursor;
		this.showAsymptote = showAsymptote;
		this.sma = sma;
		this.smb = smb;
		this.upperLeftLonLat = upperLeftLonLat;
		this.lowerRightLonLat = lowerRightLonLat;
		this.asymptoteLength = asymptoteLength;
		this.asymptoteColor = asymptoteColor;
		this.trailColor = trailColor;
		this.cursorColor = cursorColor;
		this.flight = flight;
		this.cursorDiameter = cursorDiameter;
		
    	setSize(mapSize);
    	setPreferredSize(mapSize);
		setOpaque(false);
		setDoubleBuffered(true);
		setVisible(true);
		
		buildCompositeShape();
	}

	private void buildCompositeShape() {
		courseMadeGood = courseMadeGood(sma, smb);
		approachAngle = approachAngle(sma, smb);
		center = center(sma.point, smb.point, courseMadeGood);
		dbr = distanceBetweenMeasurements(sma, smb);
        catt = conicAngleToTarget(sma.dBm, smb.dBm, dbr, sma.frequencyMHz);
        vertex = vertex(center, catt, sma.altitude);
        a = a(center, vertex);
    	b = b(a, catt);
    	if (asymptoteLength == -1) asymptoteLength = preferedAsymptoteLength();
    	hyperbola = new Hyperbola2D(lonlatToXY(center), metersToPixels(a, 90), courseMadeGood, catt, 
    			metersToPixels(asymptoteLength, courseMadeGood));
    	arcPointList = convertPixelArrayToLonlatList(hyperbola.getArcPointArray());
	}
	
	private List<Point2D.Double> convertPixelArrayToLonlatList(Point2D.Double[] pixelArray) {
		if (pixelArray == null || pixelArray.length == 0) return null;
		List<Point2D.Double> lonlatList = new ArrayList<Point2D.Double>();
		for (Point2D.Double pix : pixelArray) {
			lonlatList.add(pixelToLonlat(pix));
		}
		return lonlatList;
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
    
	public static double distanceBetweenMeasurements(StaticMeasurement sma, StaticMeasurement smb) {
		return Vincenty.distanceTo(sma.point, smb.point);
	}

	public static double approachAngle(StaticMeasurement sma, StaticMeasurement smb) {
		double altitudeDelta = smb.altitude - sma.altitude;
		double horizontalRange = Vincenty.getVincentyInverse(smb.point, sma.point).distance;
		return Math.toDegrees(Math.atan(altitudeDelta / horizontalRange));
	}

    public static double distanceInMetersFreeSpacePathLoss(double dBfspl, double frequencyMhz) {
    	Logarithm log = new Logarithm(10);
    	return log.antilog((dBfspl + 27.55 - (20 * log.log(frequencyMhz))) / 20);
    }

    public static double conicAngleToTarget(double dBma, double dBmb, double dbr, double freq) {
    	double da = distanceInMetersFreeSpacePathLoss(dBma, freq);
    	double db = distanceInMetersFreeSpacePathLoss(dBmb, freq);
    	double d = Math.abs(db - da);
    	double f = d / dbr;
    	return Math.toDegrees(Math.acos(f));
    }

    public static double courseMadeGood(StaticMeasurement sma, StaticMeasurement smb) {
    	return Vincenty.initialBearingTo(smb.point, sma.point);
    }
    
    public static Point2D.Double vertex(Point2D.Double centerPoint, double catt, double altitude) {
    	double h2 = altitude / Math.tan(Math.toRadians(catt));
    	Point2D.Double v = Vincenty.getVincentyDirect(centerPoint, 90, h2).point;
    	return v;
    }
 
    public static double a(Point2D.Double centerPoint, Point2D.Double vertex) {
    	return Vincenty.distanceTo(centerPoint, vertex);
    }
    
    public static double b(double a, double catt) {
    	return a * Math.tan(Math.toRadians(catt));
    }
    
    public static double getHypotenuseOf(Point2D.Double pa, Point2D.Double pb) {
    	double a = pa.x - pb.x;
    	double b = pa.y - pb.y;
    	return Math.sqrt((a*a)+(b*b));
    }
    
    public static double getHypotenuseOf(double a, double b) {
    	return Math.sqrt((a*a)+(b*b));
    }
    
    public static double getDistanceX(Point2D.Double a, Point2D.Double b) {
    	return Math.abs(b.x - a.x);
    }

	private double longitudeToX(double longitude) {
		double leftToRightDegrees = Math.abs(upperLeftLonLat.x - lowerRightLonLat.x);
		return getSize().width - ((lowerRightLonLat.x - longitude) * 
				(getSize().width / leftToRightDegrees));
	}

	private double latitudeToY(double latitude) {
		double topToBottomDegrees = Math.abs(upperLeftLonLat.y - lowerRightLonLat.y);
		return getSize().height + ((lowerRightLonLat.y - latitude) * 
				(getSize().height / topToBottomDegrees));
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

	private Point2D.Double lonlatToXY(Point2D.Double lonlat) {
		return new Point2D.Double(longitudeToX(lonlat.x), latitudeToY(lonlat.y));
	}

	private double preferedAsymptoteLength() {		
		return (sma.altitude * 4) + Math.abs(sma.dBm * 50);
	}
	
	private Point2D.Double center(Point2D.Double p1, Point2D.Double p2, double bearing) {
		double d = Vincenty.distanceTo(p1, p2);
		return Vincenty.getVincentyDirect(p2, bearing, d/2).point;
	}

	public List<Point2D.Double> getArcPointList() {
		return arcPointList;
	}
	
	public Hyperbola2D getHyperbola() {
		return hyperbola;
	}
	
	public Point2D.Double getCenterPosition() {
		return center;
	}

	public Point2D.Double getVertex() {
		return vertex;
	}
	
	public double getCourseMadeGood() {
		return courseMadeGood;
	}
	
	public double getDistanceBetweenMeasurements() {
		return dbr;
	}
	
	public double getConicAngleToTarget() {
		return catt;
	}
	
	public int getFlight() {
		return flight;
	}
	
	public void setCurrentStaticMeasurement(StaticMeasurement sma) throws InterruptedException, ExecutionException {
		this.sma = sma;
		buildCompositeShape();
    	repaint();
	}
	
	public StaticMeasurement getCurrentStaticMeasurement() {
		return sma;
	}
	
	public void setPreviousStaticMeasurement(StaticMeasurement smb) throws InterruptedException, ExecutionException {
		this.smb = smb;
		buildCompositeShape();
    	repaint();
	}
	
	public StaticMeasurement getPreviousStaticMeasurement() {
		return smb;
	}
	
	public void showCursor(boolean showCursor) {
		this.showCursor = showCursor;
		repaint();
	}
	
	public void showTrail(boolean showTrail) {
		this.showTrail = showTrail;
		repaint();
	}
	
	public void showAsymptote(boolean showAsymptote) {
		this.showAsymptote = showAsymptote;
		repaint();
	}
	
	public void setAsymptoteColor(Color asymptoteColor) {
		this.asymptoteColor = asymptoteColor;
		repaint();
	}
	
	public void setCursorDiameter(int cursorDiameter) {
		this.cursorDiameter = cursorDiameter;
		repaint();
	}
	
	public void setAsymptoteLength(double asymptoteLength) {
		this.asymptoteLength = asymptoteLength;
		repaint();
	}
	public void showArc(boolean showArc) {
		this.showArc = showArc;
		repaint();
	}

	public void setArcColor(Color arcColor) {
		this.arcColor = arcColor;
		repaint();
	}

	public void setCursorColor(Color cursorColor) {
		this.cursorColor = cursorColor;
		repaint();
	}
	
	public void setTrailColor(Color trailColor) {
		this.trailColor = trailColor;
		repaint();
	}
	
    public double geta() {
    	return a;
    }
    
    public double getb() {
    	return b;
    }
    
    public double getApproachAngle() {
    	return approachAngle;
    }

    public void setApproachAngle(double approachAngle) throws InterruptedException, ExecutionException {
    	this.approachAngle = approachAngle;
    	buildCompositeShape();
    	repaint();
    }

    public void setCornerLonLat(Point.Double upperLeftLonLat, Point.Double lowerRightLonLat) {
		this.upperLeftLonLat = upperLeftLonLat;
		this.lowerRightLonLat = lowerRightLonLat;
		repaint();
	}
	
    public static double getFreeSpacePathLoss(double meters, double frequencyMhz) {
    	Logarithm log = new Logarithm(10);
    	return 20 * log.log(meters) + 20 * log.log(frequencyMhz) - 27.55;
    }
    
    @Override
	protected void paintComponent(Graphics gOrig) {
        super.paintComponent(gOrig);
        Graphics2D g = (Graphics2D) gOrig.create();
        try {
        	if (upperLeftLonLat != null && lowerRightLonLat != null) {
	        	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,	RenderingHints.VALUE_ANTIALIAS_ON);
				if (showAsymptote) {
					g.setStroke(new BasicStroke(1.0f));
					g.setColor(asymptoteColor);
			    	Asymptote2D asymptote = new Asymptote2D(lonlatToXY(center), courseMadeGood, catt, 
			    		metersToPixels(asymptoteLength, catt));
					g.draw(asymptote);
				}
				hyperbola.setHyperbola(lonlatToXY(center), metersToPixels(a, 90), courseMadeGood, catt, 
					metersToPixels(asymptoteLength, courseMadeGood));
				if (showArc) {
					g.setColor(arcColor);
					g.setStroke(new BasicStroke(1.0f));
			    	g.draw(hyperbola);
				}
				if (showTrail) {
					float dash[] = {5.0f,5.0f};
					g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1.0f, dash, 0f));
					g.setColor(trailColor);
					g.draw(new Line2D.Double(lonlatToXY(smb.point), lonlatToXY(sma.point)));
					g.setStroke(new BasicStroke(1.0f));
					g.draw(new Ellipse2D.Double(longitudeToX(smb.point.x) - (cursorDiameter / 2), latitudeToY(smb.point.y) - (cursorDiameter / 2), cursorDiameter, cursorDiameter));
					g.draw(new Ellipse2D.Double(longitudeToX(sma.point.x) - (cursorDiameter / 2), latitudeToY(sma.point.y) - (cursorDiameter / 2), cursorDiameter, cursorDiameter));
				}
				if (showCursor) {
					g.setStroke(new BasicStroke(1.0f));
					g.setColor(cursorColor);
					g.draw(new Ellipse2D.Double(longitudeToX(center.x) - (cursorDiameter / 2), latitudeToY(center.y) - (cursorDiameter / 2), cursorDiameter, cursorDiameter));
					g.draw(new Ellipse2D.Double(hyperbola.getVertex().x - (cursorDiameter / 2), hyperbola.getVertex().y - (cursorDiameter / 2), cursorDiameter, cursorDiameter));
					g.draw(new Ellipse2D.Double(hyperbola.getFocus().x - (cursorDiameter / 2), hyperbola.getFocus().y - (cursorDiameter / 2), cursorDiameter, cursorDiameter));
				    Point2D.Double source = new Point2D.Double(-83.07724, 40.026563);
					g.draw(new CrissCross2D(lonlatToXY(source), 12));
				}
        	}
        } finally {
        	g.dispose();
        }
	}
    
}
