package com;

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
import javax.swing.JPanel;

public class HyperbolicProjection extends JPanel {
	private static final long serialVersionUID = -3143479088440322492L;
	
	private static final Color DEFAULT_ASYMPTOTE_COLOR = Color.CYAN;
	private static final Color DEFAULT_ARC_COLOR = Color.RED;
	private static final Color DEFAULT_CURSOR_COLOR = Color.BLACK;
	private static final Color DEFAULT_TRAIL_COLOR = Color.GREEN;
	private static final boolean DEFAULT_SHOW_ASYMPTOTE = false;
	private static final boolean DEFAULT_SHOW_ARC = true;
	private static final boolean DEFAULT_SHOW_CURSOR = false;
	private static final boolean DEFAULT_SHOW_TRAIL = true;
	private static final double DEFAULT_ASYMPTOTE_LENGTH = 10000;
	private static final int DEFAULT_CURSOR_DIAMETER = 6;
	private static final int DEFAULT_FLIGHT = 0;

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
	private Point.Double upperLeftLonLat;
	private Point.Double lowerRightLonLat;
	private ConicSection cone;
	private int flight;
	
	public HyperbolicProjection() {}

	public HyperbolicProjection(Point.Double upperLeftLonLat, Point.Double lowerRightLonLat, ConicSection cone, 
			Dimension mapSize, Color arcColor, boolean showCursor) {
		this(upperLeftLonLat, lowerRightLonLat, cone, mapSize, arcColor, DEFAULT_SHOW_ARC, 
				DEFAULT_ASYMPTOTE_LENGTH, DEFAULT_ASYMPTOTE_COLOR, DEFAULT_SHOW_ASYMPTOTE, DEFAULT_CURSOR_DIAMETER, 
				DEFAULT_CURSOR_COLOR, showCursor, DEFAULT_TRAIL_COLOR, DEFAULT_SHOW_TRAIL, DEFAULT_FLIGHT);
	}
	
	public HyperbolicProjection(Point.Double upperLeftLonLat, Point.Double lowerRightLonLat, ConicSection cone, 
			Dimension mapSize, Color arcColor) {
		this(upperLeftLonLat, lowerRightLonLat, cone, mapSize, arcColor, DEFAULT_SHOW_ARC, 
				DEFAULT_ASYMPTOTE_LENGTH, DEFAULT_ASYMPTOTE_COLOR, DEFAULT_SHOW_ASYMPTOTE, DEFAULT_CURSOR_DIAMETER, 
				DEFAULT_CURSOR_COLOR, DEFAULT_SHOW_CURSOR, DEFAULT_TRAIL_COLOR, DEFAULT_SHOW_TRAIL, DEFAULT_FLIGHT);
	}
	
	public HyperbolicProjection(Point.Double upperLeftLonLat, Point.Double lowerRightLonLat, ConicSection cone, 
			Dimension mapSize) {
		this(upperLeftLonLat, lowerRightLonLat, cone, mapSize, DEFAULT_ARC_COLOR, DEFAULT_SHOW_ARC, 
				DEFAULT_ASYMPTOTE_LENGTH, DEFAULT_ASYMPTOTE_COLOR, DEFAULT_SHOW_ASYMPTOTE, DEFAULT_CURSOR_DIAMETER, 
				DEFAULT_CURSOR_COLOR, DEFAULT_SHOW_CURSOR, DEFAULT_TRAIL_COLOR, DEFAULT_SHOW_TRAIL, DEFAULT_FLIGHT);
	}

	public HyperbolicProjection(Point.Double upperLeftLonLat, Point.Double lowerRightLonLat, ConicSection cone, 
			Dimension mapSize, Color arcColor, boolean showArc, double asymptoteLength, Color asymptoteColor, 
			boolean showAsymptote, int cursorDiameter, Color cursorColor, boolean showCursor, 
			Color trailColor, boolean showTrail, int flight) {
		
		this.arcColor = arcColor;
		this.showArc = showArc;
		this.showTrail = showTrail;
		this.showCursor = showCursor;
		this.showAsymptote = showAsymptote;
		this.upperLeftLonLat = upperLeftLonLat;
		this.lowerRightLonLat = lowerRightLonLat;
		this.asymptoteLength = asymptoteLength;
		this.asymptoteColor = asymptoteColor;
		this.trailColor = trailColor;
		this.cursorColor = cursorColor;
		this.cursorDiameter = cursorDiameter;
		this.cone = cone;
		this.flight = flight;
		
    	setSize(mapSize);
    	setPreferredSize(mapSize);
		setOpaque(false);
		setDoubleBuffered(true);
		setVisible(true);
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

    public void setCornerLonLat(Point.Double upperLeftLonLat, Point.Double lowerRightLonLat) {
		this.upperLeftLonLat = upperLeftLonLat;
		this.lowerRightLonLat = lowerRightLonLat;
		repaint();
	}
	
    public int getFlight() {
    	return flight;
    }
    
    @Override
	protected void paintComponent(Graphics gOrig) {
        super.paintComponent(gOrig);
        Graphics2D g = (Graphics2D) gOrig.create();
        try {
        	if (upperLeftLonLat != null && lowerRightLonLat != null) {
	        	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,	RenderingHints.VALUE_ANTIALIAS_ON);
				
	        	Hyperbola2D hyperbola = new Hyperbola2D(metersToPixels(cone.getA(), 90), metersToPixels(cone.getB(), 0), 
	        		cone.getDirection(), lonlatToXY(cone.getCenter()), metersToPixels(asymptoteLength, 
	        		cone.getConicAngleToTarget()));
	        	
	        	if (showAsymptote) {
					g.setStroke(new BasicStroke(1.0f));
					g.setColor(asymptoteColor);
			    	Asymptote2D asymptote = new Asymptote2D(lonlatToXY(cone.getCenter()), cone.getDirection(), 
			    		cone.getConicAngleToTarget(), metersToPixels(asymptoteLength, cone.getConicAngleToTarget()));
					g.draw(asymptote);
				}
				if (showArc) {
					g.setColor(arcColor);
					g.setStroke(new BasicStroke(1.0f));
			    	g.draw(hyperbola);
				}
				if (showTrail) {
					float dash[] = {5.0f,5.0f};
					g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1.0f, dash, 0f));
					g.setColor(trailColor);
					g.draw(new Line2D.Double(lonlatToXY(cone.getSMB().point), lonlatToXY(cone.getSMA().point)));
					g.setStroke(new BasicStroke(1.0f));
					g.draw(new Ellipse2D.Double(longitudeToX(cone.getSMB().point.x) - (cursorDiameter / 2), 
						latitudeToY(cone.getSMB().point.y) - (cursorDiameter / 2), cursorDiameter, cursorDiameter));
					g.draw(new Ellipse2D.Double(longitudeToX(cone.getSMA().point.x) - (cursorDiameter / 2), 
						latitudeToY(cone.getSMA().point.y) - (cursorDiameter / 2), cursorDiameter, cursorDiameter));
				}
				if (showCursor) {
					g.setStroke(new BasicStroke(1.0f));
					g.setColor(cursorColor);
					g.draw(new Ellipse2D.Double(longitudeToX(cone.getCenter().x) - (cursorDiameter / 2), 
						latitudeToY(cone.getCenter().y) - (cursorDiameter / 2), cursorDiameter, cursorDiameter));
					g.draw(new Ellipse2D.Double(cone.getVertex().x - (cursorDiameter / 2), cone.getVertex().y - 
						(cursorDiameter / 2), cursorDiameter, cursorDiameter));
					g.draw(new Ellipse2D.Double(cone.getFocus().x - (cursorDiameter / 2), cone.getFocus().y - 
						(cursorDiameter / 2), cursorDiameter, cursorDiameter));
				    Point2D.Double source = new Point2D.Double(-83.07724, 40.026563);
					g.draw(new CrissCross2D(lonlatToXY(source), 12));
				}
        	}
        } finally {
        	g.dispose();
        }
	}
    
}
