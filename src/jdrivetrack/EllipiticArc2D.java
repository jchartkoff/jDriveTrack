package jdrivetrack;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;

import javax.swing.JPanel;

public class EllipiticArc2D extends JPanel {
	private static final long serialVersionUID = 1L;
	private Point.Double upperLeftPoint = new Point.Double();
	private Point.Double lowerRightPoint = new Point.Double();
	private Point.Double center = new Point.Double();
	private double attackAngle;
	private double altitude;
	private double heading;
	private Color color;
	
	public EllipiticArc2D(Dimension mapSize) {
		this(new Point.Double(0,0), new Point.Double(0,0), new Point.Double(0,0), 0, 0, 0, mapSize, Color.RED);
	}
	
	public EllipiticArc2D(Point.Double upperLeftPoint, Point.Double lowerRightPoint, Point.Double center, 
			double attackAngle, double altitude, double heading, Dimension mapSize, Color color) {
		this.center = center;
		this.attackAngle = attackAngle;
		this.altitude = altitude;
		this.heading = heading;
		this.color = color;
		this.upperLeftPoint = upperLeftPoint;
		this.lowerRightPoint = lowerRightPoint;
		setSize(mapSize);
		setOpaque(false);
		setDoubleBuffered(true);
		setVisible(true);
	}

	public void setCornerLonLat(Point.Double upperLeftPoint, Point.Double lowerRightPoint) {
		this.upperLeftPoint = upperLeftPoint;
		this.lowerRightPoint = lowerRightPoint;
	}

	private double longitudeToX(double longitude) {
		double leftToRightDegrees = Math.abs(upperLeftPoint.x - lowerRightPoint.x);
		return getSize().width - ((lowerRightPoint.x - longitude) * 
				(getSize().width / leftToRightDegrees));
	}

	private double latitudeToY(double latitude) {
		double topToBottomDegrees = Math.abs(upperLeftPoint.y - lowerRightPoint.y);
		return getSize().height + ((lowerRightPoint.y - latitude) * 
				(getSize().height / topToBottomDegrees));
	}

    private Point.Double calcArcDimPix(double altitudeFeet, double angleOfAttack) {
    	double arcDiaFeet = altitudeFeet * Math.tan(Math.toRadians(angleOfAttack)) * 2.0;
    	double arcDimDeg = Vincenty.feetToDegrees(arcDiaFeet, angleOfAttack, upperLeftPoint.y);
    	double lToRDeg = Math.abs(upperLeftPoint.x - lowerRightPoint.x);
    	double pixPerDegLon = getSize().width / lToRDeg;
    	double tToBDeg = Math.abs(upperLeftPoint.y - lowerRightPoint.y);
    	double pixPerDegLat = getSize().height / tToBDeg;
    	return new Point.Double(pixPerDegLon * arcDimDeg, pixPerDegLat * arcDimDeg);
    }
    
    public void moveArc(Point.Double center, double attackAngle, double altitude, double heading, Color color) {
    	this.center = center;
    	this.attackAngle = attackAngle;
    	this.altitude = altitude;
    	this.heading = heading;
    	this.color = color;
    }
    
	@Override
	protected void paintComponent(Graphics gOrig) {
        super.paintComponent(gOrig);
        Graphics2D g = (Graphics2D) gOrig.create();
        try {
        	Point.Double sq = calcArcDimPix(altitude, attackAngle);
        	double ptulx = longitudeToX(center.x) - (sq.x / 2.0);
        	double ptuly = latitudeToY(center.y);
        	Arc2D.Double arc = new Arc2D.Double(ptulx, ptuly, sq.x, sq.y, -heading, 180.0, Arc2D.OPEN);
        	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,	RenderingHints.VALUE_ANTIALIAS_ON);
			g.setStroke(new BasicStroke(2.0f));
			g.setColor(color);
			g.draw(arc);
        } finally {
        	g.dispose();
        }
	}
}
